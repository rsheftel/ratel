@echo off

REM  Copyright 2005 Codemesh, Inc.
REM
REM  Based in large parts on the ANT startup script, licensed under
REM  the Apache 2.0 license (provided in the adm directory).
REM
REM  This startup script differs from the standard ANT startup script
REM  in that it uses a bundled JRE and a bundled copy of ANT rather than
REM  basing its choices on the values of the JAVA_HOME and ANT_HOME
REM  environment settings.
REM
REM  You can replace the bundled copy of ANT with any version of ANT later
REM  than 1.6.3.
REM
REM  Unless required by applicable law or agreed to in writing, software
REM  distributed under the License is distributed on an "AS IS" BASIS,
REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM  See the License for the specific language governing permissions and
REM  limitations under the License.

if exist "%HOME%\antrc_pre.bat" call "%HOME%\antrc_pre.bat"

if "%OS%"=="Windows_NT" @setlocal

rem %~dp0 is expanded pathname of the current script under NT
set DEFAULT_ANT_HOME=%~dp0..

set ANT_HOME=%DEFAULT_ANT_HOME%
set DEFAULT_ANT_HOME=

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).
set ANT_CMD_LINE_ARGS=%1
if ""%1""=="""" goto doneStart
shift
:setupArgs
if ""%1""=="""" goto doneStart
set ANT_CMD_LINE_ARGS=%ANT_CMD_LINE_ARGS% %1
shift
goto setupArgs
rem This label provides a place for the argument list loop to break out
rem and for NT handling to skip to.

:doneStart
rem find ANT_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x
set ANT_HOME=T:\JuggerNET
echo %ANT_HOME%
if exist "%ANT_HOME%\lib\ant.jar" goto checkJava

:noAntHome
echo ANT_HOME is set incorrectly or ant could not be located. Please set ANT_HOME.
goto end

:checkJava
rem set JAVA_HOME=%ANT_HOME%\jre
set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
goto runAnt

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe

:runAnt
if not "%CLASSPATH%"=="" goto runAntWithClasspath
"%_JAVACMD%" %ANT_OPTS% -classpath "%ANT_HOME%\lib\ant-launcher.jar;%ANT_HOME%\lib\ant.jar;%ANT_HOME%\lib\ant-nodeps.jar;%ANT_HOME%\lib\ant-codemesh.jar;%ANT_HOME%\lib\tools.jar;%ANT_HOME%\lib\xalan.jar;%ANT_HOME%\lib\xercesImpl.jar;%ANT_HOME%\lib\xml-apis.jar;%ANT_HOME%\lib\ant-trax.jar" "-Dant.home=%ANT_HOME%" org.apache.tools.ant.launch.Launcher -lib "%ANT_HOME%\lib\ant-codemesh.jar" %ANT_ARGS% %ANT_CMD_LINE_ARGS%
goto end

:runAntWithClasspath 
"%_JAVACMD%" %ANT_OPTS% -classpath "%ANT_HOME%\lib\ant-launcher.jar;%ANT_HOME%\lib\ant.jar;%ANT_HOME%\lib\ant-nodeps.jar;%ANT_HOME%\lib\ant-codemesh.jar;%ANT_HOME%\lib\tools.jar;%ANT_HOME%\lib\xalan.jar;%ANT_HOME%\lib\xercesImpl.jar;%ANT_HOME%\lib\xml-apis.jar;%ANT_HOME%\lib\ant-trax.jar" "-Dant.home=%ANT_HOME%" org.apache.tools.ant.launch.Launcher %ANT_ARGS% -lib "%ANT_HOME%\lib\ant-codemesh.jar;%CLASSPATH%" %ANT_CMD_LINE_ARGS%
goto end

:end
set _JAVACMD=
set ANT_CMD_LINE_ARGS=

if "%OS%"=="Windows_NT" @endlocal

:mainEnd
if exist "%HOME%\antrc_post.bat" call "%HOME%\antrc_post.bat"

