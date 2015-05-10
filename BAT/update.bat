@echo off
pushd %MAIN%
call deleteBeforeUpdate
TortoiseProc /command:update /notempfile /path:%MAIN%
popd
