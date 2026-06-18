package com.rhsystem.application.port;

/**
 * Porta de saída para armazenamento de arquivos (anexos).
 * Implementada na infraestrutura.
 */
public interface ArmazenamentoArquivo {

    /**
     * Armazena o conteúdo   e devolve o caminho/identificador para recuperação.
     */
    String armazenar(byte[] conteudo, String nomeArquivo);
}
