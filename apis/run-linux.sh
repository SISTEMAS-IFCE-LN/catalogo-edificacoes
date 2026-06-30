#!/bin/sh
# =============================================================================
# Script para iniciar a aplica????o em ambiente Linux/macOS.
#
# Uso:
#   1. Copie o .env.example para .env e preencha os valores reais.
#   2. Torne execut??vel: chmod +x apis/run-linux.sh
#   3. Execute: ./apis/run-linux.sh
#
# O script:
#   - Carrega vari??veis do .env no escopo do processo.
#   - Verifica se os arquivos .pem das chaves RSA existem.
#   - Mata qualquer processo que esteja ocupando a porta 8080.
#   - Inicia a aplica????o via mvn spring-boot:run a partir do main-app.
# =============================================================================

set -euo pipefail

# -----------------------------------------------------------------------------
# Carrega o arquivo .env se existir.
# O .env deve ficar em apis/.env (mesmo diret??rio deste script).
# -----------------------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

if [[ ! -f "$ENV_FILE" ]]; then
    echo "[ERRO] Arquivo .env n??o encontrado em: $ENV_FILE"
    echo "       Copie o .env.example para .env e preencha os valores reais."
    exit 1
fi

echo "[INFO] Carregando vari??veis de ambiente de $ENV_FILE..."

# Faz o source apenas das linhas que parecem VAR=valor (ignora coment??rios
# e linhas vazias). Suporta aspas duplas ou simples ao redor do valor.
set -a
# shellcheck disable=SC1090
while IFS='=' read -r key value; do
    # Pula linhas que n??o come??am com VARIAVEL_MAIUSCULA ou s??o coment??rios.
    [[ "$key" =~ ^[A-Z_][A-Z0-9_]*$ ]] || continue
    # Remove aspas ao redor do valor, se houver.
    value="${value%\"}"
    value="${value#\"}"
    value="${value%\'}"
    value="${value#\'}"
    export "$key"="$value"
done < <(grep -E '^[A-Z_][A-Z0-9_]*=' "$ENV_FILE")
set +a

# -----------------------------------------------------------------------------
# Verifica arquivos .pem das chaves RSA.
# Os caminhos em JWT_PUBLIC_KEY_PATH / JWT_PRIVATE_KEY_PATH no .env podem
# ser relativos (file:../.keys/public.pem) ou absolutos. Removemos o prefixo
# file: para verificar a exist??ncia do arquivo.
# O caminho ?? relativo ao diret??rio apis/main-app/ (working directory do Spring Boot).
# -----------------------------------------------------------------------------
if [[ -z "${JWT_PUBLIC_KEY_PATH:-}" ]]; then
    echo "[ERRO] JWT_PUBLIC_KEY_PATH nao definido no .env"
    exit 1
fi
if [[ -z "${JWT_PRIVATE_KEY_PATH:-}" ]]; then
    echo "[ERRO] JWT_PRIVATE_KEY_PATH nao definido no .env"
    exit 1
fi

# Remove prefixo file: para verificar existencia
PUBLIC_KEY_FILE="${JWT_PUBLIC_KEY_PATH#file:}"
PRIVATE_KEY_FILE="${JWT_PRIVATE_KEY_PATH#file:}"

# Muda para o diret??rio main-app para verificar os caminhos relativos
cd "$SCRIPT_DIR/main-app"

if [[ ! -f "$PUBLIC_KEY_FILE" ]]; then
    echo "[ERRO] Chave p??blica nao encontrada: $PUBLIC_KEY_FILE"
    echo "       Execute para gerar:"
    echo "         openssl genrsa -out .keys/private.pem 2048"
    echo "         openssl rsa -in .keys/private.pem -pubout -out .keys/public.pem"
    echo "         openssl pkcs8 -topk8 -in .keys/private.pem -out .keys/private_pkcs8.pem -nocrypt"
    echo "       E ajuste os caminhos no .env."
    exit 1
fi
if [[ ! -f "$PRIVATE_KEY_FILE" ]]; then
    echo "[ERRO] Chave privada nao encontrada: $PRIVATE_KEY_FILE"
    exit 1
fi

# -----------------------------------------------------------------------------
# Define o profile Maven com base no PROFILE_ACTIVE do Spring.
# Em prod o driver PostgreSQL so entra no classpath com -Pprod.
# -----------------------------------------------------------------------------
MAVEN_PROFILE=""
if [[ "${PROFILE_ACTIVE:-dev}" == "prod" ]]; then
    MAVEN_PROFILE="-Pprod"
    echo "[INFO] Profile 'prod' detectado; usando PostgreSQL ($MAVEN_PROFILE)."
fi

echo "[INFO] Vari??veis carregadas:"
echo "        PROFILE_ACTIVE    = ${PROFILE_ACTIVE:-}"
echo "        BOOTSTRAP_ADMIN   = ${BOOTSTRAP_ADMIN_EMAIL:-}"
echo "        JWT_PUBLIC_KEY    = $JWT_PUBLIC_KEY_PATH"
echo "        JWT_PRIVATE_KEY   = $JWT_PRIVATE_KEY_PATH"
if [[ "${PROFILE_ACTIVE:-dev}" == "prod" ]]; then
    echo "        DATASOURCE_URL    = ${SPRING_DATASOURCE_URL:-}"
    echo "        DATASOURCE_USER   = ${SPRING_DATASOURCE_USERNAME:-}"
fi
echo

# -----------------------------------------------------------------------------
# Limpa processos java antigos na porta 8080 (se houver).
# -----------------------------------------------------------------------------
echo "[INFO] Verificando porta 8080..."
PORT_PIDS=""
if command -v lsof >/dev/null 2>&1; then
    PORT_PIDS="$(lsof -ti :8080 2>/dev/null || true)"
elif command -v fuser >/dev/null 2>&1; then
    PORT_PIDS="$(fuser 8080/tcp 2>/dev/null || true)"
fi

if [[ -n "$PORT_PIDS" ]]; then
    echo "[INFO] Matando processo(s) $PORT_PIDS que ocupa(m) a porta 8080..."
    # shellcheck disable=SC2086
    kill -9 $PORT_PIDS 2>/dev/null || true
    sleep 3
fi

# -----------------------------------------------------------------------------
# Verifica que o mvnw existe e ?? execut??vel.
# Estrutura esperada:
#   <raiz>/
#     apis/
#       mvnw
#       main-app/  (contem CatalogoEdificacoesApp.kt)
#       run-linux.sh   (este script)
#       .env
#       .keys/  (contem public.pem, private_pkcs8.pem)
# -----------------------------------------------------------------------------
APIS_DIR="$SCRIPT_DIR"
MVNW="$APIS_DIR/mvnw"
MAIN_APP_DIR="$APIS_DIR/main-app"

if [[ ! -f "$MVNW" ]]; then
    echo "[ERRO] mvnw n??o encontrado em: $MVNW"
    exit 1
fi
chmod +x "$MVNW"

# -----------------------------------------------------------------------------
# Inicia a aplica????o.
# O CWD ?? apis/ (raiz do projeto Maven multi-m??dulo). O mvnw ?? invocado com
# -f main-app/pom.xml para executar apenas o main-app, mas o working directory
# permanece apis/ para que os caminhos relativos das chaves RSA funcionem.
# -----------------------------------------------------------------------------
echo "[INFO] Iniciando aplica????o a partir de $APIS_DIR..."
echo
cd "$APIS_DIR"
"$MVNW" $MAVEN_PROFILE clean install -DskipTests
exec "$MVNW" $MAVEN_PROFILE spring-boot:run -pl main-app
