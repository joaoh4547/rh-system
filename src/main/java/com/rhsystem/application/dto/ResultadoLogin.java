package com.rhsystem.application.dto;

/**
 * Resultado da validação de credenciais feita ANTES de efetivar o login
 * (usada para barrar o acesso enquanto os termos não forem aceitos).
 */
public enum ResultadoLogin {
    OK,
    CREDENCIAIS_INVALIDAS,
    TERMOS_PENDENTES
}
