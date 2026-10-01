# ===== Estágio 1: build (Maven + JDK 27; o vaadin-maven-plugin baixa o Node) =====
FROM eclipse-temurin:27-jdk AS build
WORKDIR /workspace

# Camada de dependências (aproveita o cache do Docker quando só o código muda)
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B -q || true

# Código-fonte e frontend
COPY src src
COPY package.json package-lock.json tsconfig.json types.d.ts vite.config.ts vite.generated.ts ./

# Build de produção (bundle do frontend Vaadin incluído)
RUN ./mvnw clean package -Pproduction -DskipTests -B

# ===== Estágio 2: runtime (JRE 27, imagem enxuta) =====
FROM eclipse-temurin:27-jre
WORKDIR /app

# Usuário sem privilégios: um RCE na aplicação não vira root no container.
# /app/storage = STORAGE_DIR do .env.example (o volume nomeado herda o dono na 1ª criação).
RUN groupadd --system rhsystem \
    && useradd --system --gid rhsystem --no-create-home --shell /usr/sbin/nologin rhsystem \
    && mkdir -p /app/storage \
    && chown -R rhsystem:rhsystem /app

COPY --from=build --chown=rhsystem:rhsystem /workspace/target/rh-system-*.jar app.jar

# Profile de produção: sem defaults de credenciais, logs em INFO, cookie Secure,
# DefaultAdminCredentialsGuard ativo.
ENV SPRING_PROFILES_ACTIVE=prod

# Flags recomendadas pelo Hazelcast para JVMs modernas (evita warnings e
# habilita otimizações internas de serialização) + mutação de campos final
# (JEP 500) liberada, igual ao spring-boot:run do pom.
# Virtual threads são ligadas via application.yml (spring.threads.virtual.enabled).
ENV JAVA_TOOL_OPTIONS="--add-modules java.se \
    --add-exports java.base/jdk.internal.ref=ALL-UNNAMED \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
    --add-opens java.management/sun.management=ALL-UNNAMED \
    --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED \
    --enable-final-field-mutation=ALL-UNNAMED"

# 8080 = HTTP; 5701 = cluster Hazelcast
EXPOSE 8080 5701

USER rhsystem

ENTRYPOINT ["java", "-jar", "app.jar"]
