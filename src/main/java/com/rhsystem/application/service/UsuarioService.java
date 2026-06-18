package com.rhsystem.application.service;

import com.rhsystem.application.dto.AtivacaoCommand;
import com.rhsystem.application.dto.AtualizarUsuarioCommand;
import com.rhsystem.application.dto.DocumentoUpload;
import com.rhsystem.application.dto.EnderecoDTO;
import com.rhsystem.application.dto.NovoUsuarioCommand;
import com.rhsystem.application.exception.RegraNegocioException;
import com.rhsystem.application.port.ArmazenamentoArquivo;
import com.rhsystem.application.port.NotificadorUsuario;
import com.rhsystem.domain.model.usuario.Documento;
import com.rhsystem.domain.model.usuario.Endereco;
import com.rhsystem.domain.model.usuario.StatusUsuario;
import com.rhsystem.domain.model.usuario.TokenAtivacao;
import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.domain.repository.TokenAtivacaoRepository;
import com.rhsystem.domain.repository.UsuarioRepository;
import com.rhsystem.domain.service.GeradorUsername;
import com.rhsystem.domain.service.ValidadorCpf;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de aplicação (casos de uso) do agregado Usuario.
 * Orquestra domínio, persistência, notificação e armazenamento — sem conter regra de negócio.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TokenAtivacaoRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificadorUsuario notificador;
    private final ArmazenamentoArquivo armazenamento;
    private final long validadeTokenHoras;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          TokenAtivacaoRepository tokenRepository,
                          PasswordEncoder passwordEncoder,
                          NotificadorUsuario notificador,
                          ArmazenamentoArquivo armazenamento,
                          @org.springframework.beans.factory.annotation.Value("${rh-system.ativacao-token-validade-horas:24}") long validadeTokenHoras) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificador = notificador;
        this.armazenamento = armazenamento;
        this.validadeTokenHoras = validadeTokenHoras;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.listarTodos();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.buscarPorId(id);
    }

    /**
     * Cria um usuário com status PENDENTE_CONFIRMACAO, gera o username,
     * persiste os anexos, cria o token e dispara o email de ativação.
     */
    @Transactional
    public Usuario criar(NovoUsuarioCommand cmd) {
        String cpf = ValidadorCpf.apenasDigitos(cmd.cpf());
        String rg = somenteAlfanumerico(cmd.rg());

        validarObrigatorios(cmd, cpf, rg);
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
        usuario.setEndereco(toEndereco(cmd.endereco()));

        if (cmd.documentos() != null) {
            for (DocumentoUpload upload : cmd.documentos()) {
                usuario.adicionarDocumento(criarDocumento(upload));
            }
        }

        Usuario salvo = usuarioRepository.salvar(usuario);

        TokenAtivacao token = new TokenAtivacao(salvo, LocalDateTime.now().plusHours(validadeTokenHoras));
        tokenRepository.salvar(token);

        notificador.enviarAtivacao(salvo, token.getToken());
        return salvo;
    }

    @Transactional
    public Usuario atualizar(AtualizarUsuarioCommand cmd) {
        Usuario usuario = usuarioRepository.buscarPorId(cmd.id())
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        String cpf = ValidadorCpf.apenasDigitos(cmd.cpf());
        String rg = somenteAlfanumerico(cmd.rg());
        if (!ValidadorCpf.isValido(cpf)) {
            throw new RegraNegocioException("CPF inválido.");
        }
        if (!usuario.getEmail().equalsIgnoreCase(cmd.email()) && usuarioRepository.existePorEmail(cmd.email())) {
            throw new RegraNegocioException("Já existe um usuário com este email.");
        }
        if (!usuario.getCpf().equals(cpf) && usuarioRepository.existePorCpf(cpf)) {
            throw new RegraNegocioException("Já existe um usuário com este CPF.");
        }
        if (!usuario.getRg().equals(rg) && usuarioRepository.existePorRg(rg)) {
            throw new RegraNegocioException("Já existe um usuário com este RG.");
        }

        usuario.setNome(cmd.nome().trim());
        usuario.setSobrenome(cmd.sobrenome().trim());
        usuario.setEmail(cmd.email().trim());
        usuario.setCpf(cpf);
        usuario.setRg(rg);
        usuario.setStatus(cmd.status());
        usuario.setEndereco(toEndereco(cmd.endereco()));
        usuario.setAtualizadoEm(LocalDateTime.now());
        return usuarioRepository.salvar(usuario);
    }

    @Transactional
    public void remover(Long id) {
        Usuario usuario = usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
        usuarioRepository.remover(usuario);
    }

    /**
     * Conclui a ativação: valida o token, confere senha/confirmação,
     * grava o hash da senha e ativa o usuário.
     */
    @Transactional
    public void ativar(AtivacaoCommand cmd) {
        if (cmd.senha() == null || cmd.senha().length() < 6) {
            throw new RegraNegocioException("A senha deve ter ao menos 6 caracteres.");
        }
        if (!cmd.senha().equals(cmd.confirmacaoSenha())) {
            throw new RegraNegocioException("A senha e a confirmação não conferem.");
        }
        TokenAtivacao token = tokenRepository.buscarPorToken(cmd.token())
                .orElseThrow(() -> new RegraNegocioException("Token de ativação inválido."));
        if (!token.isValido()) {
            throw new RegraNegocioException("Token de ativação expirado ou já utilizado.");
        }

        Usuario usuario = token.getUsuario();
        usuario.ativar(passwordEncoder.encode(cmd.senha()));
        usuario.setAtualizadoEm(LocalDateTime.now());
        usuarioRepository.salvar(usuario);

        token.setUsado(true);
        tokenRepository.salvar(token);
    }

    // ---- helpers ----

    private void validarObrigatorios(NovoUsuarioCommand cmd, String cpf, String rg) {
        if (isBlank(cmd.nome()) || isBlank(cmd.sobrenome())) {
            throw new RegraNegocioException("Nome e sobrenome são obrigatórios.");
        }
        if (isBlank(cmd.email())) {
            throw new RegraNegocioException("Email é obrigatório.");
        }
        if (isBlank(cpf) || isBlank(rg)) {
            throw new RegraNegocioException("CPF e RG são obrigatórios.");
        }
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

    private Endereco toEndereco(EnderecoDTO dto) {
        if (dto == null) {
            return new Endereco();
        }
        return new Endereco(dto.logradouro(), dto.bairro(), dto.numero(), dto.complemento(), dto.cep());
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String somenteAlfanumerico(String valor) {
        return valor == null ? "" : valor.replaceAll("[^A-Za-z0-9]", "");
    }
}
