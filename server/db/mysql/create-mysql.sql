create database if not exists db_fixity;
use db_fixity;
create user if not exists fixity@'%' identified by 'fixity';
create user if not exists fixity@'localhost' identified by 'fixity';
grant all privileges on db_fixity.* to fixity@'%';
grant all privileges on db_fixity.* to fixity@'localhost';
flush privileges;
alter user fixity@'%' identified by 'fixity';
alter user fixity@'localhost' identified by 'fixity';
