@echo off
pushd .
SETLOCAL ENABLEDELAYEDEXPANSION 
set params=
for %%d in (%*) do set params=!params! %%d

%MAIN%\dotNET\QRun\bin\Debug\QRun.exe %params%
if %errorlevel% neq 0 goto DIED
popd
exit /B 0

:DIED
popd
exit /B 1


