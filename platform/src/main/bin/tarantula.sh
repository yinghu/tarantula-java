#!/bin/bash
echo "Starting Tarantula Distribution System  ..."
CP="../lib/*:../conf:../deploy:../web"
JAVA_OPTS="$JAVA_OPTS -server -Xms12g -Xmx12g -XX:PermSize=512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=20 -XX:ConcGCThreads=5 -XX:InitiatingHeapOccupancyPercent=70"
JAVA_HZ="--add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED"
java $JAVA_OPTS $JAVA_HZ -classpath $CP com.tarantula.platform.bootstrap.TarantulaMain
