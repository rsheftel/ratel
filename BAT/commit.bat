@echo off
pushd %MAIN%
TortoiseProc /command:commit /notempfile /path:%MAIN%
popd
