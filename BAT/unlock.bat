@echo off
pushd .
call jrun db.TestLocksTable %1 %2 %3
popd
