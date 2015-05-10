@echo off
R CMD INSTALL . && rscript %MAIN%\R\scripts\run_tests.r %1 %2

