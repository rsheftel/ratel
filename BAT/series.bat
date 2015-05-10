
@echo off
set foo=">    usage series <timeseries id/name>"
echo %foo%
echo example: series "aapl close"

SETLOCAL ENABLEDELAYEDEXPANSION 
set params=
for %%d in (%*) do set params=!params! %%d
jrun tsdb.TimeSeries %params%
