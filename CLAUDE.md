# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Stack

Java 26, Spring Boot 4.1.x, Vaadin 25.2.x, PostgreSQL 17, Flyway, Lombok, Hazelcast (embedded).

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
├── domain          # Aggregates (model/usuario, model/grupo), Functionality enum, repository interfaces, domain services, ValidationResult
├── application     # Use cases (one class each), DTOs/commands as records, output ports, command validation
├── infrastructure  # JPA adapters, email SMTP, file storage, Spring Security config, i18n, Hazelcast cache
└── interfaces      # Vaadin views and UI components
```

Code identifiers are in **English** (classes, methods, columns); UI texts and some routes remain in Portuguese via i18n.

**Use cases** live in `application/usecase/{usuario,group}/` as `@Service` classes with a single `execute(...)` method. Each use case maps to one operation (e.g., `CreateUser`, `ActivateUser`, `ResetPassword`, `CreateGroup`, `EnableGroup`).

**Repository ports** are interfaces in `domain/repository/`; their JPA implementations are in `infrastructure/persistence/` as `*Adapter` classes wrapping Spring Data `Jpa*Repository` interfaces.

**Output ports** in `application/port/` (`FileStorage`, `UserNotifier`, `AccessManager`) decouple use cases from infrastructure concerns.

## UI Patterns (Vaadin 25)

There are two base CRUD classes in `interfaces/ui/shared/`:

- **`DataEditor<T>`** — in-memory CRUD for sub-lists within forms (e.g., editing documents inside a user form). Initializes via `onAttach`.
- **`BasePage<T>`** — extends `DataEditor`, adds server-side paginated grid (`DataProvider.fromCallbacks` with `Sorting`), KPI stat cards, and persists via use cases. Uses `@PostConstruct` for initialization (not `onAttach`). The entry point for full CRUD pages.

Support classes: **`AppGrid<T>`**/**`ActionsGrid<T>`** wrap Vaadin `Grid` with consistent column/action setup; **`ObjectActions<T>`**/`ObjectAction` build per-row edit/delete actions; **`EnableDialog`** confirms activate/deactivate; **`ValidationNotifier`** renders validation errors. Form infrastructure lives in `interfaces/ui/form/` (`Form`, `FormDialog`, `FormDialogAction`). Reusable components in `interfaces/ui/component/` (`LucideIcon`, `StatCard`, `DocumentField`, `RichTextEditor` + `RichTextSanitizer`).

Each entity page follows the same file set in `interfaces/ui/pages/<entity>/`: `<Entity>Page` (extends `BasePage`), `<Entity>Grid`, `<Entity>Form`, `<Entity>FormDialog`, `<Entity>FormModel`. New CRUD pages extend `BasePage` and wire use cases via Spring injection. The `@Route` annotation and `@PermitAll`/`@AnonymousAllowed` annotations control routing and security.

## Security & Permissions

Spring Security integrated with Vaadin via `VaadinSecurityConfigurer`. Views require authentication by default; use `@AnonymousAllowed` for public views (login, activation, password reset). Passwords are BCrypt-hashed. Only users with status `ACTIVE` can log in; `BLOCKED` maps to a locked account (`AppUserDetailsService`).

Permissions are modeled by the **`Functionality`** enum (`domain/model`), grouped by `Category` (USER, GROUP). Assignment:

- A **`Group`** holds a set of functionalities and an `admin` flag; a `User` belongs to groups and may also have direct functionalities.
- Effective permissions (`User.getUserFunctionalities()`): if any group is admin → ALL functionalities; otherwise direct functionalities ∪ functionalities of **active** groups.
- At login each functionality becomes a `ROLE_<NAME>` authority (`Functionality.asRole()`).
- In application/UI code, check access through the **`AccessManager`** port (`hasAccess`, `hasAccessAny`, `hasAccessAll`); implementation `AppAccessManager` reads the authenticated user.

Seed user from V3 migration: `admin.teste` / `admin123` (already ACTIVE).

## Database & Migrations

PostgreSQL 17. Flyway migrations in `src/main/resources/db/migration/`. Older files use `V{n}__{description}.sql`; new migrations use timestamp versions `V{yyyyMMddHHmmss}__{description}.sql` (e.g., `V20260703174848__...`). Schema is managed exclusively through Flyway (`ddl-auto: validate`). When adding entities, always create a new migration file.

Tables use the `rh_` prefix with English column names (renamed in V7): `rh_user`, `rh_user_document`, `rh_activation_token`, `rh_group`, `rh_group_functionality`, `rh_user_group`, `rh_user_functionality`.

## Domain Rules

- **Username** is auto-generated as `firstname.lastname` (no accents, lowercase, Portuguese connectives like "de"/"da" skipped — `UsernameGenerator`), with a numeric suffix if taken.
- **CPF** is validated by check digits (`CpfValidator`); stored as digits only (11 chars).
- **User status** (`UserStatus`): `ACTIVE`, `INACTIVE`, `BLOCKED`, `PENDING_CONFIRMATION`.
- **Activation flow**: user created with status `PENDING_CONFIRMATION` → activation email sent with token (`ActivationToken` + `TokenPurpose`, also used for password reset) → user sets password → status becomes `ACTIVE`.
- **Terms of use**: accepted at login (`LoginView` → `AcceptTerms` use case); `User.termsAcceptedAt` records the timestamp.
- Business rule violations are signaled with `ValidationException`/`BusinessException` (see Validation); caught in the UI layer to display error notifications.

## Validation

Hybrid mechanism — Bean Validation for structural rules + Notification Pattern for business rules, collecting **all** violations before throwing:

- **Structural rules** are `jakarta.validation` annotations on command records (`application/dto`). Constraint `message` values are i18n keys (e.g. `error.cpf.invalid`), translated only at the UI layer. Custom constraints live in `application/validation`: `@CPF` (check digits via domain `CpfValidator`, blank = valid) and `@FieldsMatch` (class-level, e.g. password + confirmation).
- **`CommandValidator`** (`application/validation`) runs the annotations and returns a `ValidationResult` — use `check(cmd)` to keep adding business rules, or `validate(cmd)` to throw immediately.
- **`ValidationResult`** (`domain/validation`) is the notification object: `add`/`addIf`/`requiredNotBlank` accumulate `Violation`s (field + messageKey, deduplicated); `throwIfInvalid()` throws `ValidationException` carrying the full list.
- **`BusinessException`** (`application/exception`) extends `ValidationException` (single key), so UI catches only `ValidationException`.
- **UI**: `ValidationNotifier.show(this::getTranslation, ex)` (`interfaces/ui/shared`) renders every violation in one error notification.

Use case pattern:

```java
ValidationResult validation = commandValidator.check(cmd);          // structural
validation.addIf(repo.existsByEmail(email), "email", "error.user.email.duplicate"); // business
validation.throwIfInvalid();                                        // all errors at once
```

When adding rules: new message keys go in both `i18n/messages.properties` and `messages_en.properties` (exposed to Vaadin's `getTranslation` by `TranslationProvider`, the `I18NProvider` in `infrastructure/i18n`; missing keys render as `!key`); unit tests in `src/test/java/.../validation/` run without Spring or database.

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
| `MAIL_HOST`, `MAIL_PORT` | `smtp.gmail.com`, `587` |
| `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | (empty) — requires Gmail App Password |
| `APP_BASE_URL` | `http://localhost:8080` |
| `ATIVACAO_TOKEN_HORAS` | `24` |
| `STORAGE_DIR` | `./storage/documentos` |
| `HZ_CLUSTER_NAME` | `rh-system` |
| `HZ_MEMBERS` | (empty) — comma-separated `host[:port]` list; empty = multicast discovery |
| `HZ_PORT` | `5701` |
| `CACHE_TTL_SECONDS` | `600` |
