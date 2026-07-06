# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Documentation Sync (IMPORTANT)

Whenever a change affects anything documented here — new/renamed classes, use cases, routes, entities, migrations, permissions, configuration, commands, deployment — update **both** `CLAUDE.md` (this file, in English, for Claude Code) and `README.md` (in Portuguese, for developers) in the same change. The two files must never drift apart: CLAUDE.md is the technical reference; README.md is its user-facing counterpart covering the same facts (stack, features, routes, env vars, migrations, how to run).

## Stack

Java 26, Spring Boot 4.1.x (`spring-boot-starter-parent` 4.1.0), Vaadin 25.2.x, PostgreSQL 17, Flyway, Lombok, Hazelcast embedded (distributed cache). The Spring Boot Maven plugin passes `--add-opens`/`--add-exports` JVM arguments required by Hazelcast — keep them when touching `pom.xml`.

## Commands

```bash
# Start database only (local development)
docker compose up -d postgres

# Run in development
mvnw.cmd spring-boot:run          # Windows
./mvnw spring-boot:run            # Linux/macOS

# Run tests (requires PostgreSQL running)
mvnw.cmd test

# Production build (bundles Vaadin frontend via vaadin-maven-plugin build-frontend)
mvnw.cmd clean package -Pproduction

# Full stack: postgres + 2 app instances (Hazelcast cluster) + nginx LB on http://localhost:8080
docker compose up -d --build
```

## Architecture

DDD with four layers. Dependencies flow inward: `interfaces → application → domain ← infrastructure`.

```
com.rhsystem
├── domain          # Aggregates (model/usuario, model/grupo), Functionality enum, Sorting, repository interfaces, domain services, ValidationResult
├── application     # Use cases (one class each), DTOs/commands as records, output ports, command validation
├── infrastructure  # JPA adapters, email SMTP, file storage, Spring Security config, i18n, Hazelcast cache
├── interfaces      # Vaadin views and UI components
└── utils           # Reflections (generic-type resolution used by Form/AppGrid)
```

Code identifiers are in **English** (classes, methods, columns); UI texts remain in Portuguese via i18n. Some package/route names are still in Portuguese (`usecase/usuario`, `model/usuario`, route `usuarios`).

## Domain Layer

### Aggregates & entities

- **`User`** (`rh_user`) — fields `firstName`, `lastName`, `username` (immutable, `updatable = false`), `email`, `password` (BCrypt hash), `status`, `cpf` (11 digits), `rg`, embedded `Address`, `createdAt`/`updatedAt`/`termsAcceptedAt`. Unique constraints on username, email, cpf and rg. Relations: `@OneToMany documents` (cascade ALL + orphanRemoval), `@ManyToMany groups` (join table `rh_user_group`), `@ElementCollection functionalities` (`rh_user_functionality`). Behavior methods: `activate(passwordHash)` (sets password + status ACTIVE), `resetPassword(hash)`, `acceptTerms()`/`termsAccepted()`, `addDocument(doc)`, `getFullName()`, `isAdmin()` (any group with admin flag), `getUserFunctionalities()` (see Permissions).
- **`Group`** (`rh_group`) — `name`, `description`, `active`, `admin`, `@ElementCollection functionalities` (`rh_group_functionality`). Uses Lombok `@Builder`.
- **`ActivationToken`** (`rh_activation_token`) — UUID `token`, `@OneToOne user`, `expiresAt`, `used`, `purpose` (`TokenPurpose.ACTIVATION` or `PASSWORD_RESET`). `isValid()` = not used and not expired.
- **`Document`** (`rh_user_document`) — user attachment metadata: `description`, `fileName`, `contentType`, `storagePath`, `size`, `uploadedAt`. Binary content lives on disk (see `FileStorage`).
- **`Address`** — `@Embeddable` (street, neighborhood, streetNumber, complement, postalCode).
- **`UserStatus`** — `ACTIVE`, `INACTIVE`, `BLOCKED`, `PENDING_CONFIRMATION`; each value carries an i18n label key (`status.*`).
- **`Functionality`** — permission enum (see Security & Permissions).
- **`Sorting`** — record `(field, Direction ASC|DESC)`; the UI converts Vaadin sort orders to it, `JpaSortUtil.createSort` converts it to Spring `Sort` with a fallback.

Cached entities (`User`, `Group`, `Document`, `Address`) implement `Serializable` — required by Hazelcast. Keep this when adding entities/embeddables reachable from cached queries.

### Repository ports (`domain/repository`)

