package com.rhsystem.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rhsystem.infrastructure.config.RhSystemProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalFileStorageTest {

    @TempDir
    Path tempDir;

    private LocalFileStorage storage;

    @BeforeEach
    void setUp() {
        RhSystemProperties properties = new RhSystemProperties();
        properties.setStorageDir(tempDir.toString());
        storage = new LocalFileStorage(properties);
    }

    @Test
    void storesContentAndReturnsPathInsideStorageDir() throws IOException {
        byte[] content = "conteudo do rg".getBytes(StandardCharsets.UTF_8);

        String path = storage.store(content, "rg.pdf");

        Path stored = Path.of(path);
        assertTrue(stored.startsWith(tempDir));
        assertTrue(Files.exists(stored));
        assertArrayEquals(content, Files.readAllBytes(stored));
        assertTrue(stored.getFileName().toString().endsWith("_rg.pdf"));
    }

    @Test
    void nullContentCreatesEmptyFile() throws IOException {
        String path = storage.store(null, "vazio.txt");
        assertEquals(0, Files.size(Path.of(path)));
    }

    @Test
    void sanitizesUnsafeFileNames() {
        String path = storage.store(new byte[]{1}, "../etc/pass wd?.txt");

        Path stored = Path.of(path);
        assertTrue(stored.normalize().startsWith(tempDir)); // não escapa do diretório
        String name = stored.getFileName().toString();
        assertFalse(name.contains("/"));
        assertFalse(name.contains(" "));
        assertFalse(name.contains("?"));
    }

    @Test
    void nullFileNameFallsBackToGenericName() {
        String path = storage.store(new byte[]{1}, null);
        assertTrue(Path.of(path).getFileName().toString().endsWith("_file"));
    }

    @Test
    void generatesUniqueNamesForSameFileName() {
        String a = storage.store(new byte[]{1}, "doc.pdf");
        String b = storage.store(new byte[]{2}, "doc.pdf");
        assertNotEquals(a, b);
    }
}
