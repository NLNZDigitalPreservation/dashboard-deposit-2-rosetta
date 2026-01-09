CREATE DATABASE db_fixity WITH ENCODING = 'UTF8';

\c db_fixity


CREATE ROLE fixity LOGIN PASSWORD 'fixity'
NOINHERIT
VALID UNTIL 'infinity';

GRANT USAGE ON SCHEMA public TO fixity;
