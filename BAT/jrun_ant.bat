@pushd .
@call sys.bat
@java -classpath "%JAVA_HOME%/lib/tools.jar;lib/*" -Xss2M -Xmx512M %1 %2 %3 %4 %5 %6 %7 %8 %9
@if %errorlevel% neq 0 goto DIED
@popd
exit 0

:DIED
popd
exit 1

