package com.rhsystem.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriedades de configuração da aplicação (prefixo "rh-system").
 */
@Component
@ConfigurationProperties(prefix = "rh-system")
@Getter
@Setter
public class RhSystemProperties {

    /** URL base para montar o link de ativação. */
    private String baseUrl = "http://localhost:8080";

    /** Remetente dos emails. */
    private String mailFrom = "no-reply@rhsystem.com";

    /** Validade do token de ativação, em horas. */
    private long ativacaoTokenValidadeHoras = 24;

    /** Diretório de armazenamento de anexos. */
    private String storageDir = "./storage/documentos";
}
