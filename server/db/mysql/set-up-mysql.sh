#!/usr/bin/env bash

export MYSQL_PWD=password
cat create-mysql.sql | mysql -u root

#cat schema-mysql.sql | mysql -u root -D db_fixity

#cat schema-grants-mysql.sql | mysql -u root -D db_fixity

#cat initial-data-mysql.sql | mysql -u root -D db_fixity
