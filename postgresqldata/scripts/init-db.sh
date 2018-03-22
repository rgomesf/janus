#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE graphdata;
EOSQL
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" graphdata < /home/create-tables.sql
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" graphdata < /home/events-data.sql
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" graphdata < /home/events-data2.sql
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" graphdata < /home/events-data3.sql
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" graphdata < /home/events-data4.sql
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" graphdata < /home/events-data5.sql