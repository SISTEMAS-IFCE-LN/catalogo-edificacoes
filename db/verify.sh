#!/bin/sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
: "${FLYWAY_IMAGE:?Defina FLYWAY_IMAGE com a mesma tag usada no docker-compose.yml}"

PG_IMAGE="${PG_IMAGE:-postgres:17-trixie}"
NETWORK="catalogo-migrations-verify-$$"
POSTGRES="catalogo-migrations-pg-$$"
DB_NAME="catalogo_verify"
DB_USER="catalogo_verify"
DB_PASSWORD="catalogo_verify"

cleanup() {
  docker rm -f "$POSTGRES" >/dev/null 2>&1 || true
  docker network rm "$NETWORK" >/dev/null 2>&1 || true
}
trap cleanup EXIT INT TERM

docker network create "$NETWORK" >/dev/null
docker run -d --name "$POSTGRES" --network "$NETWORK" \
  -e POSTGRES_DB="$DB_NAME" \
  -e POSTGRES_USER="$DB_USER" \
  -e POSTGRES_PASSWORD="$DB_PASSWORD" \
  "$PG_IMAGE" >/dev/null

i=0
until docker exec "$POSTGRES" pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1; do
  i=$((i + 1))
  if [ "$i" -ge 60 ]; then
    echo "PostgreSQL não ficou pronto em 60s" >&2
    exit 1
  fi
  sleep 1
done

run_flyway() {
  docker run --rm --network "$NETWORK" \
    -v "$ROOT_DIR/db/migration:/flyway/sql:ro" \
    -e FLYWAY_URL="jdbc:postgresql://$POSTGRES:5432/$DB_NAME" \
    -e FLYWAY_USER="$DB_USER" \
    -e FLYWAY_PASSWORD="$DB_PASSWORD" \
    -e FLYWAY_LOCATIONS=filesystem:/flyway/sql \
    -e FLYWAY_CLEAN_DISABLED=true \
    -e FLYWAY_BASELINE_ON_MIGRATE=false \
    -e FLYWAY_OUT_OF_ORDER=false \
    "$FLYWAY_IMAGE" "$@"
}

run_flyway migrate
run_flyway info
run_flyway validate

fail_count=$(docker exec "$POSTGRES" psql -U "$DB_USER" -d "$DB_NAME" -tAc \
  "select count(*) from flyway_schema_history where success = false")
if [ "$fail_count" != "0" ]; then
  echo "Há migrations com success=false" >&2
  exit 1
fi

applied=$(docker exec "$POSTGRES" psql -U "$DB_USER" -d "$DB_NAME" -tAc \
  "select count(*) from flyway_schema_history where success = true")
if [ "$applied" -lt 1 ]; then
  echo "Nenhuma migration aplicada" >&2
  exit 1
fi

echo "OK: $applied migration(s) aplicada(s) em PostgreSQL 17"