- `UserRepository` — save, findById/findByEmail/findByUsername, findAll, `findPaginated(offset, limit, sorting)`, count, countByStatus, delete, existsByUsername/Email/Cpf/Rg.
- `GroupRepository` — `findAllPaginated(limit, offset, sorting)`, count, countActive, save, findById, `findByIdWithFunctionalities` (fetches the lazy element collection via `@EntityGraph`).
- `ActivationTokenRepository` — save, findByToken.

### Domain services (static utilities)

- **`UsernameGenerator.generate(first, last, existsCheck)`** — lowercase, strips accents, skips Portuguese connectives (de, da, do, dos, das, e, di, du, del, la, las, los), joins as `firstname.lastname`, appends numeric suffix (`.2`, `.3`, …) while `existsCheck` returns true.
- **`CpfValidator`** — `isValid` (check digits), `digitsOnly` (normalization).

## Application Layer

### Use cases

`@Service` classes with a single `execute(...)` method, `@Transactional` (or `readOnly = true` for queries). One class per operation.

`application/usecase/usuario/`:

| Use case | Behavior |
|---|---|
| `CreateUser` | Normalizes CPF (`digitsOnly`) and RG (`alphanumericOnly`), collects structural + duplicate (email/cpf/rg) violations, generates username, status `PENDING_CONFIRMATION`, stores documents via `FileStorage`, saves, creates `ActivationToken` (validity from `rh-system.ativacao-token-validade-horas`), sends activation email via `UserNotifier`. |
| `UpdateUser` | Loads by id, duplicate checks only when the value changed, updates fields + status + address, sets `updatedAt`. Username and documents are not updated here. |
| `ActivateUser` | Validates `ActivationCommand`, loads token, requires purpose `ACTIVATION` and `isValid()`, calls `user.activate(encodedPassword)`, marks token used. |
| `RequestPasswordReset` | Silent no-op if email is blank or unknown (prevents user enumeration); otherwise creates `PASSWORD_RESET` token and emails the link. |
| `ResetPassword` | Same shape as `ActivateUser` but purpose `PASSWORD_RESET`; calls `user.resetPassword(...)`. |
| `ValidateLogin` | Returns `LoginResult`: `INVALID_CREDENTIALS` (unknown user, not ACTIVE, or bad password), `TERMS_PENDING` (valid but terms not accepted), `OK`. |
| `AcceptTerms` | Sets `termsAcceptedAt` for the username. |
| `ListUsers` | `execute()` (all) and `execute(offset, limit, sorting)` (paginated stream). |
| `GetUserSummary` | KPI record `UserSummary(total, active, pending, blocked)`. |
| `GetUserByUserName` / `RemoveUser` | Lookup / delete, throwing `BusinessException("error.user.not.found")` when missing. |
| `UserSupport` | Package-private helpers: `toAddress(dto)`, `isBlank`, `alphanumericOnly`. |

`application/usecase/group/`:

| Use case | Behavior |
|---|---|
| `CreateGroup` | Validates command, builds `Group` via builder, saves. |
| `UpdateGroup` | Loads with `findByIdWithFunctionalities`, replaces fields and the functionalities collection (clear + addAll). |
| `EnableGroup` | `EnableGroupCommand(groupId, enable)` toggles `active`. |
| `GetGroup` | Loads with functionalities or throws `error.group.not.found`. |
| `ListGroups` | Paginated stream. |
| `GetGroupSummary` | `GroupSummary(total, active)`. |

Groups are never deleted — they are disabled (`GroupPage.remove()` is intentionally empty).

### DTOs / commands (`application/dto`, all records)

`CreateUserCommand`, `UpdateUserCommand` (adds `id` + `status`), `ActivationCommand` (token + password + confirmation, `@FieldsMatch`, shared by activation and password reset), `AddressDTO`, `DocumentUpload` (description, fileName, contentType, byte[] content), `UserSummary`, `LoginResult` enum, `CreateGroupCommand`, `UpdateGroupCommand`, `EnableGroupCommand`, `GroupSummary`. Constraint messages on commands are i18n keys.

### Output ports (`application/port`)

- **`FileStorage.store(content, fileName)`** → implemented by `LocalFileStorage`: writes to `rh-system.storage-dir`, filename `UUID_sanitizedName`, returns the path stored in `Document.storagePath`.
- **`UserNotifier.sendActivation/sendPasswordReset(user, token)`** → implemented by `EmailUserNotifier`: `SimpleMailMessage` in pt-BR, subject from i18n (`email.activation.subject`, `email.reset.subject`), links `${base-url}/activate/{token}` and `${base-url}/reset-password/{token}`.
- **`AccessManager.hasAccess/hasAccessAny/hasAccessAll(Functionality...)`** → implemented by `AppAccessManager` (reads the authenticated user via Vaadin `AuthenticationContext` + `UserRepository`).

