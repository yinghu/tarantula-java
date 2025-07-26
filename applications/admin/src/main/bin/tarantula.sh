#!/bin/bash
echo "Starting Tarantula Distribution System  ..."
CP="../lib/*:../conf:../deploy:../web"
JAVA_OPTS="$JAVA_OPTS -server -Xms24g -Xmx24g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=20 -XX:ConcGCThreads=5 -XX:InitiatingHeapOccupancyPercent=70"
$JAVA_HOME/bin/java $JAVA_OPTS -classpath $CP com.icodesoftware.admin.Bootstrap
