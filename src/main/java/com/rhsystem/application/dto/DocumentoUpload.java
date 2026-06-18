package com.rhsystem.application.dto;

/**
 * Representa um anexo enviado no cadastro (conteúdo em memória + metadados).
 */
public record DocumentoUpload(
        String descricao,
        String nomeArquivo,
        String tipoConteudo,
        byte[] conteudo
) {
}