## Security & Permissions

Spring Security integrated with Vaadin via `VaadinSecurityConfigurer` (`SecurityConfig`); `LoginView` is registered as the login view. Views require authentication by default; `@AnonymousAllowed` marks public views. Passwords are BCrypt (`PasswordEncoder` bean).

**Login flow** (`LoginView`): the form is NOT submitted directly to Spring Security. It first calls `ValidateLogin`; on `TERMS_PENDING` it opens the terms-of-use dialog (accept → `AcceptTerms.execute(username)`); only on success does it submit a programmatic POST to `/login` (JS-injected form), letting Spring Security create the session. `AppUserDetailsService` loads the user: `disabled` unless status `ACTIVE`, `accountLocked` when `BLOCKED`, authorities from `getUserFunctionalities()`.

**Permissions model**: the `Functionality` enum (`domain/model`) is the unit of permission, each value with a `Category` (`USER`, `GROUP`) and an i18n label; `getFunctionalityByCategory()` groups them for UI rendering. Current values: `CREATE_USER`, `VIEW_USER`, `DELETE_USER`, `CREATE_GROUP`, `VIEW_GROUP`, `DELETE_GROUP`, `ENABLE_DISABLE_GROUP`.

- Effective permissions (`User.getUserFunctionalities()`): if any of the user's groups has `admin = true` → ALL functionalities; otherwise direct functionalities ∪ functionalities of **active** groups.
- At login each functionality becomes a `ROLE_<NAME>` authority (`Functionality.asRole()`).
- In application/UI code check access through the `AccessManager` port, not through Spring's `SecurityContext`.

Seed user from V3 migration: `admin.teste` / `admin123` (already ACTIVE).

## UI Layer (Vaadin 25)

### Routes

| Route | View | Access |
|---|---|---|
| `login` | `LoginView` | `@AnonymousAllowed` |
| `activate/{token}` | `ActivationView` (`HasUrlParameter`) | `@AnonymousAllowed` |
| `forgot-password` | `ForgotPasswordView` | `@AnonymousAllowed` |
| `reset-password/{token}` | `ResetPasswordView` (`HasUrlParameter`) | `@AnonymousAllowed` |
| `usuarios` | `UserPage` | `@PermitAll`, layout `MainLayout` |
| `groups` | `GroupPage` | `@PermitAll`, layout `MainLayout` |
| `editor-demo`, `lucide-demo` | demo pages | `MainLayout` |

`MainLayout` is an `AppLayout` with drawer navigation (`SideNav` with sections Ferramentas / Configurações / Segurança), user panel (avatar + full name via `GetUserByUserName`), logout via `AuthenticationContext.logout()`, and an `AppFooter` fixed to the bottom of the page (current year, serving instance address via `ServerInfoProvider`, and a live `SessionTimer`; added to the navbar slot but pinned to the page bottom via CSS `position: fixed`, offset by the drawer width when open). Some drawer items are placeholders without routes.

### CRUD base classes (`interfaces/ui/shared`)

- **`DataEditor<T>`** — in-memory CRUD (backing `List<T>`), used for sub-lists inside forms. Lazy-initializes on first `onAttach`: builds the grid (`buildGrid(actions)`), toolbar (title + "New" button) and layout. Subclass contract: `buildGrid`, `buildForm(@Nullable T)` (return a `Dialog`; null item = create), `tableTitle()`, `newButtonLabel()`. Standard row actions: edit + delete (delete opens a `ConfirmDialog`); add extra ones via `createAdditionalActions()`; gate them with `canEdit(obj)`/`canDelete(obj)`. In-memory helpers: `addItem`, `replaceItem`, `setData`, `getData`, `refresh`, `notifySuccess/notifyError`.
- **`BasePage<T>`** — extends `DataEditor`, the entry point for full CRUD pages. Initializes via `@PostConstruct` (overrides `onAttach` to a no-op) so injected use cases are available. Adds: page header (`pageTitle()`/`pageSubtitle()`), KPI area (`buildStats()` — return `StatCard`s in a `.stats-grid` `Div`, or null to omit), and a server-side paginated grid via `DataProvider.fromCallbacks` wired to `fetchResults(limit, offset, Collection<Sorting>)` and `countResults()`. `remove(T)` is the persistence hook; `executeRemoval` wraps it with `ValidationNotifier` error handling. `refresh()` = `dataProvider.refreshAll()` + KPI refresh — pass `this::refresh` as the save callback of form dialogs. Also abstract: `getEntityArticle()` (i18n `masc.article`/`fem.article`, used in confirmation texts).

