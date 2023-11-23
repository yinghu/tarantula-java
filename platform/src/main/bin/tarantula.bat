@echo off
set JVM_ARGS=-server -Xms2g -Xmx4g -XX:+UseCompressedOops
set JVM_HZ=--add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED
java %JVM_ARGS% %JVM_HZ% -classpath ..\lib\*;..\conf;..\deploy;..\web com.tarantula.platform.bootstrap.TarantulaMain %1 %2 %3
