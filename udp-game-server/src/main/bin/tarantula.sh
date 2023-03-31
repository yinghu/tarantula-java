#!/bin/bash
echo "Starting Tarantula Distribution System  ..."
#curl -H "Metadata-Flavor: Google" http://169.254.169.254/computeMetadata/v1/instance/network-interfaces/0/access-configs/0/external-ip > /etc/tarantula/ip.txt
find ../lib -name *.jar > lib.txt
CP="."
while read line
do
V=`echo "${line}"`
CP=${CP}:$V
done < lib.txt
rm -f lib.txt
JAVA_OPTS="$JAVA_OPTS -Xms128m -Xmx256m -XX:MaxDirectMemorySize=64M -server"
java $JAVA_OPTS -classpath $CP com.icodesoftware.integration.udp.Main