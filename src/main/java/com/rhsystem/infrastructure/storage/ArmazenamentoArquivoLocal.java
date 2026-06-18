package com.rhsystem.infrastructure.storage;

import com.rhsystem.application.exception.RegraNegocioException;
import com.rhsystem.application.port.ArmazenamentoArquivo;
import com.rhsystem.infrastructure.config.RhSystemProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Armazena os anexos no filesystem e devolve o caminho relativo gravado.
 */
@Component
public class ArmazenamentoArquivoLocal implements ArmazenamentoArquivo {

    private final RhSystemProperties properties;

    public ArmazenamentoArquivoLocal(RhSystemProperties properties) {
        this.properties = properties;
    }

    @Override
    public String armazenar(byte[] conteudo, String nomeArquivo) {
        try {
            Path dir = Paths.get(properties.getStorageDir());
            Files.createDirectories(dir);
            String nomeUnico = UUID.randomUUID() + "_" + sanitizar(nomeArquivo);
            Path destino = dir.resolve(nomeUnico);
            Files.write(destino, conteudo == null ? new byte[0] : conteudo);
            return destino.toString();
        } catch (IOException e) {
            throw new RegraNegocioException("Falha ao armazenar o arquivo: " + nomeArquivo);
        }
    }

    private String sanitizar(String nome) {
        return nome == null ? "arquivo" : nome.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
