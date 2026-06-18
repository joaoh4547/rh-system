package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.dto.DocumentoUpload;
import com.rhsystem.application.dto.NovoUsuarioCommand;
import com.rhsystem.application.exception.RegraNegocioException;
import com.rhsystem.application.port.ArmazenamentoArquivo;
import com.rhsystem.application.port.NotificadorUsuario;
import com.rhsystem.domain.model.usuario.Documento;
import com.rhsystem.domain.model.usuario.FinalidadeToken;
import com.rhsystem.domain.model.usuario.StatusUsuario;
import com.rhsystem.domain.model.usuario.TokenAtivacao;
import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.domain.repository.TokenAtivacaoRepository;
import com.rhsystem.domain.repository.UsuarioRepository;
import com.rhsystem.domain.service.GeradorUsername;
import com.rhsystem.domain.service.ValidadorCpf;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: cria um usuário (status PENDENTE), gera username, persiste anexos,
 * cria o token de ativação e dispara o email.
 */
@Service
public class CriarUsuario {

    private final UsuarioRepository usuarioRepository;
    private final TokenAtivacaoRepository tokenRepository;
    private final NotificadorUsuario notificador;
    private final ArmazenamentoArquivo armazenamento;
    private final long validadeTokenHoras;

    public CriarUsuario(UsuarioRepository usuarioRepository,
                        TokenAtivacaoRepository tokenRepository,
                        NotificadorUsuario notificador,
                        ArmazenamentoArquivo armazenamento,
                        @Value("${rh-system.ativacao-token-validade-horas:24}") long validadeTokenHoras) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.notificador = notificador;
        this.armazenamento = armazenamento;
        this.validadeTokenHoras = validadeTokenHoras;
    }

    @Transactional
    public Usuario executar(NovoUsuarioCommand cmd) {
        String cpf = ValidadorCpf.apenasDigitos(cmd.cpf());
        String rg = UsuarioSupport.somenteAlfanumerico(cmd.rg());

        UsuarioSupport.validarObrigatorios(cmd, cpf, rg);
        if (!ValidadorCpf.isValido(cpf)) {
            throw new RegraNegocioException("CPF inválido.");
        }
        if (usuarioRepository.existePorEmail(cmd.email())) {
            throw new RegraNegocioException("Já existe um usuário com este email.");
        }
        if (usuarioRepository.existePorCpf(cpf)) {
            throw new RegraNegocioException("Já existe um usuário com este CPF.");
        }
        if (usuarioRepository.existePorRg(rg)) {
            throw new RegraNegocioException("Já existe um usuário com este RG.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(cmd.nome().trim());
        usuario.setSobrenome(cmd.sobrenome().trim());
        usuario.setEmail(cmd.email().trim());
        usuario.setCpf(cpf);
        usuario.setRg(rg);
        usuario.setStatus(StatusUsuario.PENDENTE_CONFIRMACAO);
        usuario.setUsername(GeradorUsername.gerar(cmd.nome(), cmd.sobrenome(),
                usuarioRepository::existePorUsername));
        usuario.setEndereco(UsuarioSupport.toEndereco(cmd.endereco()));

        if (cmd.documentos() != null) {
            for (DocumentoUpload upload : cmd.documentos()) {
                usuario.adicionarDocumento(criarDocumento(upload));
            }
        }

        Usuario salvo = usuarioRepository.salvar(usuario);

        TokenAtivacao token = new TokenAtivacao(salvo,
                LocalDateTime.now().plusHours(validadeTokenHoras), FinalidadeToken.ATIVACAO);
        tokenRepository.salvar(token);

        notificador.enviarAtivacao(salvo, token.getToken());
        return salvo;
    }

    private Documento criarDocumento(DocumentoUpload upload) {
        String caminho = armazenamento.armazenar(upload.conteudo(), upload.nomeArquivo());
        Documento doc = new Documento();
        doc.setDescricao(upload.descricao());
        doc.setNomeArquivo(upload.nomeArquivo());
        doc.setTipoConteudo(upload.tipoConteudo());
        doc.setCaminhoArmazenamento(caminho);
        doc.setTamanho(upload.conteudo() == null ? 0L : (long) upload.conteudo().length);
        return doc;
    }
}
