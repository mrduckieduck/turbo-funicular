export PSQL_VERSION=$(grep "postgresContainerVersion" gradle.properties|cut -d'=' -f2)
export POSTGRES_PORT=$(grep "postgresContainerPort" gradle.properties|cut -d'=' -f2)

echo "Using PostgreSQL: ${PSQL_VERSION}"

cd src/test/docker/

export POSTGRES_DB=funicular
export POSTGRES_USER=user
export POSTGRES_PASSWORD=secret

docker-compose up -d --remove-orphans