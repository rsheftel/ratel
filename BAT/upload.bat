
@echo off
SETLOCAL ENABLEDELAYEDEXPANSION 
set params=
for %%d in (%*) do set params=!params! %%d

call jrunBig db.DataUpload %params%
if %errorlevel% neq 0 goto DIED
exit /B 0

:DIED
exit /B 1


