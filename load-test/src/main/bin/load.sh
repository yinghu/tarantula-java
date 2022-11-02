#!/bin/bash
echo "Starting Tarantula Distribution System Load Test ..."
CP="../lib/*:bin"
JAVA_OPTS="$JAVA_OPTS -Xms1024M -Xmx1024M -server"
#echo "16384 65535" > /proc/sys/net/ipv4/ip_local_port_range
java $JAVA_OPTS -classpath $CP com.tarantula.test.integration.Main