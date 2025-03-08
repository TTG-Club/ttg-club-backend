FROM maven:3-eclipse-temurin-8 AS base

WORKDIR /opt/app


FROM base AS build

COPY --link . .
RUN mvn -B clean install -DskipTests -Dfrontend.application.sha=


FROM eclipse-temurin:17-jre

WORKDIR /opt/app

COPY --from=build /opt/app/target/dnd5.jar dnd5.jar

ENTRYPOINT ["java","-jar","dnd5.jar"]
