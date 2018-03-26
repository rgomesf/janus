# PostgreSQL docker image

This Docker image will initialize a PostgreSQL with a database already populated.
The loaded data will be used by [Data Extract](https://github.com/rgomesf/janus/blob/master/DataExtract).

------------------------

#### Build image

docker build -t data-evts -f Dockerfile .

#### Create container (run image)

docker run -it -d --name postgres-data-evts -p 5432:5432 data-evts:latest


This image was based on configurations referenced in [PostgreSQL official repository](https://hub.docker.com/_/postgres/)

