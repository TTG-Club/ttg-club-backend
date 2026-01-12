FROM maven:3-eclipse-temurin-8 AS base

WORKDIR /opt/app


FROM base AS build

COPY --link . .
RUN FRONTEND_SHA=$(echo -n $(date +%s) | sha256sum | awk '{print $1}') && \
    mvn -B clean install -DskipTests -Dfrontend.application.sha=${FRONTEND_SHA}

FROM eclipse-temurin:17-jre

WORKDIR /opt/app

COPY --from=build /opt/app/target/dnd5.jar dnd5.jar

ENTRYPOINT ["java","-jar","dnd5.jar"]
