@echo off

SETLOCAL ENABLEDELAYEDEXPANSION 
set params=
for %%d in (%*) do set params=!params! %%d

pushd %MAIN%\Java\systematic
java -classpath "%JAVA_HOME%/lib/tools.jar;lib/*" -Xss2M -Xmx512M %params%
set level=%errorlevel%
popd
if %level% neq 0 goto DIED
echo success
exit /B 0

:DIED
echo failure
exit /B 1

