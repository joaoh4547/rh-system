# Revisão de Segurança — RH System

Primeira auditoria: 2026-07-09. **Reauditoria e correções: 2026-10-01** (Spring Boot 4.1.1, Java 27, Vaadin 25.3.0).
Escopo: código-fonte (`src/main`), configuração, migrations, Docker/nginx e dependências (Maven + npm).

Legenda: ✅ corrigido · 🟡 parcial · ⏳ pendente · ➖ risco aceito/não aplicável

---

## Resumo

| # | Item | Severidade | Status |
|---|---|---|---|
| D1 | Tomcat 11.0.24 com CVEs críticas | 🔴 Alta | ✅ 11.0.26 |
| D2 | jackson-databind 3.1.5 / 2.21.5 com CVEs | 🟠 Média | ✅ 3.1.7 / 2.21.7 |
| D3 | Tiptap 2.x — prototype pollution sem correção no 2.x | 🟠 Média | ✅ Tiptap 3.31.4 |
| D4 | Pacotes npm gerenciados pelo Vaadin (dompurify, react-router, vite…) | 🟡 Baixa/Média | ⏳ reauditar após o build com Vaadin 25.3.0 |
| 1 | Autorização não implementada (qualquer logado acessava tudo) | 🔴 Alta | ✅ |
| 2 | Usuário seed `admin.teste`/`admin123` | 🔴 Alta | ✅ (bloqueado fora de dev) |
| 3 | Senha reinjetada no DOM no login | 🔴 Alta | ✅ |
| 4 | Sem proteção contra força bruta / rate limiting | 🔴 Alta | ⏳ |
| 5 | Tokens em texto plano / não invalidados | 🟠 Média | 🟡 (falta derrubar sessões após reset) |
| 6 | Política de senha fraca (mín. 6) | 🟠 Média | ✅ |
| 7 | Enumeração de usuários | 🟠 Média | ✅ |
| 8 | Upload sem limite nem validação de tipo | 🟠 Média | ✅ |
| 9 | Cluster Hazelcast sem autenticação | 🟠 Média | ➖ mitigado por rede (ver abaixo) |
| 10 | Credenciais padrão do banco | 🟠 Média | ✅ |
| 11 | Pré-decodificação de entidades no sanitizador | 🟡 Baixa | ✅ |
| 12 | `spring-boot-devtools` não-optional | 🟡 Baixa | ✅ |
| 13 | Logging DEBUG / `format_sql` em todos os ambientes | 🟡 Baixa | ✅ |
| 14 | Ordenação por campo arbitrário | 🟡 Baixa | ➖ |
| 15 | Cookie de sessão sem flags | 🟡 Baixa | ✅ |
| N1 | Container rodando como root | 🟡 Baixa | ✅ |
| N2 | Sem `Referrer-Policy` / `Permissions-Policy`; nginx expondo versão | 🟡 Baixa | ✅ |
| N3 | Sem Content-Security-Policy | 🟡 Baixa | ⏳ |
| N4 | `AES_KEY` ausente do `.env.example` (app não sobe na stack Docker) | 🟡 Baixa | ✅ documentado |

---

## Dependências (CVEs)

Levantamento feito contra o GitHub Advisory Database (versões efetivas do BOM do Boot 4.1.1 + overrides do `pom.xml`) e `npm audit` do `package-lock.json`.

### D1 — Apache Tomcat 11.0.24 ✅

- **CVE-2026-65182** (crítica) — bypass de *security constraint* quando uma constraint de caminho mais longo vem antes de uma mais restritiva.
- **CVE-2026-68525** (crítica) — redirect após FORM auth pode furar constraint por método.
- **CVE-2026-65905** (crítica) — replay limitado no DIGEST.
- Corrigidas na 11.0.25. Na **11.0.26** (15/09/2026) vêm ainda correções de WebSocket — relevantes porque o **Vaadin Push usa WebSocket**: CVE-2026-76183 (bypass de constraints em endpoints WebSocket), CVE-2026-77791 (DoS no close), CVE-2026-79677, CVE-2026-86350 (mistura de headers).
- Exposição real: baixa para as três primeiras (a app não usa constraints/FORM/DIGEST do Tomcat — quem autentica é o Spring Security), mas atualização de patch é barata.
- **Correção:** `<tomcat.version>11.0.26</tomcat.version>` no `pom.xml`.

### D2 — jackson-databind ✅

- CVE-2026-91777 e CVE-2026-91776 (altas, DoS — publicadas em 30/09/2026), CVE-2026-68497 (DoS), CVE-2026-83557 e CVE-2026-19032 (médias).
- Exploráveis só com `@JsonIdentityInfo`/`@JsonTypeInfo(defaultImpl)`/tipos `Path` sobre JSON hostil — a app não expõe API JSON pública, mas o Vaadin usa Jackson internamente.
- **Correção:** `jackson-bom.version` 3.1.7 e `jackson-2-bom.version` 2.21.7.

### D3 — Tiptap 2.27 (GHSA-cp6q-959q-f8rh) ✅

