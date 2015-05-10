@echo off
pushd .
call c#

@echo off

SETLOCAL ENABLEDELAYEDEXPANSION
set params=
for %%d in (%*) do set params=!params! %%d

call ant %params%
if %errorlevel% neq 0 goto DIED
popd
exit /B 0

:DIED
popd
exit /B 1


