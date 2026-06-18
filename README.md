# RH System

Sistema de RH. Primeira funcionalidade implementada: **CRUD de Usuário** com
ativação de conta por email.

## Stack

- Java 26
- Spring Boot 4.x
- Vaadin 25 (UI) — versão exigida para Spring Boot 4
- Spring Security (login + BCrypt)
- PostgreSQL
- Flyway (migrations)
- Lombok
- Maven

## Arquitetura (DDD / SOLID)

```
com.rhsystem
├── domain          # Núcleo de negócio: entidades/agregados (Usuario), VOs (Endereco),
│                   #   repositórios (portas) e serviços de domínio (ValidadorCpf, GeradorUsername)
├── application     # Casos de uso (UsuarioService), DTOs/commands e portas de saída
│                   #   (NotificadorUsuario, ArmazenamentoArquivo)
├── infrastructure  # Adapters: persistência JPA, email SMTP, storage de arquivos, segurança
└── interfaces      # Apresentação: views Vaadin (lista, formulário, login, ativação)
```

Dependências apontam para o domínio: `interfaces → application → domain ← infrastructure`.

## Funcionalidade: Usuário

Campos: nome, sobrenome, username, email, senha, status, CPF, RG, endereço
(logradouro, bairro, número, complemento, CEP) e lista de documentos anexados.

Regras implementadas:

- **Username** gerado automaticamente como `nome.sobrenome` (sem acento, minúsculo);
  se já existir, recebe sufixo numérico (`joao.henrique`, `joao.henrique1`, ...).
- **CPF validado** pelos dígitos verificadores; CPF e RG têm **máscara automática** na tela.
- **Dados únicos**: username, email, CPF e RG (constraints no banco).
- **Senha não é informada no cadastro**. Ao criar, o status fica
  `Pendente de confirmação` e um **email de ativação** é enviado com um link
  (`/ativar/{token}`) válido por 24h. Na ativação o usuário define senha + confirmação,
  a senha é gravada com hash **BCrypt** e o status passa para `Ativo`.
- **Anexos** salvos em filesystem; metadados (nome, tipo, caminho, tamanho) no banco.
- **Login** via Spring Security; apenas usuários `Ativo` autenticam.

## Pré-requisitos

- JDK 26
- Node.js 24+ (Vaadin 25 usa para o build de frontend; em dev é baixado automaticamente)
- PostgreSQL (via Docker Compose abaixo)

## Banco de dados

```bash
docker compose up -d        # sobe o PostgreSQL (banco rh_system)
docker compose down         # para (mantém os dados)
docker compose down -v      # para e apaga os dados
```

Variáveis de ambiente (com padrões): `DB_HOST` (localhost), `DB_PORT` (5432),
`DB_NAME` (rh_system), `DB_USER` (postgres), `DB_PASSWORD` (postgres), `SERVER_PORT` (8080).

O Flyway aplica as migrations de `src/main/resources/db/migration` no start.

## Email (SMTP do Gmail)

Use uma **Senha de app** do Google (não a senha normal da conta — exige 2FA ativo).
Configure as variáveis de ambiente antes de subir a aplicação:

```bash
export MAIL_USERNAME="sua-conta@gmail.com"
export MAIL_PASSWORD="senha-de-app-de-16-digitos"
export MAIL_FROM="sua-conta@gmail.com"        # opcional (default = MAIL_USERNAME)
export APP_BASE_URL="http://localhost:8080"   # base do link de ativação
```

## Como rodar

```bash
./mvnw spring-boot:run        # Linux/macOS
mvnw.cmd spring-boot:run      # Windows
```

Acesse http://localhost:8080 (redireciona para `/usuarios`; exige login).
A tela de ativação fica em `/ativar/{token}` (link enviado por email).

## Build de produção

```bash
./mvnw clean package -Pproduction
```

## Testes

Requer o PostgreSQL no ar (`docker compose up -d`):

```bash
./mvnw test
```

## Configurações da aplicação (`rh-system.*`)

| Propriedade                          | Variável             | Padrão                       |
|--------------------------------------|----------------------|------------------------------|
| `rh-system.base-url`                 | `APP_BASE_URL`       | `http://localhost:8080`      |
| `rh-system.mail-from`                | `MAIL_FROM`          | `MAIL_USERNAME`              |
| `rh-system.ativacao-token-validade-horas` | `ATIVACAO_TOKEN_HORAS` | `24`                  |
| `rh-system.storage-dir`              | `STORAGE_DIR`        | `./storage/documentos`       |
