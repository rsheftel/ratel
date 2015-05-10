
@echo off

SETLOCAL ENABLEDELAYEDEXPANSION 
set params=
for %%d in (%*) do set params=!params! %%d

jrun tsdb.SeriesControl -command add %params%
