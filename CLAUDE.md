# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Start database only (local development)
docker compose up -d postgres

# Run in development
mvnw.cmd spring-boot:run          # Windows
./mvnw spring-boot:run            # Linux/macOS

# Run tests (requires PostgreSQL running)
mvnw.cmd test

# Production build (bundles Vaadin frontend)
mvnw.cmd clean package -Pproduction

# Full stack: postgres + 2 app instances (Hazelcast cluster) + nginx LB on http://localhost:8080
docker compose up -d --build
```

## Architecture

DDD with four layers. Dependencies flow inward: `interfaces → application → domain ← infrastructure`.

```
com.rhsystem
├── domain          # Entities/aggregates, value objects, repository interfaces, domain services
├── application     # Use cases (one class each), DTOs/commands as records, output ports
├── infrastructure  # JPA adapters, email SMTP, file storage, Spring Security config
└── interfaces      # Vaadin views and UI components
```

**Use cases** live in `application/usecase/` as `@Service` classes with a single `executar(...)` method. Each use case maps to one operation (e.g., `CriarUsuario`, `AtivarUsuario`, `RedefinirSenha`).

**Repository ports** are interfaces in `domain/repository/`; their JPA implementations are in `infrastructure/persistence/` as `*Adapter` classes wrapping Spring Data `Jpa*Repository` interfaces.

**Output ports** in `application/port/` (`ArmazenamentoArquivo`, `NotificadorUsuario`) decouple use cases from infrastructure concerns.

## UI Patterns (Vaadin 25)

There are two base CRUD classes:

- **`DataEditor<T>`** (`interfaces/ui/shared/`) — in-memory CRUD for sub-lists within forms (e.g., editing documents inside a user form). Initializes via `onAttach`.
- **`BasePage<T>`** — extends `DataEditor`, adds server-side paginated grid (`DataProvider.fromCallbacks`), KPI stat cards, and persists via use cases. Uses `@PostConstruct` for initialization (not `onAttach`). The entry point for full CRUD pages.

**`AppGrid<T>`** wraps Vaadin `Grid` with consistent column/action setup. **`FormDialog`** wraps Vaadin `Dialog` for modal forms. **`ObjectActions<T>`** is a builder for per-row edit/delete actions passed to the grid.

New CRUD pages extend `BasePage` and wire use cases via Spring injection. The `@Route` annotation and `@PermitAll`/`@AnonymousAllowed` annotations control routing and security.

## Security

Spring Security integrated with Vaadin via `VaadinSecurityConfigurer`. Views require authentication by default; use `@AnonymousAllowed` for public views (login, activation). Passwords are BCrypt-hashed. Only users with status `ATIVO` can log in (`UsuarioDetailsService`).

## Database & Migrations

PostgreSQL 17. Flyway migrations in `src/main/resources/db/migration/`, versioned `V{n}__{description}.sql`. Schema is managed exclusively through Flyway (`ddl-auto: validate`). When adding entities, always create a new migration file.

## Domain Rules

- **Username** is auto-generated as `nome.sobrenome` (no accents, lowercase), with a numeric suffix if taken.
- **CPF** is validated by check digits; stored as digits only (11 chars).
- **Activation flow**: user created with status `PENDENTE_CONFIRMACAO` → activation email sent with token → user sets password → status becomes `ATIVO`.
- **`RegraNegocioException`** signals business rule violations; caught in UI layer to display error notifications.

## Validation

Hybrid mechanism — Bean Validation for structural rules + Notification Pattern for business rules, collecting **all** violations before throwing:

- **Structural rules** are `jakarta.validation` annotations on command records (`application/dto`). Constraint `message` values are i18n keys (e.g. `error.cpf.invalid`), translated only at the UI layer. Custom constraints live in `application/validation`: `@CPF` (check digits via domain `CpfValidator`, blank = valid) and `@FieldsMatch` (class-level, e.g. password + confirmation).
- **`CommandValidator`** (`application/validation`) runs the annotations and returns a `ValidationResult` — use `check(cmd)` to keep adding business rules, or `validate(cmd)` to throw immediately.
- **`ValidationResult`** (`domain/validation`) is the notification object: `add`/`addIf`/`requiredNotBlank` accumulate `Violation`s (field + messageKey, deduplicated); `throwIfInvalid()` throws `ValidationException` carrying the full list.
- **`BusinessException`** extends `ValidationException` (single key), so UI catches only `ValidationException`.
- **UI**: `ValidationNotifier.show(this::getTranslation, ex)` renders every violation in one error notification.

Use case pattern:

```java
ValidationResult validation = commandValidator.check(cmd);          // structural
validation.addIf(repo.existsByEmail(email), "email", "error.user.email.duplicate"); // business
validation.throwIfInvalid();                                        // all errors at once
```

When adding rules: new message keys go in both `i18n/messages.properties` and `messages_en.properties`; unit tests in `src/test/java/.../validation/` run without Spring or database.

## Distributed Cache

Hazelcast **embedded** (`CacheConfig` in `infrastructure/config`) via Spring Cache abstraction. Each instance embeds a cluster member; members with the same `HZ_CLUSTER_NAME` discover each other (TCP-IP via `HZ_MEMBERS`, or multicast when empty) and share the cache, so eviction on one instance propagates to all.

- Caches: `CacheConfig.USERS` and `CacheConfig.GROUPS`, TTL `CACHE_TTL_SECONDS` (default 600s), LRU eviction.
- `@Cacheable`/`@CacheEvict` live on the `*Adapter` classes (infrastructure), never on domain or use cases. Writes evict with `allEntries = true`.
- Only list/count queries are cached. Point lookups (`findById`, `findByUsername`, `findByEmail`) and `exists*` are NOT cached — they must stay fresh for authentication and uniqueness validation.
- Cached entities must implement `Serializable` (including embeddables and child entities).
- Multiple instances still require **sticky sessions** at the load balancer (Vaadin state lives in the HTTP session; only the cache is shared).

## Docker Deployment

`Dockerfile` is multi-stage (JDK 26 Maven build with `-Pproduction`, then JRE runtime). `docker-compose.yml` runs the full stack: `postgres`, `app1` + `app2` (defined via the `x-app-common` YAML anchor — Hazelcast TCP-IP discovery through `HZ_MEMBERS: app1:5701,app2:5701`, shared `app_storage` volume for document uploads), and `lb` (nginx on port 8080, config in `nginx.conf`). The nginx upstream uses `ip_hash` for sticky sessions and forwards WebSocket upgrade headers for Vaadin Push. Note: with `ip_hash`, requests from one client IP always land on the same instance — to see both instances locally, test from different IPs or temporarily switch the upstream to `least_conn` (breaks session affinity).

## Environment Variables

| Variable | Default |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | `localhost`, `5432`, `rh_system`, `postgres`, `postgres` |
| `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | (empty) — requires Gmail App Password |
| `APP_BASE_URL` | `http://localhost:8080` |
| `ATIVACAO_TOKEN_HORAS` | `24` |
| `STORAGE_DIR` | `./storage/documentos` |
| `HZ_CLUSTER_NAME` | `rh-system` |
| `HZ_MEMBERS` | (empty) — comma-separated `host[:port]` list; empty = multicast discovery |
| `HZ_PORT` | `5701` |
| `CACHE_TTL_SECONDS` | `600` |