'Constants
const dbPath = "u:\ST Process\Environ\"
const dbFilename = "VBLauncherJobs.csv"
const logFileDir = "c:\logs\"

'Open log file
	dim fso, logFileName, logFile
	logFileName = logFileDir & "VBLauncher_" & _
					Year(Date()) & Pd(Month(date()),2) & Pd(DAY(date()),2) & _
					"_" & pd(hour(now()),2) & pd(minute(now()),2) & pd(second(now()),2) & ".log"
	Set fso = CreateObject("Scripting.FileSystemObject")
	Set logFile = fso.CreateTextFile(logFileName, True)
	logFile.WriteLine(FormatDateTime(now(), vbGeneralDate))

'Get this computer name
	Dim ob
	dim computerName
	set ob = Wscript.CreateObject("Wscript.Network")
	computerName = ob.ComputerName
	Set ob = nothing
	logFile.writeLine("ComputerName : " & computerName)

'Open the master jobs file as a databse
	Dim oConn, oComm, job
	set oConn= createobject("ADODB.Connection")
	Set oComm = createObject("ADODB.COMMAND")
	Set ob = WScript.CreateObject ("WSCript.shell")
	
	logFile.WriteLine("Jobs dir : " & dbPath)
	logFile.WriteLine("Jobs file: " & dbFilename)
	oConn.Open ("Provider=Microsoft.Jet.OLEDB.4.0;" & _
          "Data Source=" & dbPath & ";" & _
          "Extended Properties=""text;HDR=YES;FMT=Delimited""")
	oComm.ActiveConnection = oConn
	
'Read the jobs for this machine	
	oComm.CommandText = "SELECT * FROM " & dbFilename & " WHERE ComputerName='" & computerName & "'"
	Set oRecordSet = oComm.Execute
	dim jobRecords
	if not(oRecordSet.EOF) then
		jobRecords = oRecordSet.GetRows()
		oConn.Close		
		logFile.WriteLine("Job file closed.")
	'Execute each one
	'Write out the result to the log file	
		for count = 0 to UBound(jobRecords,2)
			job = jobRecords(1,count)
			logFile.writeLine("Executing : " & job)
			'msgbox job
			return = ob.Run(job,1,true)
			logFile.writeLine("Result : " & result)
			logFile.writeLine("Waiting...")
			Wscript.Sleep (jobRecords(2,count) * 1000)
		next
	else
		logFile.WriteLine("No jobs found.")
	end if
	Set oComm = Nothing
	Set oConn = Nothing
	
'Close log file and clean up
	logFile.writeLine("Done.")
	logFile.Close
	'msgBox "All Done"
	set fso = nothing
	set logFile = nothing
	
'Supporting Functions
Function pd(n, totalDigits) 
	if totalDigits > len(n) then 
		pd = String(totalDigits-len(n),"0") & n 
	else 
		pd = n 
	end if 
End Function 