@echo off
REM =============================================================================
REM Script para iniciar a aplicacao em ambiente Windows.
REM
REM Uso:
REM   1. Copie o .env.example para .env e preencha os valores reais.
REM   2. Execute: apis\run-windows.bat
REM
REM O script:
REM   - Carrega variaveis do .env no escopo do processo.
REM   - Verifica se os arquivos .pem das chaves RSA existem.
REM   - Mata qualquer processo que esteja ocupando a porta 8080.
REM   - Inicia a aplicacao via mvn spring-boot:run a partir do main-app.
REM =============================================================================

REM Garante que o batch leia o .env em UTF-8 (encoding do .env).
chcp 65001 >nul

setlocal EnableExtensions EnableDelayedExpansion

REM -----------------------------------------------------------------------------
REM Carrega o arquivo .env se existir.
REM O .env deve ficar em apis/.env (mesmo diretorio deste script).
REM -----------------------------------------------------------------------------
set "SCRIPT_DIR=%~dp0"
set "ENV_FILE=%SCRIPT_DIR%.env"

if not exist "%ENV_FILE%" (
    echo [ERRO] Arquivo .env nao encontrado em: %ENV_FILE%
    echo        Copie o .env.example para .env e preencha os valores reais.
    exit /b 1
)

echo [INFO] Carregando variaveis de ambiente de %ENV_FILE%...

REM Le linha a linha para evitar problemas com espacos, aspas e caracteres
REM especiais. Cada linha valida segue o padrao NOME=VALOR.
for /f "usebackq eol=# tokens=1* delims==" %%A in ("%ENV_FILE%") do (
    set "KEY=%%A"
    set "VAL=%%B"
    REM Ignora linhas em branco (KEY vazio) e comentarios
    if not "!KEY!"=="" set "!KEY!=!VAL!"
)

REM -----------------------------------------------------------------------------
REM Verifica arquivos .pem das chaves RSA.
REM Os caminhos em JWT_PUBLIC_KEY_PATH / JWT_PRIVATE_KEY_PATH no .env podem
REM ser relativos (file:../.keys/public.pem) ou absolutos. Removemos o prefixo
REM file: para verificar a existencia do arquivo.
REM O caminho e relativo ao diretorio apis/main-app/ (working directory do Spring Boot).
REM -----------------------------------------------------------------------------
if not defined JWT_PUBLIC_KEY_PATH (
    echo [ERRO] JWT_PUBLIC_KEY_PATH nao definido no .env
    exit /b 1
)
if not defined JWT_PRIVATE_KEY_PATH (
    echo [ERRO] JWT_PRIVATE_KEY_PATH nao definido no .env
    exit /b 1
)

REM Remove prefixo file: para verificar existencia
set "PUBLIC_KEY_FILE=%JWT_PUBLIC_KEY_PATH:file:=%"
set "PRIVATE_KEY_FILE=%JWT_PRIVATE_KEY_PATH:file:=%"

REM Muda para o diretorio main-app para verificar os caminhos relativos
cd /d "%SCRIPT_DIR%main-app"

if not exist "%PUBLIC_KEY_FILE%" (
    echo [ERRO] Chave publica nao encontrada: %PUBLIC_KEY_FILE%
    echo        Execute para gerar:
    echo          openssl genrsa -out .keys/private.pem 2048
    echo          openssl rsa -in .keys/private.pem -pubout -out .keys/public.pem
    echo          openssl pkcs8 -topk8 -in .keys/private.pem -out .keys/private_pkcs8.pem -nocrypt
    echo        E ajuste os caminhos no .env.
    exit /b 1
)
if not exist "%PRIVATE_KEY_FILE%" (
    echo [ERRO] Chave privada nao encontrada: %PRIVATE_KEY_FILE%
    exit /b 1
)

REM -----------------------------------------------------------------------------
REM Define o profile Maven com base no PROFILE_ACTIVE do Spring.
REM Em prod o driver PostgreSQL so entra no classpath com -Pprod.
REM -----------------------------------------------------------------------------
set "MAVEN_PROFILE="
if "%PROFILE_ACTIVE%"=="prod" (
    set "MAVEN_PROFILE=-Pprod"
    echo [INFO] Profile 'prod' detectado; usando PostgreSQL: !MAVEN_PROFILE!
)

echo [INFO] Variaveis carregadas:
echo        PROFILE_ACTIVE    = %PROFILE_ACTIVE%
echo        BOOTSTRAP_ADMIN   = %BOOTSTRAP_ADMIN_EMAIL%
echo        JWT_PUBLIC_KEY    = %JWT_PUBLIC_KEY_PATH%
echo        JWT_PRIVATE_KEY   = %JWT_PRIVATE_KEY_PATH%
if "%PROFILE_ACTIVE%"=="prod" (
    echo        DATASOURCE_URL    = !SPRING_DATASOURCE_URL!
    echo        DATASOURCE_USER   = !SPRING_DATASOURCE_USERNAME!
)
echo.

REM -----------------------------------------------------------------------------
REM Limpa processos java antigos na porta 8080 (se houver).
REM -----------------------------------------------------------------------------
echo [INFO] Verificando porta 8080...
set "PORT_IN_USE=0"
for /f "tokens=5" %%P in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    echo [INFO] Matando processo %%P que ocupa a porta 8080...
    taskkill /F /PID %%P >nul 2>&1
    set "PORT_IN_USE=1"
)
if "!PORT_IN_USE!"=="1" (
    timeout /t 3 /nobreak >nul
)

REM -----------------------------------------------------------------------------
REM Verifica que o mvnw.cmd existe.
REM Estrutura esperada:
REM   <raiz>/
REM     apis/
REM       mvnw.cmd
REM       main-app/  (contem CatalogoEdificacoesApp.kt)
REM       run-windows.bat   (este script)
REM       .env
REM       .keys/  (contem public.pem, private_pkcs8.pem)
REM -----------------------------------------------------------------------------
REM %~dp0 termina com barra invertida; removemos para evitar ".\" no path.
set "APIS_DIR=%SCRIPT_DIR:~0,-1%"
set "MVNW=%APIS_DIR%\mvnw.cmd"

if not exist "%MVNW%" (
    echo [ERRO] mvnw.cmd nao encontrado em: %MVNW%
    exit /b 1
)

REM -----------------------------------------------------------------------------
REM Inicia a aplicacao.
REM O CWD e apis/ (raiz do projeto Maven multi-modulo). O mvnw e invocado com
REM -f main-app/pom.xml para executar apenas o main-app, mas o working directory
REM permanece apis/ para que os caminhos relativos das chaves RSA funcionem.
REM -----------------------------------------------------------------------------
echo [INFO] Iniciando aplicacao a partir de %APIS_DIR%...
echo.
cd /d "%APIS_DIR%"
call "%MVNW%" %MAVEN_PROFILE% clean install -DskipTests
call "%MVNW%" %MAVEN_PROFILE% spring-boot:run -pl main-app

endlocal
