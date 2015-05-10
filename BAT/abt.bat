@echo off
pushd .
call src
call ab && rscript %MAIN%/R/scripts/run_all_tests.r
popd
