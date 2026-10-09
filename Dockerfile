FROM maven:3.9.9-eclipse-temurin-17 AS build

ARG SERVICE_MODULE
WORKDIR /workspace
COPY . .
RUN test -n "${SERVICE_MODULE}" \
    && mvn -B -ntp -pl "${SERVICE_MODULE}" -am package

FROM eclipse-temurin:17-jre-jammy AS runtime

ARG SERVICE_MODULE
WORKDIR /app
RUN groupadd --system spring \
    && useradd --system --gid spring --home-dir /app spring
COPY --from=build --chown=spring:spring \
    /workspace/${SERVICE_MODULE}/target/${SERVICE_MODULE}-*.jar /app/app.jar
USER spring
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
