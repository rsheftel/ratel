REM CHANGE THE FOLLOWING THREE LINES FOR THE USERNAME/PASSWORD/DESTDIR
SET $user=fftw
SET $password=pinal812
SET $dest=\\NYUX51\data\IvyDBServer
SET $ldest=c:\systematic\batch

FOR /F "tokens=1-6 delims=/ " %%i in ('date/t') do SET $yyyymmdd=%%l%%j%%k
FOR /F "tokens=1-6 delims=: " %%i in ('time/t') do SET $hhmm=%%i%%j

SET $DateString=%1%
if DEFINED $DateString goto :Continue
Call :GetDateString
:Continue

echo user %$user% %$password% > ftp.tmp
echo get Patch/V2.5/ptcivydb.%$DateString%.zip "%$dest%\ptcivydb.%$DateString%.zip" >> ftp.tmp
echo quit >> ftp.tmp
ftp -s:ftp.tmp -d -n ftp.ivydb.com > c:\systematic\batch\log\IvyDBFetchPatch.%$yyyymmdd%%$hhmm%.log


echo user %$user% %$password% > ftp.tmp
echo dir Patch/V2.5/ptcivydb.%$DateString%.zip "%$ldest%\fileStats.txt" >> ftp.tmp
echo quit >> ftp.tmp
ftp -s:ftp.tmp -d -n ftp.ivydb.com >> c:\systematic\batch\log\IvyDBFetchPatch.%$yyyymmdd%%$hhmm%.log
goto :eof

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
