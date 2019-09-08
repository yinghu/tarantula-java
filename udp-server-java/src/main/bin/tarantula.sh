#!/bin/bash
echo "Starting Tarantula Distribution System  ..."
find ../lib -name *.jar > lib.txt
CP="."
while read line
do
V=`echo "${line}"`
CP=${CP}:$V
done < lib.txt
rm -f lib.txt
JAVA_OPTS="$JAVA_OPTS -Xms512m -Xmx1024m -XX:MaxDirectMemorySize=64M -server"
java $JAVA_OPTS -classpath $CP com.tarantula.integration.udp.Main


