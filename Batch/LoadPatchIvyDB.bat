rem Wrapper for Option Metrics IvyDB data load and patch processes
rem
set JAVA_HOME=c:\Program Files\Java\jdk1.6.0_03
set GROOVY_HOME=c:\groovy\groovy-1.5.6
rem
rem download IvyDB data file
c:\groovy\groovy=1.5.6\bin\groovy.bat c:\systematic\batch\IvyDBFetch.groovy -c c:\systematic\batch\IvyDBFetch.bat -d \\nyux51\data\IvyDBServer\ -p IvyDB. -s D.zip -r 6 -w 600 
rem load IvyDB data file
c:\systematic\batch\IvyDBUpd.bat
rem download IvyDB patch file
c:\groovy\groovy=1.5.6\bin\groovy.bat c:\systematic\batch\IvyDBFetch.groovy -c c:\systematic\batch\IvyDBFetchPatch.bat -d \\nyux51\data\IvyDBServer\ -p ptcivydb. -s .zip -r 6 -w 600 
rem load IvyDB patch file
c:\systematic\batch\IvyDBPatch.bat
