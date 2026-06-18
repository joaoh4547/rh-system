package com.rhsystem.domain.model.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Documento/anexo solicitado no cadastro do usuário.
 * O conteúdo do arquivo é armazenado em filesystem; aqui guardamos os metadados.
 */
@Entity
@Table(name = "usuario_documento")
@Getter
@Setter
@NoArgsConstructor
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Nome/tipo do documento solicitado (ex: "RG", "Comprovante de residência"). */
    @Column(name = "descricao", nullable = false)
    private String descricao;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(name = "tipo_conteudo")
    private String tipoConteudo;

    @Column(name = "caminho_armazenamento", nullable = false)
    private String caminhoArmazenamento;

    @Column(name = "tamanho")
    private Long tamanho;

    @Column(name = "enviado_em", nullable = false)
    private LocalDateTime enviadoEm = LocalDateTime.now();
}
