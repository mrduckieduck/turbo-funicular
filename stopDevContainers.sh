#!/usr/bin/env bash


export PSQL_VERSION=$(grep "postgresContainerVersion" gradle.properties|cut -d'=' -f2)
export POSTGRES_PORT=$(grep "postgresContainerPort" gradle.properties|cut -d'=' -f2)


cd src/test/docker/

docker-compose down --rmi local -v