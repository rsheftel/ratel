
@echo off

SETLOCAL ENABLEDELAYEDEXPANSION
set params=
for %%d in (%*) do set params=!params! %%d
if "%params%"=="" goto usage
jrun systemdb.data.bars.Bars -symbol %params%
exit

:usage
echo usage: bars symbol [ -head count or -tail count ] [ -oldestLast ] [-start date] [-end date]
echo date range is applied before head/tail.

