-- grant select, insert, update, delete on id_generator to depdash;
grant select, insert, update, delete, truncate on global_setting to depdash;
grant select, insert, update, delete, truncate on deposit_account to depdash;
grant select, insert, update, delete, truncate on storage_location to depdash;
grant select, insert, update, delete, truncate on whitelist_setting to depdash;
grant select, insert, update, delete, truncate on flow_setting to depdash;
grant select, insert, update, delete, truncate on deposit_job to depdash;

alter table global_setting owner to depdash;
alter table deposit_account owner to depdash;
alter table storage_location owner to depdash;
alter table whitelist_setting owner to depdash;
alter table flow_setting owner to depdash;
alter table deposit_job owner to depdash;
