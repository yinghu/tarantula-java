# syntax=docker/dockerfile:1

#### BUILD
FROM maven:3.8.4-openjdk-17-slim AS build

WORKDIR /app

COPY lib ./lib
COPY modules ./modules
COPY platform ./platform

RUN mvn -f modules/protocol/pom.xml clean install
RUN mvn -f platform/pom.xml clean
RUN mvn -f platform/pom.xml clean install

#### PACKAGE
FROM gcr.io/distroless/java17-debian11

COPY --from=build /app/platform/target/release/gec-platform-2.0-bin/gec-platform-2.0 /platform
EXPOSE 8090

ENV JAVA_OPTS="-Xms1024m -Xmx2048m -XX:MaxDirectMemorySize=4096M -server"
ENV JAVA_HZ="--add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED"

WORKDIR /platform/bin
ENTRYPOINT ["java", "-classpath", "../conf:../deploy:../web:../lib/*:gec-agent-1.0.jar", "com.tarantula.platform.bootstrap.TarantulaMain"]
