package com.rhsystem.domain.model.usuario;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Raiz de agregado (DDD) que representa um usuário do sistema.
 */
@Entity
@Table(
    name = "usuario",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_usuario_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_usuario_cpf", columnNames = "cpf"),
        @UniqueConstraint(name = "uk_usuario_rg", columnNames = "rg")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "sobrenome", nullable = false)
    private String sobrenome;

    @Column(name = "username", nullable = false, updatable = false)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    /** Hash da senha (BCrypt). Nulo enquanto o usuário não ativar a conta. */
    @Column(name = "senha")
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusUsuario status = StatusUsuario.PENDENTE_CONFIRMACAO;

    /** Somente dígitos. */
    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;

    @Column(name = "rg", nullable = false)
    private String rg;

    @Embedded
    private Endereco endereco = new Endereco();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Documento> documentos = new ArrayList<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    /** Data/hora em que o usuário aceitou os termos de uso (nulo = pendente). */
    @Column(name = "termos_aceito_em")
    private LocalDateTime termosAceitoEm;

    /** Nome completo do usuário. */
    public String getNomeCompleto() {
        return nome + " " + sobrenome;
    }

    /** Adiciona um documento mantendo a consistência do agregado. */
    public void adicionarDocumento(Documento documento) {
        documento.setUsuario(this);
        this.documentos.add(documento);
    }

    /** Conclui a ativação da conta definindo a senha (já com hash) e ativando o usuário. */
    public void ativar(String senhaComHash) {
        this.senha = senhaComHash;
        this.status = StatusUsuario.ATIVO;
    }

    /** Define uma nova senha (já com hash). */
    public void redefinirSenha(String senhaComHash) {
        this.senha = senhaComHash;
    }

    /** Registra o aceite dos termos de uso. */
    public void aceitarTermos() {
        this.termosAceitoEm = LocalDateTime.now();
    }

    public boolean termosAceitos() {
        return termosAceitoEm != null;
    }
}
