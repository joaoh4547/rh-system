-- Tabelas do agregado Usuario

CREATE TABLE usuario (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome           VARCHAR(120) NOT NULL,
    sobrenome      VARCHAR(120) NOT NULL,
    username       VARCHAR(150) NOT NULL,
    email          VARCHAR(180) NOT NULL,
    senha          VARCHAR(100),
    status         VARCHAR(30)  NOT NULL,
    cpf            VARCHAR(11)  NOT NULL,
    rg             VARCHAR(20)  NOT NULL,
    -- endereço (objeto de valor)
    logradouro     VARCHAR(180),
    bairro         VARCHAR(120),
    numero         VARCHAR(20),
    complemento    VARCHAR(120),
    cep            VARCHAR(9),
    criado_em      TIMESTAMP    NOT NULL DEFAULT now(),
    atualizado_em  TIMESTAMP,
    CONSTRAINT uk_usuario_username UNIQUE (username),
    CONSTRAINT uk_usuario_email    UNIQUE (email),
    CONSTRAINT uk_usuario_cpf      UNIQUE (cpf),
    CONSTRAINT uk_usuario_rg       UNIQUE (rg)
);

CREATE TABLE usuario_documento (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id            BIGINT       NOT NULL,
    descricao             VARCHAR(180) NOT NULL,
    nome_arquivo          VARCHAR(255) NOT NULL,
    tipo_conteudo         VARCHAR(150),
    caminho_armazenamento VARCHAR(500) NOT NULL,
    tamanho               BIGINT,
    enviado_em            TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_documento_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE CASCADE
);

CREATE TABLE token_ativacao (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    token       VARCHAR(80)  NOT NULL,
    usuario_id  BIGINT       NOT NULL,
    expira_em   TIMESTAMP    NOT NULL,
    usado       BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_token_ativacao UNIQUE (token),
    CONSTRAINT fk_token_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE CASCADE
);

CREATE INDEX idx_documento_usuario ON usuario_documento (usuario_id);
CREATE INDEX idx_token_usuario     ON token_ativacao (usuario_id);
