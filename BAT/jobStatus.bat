
@echo off
set foo=">    usage jobStatus -id or -name <id/name> -date <date> -recent <boolean>"
echo %foo%
echo example: jobStatus -name "tbaModified"

SETLOCAL ENABLEDELAYEDEXPANSION 
set params=
for %%d in (%*) do set params=!params! %%d
jrun schedule.StatusHistory -date today -recent 14 %params%
