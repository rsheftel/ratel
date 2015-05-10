@echo off & setlocal enableextensions
REM
REM IvyDB Installation
REM
REM Load Initialization files
Call :LoadIni

SET $DateString=%1%
if DEFINED $DateString goto :Continue
Call :GetDateString
:Continue

REM Clear log
echo IvyDBUpd %$DateString%
echo %DATE% %TIME% IvyDBUpd starting > c:\systematic\batch\log\IvyDBUpd.log

REM INITIALIZATION
echo Initialization
Call :Logit Initialization
%$SqlCmd% -S%$server% -U%$user% -P%$password% -d%$db% -i c:\systematic\batch\IvyDBUpd01.sql >> c:\systematic\batch\log\IvyDBUpd.log

REM LOAD DATA
Call :InsertAll

echo Update
REM UPDATE
Call :Logit Update
%$SqlCmd% -S%$server% -U%$user% -P%$password% -d%$db%  -i c:\systematic\batch\IvyDBUpd02.sql >> c:\systematic\batch\log\IvyDBUpd.log

Call :Logit IvyDBInstall complete.
GOTO :eof

:LoadIni
for %%i in (User Password Server Db LclPath LclTemp UNCTemp SqlCmd) DO (
  for /f "tokens=2 delims==" %%j in ('find /i "%%i=" ^< c:\systematic\batch\IvyDBUpd.ini') do set $%%i=%%j
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
c:\systematic\batch\unzip.exe -qo "%$lclpath%\IVYDB.%$DateString%D.zip" -d "%$lcltemp%"
IF EXIST "%$lcltemp%\IVYSECUR.%$DateString%D.txt" call :BI SECURITY IVYSECUR.%$DateString%D.txt
IF EXIST "%$lcltemp%\IVYSECNM.%$DateString%D.txt" call :BI SECURITY_NAME IVYSECNM.%$DateString%D.txt
IF EXIST "%$lcltemp%\IVYEXCHG.%$DateString%D.txt" call :BI EXCHANGE IVYEXCHG.%$DateString%D.txt
IF EXIST "%$lcltemp%\IVYDISTR.%$DateString%D.txt" call :BI DISTRIBUTION IVYDISTR.%$DateString%D.txt
IF EXIST "%$lcltemp%\IVYOPINF.%$DateString%D.txt" call :BI OPTION_INFO IVYOPINF.%$DateString%D.txt
REM
IF EXIST "%$lcltemp%\IVYSECPR.%$DateString%D.txt" call :BI SECURITY_PRICE_TEMP IVYSECPR.%$DateString%D.txt
IF EXIST "%$lcltemp%\IVYOPPRC.%$DateString%D.txt" call :BI OPTION_PRICE_TEMP IVYOPPRC.%$DateString%D.txt
IF EXIST "%$lcltemp%\IVYOPVOL.%$DateString%D.txt" call :BI OPTION_VOLUME_TEMP IVYOPVOL.%$DateString%D.txt
IF EXIST "%$lcltemp%\IVYSTDOP.%$DateString%D.txt" call :BI STD_OPTION_PRICE_TEMP IVYSTDOP.%$DateString%D.txt
IF EXIST "%$lcltemp%\IVYVSURF.%$DateString%D.txt" call :BI VOLATILITY_SURFACE_TEMP IVYVSURF.%$DateString%D.txt
IF EXIST "%$lcltemp%\IVYHVOL.%$DateString%D.txt"  call :BI HISTORICAL_VOLATILITY_TEMP IVYHVOL.%$DateString%D.txt
IF EXIST "%$lcltemp%\IVYIDXDV.%$DateString%D.txt" call :BI INDEX_DIVIDEND_TEMP IVYIDXDV.%$DateString%D.txt
IF EXIST "%$lcltemp%\IVYZEROC.%$DateString%D.txt" call :BI ZERO_CURVE_TEMP IVYZEROC.%$DateString%D.txt
GOTO :eof

:BI
echo Loading %1
Call :Logit Loading %1
SET $file=%2
SET $table=%1
SET $full="%$unctemp%\%$file%"


%$SqlCmd% -S%$server% -U%$user% -P%$password% -d%$db% -Q" delete %$table% BULK INSERT %$table% FROM %$full:"='%"
del  /Q %$lcltemp%\%$file%
GOTO :eof

:logit
echo %DATE% %TIME% %1 %2 %3 %4 %5 %6 %7 %8 %9 >> c:\systematic\batch\log\IvyDBUpd.log
GOTO :eof



