@echo off
SETLOCAL ENABLEDELAYEDEXPANSION 
set params=
for %%d in (%*) do set params=!params! %%d

call jrun -Xmx1024M %params%
if %errorlevel% neq 0 goto DIED
exit /B 0

:DIED
exit /B 1

