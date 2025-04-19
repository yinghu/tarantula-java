#### BUILD
FROM maven:3.9.9-sapmachine-24 AS build
WORKDIR /app

COPY pom.xml ./pom.xml
COPY modules ./modules
COPY platform ./platform

RUN mvn clean install

#### PACKAGE
FROM openjdk:24

COPY --from=build /app/platform/target/release/gec-platform-3.0-bin/gec-platform-3.0 /platform
#COPY docker-entrypoint.sh /platform/bin/docker-entrypoint.sh
COPY LICENSE /platform/bin/LICENSE
EXPOSE 8090/tcp

ENV USE_HOSTNAME=false

ENV TARANTULA_DATA_STORE_SIZE=2000
ENV TARANTULA_DATA_STORE_DIR=/tarantula/data
ENV TARANTULA_SERVICE_DEPLOY_DIR=/tarantula/deploy
ENV TARANTULA_AUTH_CONTEXT=localhost

ENV TARANTULA_KUBERNETES_ENABLED=false
ENV TARANTULA_KUBERNETES_NAME=gameserver

VOLUME /tarantula

WORKDIR /platform/bin

#ENV JAVA_MEM_OPTS="-Xms2g -Xmx4g"

#ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=20 -XX:ConcGCThreads=5 -XX:InitiatingHeapOccupancyPercent=70"

#ENV JAVA_MANAGEMENT="-Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.port=8200 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.rmi.port=8200 -Djava.rmi.server.hostname=localhost"

ENTRYPOINT ["sh", "/platform/bin/dev_tarantula.sh"]