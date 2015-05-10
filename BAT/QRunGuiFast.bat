@echo off

SETLOCAL ENABLEDELAYEDEXPANSION 
set params=
for %%d in (%*) do set params=!params! %%d

start %MAIN%\dotNET\QRunGui\bin\Debug\QRunGui.exe %params%
