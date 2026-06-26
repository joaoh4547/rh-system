# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Start database
docker compose up -d

# Run in development
mvnw.cmd spring-boot:run          # Windows
./mvnw spring-boot:run            # Linux/macOS

# Run tests (requires PostgreSQL running)
mvnw.cmd test

# Production build (bundles Vaadin frontend)
mvnw.cmd clean package -Pproduction
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

## Environment Variables

| Variable | Default |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | `localhost`, `5432`, `rh_system`, `postgres`, `postgres` |
| `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | (empty) — requires Gmail App Password |
| `APP_BASE_URL` | `http://localhost:8080` |
| `ATIVACAO_TOKEN_HORAS` | `24` |
| `STORAGE_DIR` | `./storage/documentos` |