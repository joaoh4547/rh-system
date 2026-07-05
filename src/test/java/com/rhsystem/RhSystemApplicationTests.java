package com.rhsystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: sobe o contexto completo (Vaadin, Security, Hazelcast, Flyway)
 * usando o banco H2 em memória do profile "test" — não requer PostgreSQL.
 */
@SpringBootTest
@ActiveProfiles("test")
class RhSystemApplicationTests {

    @Test
    void contextLoads() {
    }
}
