@echo off & setlocal enableextensions
REM
REM IvyDB Patching
REM

REM Load Initialization files
Call :LoadIni

SET $DateString=%1%
if DEFINED $DateString goto :Continue
Call :GetDateString
:Continue

REM Clear log
echo %DATE% %TIME% IvyDBPatch starting > c:\systematic\batch\log\IvyDBPatch.log

REM INITIALIZATION
echo Initialization
Call :Logit Initialization
%$SqlCmd% -S%$server% -U%$user% -P%$password% -d%$db%  -i c:\systematic\batch\IvyDBPatch01.sql >> c:\systematic\batch\log\IvyDBPatch.log

REM LOAD DATA
Call :InsertAll

echo Update
REM UPDATE
Call :Logit Update
%$SqlCmd% -S%$server% -U%$user% -P%$password% -d%$db%  -i c:\systematic\batch\IvyDBPatch02.sql >> c:\systematic\batch\log\IvyDBPatch.log

REM Update Patch log
%$SqlCmd% -S%$server% -U%$user% -P%$password% -d%$db%  -Q" INSERT PATCH_LOG SELECT CAST('%$DateString%' AS smalldatetime),'Patch completed.'"

Call :Logit IvyDBPatch complete.
GOTO :eof

:LoadIni
for %%i in (User Password Server Db Path LclTemp UNCTemp SqlCmd) DO (
  for /f "tokens=2 delims==" %%j in ('find /i "%%i=" ^< c:\systematic\batch\IvyDBPatch.ini') do set $%%i=%%j
)
goto :Eof

:GetDateString
for /f %%a in ('cscript c:\systematic\batch\date.vbs //Nologo') do set "yesterday=%%a"
echo %yesterday% 
for /f "tokens=1-3 delims=/" %%a in ("%yesterday%") do ( 
set "mm=%%a" 
set "dd=%%b" 
set "yy=%%c" 
) 
if "%mm:~1,1%"=="" set mm=0%mm%
if "%dd:~1,1%"=="" set dd=0%dd%
Set $DateString=%yy:~-4%%mm%%dd%
goto :eof

:InsertAll
echo Uncompress Patch Files
Call :Logit Uncompress Patch Files.

c:\systematic\batch\unzip.exe -qo "%$path%\ptcivydb.%$DateString%.zip" -d "%$LclTemp%"
IF EXIST "%$LclTemp%\ptcsecpr.%$DateString%.txt" call :BI PATCH_SecurityPrice_TEMP ptcsecpr.%$DateString%.txt
IF EXIST "%$LclTemp%\ptcopprc.%$DateString%.txt" call :BI PATCH_OptionPrice_TEMP ptcopprc.%$DateString%.txt
IF EXIST "%$LclTemp%\ptcopprc.%$DateString%.txt" call :BI PATCH_StdOptionPrice_TEMP ptcstdop.%$DateString%.txt
IF EXIST "%$LclTemp%\ptcvsurf.%$DateString%.txt" call :BI PATCH_VolatilitySurface_TEMP ptcvsurf.%$DateString%.txt
GOTO :eof

:BI
echo Loading %1
Call :Logit Loading %1
SET $file=%2
SET $table=%1
SET $full="%$unctemp%\%$file%"

%$SqlCmd% -S%$server% -U%$user% -P%$password% -d%$db%  -Q" delete %$table% BULK INSERT %$table% FROM %$full:"='%"  >> c:\systematic\batch\log\IvyDBPatch.log
del /Q %$LclTemp%\%$file%
GOTO :eof

:logit
echo %DATE% %TIME% %$DateString% %1 %2 %3 %4 %5 %6 %7 %8 %9 >> c:\systematic\batch\log\IvyDBPatch.log
GOTO :eof

