# ===== Estágio 1: build (Maven + JDK 26; o vaadin-maven-plugin baixa o Node) =====
FROM eclipse-temurin:26-jdk AS build
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

# ===== Estágio 2: runtime (JRE 26, imagem enxuta) =====
FROM eclipse-temurin:26-jre
WORKDIR /app

COPY --from=build /workspace/target/rh-system-*.jar app.jar

# Flags recomendadas pelo Hazelcast para JVMs modernas (evita warnings e
# habilita otimizações internas de serialização)
ENV JAVA_TOOL_OPTIONS="--add-modules java.se \
    --add-exports java.base/jdk.internal.ref=ALL-UNNAMED \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
    --add-opens java.management/sun.management=ALL-UNNAMED \
    --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED"

# 8080 = HTTP; 5701 = cluster Hazelcast
EXPOSE 8080 5701

ENTRYPOINT ["java", "-jar", "app.jar"]
