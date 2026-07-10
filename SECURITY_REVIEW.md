# Revisão de Segurança — RH System

Data: 2026-07-09. Escopo: código-fonte principal (`src/main/java`), configurações e migrations.

---

## 🔴 Severidade Alta

### 1. Autorização não implementada (controle de acesso quebrado)

**Onde:** `AppAccessManager.java`, todas as páginas em `interfaces/ui/pages`

`AppAccessManager.loadFunctionalities()` retorna lista vazia (TODO pendente), então `hasAccess()` sempre retorna `false` — o controle por funcionalidade não existe. Pior: todas as páginas (`UserPage`, `GroupPage`, `ParameterPage`) usam apenas `@PermitAll`, ou seja, **qualquer usuário autenticado acessa qualquer tela via URL direta**, incluindo gestão de usuários e grupos.

Há também um bug lógico: `hasAccessAny()` usa `containsAll` (semântica de "todos") em vez de "qualquer um".

**Como resolver:**
- Implementar `loadFunctionalities()` retornando as funcionalidades reais do usuário (via grupos).
- Corrigir `hasAccessAny`: `Arrays.stream(functionalities).anyMatch(loadFunctionalities()::contains)`.
- Trocar `@PermitAll` por `@RolesAllowed("ROLE_X")` nas páginas administrativas (as authorities já são geradas em `AppUserDetailsService.makeAuthorities`).
- Como defesa em profundidade, validar acesso também nos use cases (camada de aplicação), não só na UI.

### 2. Usuário admin seed com senha conhecida

**Onde:** `V3__seed_admin_user.sql`

A migration cria `admin.teste` com senha `admin123` documentada em comentário e hash BCrypt versionado. Se rodar em produção, é acesso administrativo trivial.

**Como resolver:**
- Remover o seed das migrations Flyway e criar o admin via processo de bootstrap (ex.: `ApplicationRunner` que só cria o usuário se a tabela estiver vazia, com senha vinda de variável de ambiente, status pendente de ativação, ou fluxo de ativação por email).
- Se mantiver para dev, condicionar a um profile (`spring.flyway.locations` diferente por profile) e nunca incluir no path de produção.

### 3. Senha reinjetada no DOM pelo servidor

**Onde:** `LoginView.authenticate()`

Após validar as credenciais no servidor, a senha em texto plano é enviada **de volta ao navegador** via `executeJs` e inserida num `<input>` criado dinamicamente para fazer o POST nativo. A senha trafega servidor→cliente pelo canal UIDL do Vaadin, fica em memória/DOM do navegador e pode vazar por extensões, XSS ou ferramentas de replay de sessão.

**Como resolver:**
- Usar o fluxo padrão: `LoginForm.setAction("login")` faz o POST nativo direto do formulário, sem round-trip da senha pelo servidor.
- Para o fluxo de termos: autenticar primeiro (POST nativo) e exibir o dialog de termos **após** o login, bloqueando a navegação (`BeforeEnterObserver` no `MainLayout`) enquanto os termos não forem aceitos. Isso também elimina o `ValidateLogin` duplicado.

### 4. Ausência de proteção contra força bruta e rate limiting

**Onde:** `ValidateLogin`, `RequestPasswordReset`, `ActivateUser`, `ResetPassword`

Não há lockout de conta, atraso progressivo, CAPTCHA nem limite de tentativas em nenhum fluxo:
- Login: tentativas ilimitadas de senha (e `ValidateLogin` roda antes do Spring Security, duplicando a superfície).
- Forgot password: chamadas ilimitadas → flooding de emails (email bombing) e acúmulo de tokens válidos.
- Ativação/reset: brute force ilimitado contra tokens.

**Como resolver:**
- Implementar rate limiting por IP + por conta (ex.: Bucket4j com o cache Hazelcast já existente no projeto).
- Bloquear a conta temporariamente após N falhas (registrar tentativas na tabela de usuário ou no cache).
- Limitar solicitações de reset por email (ex.: 3/hora) e invalidar tokens anteriores ao gerar um novo.

---

## 🟠 Severidade Média

### 5. Tokens de ativação/reset em texto plano no banco

**Onde:** `ActivationToken.java`, `RequestPasswordReset.java`

O token (UUID v4 — geração ok, usa CSPRNG) é armazenado em claro. Quem obtiver leitura do banco (SQL injection futura, backup vazado, insider) consegue tomar contas via reset. Além disso, tokens antigos não são invalidados quando um novo é emitido, e sessões ativas não são derrubadas após reset de senha.

**Como resolver:**
- Armazenar apenas o hash do token (SHA-256) e comparar o hash no lookup.
- Ao emitir novo token, invalidar os anteriores do mesmo usuário/propósito (`UPDATE ... SET used = true`).
- Após reset de senha, invalidar todas as sessões do usuário (`SessionRegistry` do Spring Security).
- Reduzir a validade do token de reset (24h é longo; 30–60 min é o usual para reset).

### 6. Política de senha fraca

**Onde:** `ActivationCommand.java`

Mínimo de 6 caracteres, sem qualquer outro requisito. Permite `123456`.

**Como resolver:**
- Mínimo 8–12 caracteres (NIST 800-63B recomenda ≥8 com verificação contra listas de senhas vazadas).
- Criar um validador (`@ValidPassword`) que rejeite senhas comuns (ex.: integração com API Have I Been Pwned k-anonymity, ou lista local top-10k).
- Rejeitar senha igual ao username/email.

### 7. Enumeração de usuários

**Onde:** `AppUserDetailsService`, `ValidateLogin`

