#!/bin/bash
echo "Starting Tarantula Distribution System Load Test ..."
CP="../lib/*"
JAVA_OPTS="$JAVA_OPTS -Xms1024M -Xmx2048M -XX:MaxDirectMemorySize=512M -server"
#echo "16384 65535" > /proc/sys/net/ipv4/ip_local_port_range
java $JAVA_OPTS -classpath $CP com.tarantula.test.integration.Main


