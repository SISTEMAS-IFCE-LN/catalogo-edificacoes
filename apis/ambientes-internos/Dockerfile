FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp clean package
WORKDIR /build/target
RUN mv *.jar app.jar
FROM gcr.io/distroless/java21-debian13:nonroot AS release
WORKDIR /app
COPY --from=build /build/target/app.jar ./app.jar
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "./app.jar" ]