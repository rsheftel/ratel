@echo off

SETLOCAL ENABLEDELAYEDEXPANSION 
set params=
for %%d in (%*) do set params=!params! %%d

start %MAIN%\dotNET\QRunGui\bin\debug\QRunGui.exe %params%