### Grid stack

- **`AppGrid<T>`** — `Grid` with `LUMO_ROW_STRIPES`/`LUMO_NO_BORDER`, full size; resolves its bean type reflectively (`Reflections.getGenericType`). `addColumn(property, provider)` sets `sortProperty` — the property name must match the JPA entity property because it flows into `Sorting` → `JpaSortUtil` → `Sort`. `booleanColumn` renders Sim/Não.
- **`ActionsGrid<T>`** — `AppGrid` subclass for row actions: implement `configColumns()`; the actions column renders each `ObjectAction` as a small tertiary icon button with tooltip, honoring `enabled`/`visible` predicates. Entity grids (`UserGrid`, `GroupGrid`) extend it.
- **`ObjectAction<T>`** — Lombok builder: `icon` (Supplier<Component>), `label`, `handler`, optional `buttonVariants` (e.g. `LUMO_ERROR`), `enabled`/`visible` predicates (default true).
- **`EnableDialog<T>`** — `ConfirmDialog` for activate/deactivate with i18n texts (`enable.entity.*`/`disable.entity.*`) parameterized by entity label + article; see `GroupPage.createEnable(...)` for usage.

### Form stack (`interfaces/ui/form`)

- **`Form<T>`** — `Div` bound via `BeanValidationBinder<T>` (bean type explicit or resolved reflectively). Provides bind helpers (`bind`, `bindRequired`) and field factories: `textField`, `requiredTextField`, `emailField`, `passwordField`, `textArea`, `integerField`, `numberField`, `bigDecimalField`, `datePicker`, `timePicker`, checkbox, etc., each with an optional `(label, property)` overload that auto-binds. Write with `writeBeanIfValid(target)` / `ifValid(target)`.
- **`FormDialog<T>`** — `Dialog` wrapper: translated title, draggable/resizable, maximize/restore button, `width("680px")`, footer actions via `actions(FormDialogAction...)`, `open(bean)` (setBean + open), `notify(key, success)`.
- **`FormDialogAction`** — footer button builder; factories `FormDialogAction.cancel(text)` and `FormDialogAction.primary(text, handler)`.

### Per-entity page pattern

Each entity has in `interfaces/ui/pages/<entity>/`: `<Entity>Page` (extends `BasePage`), `<Entity>Grid` (extends `ActionsGrid`), `<Entity>Form` (extends `Form<Model>`), `<Entity>FormDialog` (extends `FormDialog<Model>`), `<Entity>FormModel` (mutable UI bean with a `from(entity)` factory). The dialog's save flow: `writeBeanIfValid(model)` → convert model to command record → call create/update use case → `notify` + `onSaved.run()` + `close()`, catching `ValidationException` with `ValidationNotifier`. The FormModel isolates the UI from both entity and commands — follow this for new entities.

