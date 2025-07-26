@echo off
set JVM_ARGS=-server -Xms2g -Xmx4g -XX:+UseCompressedOops
java %JVM_ARGS% %JVM_HZ% -classpath ..\lib\*;..\conf;..\deploy;..\web com.icodesoftware.admin.Bootstrap
