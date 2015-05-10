@echo off

SETLOCAL ENABLEDELAYEDEXPANSION 
set params=
for %%d in (%*) do set params=!params! %%d

start U:\Tools\Recon\QRunGuiHideFromKQ.exe Recon %params%