Entity-specific notes: `UserForm` collects document uploads exposed as `getAttachments()` (`List<DocumentUpload>`, used only on create). `GroupForm` uses tabs, with one `CheckboxGroup<Functionality>` per `Functionality.Category` synced manually to the model (they can't bind directly to a single property). `GroupPage` restricts editing to active groups (`canEdit`) and adds enable/disable actions with `EnableDialog`.

### Components (`interfaces/ui/component`)

- **`AppFooter`** — page footer bar (`Footer`) fixed to the bottom of the page, showing `footer.copyright` (current `Year`, passed as a `String` so it isn't rendered with a thousands separator), `footer.server` (instance address from `ServerInfoProvider`), and the `SessionTimer`. Visible to all authenticated users.
- **`SessionTimer`** — live session countdown (`Span`) in the footer. The countdown runs **client-side** (JS via `executeJs`) for a smooth per-second display and resets on real user activity (mousedown/keydown/scroll/touchstart/click — bare `mousemove` is intentionally excluded so hovering doesn't keep the session alive). It calls three `@ClientCallable` server methods: `keepAlive()` (throttled to once per minute on activity, refreshing the HTTP session), `showWarning()` (at `warningMinutes` remaining, opens a `ConfirmDialog`; confirming resets the client timer), and `expire()` (at zero, opens a blocking `Dialog` whose only action runs `AuthenticationContext.logout()`). Durations come from `rh-system.session` (`timeoutMinutes`/`warningMinutes`). The server-side `server.servlet.session.timeout` is the authoritative backstop; `vaadin.closeIdleSessions=true` is required so Vaadin heartbeats don't keep idle sessions alive forever.
- **`LucideIcon`** — Lucide icon component with static factories (`edit`, `delete`, `add`, `check`, `lock`, `unLock`, `functionalities`).
- **`StatCard(label, value, VaadinIcon, Accent)`** — KPI card; `Accent`: PRIMARY, SUCCESS, WARNING, DANGER.
- **`DocumentField`** — masked `TextField` for `Type.CPF`/`Type.RG` with `getDigits()`/`setDigits()`.
- **`RichTextEditor`** + **`RichTextSanitizer`** — rich text editing; sanitizer uses OWASP java-html-sanitizer with a strict allowlist policy. Always sanitize HTML before persisting/rendering.

## Validation

Hybrid mechanism — Bean Validation for structural rules + Notification Pattern for business rules, collecting **all** violations before throwing:

- **Structural rules** are `jakarta.validation` annotations on command records (`application/dto`). Constraint `message` values are i18n keys (e.g. `error.cpf.invalid`), translated only at the UI layer. Custom constraints live in `application/validation`: `@CPF` (check digits via domain `CpfValidator`, blank = valid) and `@FieldsMatch` (class-level, e.g. password + confirmation).
- **`CommandValidator`** (`application/validation`) runs the annotations and returns a `ValidationResult` — use `check(cmd)` to keep adding business rules, or `validate(cmd)` to throw immediately.
- **`ValidationResult`** (`domain/validation`) is the notification object: `add`/`addIf`/`requiredNotBlank` accumulate `Violation`s (field + messageKey, deduplicated); `throwIfInvalid()` throws `ValidationException` carrying the full list.
- **`BusinessException`** (`application/exception`) extends `ValidationException` (single key), so UI catches only `ValidationException`.
- **UI**: `ValidationNotifier.show(this::getTranslation, ex)` (`interfaces/ui/shared`) renders every violation (distinct keys, translated) in one middle-positioned error notification (6s).

Use case pattern:

```java
ValidationResult validation = commandValidator.check(cmd);          // structural
validation.addIf(repo.existsByEmail(email), "email", "error.user.email.duplicate"); // business
validation.throwIfInvalid();                                        // all errors at once
```

When adding rules: new message keys go in both `i18n/messages.properties` and `messages_en.properties`; unit tests in `src/test/java/.../validation/` run without Spring or database.

## i18n

`spring.messages.basename: i18n/messages` — bundles `i18n/messages.properties` (pt-BR, default) and `messages_en.properties`. `TranslationProvider` (`infrastructure/i18n`) implements Vaadin's `I18NProvider` over the Spring `MessageSource`; supported locales pt-BR (default) and en; missing keys render as `!key`. All UI strings, enum labels (`status.*`, functionality labels), validation messages and email subjects are message keys. Emails are always sent in pt-BR.

## Database & Migrations

PostgreSQL 17. Flyway migrations in `src/main/resources/db/migration/` (`baseline-on-migrate: true`). Older files use `V{n}__{description}.sql`; **new migrations use timestamp versions** `V{yyyyMMddHHmmss}__{description}.sql` (e.g., `V20260703174848__...`). Schema is managed exclusively through Flyway (`ddl-auto: validate`, `open-in-view: false`). When adding entities, always create a new migration file.

Current migrations: V1 init, V2 usuario, V3 seed admin user, V4 token purpose, V5 terms, V6 grupo, V7 rename to English/`rh_` prefix, V20260703174848 user↔group and user↔functionality tables.

Tables (English columns since V7): `rh_user`, `rh_user_document`, `rh_activation_token`, `rh_group`, `rh_group_functionality`, `rh_user_group`, `rh_user_functionality`.

### Persistence adapters

Domain repository ports are implemented in `infrastructure/persistence` by `*Adapter` classes wrapping Spring Data `Jpa*Repository` interfaces (`JpaUserRepository`, `JpaGroupRepository`, `JpaActivationTokenRepository`). `JpaGroupRepository.findByIdWithFunctionalities` uses `@EntityGraph(attributePaths = "functionalities")`. `JpaSortUtil.createSort(sorting, fallback)` converts domain `Sorting` to Spring `Sort`.

## Distributed Cache

Hazelcast **embedded** (`CacheConfig` in `infrastructure/config`) via Spring Cache abstraction (`HazelcastCacheManager`). Each instance embeds a cluster member; members with the same `HZ_CLUSTER_NAME` discover each other (TCP-IP when `HZ_MEMBERS` is set, multicast otherwise; port auto-increments if busy) and share the cache, so eviction on one instance propagates to all.

- Caches: `CacheConfig.USERS` and `CacheConfig.GROUPS`. Map config: TTL `CACHE_TTL_SECONDS` (default 600s), LRU eviction, `PER_NODE` max size (default 5000, `rh-system.cache.max-size`), 1 backup.
- `@Cacheable`/`@CacheEvict` live on the `*Adapter` classes (infrastructure), never on domain or use cases. All writes (`save`, `delete`) evict with `allEntries = true`.
- Only list/count queries are cached (keys like `'all'`, `'count'`, `'count:' + #status`, and offset/limit/sort-composed keys for pagination). Point lookups (`findById`, `findByUsername`, `findByEmail`) and `exists*` are NOT cached — they must stay fresh for authentication and uniqueness validation.
- Cached entities must implement `Serializable` (including embeddables and child entities).
- Multiple instances still require **sticky sessions** at the load balancer (Vaadin state lives in the HTTP session; only the cache is shared).

## Configuration

`RhSystemProperties` (`@ConfigurationProperties(prefix = "rh-system")`): `baseUrl`, `mailFrom`, `activationTokenValidityHours`, `storageDir`, nested `cache` (clusterName, members, port, ttlSeconds, maxSize), nested `session` (`timeoutMinutes` default 60, `warningMinutes` default 5). `application.yml` also sets: `open-in-view: false`, SMTP with STARTTLS required, logging `com.rhsystem: DEBUG`, `server.servlet.session.timeout=60m`, and `vaadin.closeIdleSessions=true` + `vaadin.heartbeatInterval=300` (so idle sessions actually expire despite Vaadin heartbeats). These two are Vaadin servlet init parameters (not bound to `VaadinConfigurationProperties`), so the IDE flags them as unresolved even though they apply; `SessionConfigLogger` (a `VaadinServiceInitListener`) logs the effective `isCloseIdleSessions()`/`getHeartbeatInterval()` at startup to confirm.

### Environment variables

| Variable | Default |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | `localhost`, `5432`, `rh_system`, `postgres`, `postgres` |
| `SERVER_PORT` | `8080` |
| `MAIL_HOST`, `MAIL_PORT` | `smtp.gmail.com`, `587` |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | (empty) — requires Gmail App Password |
| `MAIL_FROM` | falls back to `MAIL_USERNAME`, then `no-reply@rhsystem.com` |
| `APP_BASE_URL` | `http://localhost:8080` — used to build activation/reset links |
| `ATIVACAO_TOKEN_HORAS` | `24` |
| `STORAGE_DIR` | `./storage/documentos` |
| `SESSION_TIMEOUT_MINUTES` | `60` — session lifetime without activity (also drives the footer timer) |
| `SESSION_WARNING_MINUTES` | `5` — how early the expiration warning dialog appears |
| `HZ_CLUSTER_NAME` | `rh-system` |
| `HZ_MEMBERS` | (empty) — comma-separated `host[:port]` list; empty = multicast discovery |
| `HZ_PORT` | `5701` |
| `CACHE_TTL_SECONDS` | `600` |

## Docker Deployment

`Dockerfile` is multi-stage (JDK 26 Maven build with `-Pproduction`, then JRE runtime). `docker-compose.yml` runs the full stack: `postgres`, `app1` + `app2` (defined via the `x-app-common` YAML anchor — Hazelcast TCP-IP discovery through `HZ_MEMBERS: app1:5701,app2:5701`, shared `app_storage` volume for document uploads), and `lb` (nginx on port 8080, config in `nginx.conf`). The nginx upstream uses `ip_hash` for sticky sessions and forwards WebSocket upgrade headers for Vaadin Push. Note: with `ip_hash`, requests from one client IP always land on the same instance — to see both instances locally, test from different IPs or temporarily switch the upstream to `least_conn` (breaks session affinity).

## Testing

- `src/test/java/.../validation/` — plain unit tests (`CommandValidatorTest`, `ValidationResultTest`), no Spring or database.
- `RhSystemApplicationTests` — Spring context smoke test; requires PostgreSQL running (`docker compose up -d postgres`).
