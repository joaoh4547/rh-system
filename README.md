# RH System

Sistema de gestão de RH construído com **Java 27 + Spring Boot 4 + Vaadin 25** (virtual threads ligadas), seguindo **DDD** e **SOLID**. Funcionalidades atuais: **CRUD de Usuários** com ativação de conta por email, redefinição de senha, aceite de termos de uso, **Grupos com permissões (funcionalidades)**, cache distribuído com **Hazelcast** e deploy clusterizado com **nginx** como load balancer.

## Sumário

- [Stack](#stack)
- [Arquitetura](#arquitetura-ddd--solid)
- [Funcionalidades](#funcionalidades)
- [Segurança e Permissões](#segurança-e-permissões)
- [Rotas da aplicação](#rotas-da-aplicação)
- [Como rodar](#como-rodar)
- [Banco de dados e migrations](#banco-de-dados-e-migrations)
- [Email (SMTP)](#email-smtp-do-gmail)
- [Cache distribuído (Hazelcast)](#cache-distribuído-hazelcast)
- [Deploy com Docker (cluster + load balancer)](#deploy-com-docker-cluster--load-balancer)
- [Configurações e variáveis de ambiente](#configurações-e-variáveis-de-ambiente)
- [Validação](#validação)
- [Internacionalização (i18n)](#internacionalização-i18n)
- [UI — componentes e padrões](#ui--componentes-e-padrões)
- [Testes](#testes)
- [Estrutura do projeto](#estrutura-do-projeto)

## Stack

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 27 | Linguagem (virtual threads habilitadas) |
| Spring Boot | 4.1.x | Framework base (`spring-boot-starter-parent` 4.1.1) |
| Vaadin | 25.3.x | UI web server-side (versão exigida pelo Spring Boot 4) |
| Spring Security | (gerenciado) | Autenticação, autorização, BCrypt |
| PostgreSQL | 17 | Banco de dados |
| Flyway | (gerenciado) | Migrations — única forma de evoluir o schema |
| Hazelcast | embedded | Cache distribuído entre instâncias |
| Lombok | 1.18.48 | Redução de boilerplate (primeira versão com suporte ao JDK 27) |
| Maven | wrapper incluído | Build (`mvnw` / `mvnw.cmd`) |
| nginx | 1.27 | Load balancer (deploy Docker) |

> O plugin do Spring Boot no `pom.xml` passa argumentos `--add-opens`/`--add-exports` exigidos pelo Hazelcast na JVM — não remova ao alterar o build.

> **Overrides de segurança no `pom.xml`** (CVEs no BOM do Boot 4.1.1): `tomcat.version` 11.0.26, `jackson-bom.version` 3.1.7, `jackson-2-bom.version` 2.21.7. Remova quando uma versão nova do Boot já trouxer versões iguais ou maiores. O editor rich text usa **Tiptap 3.31.4+** (o 2.x tem advisory sem correção).

## Arquitetura (DDD / SOLID)

Quatro camadas com dependências apontando para o domínio: `interfaces → application → domain ← infrastructure`.

```
com.rhsystem
├── domain          # Núcleo de negócio: agregados (User, Group), VOs (Address), enums
│                   #   (UserStatus, Functionality, TokenPurpose), Sorting, portas de
│                   #   repositório, serviços de domínio (CpfValidator, UsernameGenerator)
│                   #   e eventos de domínio (UserCreated, PasswordResetRequested)
├── application     # Casos de uso (1 classe = 1 operação, método único execute),
│                   #   DTOs/commands como records, portas de saída (FileStorage,
│                   #   UserNotifier, DomainEventPublisher, AccessManager), validação de
│                   #   commands e listeners de eventos (UserNotificationListener)
├── infrastructure  # Adapters: persistência JPA (+cache), email SMTP, storage em disco,
│                   #   Spring Security, i18n, Hazelcast
├── interfaces      # Apresentação: views Vaadin, componentes e infraestrutura de forms
└── utils           # Utilitários (reflexão para tipos genéricos)
```

Convenção de idioma: **identificadores de código em inglês** (classes, métodos, colunas); textos de UI em português via i18n.

### Casos de uso

Cada operação é uma classe `@Service` com um único método `execute(...)`:

- **Usuários** (`application/usecase/usuario`): `CreateUser`, `UpdateUser`, `RemoveUser`, `ListUsers`, `GetUserSummary`, `GetUser` (carrega o usuário com os grupos para o formulário de edição), `GetUserByUserName`, `ActivateUser`, `RequestPasswordReset`, `ResetPassword`, `AcceptTerms`.
- **Grupos** (`application/usecase/group`): `CreateGroup`, `UpdateGroup`, `EnableGroup`, `GetGroup`, `ListGroups`, `GetGroupSummary`.

### Portas de saída

- `FileStorage` → `LocalFileStorage`: grava anexos em `STORAGE_DIR` com nome `UUID_nomeSanitizado`.
- `DomainEventPublisher` → `SpringDomainEventPublisher`: os casos de uso **anunciam fatos** (eventos de domínio) em vez de chamar efeitos colaterais direto.
- `UserNotifier` → `EmailUserNotifier`: envia emails de ativação e redefinição de senha (pt-BR) com links montados a partir de `APP_BASE_URL`. Quem chama é o `UserNotificationListener`, nunca o caso de uso.

### Eventos de domínio

`CreateUser` publica `UserCreated` e `RequestPasswordReset` publica `PasswordResetRequested` (records imutáveis em `domain/event`, só com valores — o `toString()` esconde o token). O `UserNotificationListener` escuta com `@Async` + `@TransactionalEventListener(AFTER_COMMIT, fallbackExecution = true)`:

- o email só sai **depois do commit** (rollback = nenhum email; o token já está no banco quando o usuário clica no link);
- roda em **virtual thread** (`AsyncConfig`, threads `async-N`), sem travar a tela esperando o Gmail;
- falha de SMTP é logada (sem os parâmetros, para não vazar token) e **não desfaz o cadastro**.

Para reagir a um novo fato (auditoria, log, etc.), basta criar outro listener — o caso de uso não muda.
- `AccessManager` → `AppAccessManager`: consulta de permissões do usuário autenticado.

## Funcionalidades

### Usuários

Campos: nome, sobrenome, username, email, senha, status, CPF, RG, endereço (logradouro, bairro, número, complemento, CEP), documentos anexados e **grupos vinculados**.

- **Vínculo com grupos**: o formulário de usuário tem um seletor duplo estilo "shuttle" (`Shuttle<Group>`, duas listas com botões para mover itens entre "Disponíveis" e "Selecionados") para associar o usuário a um ou mais grupos, tanto na criação quanto na edição; apenas grupos **ativos** são oferecidos (`ListGroups.executeActive()`). Se o usuário tiver vínculo com um grupo inativo, ele não aparece no seletor e é removido ao salvar a edição (o `UpdateUser` substitui a associação pelos `groupIds` enviados). Na edição o usuário é recarregado com os grupos já inicializados (`GetUser`), evitando erro de lazy loading.

- **Username automático**: `nome.sobrenome` (minúsculo, sem acentos, conectivos como "de"/"da"/"dos" são ignorados); se já existir recebe sufixo numérico (`joao.henrique`, `joao.henrique.2`, ...). Imutável após criação.
- **CPF validado** pelos dígitos verificadores e armazenado só com dígitos (11 chars); CPF e RG têm **máscara automática** na tela (`DocumentField`).
- **Dados únicos**: username, email, CPF e RG (constraints no banco + validação com mensagem amigável).
- **Senha não é informada no cadastro**. Ao criar, o status fica `PENDING_CONFIRMATION` e um **email de ativação** é enviado com link `/activate/{token}` válido por 24h (configurável). Na ativação o usuário define senha + confirmação, gravada com **BCrypt**, e o status passa a `ACTIVE`.
- **Redefinição de senha**: fluxo "esqueci minha senha" (`/forgot-password`) envia email com link `/reset-password/{token}`; a resposta é idêntica para email existente ou não (evita enumeração de usuários). O mesmo mecanismo de token é usado (`TokenPurpose.PASSWORD_RESET`).
- **Status**: `ACTIVE`, `INACTIVE`, `BLOCKED` (conta travada no login), `PENDING_CONFIRMATION`.
- **Termos de uso**: exigidos logo após o primeiro login (tela `/terms`); o aceite é registrado em `terms_accepted_at`.
- **Anexos** salvos em filesystem; metadados (descrição, nome, tipo, caminho, tamanho, data) no banco. Só **PDF, PNG ou JPEG** (tipo detectado pelos *magic bytes* do arquivo, não pela extensão), até **5 MB** cada e **10** por usuário — validado no servidor (`AttachmentPolicy`).
- **Senha** (ativação/redefinição): de 8 a 72 caracteres, não pode ser uma senha comum nem igual ao usuário/email (`PasswordPolicy`).
- **Redefinição de senha**: o link vale **30 minutos** e só o mais recente funciona (os anteriores são invalidados).
- **KPIs** na tela: total, ativos, pendentes e bloqueados.

### Grupos e permissões

- Grupo tem nome, descrição, flag **ativo**, flag **admin** e um conjunto de **funcionalidades** (permissões).
- Funcionalidades são o enum `Functionality`, agrupado por categoria (USER, GROUP, SYSTEM): `CREATE_USER`, `VIEW_USER`, `DELETE_USER`, `CREATE_GROUP`, `VIEW_GROUP`, `DELETE_GROUP`, `ENABLE_DISABLE_GROUP`, `MANAGE_PARAMETERS`, `MANAGE_CACHE`.
- **As permissões valem de verdade**: cada tela exige uma funcionalidade (`@RolesAllowed`), o menu e os botões (novo/editar/excluir/ativar) só aparecem para quem pode, e os casos de uso também checam (`@PreAuthorize`) — chamar o caso de uso por fora da tela não fura a regra. Mudanças de permissão valem a partir do próximo login.
- Usuário pode pertencer a vários grupos e também ter funcionalidades diretas.
- **Permissões efetivas**: se algum grupo do usuário é admin → todas as funcionalidades; senão → funcionalidades diretas ∪ funcionalidades dos grupos **ativos**.
- Grupos **não são excluídos** — são desativados/reativados (com diálogo de confirmação). Só grupos ativos podem ser editados.
- KPIs na tela: total e ativos.

### Gerenciamento de Cache

- **Cards-resumo** no topo: quantidade de caches ativos, total de entradas, memória utilizada e total de hits.
- Grid de caches (Hazelcast IMap) com nome, entradas, memória e hits por cache.
- Cada linha pode ser **expandida** (clique) e mostra: cards com as métricas agregadas do cache, com rótulos amigáveis — "Entradas nesta instância" / "Cópias de outras instâncias" (próprias/backup no cluster), "Memória (nesta instância)" / "Memória (cópias)", "Acessos com sucesso" (hits) e "Consultas realizadas" (get), `CacheDetail` via `LocalMapStats` — **e uma grade com cada entrada armazenada** (chave, tipo, prévia do valor, memória e hits por entrada — `CacheEntry` via `getEntryView`).
- A única operação exposta é a **limpeza (evict)**: individual por cache (`ClearCache`) ou global. As métricas de escrita/remoção (put/remove) foram removidas por não fazerem sentido no padrão de cache do projeto, que usa `@CacheEvict(allEntries=true)` (= `IMap.clear`).
- Acesso restrito à funcionalidade `MANAGE_CACHE`.

## Segurança e Permissões

- Spring Security integrado ao Vaadin via `VaadinSecurityConfigurer`; views exigem autenticação por padrão, e as públicas (login, ativação, esqueci/redefinir senha) usam `@AnonymousAllowed`.
- **Fluxo de login** (`LoginView`): o `LoginForm` faz **POST nativo** ao `/login` do Spring Security — a senha vai direto do navegador para o filtro de autenticação e nunca passa pelo servidor do Vaadin nem volta ao DOM. Falha = `/login?error` com mensagem genérica (não revela se o usuário existe).
- **Termos de uso depois do login**: o `TermsAcceptanceGuard` redireciona qualquer navegação para `/terms` enquanto o usuário não aceitar; recusar faz logout.
- **Autorização em duas camadas**: telas com `@RolesAllowed(Roles.X)` (checadas pelo Vaadin) e casos de uso com `@PreAuthorize` (Spring Security). Constantes em `Functionality.Roles`.
- `AppUserDetailsService` carrega o usuário: somente `ACTIVE` autentica; `BLOCKED` vira conta travada.
- No login, cada funcionalidade do usuário vira uma authority `ROLE_<NOME>` (ex.: `ROLE_CREATE_USER`).
- Em código de aplicação/UI, verifique permissão pela porta **`AccessManager`** (`hasAccess`, `hasAccessAny`, `hasAccessAll`) — não pelo `SecurityContext` diretamente.
- Senhas com hash **BCrypt**; tokens de ativação/redefinição guardados só como **hash SHA-256** (quem lê o banco não consegue usar os links).
- Headers extras: `Referrer-Policy` e `Permissions-Policy` (além dos padrões do Spring Security). Cookie de sessão `HttpOnly`, `SameSite=Lax` e `Secure` por padrão.
- Auditoria completa, com o status de cada item: [`SECURITY_REVIEW.md`](SECURITY_REVIEW.md).

**Usuário seed** (migration V3): login `admin.teste` / senha `admin123` (já ativo), membro do grupo **Administradores** (migration V20261001013000). **Só vale em desenvolvimento**: fora dos profiles `dev`/`test`, o `DefaultAdminCredentialsGuard` troca essa senha pela de `ADMIN_INITIAL_PASSWORD` ou, sem ela, **bloqueia** o usuário.

## Rotas da aplicação

| Rota | Tela | Acesso |
|---|---|---|
| `/login` | Login | Pública |
| `/terms` | Aceite dos termos de uso (após o login) | Autenticado |
| `/activate/{token}` | Ativação de conta (define senha) | Pública |
| `/forgot-password` | Solicitar redefinição de senha | Pública |
| `/reset-password/{token}` | Redefinir senha | Pública |
| `/usuarios` | CRUD de usuários + KPIs | `VIEW_USER` |
| `/groups` | CRUD de grupos + KPIs | `VIEW_GROUP` |
| `/parameters` | Parâmetros do sistema | `MANAGE_PARAMETERS` |
| `/cache` | Gerenciamento de cache | `MANAGE_CACHE` |
| `/editor-demo`, `/lucide-demo` | Páginas demo (editor rich text, ícones) | Autenticado |

## Como rodar

### Pré-requisitos

- JDK 27 (o Spring Boot 4.1.1 documenta oficialmente até o 26 — o 27 só vira oficial no 4.2; ver notas no `pom.xml`)
- Node.js 24+ (Vaadin usa no build de frontend; em dev é baixado automaticamente)
- Docker (para o PostgreSQL e/ou stack completa)

### Desenvolvimento

```bash
docker compose up -d postgres     # sobe só o PostgreSQL (banco rh_system)

./mvnw spring-boot:run            # Linux/macOS
mvnw.cmd spring-boot:run          # Windows
```

Acesse http://localhost:8080 (exige login — use o usuário seed acima). O Flyway aplica as migrations automaticamente no start.

### Build de produção

```bash
./mvnw clean package -Pproduction   # gera o bundle de frontend do Vaadin
```

## Banco de dados e migrations

- PostgreSQL 17; schema gerenciado **exclusivamente** pelo Flyway (`ddl-auto: validate`) — toda mudança de schema é uma nova migration em `src/main/resources/db/migration/`.
- Convenção de versão: arquivos antigos `V{n}__descricao.sql`; **novas migrations usam timestamp** `V{yyyyMMddHHmmss}__descricao.sql` (ex.: `V20260703174848__...`).
- Migrations recentes: `V20261001013000` (grupo admin + vínculo do `admin.teste`) e `V20261001013100` — **migration Java** em `src/main/java/db/migration` que converte os tokens existentes para hash SHA-256 (Java porque as funções de hash do PostgreSQL e do H2 diferem).
- Tabelas com prefixo `rh_` e colunas em inglês (renomeadas na V7): `rh_user`, `rh_user_document`, `rh_activation_token`, `rh_group`, `rh_group_functionality`, `rh_user_group`, `rh_user_functionality`.

```bash
docker compose up -d postgres   # sobe o banco
docker compose down             # para (mantém os dados)
docker compose down -v          # para e apaga os dados
```

## Email (SMTP do Gmail)

Use uma **Senha de app** do Google (não a senha normal — exige 2FA ativo). Configure antes de subir a aplicação:

```bash
export MAIL_USERNAME="sua-conta@gmail.com"
export MAIL_PASSWORD="senha-de-app-de-16-digitos"
export MAIL_FROM="sua-conta@gmail.com"        # opcional (default = MAIL_USERNAME)
export APP_BASE_URL="http://localhost:8080"   # base dos links de ativação/redefinição
```

Emails enviados (sempre em pt-BR): ativação de conta (`/activate/{token}`) e redefinição de senha (`/reset-password/{token}`), ambos com validade configurável (`ATIVACAO_TOKEN_HORAS`, padrão 24h).

## Cache distribuído (Hazelcast)

Hazelcast **embedded** via Spring Cache — cada instância da aplicação embute um membro do cluster; instâncias com o mesmo `HZ_CLUSTER_NAME` se descobrem e compartilham o cache (eviction em uma instância propaga para todas).

- Descoberta: **TCP-IP** quando `HZ_MEMBERS` está definido (lista `host[:porta]` separada por vírgula); **multicast** quando vazio (rede local/mesma máquina). A porta base (`HZ_PORT`, padrão 5701) incrementa automaticamente se ocupada.
- Liga/desliga: `rh-system.cache.enabled` (padrão `true`). Com `false`, nenhum nó Hazelcast é criado e as anotações de cache viram no-ops — é assim que os testes rodam.
- Caches `users` e `groups`: TTL `CACHE_TTL_SECONDS` (padrão 600s), eviction LRU, tamanho máximo por nó 5000 entradas, 1 backup.
- **Somente consultas de lista/contagem são cacheadas.** Buscas pontuais (`findById`, `findByUsername`, `findByEmail`) e `exists*` NÃO são — precisam estar sempre frescas para autenticação e validação de unicidade.
- Anotações `@Cacheable`/`@CacheEvict` ficam apenas nos `*Adapter` de persistência (infraestrutura); escritas evictam com `allEntries = true`.
- Entidades cacheadas implementam `Serializable`.
- Com múltiplas instâncias ainda é necessário **sticky session** no load balancer (o estado do Vaadin vive na sessão HTTP; só o cache é compartilhado).

## Deploy com Docker (cluster + load balancer)

O `Dockerfile` é multi-stage (build Maven com JDK 27 e `-Pproduction`, runtime só com JRE 27, rodando como usuário **sem privilégios** `rhsystem` e com `SPRING_PROFILES_ACTIVE=prod`). O `docker-compose.yml` sobe a stack completa:

> ⚠️ O profile `prod` usa cookie de sessão `Secure`. Para testar a stack local em `http://localhost:8080`, coloque `SESSION_COOKIE_SECURE=false` no `.env` (já está no `.env.example`) — senão o login não mantém a sessão.

```bash
docker compose up -d --build    # postgres + app1 + app2 + nginx em http://localhost:8080
```

- `app1` e `app2` são definidos por uma âncora YAML `x-app-common`; formam cluster Hazelcast via `HZ_MEMBERS: app1:5701,app2:5701` e compartilham o volume `app_storage` para os anexos.
- `lb` é um nginx 1.27 (config em `nginx.conf`) com upstream `ip_hash` (sticky session) e repasse dos headers de upgrade WebSocket para o Vaadin Push.
- Atenção: com `ip_hash`, requisições do mesmo IP caem sempre na mesma instância — para ver as duas instâncias localmente, teste de IPs diferentes ou troque temporariamente para `least_conn` (quebra a afinidade de sessão).

## Configurações e variáveis de ambiente

| Propriedade (`application.yml`) | Variável | Padrão |
|---|---|---|
| `spring.datasource.*` | `DB_HOST`, `DB_PORT`, `DB_NAME` | `localhost`, `5432`, `rh_system` |
| `spring.datasource.username` / `password` | `DB_USER`, `DB_PASSWORD` | **sem padrão** (profile `dev`: `postgres`/`postgres`) |
| `server.port` | `SERVER_PORT` | `8080` |
| `spring.mail.host` / `port` | `MAIL_HOST`, `MAIL_PORT` | `smtp.gmail.com`, `587` |
| `spring.mail.username` / `password` | `MAIL_USERNAME`, `MAIL_PASSWORD` | (vazio) |
| `rh-system.base-url` | `APP_BASE_URL` | `http://localhost:8080` |
| `rh-system.mail-from` | `MAIL_FROM` | `MAIL_USERNAME` → `no-reply@rhsystem.com` |
| `rh-system.ativacao-token-validade-horas` | `ATIVACAO_TOKEN_HORAS` | `24` |
| `rh-system.storage-dir` | `STORAGE_DIR` | `./storage/documentos` |
| `rh-system.session.timeout-minutes` | `SESSION_TIMEOUT_MINUTES` | `60` (tempo de vida da sessão sem atividade) |
| `rh-system.session.warning-minutes` | `SESSION_WARNING_MINUTES` | `5` (antecedência do aviso de expiração) |
| `rh-system.cache.cluster-name` | `HZ_CLUSTER_NAME` | `rh-system` |
| `rh-system.cache.members` | `HZ_MEMBERS` | (vazio = multicast) |
| `rh-system.cache.port` | `HZ_PORT` | `5701` |
| `spring.threads.virtual.enabled` | — | `true` (Tomcat, `@Async` e scheduler em virtual threads) |
| `rh-system.cache.ttl-seconds` | `CACHE_TTL_SECONDS` | `600` |
| `server.servlet.session.cookie.secure` | `SESSION_COOKIE_SECURE` | `true` (profile `dev`: `false`) |
| `rh-system.password-reset-token-validity-minutes` | `PASSWORD_RESET_TOKEN_MINUTES` | `30` |
| `rh-system.admin-initial-password` | `ADMIN_INITIAL_PASSWORD` | (vazio) — fora de dev: nova senha do `admin.teste`, senão ele é bloqueado |
| `spring.profiles.active` | `SPRING_PROFILES_ACTIVE` | vazio → `dev`; imagem Docker → `prod` |

**Profiles**: sem profile (IDE / `mvnw`) entra o `dev` (`application-dev.yml`: credenciais `postgres`/`postgres`, `format_sql`, cookie não-`Secure`, log `DEBUG`). O `application.yml` base é o seguro para produção: sem credenciais padrão (*fail-fast*), log `INFO`, cookie `Secure`.

## Validação

Mecanismo híbrido — **Bean Validation** para regras estruturais + **Notification Pattern** para regras de negócio, acumulando **todas** as violações antes de lançar:

- Regras estruturais são anotações `jakarta.validation` nos commands (records); as mensagens são **chaves i18n** traduzidas só na UI. Constraints customizadas: `@CPF` (dígitos verificadores) e `@FieldsMatch` (ex.: senha + confirmação).
- `CommandValidator.check(cmd)` roda as anotações e devolve um `ValidationResult`, ao qual os casos de uso adicionam regras de negócio (`addIf(...)`) antes de `throwIfInvalid()`.
- `ValidationException` carrega a lista completa de violações; `BusinessException` é a variante de chave única. A UI captura e exibe tudo em **uma única notificação** (`ValidationNotifier`).

```java
ValidationResult validation = commandValidator.check(cmd);           // estrutural
validation.addIf(repo.existsByEmail(email), "email", "error.user.email.duplicate"); // negócio
validation.throwIfInvalid();                                         // todos os erros de uma vez
```

## Internacionalização (i18n)

Bundles em `src/main/resources/i18n/` (`messages.properties` pt-BR padrão, `messages_en.properties`). O `TranslationProvider` implementa o `I18NProvider` do Vaadin sobre o `MessageSource` do Spring — nas views use `getTranslation("chave")`; chave ausente renderiza como `!chave`. Toda string de UI, label de enum, mensagem de validação e assunto de email é uma chave de mensagem (sempre adicione nos dois arquivos).

## UI — componentes e padrões

Base de CRUD reutilizável em `interfaces/ui/shared`:

- **`DataEditor<T>`** — CRUD em memória para sub-listas dentro de forms (ex.: documentos do usuário), com toolbar, ações de editar/remover por linha e diálogo de confirmação.
- **`BasePage<T>`** — estende `DataEditor`; página CRUD completa com cabeçalho, cards de KPI (`StatCard`) e grid paginado no servidor (`DataProvider.fromCallbacks` + ordenação via `Sorting`), persistindo pelos casos de uso.
- **`AppGrid`/`ActionsGrid`** — grid padronizado + coluna de ações por linha (`ObjectAction` com ícone, tooltip e predicados de habilitado/visível); **`EnableDialog`** — confirmação de ativar/desativar.

Infra de formulários em `interfaces/ui/form`: **`Form<T>`** (binder + fábricas de campos), **`FormDialog<T>`** (diálogo arrastável/redimensionável com maximizar) e **`FormDialogAction`** (botões do rodapé). Cada entidade segue o conjunto `Page` / `Grid` / `Form` / `FormDialog` / `FormModel` em `interfaces/ui/pages/<entidade>/`. Os `FormDialog`s são beans Spring de escopo prototype: os use cases são injetados pelo Spring e a página obtém o diálogo via `ObjectProvider`, chamando `edit(entidade, onSaved)`.

Componentes reutilizáveis (`interfaces/ui/component`): `LucideIcon` (ícones Lucide), `StatCard` (KPI), `DocumentField` (campo com máscara de CPF/RG), `RichTextEditor` com `RichTextSanitizer` (sanitização OWASP do HTML), `AppFooter` (rodapé do drawer com ano, endereço do servidor e timer) e `SessionTimer`.

O `MainLayout` tem um rodapé (`AppFooter`) fixo na base da página (barra full-width à direita da sidebar) exibindo o ano atual, o endereço (IP/hostname) da instância que atendeu a requisição — via `ServerInfoProvider`, útil para achar logs quando há mais de uma instância atrás do balanceador — e um `SessionTimer` com contagem regressiva ao vivo. O timer roda no client (JS) e é reiniciado a cada atividade real do usuário (clique, teclado, scroll, toque). Mover o mouse sem clicar **não** reinicia — a contagem continua descendo enquanto o ponteiro só passa por cima. Faltando 5 min (`SESSION_WARNING_MINUTES`) abre um aviso; ao confirmar, o tempo é reiniciado. Ao zerar, um diálogo bloqueante faz logout. A sessão dura 60 min sem atividade (`SESSION_TIMEOUT_MINUTES`), controlada por `server.servlet.session.timeout` com `vaadin.closeIdleSessions=true` (senão os heartbeats do Vaadin manteriam a sessão viva para sempre).

## Testes

```bash
./mvnw test        # NÃO requer PostgreSQL — os testes usam H2 em memória
```

A suíte roda inteira contra um banco **H2 em memória** (modo de compatibilidade PostgreSQL), configurado pelo profile `test` (`src/test/resources/application-test.yml`). As migrations Flyway reais são aplicadas no H2, então o schema testado é o mesmo do banco de produção. O PostgreSQL nunca é tocado pelos testes.

Camadas cobertas:

- **Domínio** (sem Spring): entidades (`UserTest`, `GroupTest`, `ActivationTokenTest`, `FunctionalityTest`) e serviços (`CpfValidatorTest`, `UsernameGeneratorTest`).
- **Validação** (sem Spring): `CommandValidatorTest`, `ValidationResultTest`.
- **Casos de uso** (Mockito, sem banco): criação/atualização/ativação de usuário, reset de senha, login, aceite de termos, consultas e todos os casos de uso de grupo (`application/usecase/**`).
- **Fluxo de eventos** (`UserNotificationListenerTest`, contexto Spring mínimo + transação real em H2 vazio): email só depois do commit e em virtual thread, nada em rollback, execução mesmo sem transação, falha do SMTP não propaga.
- **Persistência** (`@DataJpaTest` + H2 + Flyway): adapters `UserRepositoryAdapter`, `GroupRepositoryAdapter`, `ActivationTokenRepositoryAdapter`, paginação/ordenação (`JpaSortUtilTest`) e verificação do usuário seed.
- **Infraestrutura/UI utilitários**: `LocalFileStorageTest` (diretório temporário), `RichTextSanitizerTest` (XSS).
- **Segurança**: `UseCaseAuthorizationTest` (caso de uso sem login → rejeitado; sem a funcionalidade → negado; com ela → ok), `PasswordPolicyTest`, `ActivationTokenTest`/`ActivationTokenPersistenceTest` (banco guarda só o hash; invalidação de tokens anteriores), `UserPersistenceTest` (admin seed no grupo admin), `FunctionalityTest` (constantes `Roles` ↔ enum) e `CreateUserTest` (anexo validado por magic bytes).
- **Smoke test**: `RhSystemApplicationTests` sobe o contexto completo (Vaadin, Security) sobre o H2. O Hazelcast fica **desligado** nos testes (`rh-system.cache.enabled: false` no profile `test`) — nenhum nó/cluster é criado e as anotações de cache viram no-ops.

## Estrutura do projeto

```
├── src/main/java/com/rhsystem/
│   ├── domain/            # model/{usuario,grupo}, Functionality, Sorting, repository/, service/, validation/
│   ├── application/       # usecase/{usuario,group}, dto/, port/, validation/, exception/
│   ├── infrastructure/    # config/ (security, cache, properties), persistence/, email/, storage/, i18n/
│   ├── interfaces/ui/     # MainLayout, pages/{usuario,groups,auth,...}, form/, component/, shared/
│   └── utils/
├── src/main/resources/
│   ├── application.yml
│   ├── db/migration/      # migrations Flyway (V1..V7 + timestamps)
│   └── i18n/              # messages.properties (pt-BR), messages_en.properties
├── src/test/java/         # testes de domínio, casos de uso, persistência (H2) e smoke test
├── src/test/resources/    # application-test.yml (H2 em memória, modo PostgreSQL)
├── docker-compose.yml     # postgres + app1 + app2 + nginx
├── Dockerfile             # build multi-stage (produção)
└── nginx.conf             # load balancer (ip_hash + WebSocket)
```
