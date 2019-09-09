@echo off
set JVM_ARGS=-server -XX:MaxDirectMemorySize=64M -Xms512M -Xmx1024M
setLocal EnableDelayedExpansion
set CLASSPATH="
for /R ../lib %%a in (*.jar) do (
   set CLASSPATH=!CLASSPATH!;%%a
)
set CLASSPATH=!CLASSPATH!"
java %JVM_ARGS% -classpath %CLASSPATH%;. com.tarantula.integration.udp.Main