- `UsernameNotFoundException("Usuário não encontrado: " + username)` — se a mensagem chegar a logs/UI, revela existência de contas.
- Em `ValidateLogin`, o `passwordEncoder.matches()` só executa quando o usuário existe → diferença de tempo mensurável (timing oracle) permite enumerar usernames.

**Como resolver:**
- Mensagem genérica ("Credenciais inválidas") sem incluir o username.
- Quando o usuário não existir, executar um `matches()` contra um hash dummy para equalizar o tempo de resposta.

### 8. Upload sem limite de tamanho nem validação de tipo

**Onde:** `UserForm.configureUpload()` (`UploadHandler.inMemory`), `LocalFileStorage.java`

- `UploadHandler.inMemory` carrega o arquivo inteiro em memória sem `setMaxFileSize` → DoS por exaustão de memória.
- Nenhuma validação de extensão ou content-type: aceita `.exe`, `.html`, `.svg` etc. Se esses arquivos forem servidos de volta um dia, `.html`/`.svg` viram XSS armazenado.
- Sem limite de quantidade de arquivos por usuário.

**Como resolver:**
- `upload.setMaxFileSize(...)` (ex.: 5 MB) e `upload.setMaxFiles(...)`.
- Allowlist de extensões e MIME types (PDF, JPG, PNG) validada **no servidor** (`CreateUser`), incluindo verificação de magic bytes — não confiar no MIME enviado pelo cliente.
- Ao servir os arquivos, usar `Content-Disposition: attachment` e `X-Content-Type-Options: nosniff`.

### 9. Cluster Hazelcast sem autenticação

**Onde:** `application.yml` (`rh-system.cache`), `pom.xml`

Descoberta por multicast por padrão e sem credenciais/TLS: qualquer processo na mesma rede pode ingressar no cluster e ler/alterar o cache (que pode conter dados de usuários e grupos).

**Como resolver:**
- Em produção, usar descoberta TCP-IP com lista explícita de membros (`HZ_MEMBERS`), desabilitar multicast.
- Habilitar autenticação entre membros e TLS (Hazelcast Enterprise) ou restringir a porta 5701 por firewall/rede isolada.
- Não armazenar dados sensíveis (senhas, tokens) no cache.

### 10. Credenciais default nas configurações

**Onde:** `application.yml`

`${DB_USER:postgres}` / `${DB_PASSWORD:postgres}` — se as variáveis não forem setadas em produção, a aplicação sobe silenciosamente com credenciais triviais.

**Como resolver:**
- Remover os defaults de credenciais (deixar `${DB_PASSWORD}` sem fallback → falha na inicialização se ausente, fail-fast).
- Manter defaults apenas num `application-dev.yml` de profile de desenvolvimento.

---

## 🟡 Severidade Baixa

### 11. Pré-decodificação manual de entidades no sanitizador

**Onde:** `RichTextSanitizer.sanitize()`

Decodificar `&lt;`, `&gt;` etc. manualmente **antes** do OWASP sanitizer transforma texto que o usuário digitou literalmente em markup ativo, e a cadeia de `replace` não cobre todas as formas de encoding (ex.: `&#x3C;`, `&#60;`). O `replace("&gt;", ">")` pós-sanitização também desfaz encoding intencional. O sanitizer OWASP já lida com entidades corretamente.

**Como resolver:** remover o pré-decode e o pós-replace; chamar `POLICY.sanitize(html)` diretamente. Se houver problema de dupla renderização, tratar na origem (Tiptap), não no sanitizador.

### 12. `spring-boot-devtools` sem marcação optional

**Onde:** `pom.xml`

Com `scope=runtime` (sem `<optional>true</optional>`), o devtools pode acabar no jar final. Ele é auto-desabilitado em jars empacotados, mas se `spring.devtools.remote.secret` for definido, habilita endpoint de execução remota de código.

**Como resolver:** adicionar `<optional>true</optional>` e garantir a exclusão no `spring-boot-maven-plugin` para builds de produção.

### 13. Logging DEBUG + `format_sql` em configuração única

**Onde:** `application.yml`

`com.rhsystem: DEBUG` e `format_sql: true` valem para todos os ambientes — SQL e dados de negócio podem vazar em logs de produção (CPF, RG, emails — dados pessoais sob LGPD).

**Como resolver:** mover para `application-dev.yml`; em produção usar `INFO` e nunca logar parâmetros SQL. Revisar se nenhum log imprime entidade `User` inteira.

### 14. Ordenação por campo arbitrário

**Onde:** `JpaSortUtil.java`

Nomes de campo vindos da UI vão direto para `Sort.by`. Não é SQL injection (Spring Data valida contra o metamodelo), mas permite ordenar por campos sensíveis (ex.: `password`), possibilitando inferência de dados por observação da ordem.

**Como resolver:** validar o campo contra uma allowlist por entidade antes de montar o `Sort`.

### 15. Cookies de sessão sem flags explícitas

**Onde:** `application.yml`

**Como resolver:** em produção (HTTPS), definir:

```yaml
server:
  servlet:
    session:
      cookie:
        secure: true
        http-only: true
        same-site: lax
```

---

## ✅ Pontos positivos observados

BCrypt para senhas; AES-GCM com IV aleatório e validação de tamanho de chave (`AesCryptographer`); tokens single-use com expiração; `RequestPasswordReset` não revela existência do email; sanitização por allowlist OWASP no rich text; `ddl-auto: validate` + Flyway; segredos via variáveis de ambiente; `open-in-view: false`; timeout de sessão curto com `closeIdleSessions`.

## Prioridade sugerida

1. Itens 1–4 (alta) antes de qualquer deploy.
2. Itens 5–10 na sequência.
3. Itens 11–15 como melhoria contínua.
