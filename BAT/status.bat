@echo off
pushd %MAIN%
TortoiseProc /command:repostatus /notempfile /path:%MAIN%
popd
