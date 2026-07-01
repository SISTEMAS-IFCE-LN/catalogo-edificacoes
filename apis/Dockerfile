# ============== Estágio de build ==============
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build

# Copia SOMENTE a árvore de POMs do reator (mantém o cache de dependências
# estável: alterações em código-fonte não invalidam o `go-offline`).
COPY pom.xml ./pom.xml
COPY common-module/pom.xml ./common-module/pom.xml
COPY security-module/pom.xml ./security-module/pom.xml
COPY ambientes-internos-module/pom.xml ./ambientes-internos-module/pom.xml
COPY main-app/pom.xml ./main-app/pom.xml

# Pré-resolve dependências do reator (módulos `-am` resolvidos em cascata).
RUN mvn -B -ntp -q -e -DskipTests \
        -Pprod \
        -pl main-app -am \
        dependency:go-offline

# Copia o restante do código-fonte e empacota o jar executável.
COPY . .
RUN mvn -B -ntp -e -DskipTests \
        -Pprod \
        -pl main-app -am \
        clean package

# Padroniza o nome do artefato para `app.jar` (compatível com o entrypoint).
RUN mv main-app/target/main-app-*.jar app.jar

# ============== Estágio de release ==============
FROM gcr.io/distroless/java21-debian13:nonroot AS release
WORKDIR /app
COPY --from=build /build/app.jar ./app.jar
EXPOSE 8080
USER nonroot
ENTRYPOINT [ "java", "-jar", "./app.jar" ]
