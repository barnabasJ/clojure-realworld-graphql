FROM createat/leiningen:openjdk-8 AS build

WORKDIR /app

COPY project.clj ./

RUN lein deps

COPY resources resources
COPY src src

RUN lein uberjar

FROM openjdk:8u252-jre

WORKDIR /app

COPY --from=build /app/target/crg-0.1.0-SNAPSHOT-standalone.jar ./

CMD ["java", "-jar", "crg-0.1.0-SNAPSHOT-standalone.jar"]

