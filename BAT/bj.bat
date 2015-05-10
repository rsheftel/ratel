@echo off
pushd .
call src

call build jar-java
call sys
call jrun r.RGeneratorMain
call src
R CMD INSTALL Java?
popd
