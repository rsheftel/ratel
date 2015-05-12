@echo off
setlocal
echo FFTW Maven Project Release Script
echo.
if [%1]==[] goto incorrect_param_routine
if [%2] NEQ [] goto incorrect_param_routine

echo *** All Resolved JIRA issues for this project need to be Closed!
echo *** DO NOT CONTINUE this script unless all Resolved JIRA issues
echo *** are Closed with the correct fix version specified.
pause

set SVN_REPO=http://scm.fftw.com/svn/systematic/workspace
set PROJECT=%1
set BUILD_DIR=%PROJECT%-build

Echo Releasing Maven Project %PROJECT%
Echo from Subversion Repository %SVN_REPO%
Echo into temporary build directory %BUILD_DIR%
Echo.

svn checkout %SVN_REPO%/%PROJECT%/trunk %BUILD_DIR%
if %ERRORLEVEL% NEQ 0 (
  echo Unable to checkout project from Subversion Repository!
  goto finished
)
Echo.

pushd %BUILD_DIR%
echo Ready for a Dry Run of the release.
cd
pause
call mvn release:prepare -Darguments=-Pproduction -DdryRun=true
if %ERRORLEVEL% NEQ 0 goto failed

echo *** Please confirm that the Dry Run was successful!
echo *** Ctrl-C if the Dry Run was NOT successful.
pause

echo Preparing the release...
call mvn release:clean release:prepare -Darguments=-Pproduction
if %ERRORLEVEL% NEQ 0 goto failed
echo *** Please confirm that the Prepare was successful!
echo *** Ctrl-C if the Prepare was NOT successful.
pause

echo Performing the release...
REM mvn release:perform will upload jars and site reports to the internal Maven repository
call mvn release:perform -Darguments=-Pproduction
if %ERRORLEVEL% NEQ 0 goto failed
echo *** Please confirm that the release was successful!
pause

goto clean_up

:clean_up
  echo Cleaning up temporary build files...
  popd
  rd /s /q %BUILD_DIR%
  goto finished

:failed
cd
  echo !!! Failed to release!  Please fix issues and rerun script.
  goto clean_up

:incorrect_param_routine
  echo Incorrect number of parameters!
  goto help_routine
  
:help_routine
  echo.
  echo This script will release a Maven project.
  echo.
  echo Usage:  ReleaseProject.bat [Maven-project-name]
  goto finished

:finished
  echo.
