FROM postgres:9.6
COPY scripts/init-db.sh /docker-entrypoint-initdb.d/init-db.sh
COPY scripts/*.sql /home/