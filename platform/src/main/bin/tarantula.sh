#!/bin/bash
echo "Starting Tarantula Distribution System  ..."
curl -H "Metadata-Flavor: Google" http://169.254.169.254/computeMetadata/v1/instance/network-interfaces/0/access-configs/0/external-ip >/etc/tarantula/ip.txt
find ../lib -name *.jar > lib.txt
CP="../conf:../deploy:../web"
while read line
do
V=`echo "${line}"`
CP=${CP}:$V
done < lib.txt
rm -f lib.txt
JAVA_OPTS="$JAVA_OPTS -Xms1024m -Xmx2048m -XX:MaxDirectMemorySize=4096M -server"
JAVA_HZ="--add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED"
java $JAVA_OPTS $JAVA_HZ -classpath $CP com.tarantula.platform.bootstrap.TarantulaMain


