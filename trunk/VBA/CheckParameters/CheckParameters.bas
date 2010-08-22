Attribute VB_Name = "CheckParameters"
Option Explicit
Option Base 1

Public Const paramCheckDir = "v:\stprocess\environ\params\checkfiles\"

Function getParameterValuesFromCSV(strFilenameFull As String, ByRef paramValues() As Variant) As Variant
'-------------------------------------------------------------------
'
'Desc:      Opens the specified CSI ASCII output file and reads the "paramName, paramValue" pairs into an array.
'           Also reads in the DateTime of the save from the DateTime row in the file
'
'Inputs:    fileNameFull - The full directory and filename of the CSV file
'           paramValues - A variant array byRef that will receive the paramName-paramValue pairs
'
'Output:    Either the dateTime or an error message
'-------------------------------------------------------------------


Dim lineCount As Integer, dataLine As String
Dim dateTime As String
Dim paramName As String, paramValue As String

Dim oFileSystem As Scripting.FileSystemObject
Dim oFile As Scripting.TextStream

    
On Error GoTo errorTrap

    'Set up the objects
    Set oFileSystem = New Scripting.FileSystemObject
    Set oFile = oFileSystem.OpenTextFile(strFilenameFull, ForReading, False)
    
    'Read each line in file and populate paramValues array, and dateTime value
    dateTime = "#Error No DateTime Found!"
    lineCount = 1
    ReDim paramValues(2, 1)
    Do While oFile.AtEndOfStream <> True
        dataLine = oFile.ReadLine
        paramName = Left$(dataLine, InStrRev(dataLine, ",") - 1)
        paramValue = Right$(dataLine, Len(dataLine) - InStrRev(dataLine, ","))
        If LCase(paramName) = "datetime" Then
            dateTime = paramValue
        Else
            ReDim Preserve paramValues(2, lineCount)
            paramValues(1, lineCount) = paramName
            paramValues(2, lineCount) = paramValue
            lineCount = lineCount + 1
        End If
    Loop
    
    getParameterValuesFromCSV = dateTime
    
Done:
    Exit Function

errorTrap:
    getParameterValuesFromCSV = "#Error " & Err.Description
End Function


Function getParameterValueFromSystemDB(system As String, strategy As String, paramSet As String, paramName As String) As Variant
'Returns the latest value from the SystemDB, or #Error if not found

'Dim dbConnection As ADODB.Connection
Dim rsData As ADODB.Recordset
Dim sConnect As String
Dim sSQL As String

Dim test As Variant

On Error GoTo adoError

    'Create the connection string
    sConnect = "Provider=SQLOLEDB;" & _
                "Data Source=SQLPRODTS;" & _
                "Initial Catalog=SystemDB;" & _
                "Integrated Security=sspi"
                
    'Create the SQL statement
    sSQL = "select paramValues.ParameterValue from SystemDB..ParameterValues paramValues " & _
            "where " & _
            "paramValues.System = '" & system & "' and " & _
            "paramValues.Strategy = '" & strategy & "' and " & _
            "paramValues.Name = '" & paramSet & "' and " & _
            "paramValues.ParameterName ='" & paramName & "' and " & _
            "paramValues.AsOfDate = (select max(maxDate.AsOfDate) as maxAsOfDate " & _
                "from SystemDB..ParameterValues maxDate where " & _
                "maxDate.System = paramValues.System and " & _
                "maxDate.Name = paramValues.Name and " & _
                "maxDate.Strategy = paramValues.Strategy and " & _
                "maxDate.ParameterName = paramValues.ParameterName and " & _
                "maxDate.AsOfDate <= '" & Format(Now(), "yyyy/mm/dd hh:mm:ss") & "')"
            
    'Set dbConnection = New ADODB.Connection
    'With dbConnection
    '    .ConnectionString = sConnect
    '    .Open
    'End With
        
    'Execute the SQL command
    Set rsData = New ADODB.Recordset
    rsData.Open sSQL, sConnect, adOpenForwardOnly, adLockReadOnly, adCmdText
    
    'Set the value
    getParameterValueFromSystemDB = rsData.Fields("ParameterValue").Value
    
Done:
    'Clean up
    rsData.Close
    Set rsData = Nothing
    Exit Function
                
adoError:
    getParameterValueFromSystemDB = "#Error " & Err.Description
    
End Function

Function CheckParameterValues(market As String, system As String, interval As String, version As String, strategy As String, paramSet As String) As Variant
'Check all of the parameters in the CheckParam csv file

Dim csvParamValues() As Variant
Dim csvFilename As String, csvDateTime As String
Dim sResult As Variant
Dim paramCount As Integer, paramName As String
Dim paramValueCsv As Variant, paramValueSystemDB As Variant
    
On Error GoTo errorExit

    'Get the parameter values from the csv check file
    csvFilename = paramCheckDir & system & "_" & strategy & "_" & version & "_" & interval & "_" & paramSet & "_" & market & ".csv"
    csvDateTime = getParameterValuesFromCSV(csvFilename, csvParamValues)

    If Left$(csvDateTime, 6) = "#Error" Then
        sResult = csvDateTime
        GoTo insideError
    Else
        For paramCount = 1 To UBound(csvParamValues, 2)
            paramName = csvParamValues(1, paramCount)
            paramValueCsv = csvParamValues(2, paramCount)
            sResult = getParameterValueFromSystemDB(system, strategy, paramSet, paramName)
            If Left$(sResult, 6) = "#Error" Then
                GoTo insideError
            Else
                paramValueSystemDB = sResult
            End If
            If (paramValueCsv <> paramValueSystemDB) Then
                sResult = "#Error " & paramName & " value does not match!"
                GoTo insideError
            End If
        Next
    End If
    
Done:
    CheckParameterValues = csvDateTime
    Exit Function
    
insideError:
    CheckParameterValues = sResult
    Exit Function
    
errorExit:
    CheckParameterValues = "#Error"
    Exit Function
End Function
