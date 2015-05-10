@echo off
pushd .

call build jar-java
call sys
call jrun r.RGeneratorMain

call src
SETLOCAL ENABLEDELAYEDEXPANSION 
set files=
for /f %%d in ('type build.order') do if not "%%d"==".project" set files=!files! %%d

R CMD INSTALL %FILES%

popd
