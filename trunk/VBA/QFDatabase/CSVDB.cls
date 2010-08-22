VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "CSVDB"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
Option Explicit

Private oConn As ADODB.Connection
Private oCmd As ADODB.Command
Private oRS As ADODB.Recordset

Private strFilePath As String, strFileName As String

Public Sub openCSV(strFilenameFull As String)
    
    'Parse the fileName into the pathName and fileName
    strFilePath = Left$(strFilenameFull, InStrRev(strFilenameFull, "\"))
    strFileName = Right$(strFilenameFull, Len(strFilenameFull) - InStrRev(strFilenameFull, "\"))
    
    'Set up connection
    Set oConn = New ADODB.Connection
    Set oCmd = New ADODB.Command
    Set oRS = New ADODB.Recordset
    
    oConn.Open "Provider=Microsoft.Jet.OLEDB.4.0;" & _
          "Data Source=" & strFilePath & ";" & _
          "Extended Properties=""text;HDR=YES;FMT=Delimited"""
    With oCmd
        .ActiveConnection = oConn
        .CommandText = "SELECT * FROM " & strFileName
        Set oRS = .Execute
    End With
End Sub

Public Sub fieldToRange(fieldName As String, rangeDestination As range)

    Dim localRS As ADODB.Recordset
    
    With oCmd
        .CommandText = "SELECT " & fieldName & " FROM " & strFileName
        Set localRS = .Execute
    End With
        
    rangeDestination.CopyFromRecordset localRS
End Sub

Public Function fieldToArray(fieldName As String) As Variant
    
'THIS METHOD IS NOT COMPLETE YET

    Dim resultArray As Variant
    
    With oRS
        .MoveFirst
        Do Until .EOF
            .MoveNext
        Loop
    End With
End Function

Private Sub Class_Terminate()
    oRS.Close
    Set oRS = Nothing
    Set oCmd = Nothing
    oConn.Close
    Set oConn = Nothing
End Sub
