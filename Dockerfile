# syntax=docker/dockerfile:1

#### BUILD
FROM maven:3.8.4-openjdk-17-slim AS build

WORKDIR /app

COPY pom.xml ./pom.xml
COPY lib ./lib
COPY modules ./modules
COPY platform ./platform
COPY load-test ./load-test
COPY udp-replication-server ./udp-replication-server

RUN mvn clean
RUN mvn clean install

#### PACKAGE
FROM gcr.io/distroless/java17-debian11

COPY --from=build /app/platform/target/release/gec-platform-2.0-bin/gec-platform-2.0 /platform
EXPOSE 8090

ENV JAVA_OPTS="-server -Xms24g -Xmx60g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=20 -XX:ConcGCThreads=5 -XX:InitiatingHeapOccupancyPercent=70"
ENV JAVA_HZ="--add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED"

WORKDIR /platform/bin
ENTRYPOINT ["java", "-classpath", "../lib/*:../conf:../deploy:../web", "com.tarantula.platform.bootstrap.TarantulaMain"]
