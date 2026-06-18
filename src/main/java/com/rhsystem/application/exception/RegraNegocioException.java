package com.rhsystem.application.exception;

/**
 * Exceção para violações de regra de negócio (validações de aplicação/domínio).
 */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
