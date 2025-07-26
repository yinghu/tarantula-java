#!/bin/bash
echo "Starting Tarantula Distribution System  ..."
CP="../lib/*:../conf:../deploy:../web"
JAVA_OPTS="$JAVA_OPTS -server -Xms2g -Xmx2g -XX:+UseCompressedOops"
$JAVA_HOME/bin/java $JAVA_OPTS -classpath $CP com.icodesoftware.admin.Bootstrap