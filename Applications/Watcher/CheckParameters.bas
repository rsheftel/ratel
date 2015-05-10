Attribute VB_Name = "CheckParameters"
Option Explicit
Option Base 1

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
