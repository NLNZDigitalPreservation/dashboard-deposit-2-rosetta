--Table: global_settings
create table global_settings (
    id BIGSERIAL primary key,
    paused BOOLEAN not null default false,
    paused_start_time DOUBLE PRECISION not null default 0,
    paused_end_time DOUBLE PRECISION not null default 0
);
insert into global_settings (id)
values (1);
--
--
-- 
-- white_list
create table white_list (
    id BIGSERIAL primary key,
    username VARCHAR(64) not null,
    role VARCHAR(8) not null
);
create index idx_wl_username on white_list (username);
--
--
--
--Table: fixity_task
create table fixity_task (
    id BIGSERIAL primary key,
    name VARCHAR(255) not null default null,
    fixity_type INT not null default 0,
    fixity_scope INT not null default 0,
    sync_data_flag BOOLEAN not null default false,
    creation_time DOUBLE PRECISION not null default 0,
    start_time DOUBLE PRECISION not null default 0,
    end_time DOUBLE PRECISION not null default 0,
    actual_start_time DOUBLE PRECISION not null default 0,
    actual_end_time DOUBLE PRECISION not null default 0,
    state INT not null default 0
);
create unique index task_name on fixity_task (name);
create index task_state on fixity_task (state);
--
--
-- 
-- Table: blob_event_queue
create table blob_event_queue (
    id BIGSERIAL primary key,
    creation_time DOUBLE PRECISION not null default 0,
    blob_url VARCHAR(1024) default null,
    is_running BOOLEAN not null default false
);
create unique index blobeventqueue_blob_url on blob_event_queue (blob_url);
create index blobeventqueue_creation_time on blob_event_queue (creation_time);
create index blobeventqueue_is_running on blob_event_queue (is_running);
--
--
-- 
-- Table:  blob_event_history
create table blob_event_history (
    id BIGSERIAL primary key,
    creation_time DOUBLE PRECISION not null default 0,
    blob_url VARCHAR(1024) default null,
    pir_file_check_state INT not null default 0,
    pir_file_check_desc VARCHAR(4000) default null,
    pir_mets_check_state INT not null default 0,
    pir_mets_check_desc VARCHAR(4000) default null
);
create unique index blobeventhistory_blob_url on blob_event_history (blob_url);
create index blobeventhistory_creation_date on blob_event_history (creation_time);
create index blobeventhistory_pir_file_check_state on blob_event_history (pir_file_check_state);
create index blobeventhistory_pir_mets_check_state on blob_event_history (pir_mets_check_state);
--
--
-- 
-- Table:  permanent_index
create table permanent_index (
    id BIGINT not null,
    file_size BIGINT,
    version BIGINT,
    status BIGINT,
    stored_entity_id VARCHAR(255),
    check_sum_type VARCHAR(255),
    storage_id BIGINT,
    update_date TIMESTAMP,
    storage_entity_type VARCHAR(8),
    index_location VARCHAR(255),
    check_sum VARCHAR(255),
    update_check_sum SMALLINT,
    phys_check_sum VARCHAR(255),
    phys_check_sum_type VARCHAR(255),
    xsd_versions VARCHAR(50),
    created_by VARCHAR(255),
    title VARCHAR(4000),
    ie_pi_id BIGINT default null,
    pir_file_check_state INT not null default 0,
    pir_file_check_desc VARCHAR(4000) default null,
    pir_file_checksum VARCHAR(255) default null,
    pir_mets_check_state INT not null default 0,
    pir_mets_check_desc VARCHAR(4000) default null,
    pir_mets_checksum VARCHAR(255) default null,
    constraint permanent_index_pk_01 primary key (id),
    constraint permanent_index_uk_02 unique (stored_entity_id, version, storage_entity_type)
);
-- Additional Non-Unique Indexes
-- These correspond to the blue diamond icons in the bottom list of your image
create index permanentindex_storageentity on permanent_index (storage_entity_type);
create index permanentindex_s_e_id on permanent_index (stored_entity_id);
create index permanentindex_version on permanent_index (version);
create index permanentindex_pir_file_check_state on permanent_index (pir_file_check_state);
create index permanentindex_pir_mets_check_state on permanent_index (pir_mets_check_state);
--
--
-- 
-- Table:  permanent_index_history
create table permanent_index_history (
    id BIGSERIAL primary key,
    pi_id BIGINT not null,
    file_size BIGINT,
    version BIGINT,
    status BIGINT,
    stored_entity_id VARCHAR(255),
    check_sum_type VARCHAR(255),
    storage_id BIGINT,
    update_date TIMESTAMP,
    storage_entity_type VARCHAR(8),
    index_location VARCHAR(255),
    check_sum VARCHAR(255),
    update_check_sum SMALLINT,
    phys_check_sum VARCHAR(255),
    phys_check_sum_type VARCHAR(255),
    xsd_versions VARCHAR(50),
    created_by VARCHAR(255),
    title VARCHAR(4000),
    ie_pi_id BIGINT default null,
    pir_file_check_state INT not null default 0,
    pir_file_check_desc VARCHAR(4000) default null,
    pir_file_checksum VARCHAR(255) default null,
    pir_mets_check_state INT not null default 0,
    pir_mets_check_desc VARCHAR(4000) default null,
    pir_mets_checksum VARCHAR(255) default null,
    constraint permanent_index_history_uk_02 unique (pi_id, stored_entity_id, version, storage_entity_type)
);
-- Additional Non-Unique Indexes
-- These correspond to the blue diamond icons in the bottom list of your image
create index permanentindexhistory_storageentity on permanent_index_history (storage_entity_type);
create index permanentindexhistory_pi_id on permanent_index_history (pi_id);
create index permanentindexhistory_s_e_id on permanent_index_history (stored_entity_id);
create index permanentindexhistory_version on permanent_index_history (version);
create index permanentindexhistory_pir_file_check_state on permanent_index_history (pir_file_check_state);
create index permanentindexhistory_pir_mets_check_state on permanent_index_history (pir_mets_check_state);
--
--
-- 
-- Table: storage_parameter
create table storage_parameter (
    id BIGINT not null,
    value VARCHAR(4000),
    key VARCHAR(50),
    storage_id BIGINT,
    constraint storage_parameter_pk_01 primary key (id)
);
-- Index on the Foreign Key column (corresponds to STORAGE_ID_IDX)
create index storage_id_idx on storage_parameter (storage_id);
