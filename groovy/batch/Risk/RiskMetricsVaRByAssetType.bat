set PATH=C:\Program Files\groovy\groovy-1.5.1\bin;.
set GROOVY_HOME=c:\program files\groovy\groovy-1.5.1
call groovy C:\systematic\groovy\batch\Risk\RiskMetricsVaRByAssetType.groovy -d \\nysrv37\risk\Extracts\RM -f Malbec_VaR_by_Asset_Type.xls
echo %ERRORLEVEL%
REM if errorlevel > 0 exit /B errorlevel