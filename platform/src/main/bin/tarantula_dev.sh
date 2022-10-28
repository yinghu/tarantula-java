#!/bin/bash
echo "Starting Tarantula Distribution System  ..."
CP="../lib/*:../conf:../deploy:../web"
JAVA_OPTS="$JAVA_OPTS -Xms2G -Xmx2G -XX:MaxDirectMemorySize=1G -server"
JAVA_HZ="--add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED"
java $JAVA_OPTS $JAVA_HZ -classpath $CP com.tarantula.platform.bootstrap.TarantulaMain
