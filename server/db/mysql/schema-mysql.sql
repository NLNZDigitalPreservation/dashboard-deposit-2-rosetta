set foreign_key_checks=0;

drop table if exists db_fixity.id_generator;
create table db_fixity.id_generator (
	ig_type varchar(255), 
	ig_value bigint
);

drop table if exists db_fixity.global_settings;
create table db_fixity.global_settings(
	id bigint not null,
	max_threads_data_load int not null default 1,
	max_seconds_event_loop int not null default 60,
  max_storage_days_after_purged int not null default 7,
	primary key (id)
);
insert into db_fixity.global_settings(id) values (1);

drop table if exists db_fixity.white_list;
create table db_fixity.white_list(
  id bigint(20) NOT NULL AUTO_INCREMENT,
	username varchar(64) not null,
	role varchar(8) not null,
	primary key (id),
	index idx_wl_username (username)
);

drop table if exists db_fixity.job_target;
create table db_fixity.job_target(
	id bigint not null,
  state int not null default 0,
  fixity_type int not null default 0,
  running_round bigint not null default 0,
	creation_date bigint not null default 0,
	start_datetime bigint not null default 0,
	latest_datetime bigint not null default 0,
	primary key (id)
);
insert into db_fixity.job_target(id) values (1);


drop table if exists db_fixity.original_job;
create table db_fixity.original_job(
  id bigint(20) NOT NULL AUTO_INCREMENT,
  pi_id bigint(20) NOT NULL,
  index_location varchar(255) DEFAULT NULL,
  file_size bigint(20) DEFAULT NULL,
  check_sum varchar(255) DEFAULT NULL,
  check_sum_type varchar(255) DEFAULT NULL,
  storage_entity_type varchar(8) DEFAULT NULL,
  stored_entity_id varchar(32) DEFAULT NULL,
  version float DEFAULT NULL,
  update_check_sum int(11) DEFAULT NULL,
  phys_check_sum varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY job_pi_id (pi_id),
  UNIQUE KEY job_stored_entity_id_version_storage_entity_type (stored_entity_id,version,storage_entity_type),
  KEY job_stored_entity_id (stored_entity_id),
  KEY job_version (version),
  KEY job_storage_entity_type (storage_entity_type)
);

drop table if exists db_fixity.job;
create table db_fixity.job(
  id bigint(20) NOT NULL AUTO_INCREMENT,
  pi_id bigint(20) NOT NULL,
  index_location varchar(255) DEFAULT NULL,
  file_size bigint(20) DEFAULT NULL,
  check_sum varchar(255) DEFAULT NULL,
  check_sum_type varchar(255) DEFAULT NULL,
  storage_entity_type varchar(8) DEFAULT NULL,
  stored_entity_id varchar(32) DEFAULT NULL,
  version float DEFAULT NULL,
  update_check_sum int(11) DEFAULT NULL,
  phys_check_sum varchar(255) DEFAULT NULL,
  ie_pi_id bigint(20) DEFAULT NULL,
  pir_file_check_state int(11) NOT NULL DEFAULT 0,
  pir_file_check_desc varchar(4000) DEFAULT NULL,
  pir_file_checksum varchar(255) DEFAULT NULL,
  pir_mets_check_state int(11) NOT NULL DEFAULT 0,
  pir_mets_check_desc varchar(4000) DEFAULT NULL,
  pir_mets_checksum varchar(255) DEFAULT NULL,
  pir_job_state int(11) NOT NULL DEFAULT 0,
  pir_job_desc varchar(4000) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY job_pi_id (pi_id),
  UNIQUE KEY job_stored_entity_id_version_storage_entity_type (stored_entity_id,version,storage_entity_type),
  KEY job_stored_entity_id (stored_entity_id),
  KEY job_version (version),
  KEY job_storage_entity_type (storage_entity_type),
  KEY job_pir_job_state (pir_job_state)
);

