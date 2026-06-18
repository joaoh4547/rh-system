package com.rhsystem.application.dto;

/**
 * Dados de endereço usados nos casos de uso.
 */
public record EnderecoDTO(
        String logradouro,
        String bairro,
        String numero,
        String complemento,
        String cep
) {
}
