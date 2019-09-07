#!/bin/bash
echo "Starting Tarantula Distribution System Load Test ..."
find ../lib -name *.jar > lib.txt
CP="../conf:../deploy:../../../../test-classes"
while read line
do
V=`echo "${line}"`
CP=${CP}:$V
done < lib.txt
rm -f lib.txt
JAVA_OPTS="$JAVA_OPTS -Xms512m -Xmx1024m -server"
echo "16384 65535" > /proc/sys/net/ipv4/ip_local_port_range
java $JAVA_OPTS -classpath $CP Main $1 $2 $3 $4