- `mergeAttributes()` trata `__proto__` como chave comum → atributos herdados viram atributos de DOM executáveis (ex.: `onerror`). **Não há correção na linha 2.x**.
- Exposição real era baixa (o HTML vem do servidor já sanitizado e o nó customizado usa atributos fixos), mas o pacote ficava eternamente vulnerável.
- **Correção:** migração para **Tiptap 3.31.4** (`@NpmPackage` em `RichTextEditor` + imports/API em `rich-text-editor.ts`, verificados com `tsc` contra os tipos do v3). `npm audit` da árvore do Tiptap 3: 0 vulnerabilidades.

### D4 — Pacotes npm do Vaadin ⏳

`npm audit` do lock atual (gerado com Vaadin 25.2) aponta, além do Tiptap: `dompurify` ≤3.4.12 (via `@vaadin/markdown`), `react-router` 7.12–7.18.1 (CSRF só no modo RSC — não usado), e ferramentas de build (`vite`→`postcss`, `nanoid`, `browserslist`) — estas só rodam no build, não chegam ao navegador.
**Ação:** o Vaadin regenera `package.json`/lock no próximo build com a 25.3.0; rode `npm audit --omit=dev` depois e reavalie. Não fixe versões de pacotes do Vaadin manualmente.

### Sem CVEs conhecidas (2026-10-01)

Spring Framework 7.0.9, Spring Security 7.1.1, Hibernate 7.4.5, Hibernate Validator 9.1.3, HikariCP 7.0.2, Logback 1.5.38, PostgreSQL JDBC 42.7.13, H2 2.4.240, Flyway 12.4.0, Hazelcast 5.7.0, Guava 33.7.1, commons-lang3 3.20.0, commons-collections4 4.5.0, OWASP HTML Sanitizer 20260313.1, Lombok 1.18.48, Netty 4.2.17, Angus Mail 2.0.5, Vaadin Flow 25.3.0 (jsoup 1.23.2 — já com a correção da CVE-2026-71497).

---

## Código e configuração

### 1. Autorização (controle de acesso quebrado) ✅

**Era:** todas as páginas `@PermitAll`, `AppAccessManager` retornava lista vazia, `hasAccessAny` com semântica de "todos". Qualquer logado abria usuários, grupos, parâmetros e cache (inclusive limpar cache e ver prévias dos valores cacheados).

**Agora:**
- Telas com `@RolesAllowed`: `UserPage` (`VIEW_USER`), `GroupPage` (`VIEW_GROUP`), `ParameterPage` (`MANAGE_PARAMETERS`), `CachePage` (`MANAGE_CACHE`). Novas funcionalidades `MANAGE_PARAMETERS` e `MANAGE_CACHE` (categoria `SYSTEM`).
- **Defesa em profundidade:** todos os casos de uso dessas telas com `@PreAuthorize` (`@EnableMethodSecurity`). JSR-250 propositalmente desligado no method security (faria proxy AOP das views do Vaadin).
- `AppAccessManager` lê as authorities da sessão (`AuthenticationContext.hasRole/hasAnyRole/hasAllRoles`) — mesma fonte das anotações. Menu e ações (novo/editar/excluir/ativar) só aparecem com permissão.
- Migration `V20261001013000` garante um grupo admin e vincula o `admin.teste`, para ninguém ficar trancado fora.
- Testes: `UseCaseAuthorizationTest`, `FunctionalityTest`, `UserPersistenceTest`.

**Observação:** permissões mudam no próximo login (authorities são montadas na autenticação).

### 2. Usuário seed com senha conhecida ✅

A migration V3 não pode ser alterada (checksum do Flyway). **Fora dos profiles `dev`/`test`**, o `DefaultAdminCredentialsGuard` verifica na subida se o `admin.teste` ainda tem a senha `admin123`: com `ADMIN_INITIAL_PASSWORD` (válida pela `PasswordPolicy`) troca a senha; sem ela, **bloqueia** o usuário. A imagem Docker roda com `SPRING_PROFILES_ACTIVE=prod`.

### 3. Senha reinjetada no DOM ✅

`LoginForm.setAction("login")`: POST nativo direto ao Spring Security — a senha não passa pelo canal UIDL nem volta ao navegador. O aceite de termos foi para **depois** do login (`TermsAcceptanceGuard` + `TermsView`), o que eliminou o `ValidateLogin` (superfície duplicada de verificação de senha).

### 4. Força bruta / rate limiting ⏳

Continua aberto — é o item mais importante que falta.
**Recomendação:** lockout temporário por conta após N falhas (listener de `AuthenticationFailureBadCredentialsEvent` + contador no Hazelcast, que já é compartilhado entre instâncias) e limite por IP/email no "esqueci minha senha" (ex.: Bucket4j sobre Hazelcast). Os tokens de reset já ficaram mais difíceis de abusar (item 5).

### 5. Tokens de ativação/reset 🟡

