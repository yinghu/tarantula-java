#!/bin/bash
echo "Starting Tarantula Distribution System Load Test ..."
CP="../lib/*:bin"
JAVA_OPTS="$JAVA_OPTS -Xms1024M -Xmx1024M -server"
#echo "16384 65535" > /proc/sys/net/ipv4/ip_local_port_range
JAVA_HZ="--add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED"

java $JAVA_OPTS $JAVA_HZ -classpath $CP com.tarantula.test.integration.LMDBLoadVerifier