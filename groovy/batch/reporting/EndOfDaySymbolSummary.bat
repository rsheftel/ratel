set GROOVY_HOME=C:\groovy-1.5.6
set PATH=%GROOVY_HOME%\bin;.
set BATCH_HOME=C:\Groovy-Batch
call groovy %BATCH_HOME%\reporting\EndOfDaySymbolSummary.groovy -r
echo %ERRORLEVEL%
