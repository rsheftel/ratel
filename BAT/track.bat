@echo off


SETLOCAL ENABLEDELAYEDEXPANSION
set params=
for %%d in (%*) do set params=!params! %%d

call QRunGui Recon %params%
