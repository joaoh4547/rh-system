-- Rename all tables to use the rh_ prefix and English column naming conventions.

-- ── 1. Rename columns (while tables still hold their original names) ──────────

-- usuario
ALTER TABLE usuario RENAME COLUMN nome             TO first_name;
ALTER TABLE usuario RENAME COLUMN sobrenome        TO last_name;
ALTER TABLE usuario RENAME COLUMN senha            TO password;
ALTER TABLE usuario RENAME COLUMN logradouro       TO street;
ALTER TABLE usuario RENAME COLUMN bairro           TO neighborhood;
ALTER TABLE usuario RENAME COLUMN numero           TO street_number;
ALTER TABLE usuario RENAME COLUMN complemento      TO complement;
ALTER TABLE usuario RENAME COLUMN cep              TO postal_code;
ALTER TABLE usuario RENAME COLUMN criado_em        TO created_at;
ALTER TABLE usuario RENAME COLUMN atualizado_em    TO updated_at;
ALTER TABLE usuario RENAME COLUMN termos_aceito_em TO terms_accepted_at;

-- usuario_documento
ALTER TABLE usuario_documento RENAME COLUMN usuario_id            TO user_id;
ALTER TABLE usuario_documento RENAME COLUMN descricao             TO description;
ALTER TABLE usuario_documento RENAME COLUMN nome_arquivo          TO file_name;
ALTER TABLE usuario_documento RENAME COLUMN tipo_conteudo         TO content_type;
ALTER TABLE usuario_documento RENAME COLUMN caminho_armazenamento TO storage_path;
ALTER TABLE usuario_documento RENAME COLUMN tamanho               TO size;
ALTER TABLE usuario_documento RENAME COLUMN enviado_em            TO uploaded_at;

-- token_ativacao
ALTER TABLE token_ativacao RENAME COLUMN usuario_id TO user_id;
ALTER TABLE token_ativacao RENAME COLUMN expira_em  TO expires_at;
ALTER TABLE token_ativacao RENAME COLUMN usado      TO used;
ALTER TABLE token_ativacao RENAME COLUMN finalidade TO purpose;

-- grupo
ALTER TABLE grupo RENAME COLUMN nome      TO name;
ALTER TABLE grupo RENAME COLUMN descricao TO description;
ALTER TABLE grupo RENAME COLUMN ativo     TO active;

-- grupo_funcionalidade
ALTER TABLE grupo_funcionalidade RENAME COLUMN grupo_id       TO group_id;
ALTER TABLE grupo_funcionalidade RENAME COLUMN funcionalidade TO functionality;

-- ── 2. Rename tables ──────────────────────────────────────────────────────────

ALTER TABLE schema_metadata      RENAME TO rh_schema_metadata;
ALTER TABLE usuario              RENAME TO rh_user;
ALTER TABLE usuario_documento    RENAME TO rh_user_document;
ALTER TABLE token_ativacao       RENAME TO rh_activation_token;
ALTER TABLE grupo                RENAME TO rh_group;
ALTER TABLE grupo_funcionalidade RENAME TO rh_group_functionality;

-- ── 3. Rename constraints ─────────────────────────────────────────────────────

ALTER TABLE rh_user RENAME CONSTRAINT uk_usuario_username TO uk_rh_user_username;
ALTER TABLE rh_user RENAME CONSTRAINT uk_usuario_email    TO uk_rh_user_email;
ALTER TABLE rh_user RENAME CONSTRAINT uk_usuario_cpf      TO uk_rh_user_cpf;
ALTER TABLE rh_user RENAME CONSTRAINT uk_usuario_rg       TO uk_rh_user_rg;

ALTER TABLE rh_user_document    RENAME CONSTRAINT fk_documento_usuario TO fk_rh_user_document_user;

ALTER TABLE rh_activation_token RENAME CONSTRAINT uk_token_ativacao TO uk_rh_activation_token;
ALTER TABLE rh_activation_token RENAME CONSTRAINT fk_token_usuario   TO fk_rh_activation_token_user;

ALTER TABLE rh_group RENAME CONSTRAINT pk_grupo TO pk_rh_group;

-- ── 4. Rename indexes ─────────────────────────────────────────────────────────

ALTER INDEX idx_documento_usuario RENAME TO idx_rh_user_document_user;
ALTER INDEX idx_token_usuario     RENAME TO idx_rh_activation_token_user;