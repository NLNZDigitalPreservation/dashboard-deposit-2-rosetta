CREATE TABLE IF NOT EXISTS global_setting (
    id BIGINT PRIMARY KEY,
    audit_rst BOOLEAN NOT NULL DEFAULT TRUE,
    audit_msg TEXT NOT NULL DEFAULT 'OK',
    paused BOOLEAN NOT NULL DEFAULT FALSE,
    paused_start_time TEXT NOT NULL,
    paused_end_time TEXT NOT NULL,
    delays BIGINT NOT NULL,
    delay_unit VARCHAR(8) NOT NULL
);

CREATE TABLE IF NOT EXISTS deposit_account_setting (
    id BIGINT PRIMARY KEY,
    audit_rst BOOLEAN NOT NULL DEFAULT TRUE,
    audit_msg TEXT NOT NULL DEFAULT 'OK',
    deposit_user_institute VARCHAR(64) NOT NULL,
    deposit_user_name VARCHAR(255) NOT NULL,
    deposit_user_password TEXT NOT NULL,
    producers JSONB NOT NULL DEFAULT '[]'::jsonb
);

CREATE TABLE IF NOT EXISTS storage_location (
    id BIGINT PRIMARY KEY,
    audit_rst BOOLEAN NOT NULL DEFAULT TRUE,
    audit_msg TEXT NOT NULL DEFAULT 'OK',
    scan_mode VARCHAR(8),
    root_path TEXT,
    ftp_server TEXT,
    ftp_port INTEGER NOT NULL DEFAULT 0,
    ftp_username TEXT,
    ftp_password TEXT,
    ftp_proxy_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ftp_proxy_host TEXT,
    ftp_proxy_port INTEGER NOT NULL DEFAULT 0,
    ftp_proxy_username TEXT,
    ftp_proxy_password TEXT
);

CREATE TABLE IF NOT EXISTS whitelist_setting (
    id BIGINT PRIMARY KEY,
    audit_rst BOOLEAN NOT NULL DEFAULT TRUE,
    audit_msg TEXT NOT NULL DEFAULT 'OK',
    white_user_name VARCHAR(255) NOT NULL,
    white_user_role VARCHAR(16) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_whitelist_setting_white_user_name
    ON whitelist_setting (white_user_name);

CREATE TABLE IF NOT EXISTS flow_setting (
    id BIGINT PRIMARY KEY,
    audit_rst BOOLEAN NOT NULL DEFAULT TRUE,
    audit_msg TEXT NOT NULL DEFAULT 'OK',
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    deposit_account_id BIGINT NOT NULL,
    material_flow_id TEXT NOT NULL,
    material_flow_name TEXT,
    producer_id TEXT NOT NULL,
    producer_name TEXT,
    root_path TEXT NOT NULL,
    stream_location TEXT NOT NULL,
    injection_complete_file_name TEXT NOT NULL,
    max_active_days BIGINT NOT NULL,
    max_save_days BIGINT NOT NULL,
    delays BIGINT,
    delay_unit VARCHAR(8),
    weekly_max_concurrency JSONB NOT NULL DEFAULT '[0,0,0,0,0,0,0]'::jsonb,
    actual_content_delete_options TEXT,
    backup_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    actual_content_backup_options TEXT NOT NULL,
    backup_path TEXT,
    backup_sub_folders TEXT
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_flow_setting_material_flow_id
    ON flow_setting (material_flow_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_flow_setting_root_path
    ON flow_setting (root_path);
CREATE INDEX IF NOT EXISTS idx_flow_setting_deposit_account_id
    ON flow_setting (deposit_account_id);


CREATE TABLE IF NOT EXISTS deposit_job (
    id BIGINT PRIMARY KEY,
    audit_rst BOOLEAN NOT NULL DEFAULT TRUE,
    audit_msg TEXT NOT NULL DEFAULT 'OK',
    initial_time BIGINT,
    latest_time BIGINT,
    deposit_start_time BIGINT,
    deposit_end_time BIGINT,
    finalized_time BIGINT,
    finished_time BIGINT,
    injection_path TEXT,
    injection_title TEXT,
    file_count BIGINT NOT NULL DEFAULT 0,
    file_size BIGINT NOT NULL DEFAULT 0,
    is_successful BOOLEAN NOT NULL DEFAULT FALSE,
    sip_id TEXT,
    sip_module TEXT,
    sip_stage TEXT,
    sip_status TEXT,
    stage VARCHAR(16) NOT NULL,
    state VARCHAR(16) NOT NULL,
    deposit_set_id TEXT,
    result_message TEXT,
    applied_flow_setting BIGINT NOT NULL,
    actual_content_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    backup_completed BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_deposit_job_initial_time
    ON deposit_job (initial_time);
CREATE INDEX IF NOT EXISTS idx_deposit_job_latest_time
    ON deposit_job (latest_time);
CREATE INDEX IF NOT EXISTS idx_deposit_job_sip_id
    ON deposit_job (sip_id);
CREATE INDEX IF NOT EXISTS idx_deposit_job_state
    ON deposit_job (state);
CREATE INDEX IF NOT EXISTS idx_deposit_job_flow_setting
    ON deposit_job (applied_flow_setting);