- ✅ O banco guarda **só o SHA-256** do token (`ActivationToken.tokenHash`); o valor puro existe apenas em memória para montar o email. Migration Java `V20261001013100` converteu os tokens existentes (links já enviados continuam valendo).
- ✅ Ao pedir um novo reset, os tokens anteriores do usuário são invalidados (só o link mais recente funciona).
- ✅ Validade do reset: **30 min** (`PASSWORD_RESET_TOKEN_MINUTES`).
- ✅ Corrigido de quebra: `@OneToOne` → `@ManyToOne` (um usuário tem vários tokens ao longo do tempo).
- ⏳ Derrubar as sessões ativas do usuário após o reset de senha (exige `SessionRegistry`/Spring Session).

### 6. Política de senha ✅

8 a 72 caracteres (72 = limite do BCrypt), blocklist local de senhas comuns e proibição de senha igual ao usuário/email (`PasswordPolicy`, alinhado ao NIST SP 800-63B). Senha rejeitada não consome o token.

### 7. Enumeração de usuários ✅

`UsernameNotFoundException` sem o username na mensagem; o `DaoAuthenticationProvider` do Spring já esconde a exceção e roda um BCrypt *dummy* para igualar o tempo. O `ValidateLogin` (que tinha *timing oracle*) foi removido.
➖ Contas desativadas/bloqueadas são checadas antes da senha (padrão do Spring) — a resposta é a mesma (`/login?error`), sobra só diferença de tempo; aceito.

### 8. Upload ✅

`AttachmentPolicy`: só PDF/PNG/JPEG **detectados por magic bytes** (extensão e MIME do cliente não são confiáveis), 5 MB por arquivo, 10 por usuário — validado no `CreateUser` (servidor); o tipo gravado é o detectado. No `Upload`: `setMaxFileSize`, `setMaxFiles`, `setAcceptedFileTypes` (só UX). nginx: `client_max_body_size 10m`.

### 9. Hazelcast sem autenticação ➖

A edição Community não tem autenticação/TLS entre membros. Mitigação atual: o `docker-compose.yml` não publica a porta 5701 no host (só rede interna do compose) e usa descoberta TCP-IP explícita (`HZ_MEMBERS`).
**Em produção:** rede isolada/firewall na 5701, nunca multicast, e não colocar segredos no cache.

### 10. Credenciais padrão ✅

`application.yml` base sem default para `DB_USER`/`DB_PASSWORD` (*fail-fast*). Os defaults de desenvolvimento ficaram no `application-dev.yml` (profile ativado automaticamente quando nenhum é informado).

### 11. Sanitizador ✅

Removidos o pré-decode manual de entidades e o `replace("&gt;", ">")` pós-sanitização: o HTML vai direto à política OWASP. Texto escapado continua texto (exibido, nunca executado). Testes cobrem `&lt;script&gt;` e entidades numéricas.

### 12. Devtools ✅ — `<optional>true</optional>`.

### 13. Logging ✅ — base em `INFO` sem `format_sql`; `DEBUG` só no profile `dev`.

### 14. Ordenação por campo arbitrário ➖

No Vaadin o cliente envia a **coluna**, e o servidor mapeia coluna → propriedade; não dá para o navegador injetar `password` como campo de ordenação. Risco aceito; revisar se um dia houver API REST.

### 15. Cookie de sessão ✅

`HttpOnly`, `SameSite=Lax` e `Secure` por padrão (`SESSION_COOKIE_SECURE`, `false` só no profile `dev` e para testar a stack Docker em http). `server.forward-headers-strategy: native` para respeitar o `X-Forwarded-Proto` do nginx.

### N1. Container como root ✅ — runtime com usuário `rhsystem` (sem shell), `/app` com dono correto.

### N2. Headers ✅ — `Referrer-Policy: strict-origin-when-cross-origin` e `Permissions-Policy` restritivo no Spring Security; `server_tokens off` no nginx. (O Spring já envia `X-Content-Type-Options`, `X-Frame-Options: DENY`, `Cache-Control` e HSTS em HTTPS.)

### N3. Content-Security-Policy ⏳

O Vaadin exige `'unsafe-inline'`/`'unsafe-eval'` em vários cenários (bootstrap, Push), então uma CSP útil precisa ser montada e testada com a aplicação rodando. Começar em `Content-Security-Policy-Report-Only`.

### N4. `AES_KEY` ✅ documentado

O `AesCryptographer` exige a chave na subida e ela não constava no `.env.example` — adicionada com instrução de geração (`openssl rand -base64 32`). O profile `test` tem uma chave própria, gerada só para os testes.

---

## ✅ Pontos positivos

BCrypt; AES-GCM com IV aleatório; tokens single-use com expiração (agora com hash); `RequestPasswordReset` não revela se o email existe; sanitização OWASP por allowlist; `ddl-auto: validate` + Flyway; segredos por variável de ambiente; `open-in-view: false`; timeout de sessão com `closeIdleSessions`; emails disparados só após o commit (eventos de domínio), com tokens fora dos logs.

## Próximos passos (ordem sugerida)

1. **Item 4** — lockout + rate limiting (login e esqueci a senha).
2. **D4** — `npm audit --omit=dev` após o build com Vaadin 25.3.0.
3. **Item 5** — invalidar sessões após reset de senha.
4. **N3** — CSP em modo report-only.
5. Remover os overrides de Tomcat/Jackson do `pom.xml` quando o Boot 4.1.2+ trouxer versões iguais ou maiores.
