@echo off
set JVM_ARGS=-server -XX:MaxDirectMemorySize=64M -Xms128M -Xmx256M
setLocal EnableDelayedExpansion
set CLASSPATH="
for /R ../lib %%a in (*.jar) do (
   set CLASSPATH=!CLASSPATH!;%%a
)
set CLASSPATH=!CLASSPATH!"
java %JVM_ARGS% -classpath %CLASSPATH%;. com.icodesoftware.integration.Main

