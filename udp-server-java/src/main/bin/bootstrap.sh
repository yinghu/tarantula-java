#!/bin/bash
echo "Starting Tarantula UDP Integration Service ..."
find ../lib -name *.jar > lib.txt
while read line
do
V=`echo "${line}"`
CP=${CP}:$V
done < lib.txt
rm -f lib.txt
JAVA_OPTS="$JAVA_OPTS -Xms512m -Xmx1024m -server"
java $JAVA_OPTS -classpath $CP com.tarantula.integration.udp.Main


