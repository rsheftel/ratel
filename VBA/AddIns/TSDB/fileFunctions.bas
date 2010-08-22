Attribute VB_Name = "fileFunctions"
Option Explicit
Option Base 1

Function tsdbWriteTimeSeriesFileDate(outFilename As String, tsdbNames As Variant, tsdbSources As Variant, tsdbValues As Variant, tsdbDateTime As Date) As Variant

'Desc:  Given the data names, sources, and values, will write a csv file to the TimeSeriesFile specs that can be
'       read by the R crawler and uploaded to tsdb. Works for ranges or VBA arrays
'
'Inputs:    outFilename     - Full directory and name of output file
'           tsdbNames       - Range of names
'           tsdbSources     - Range of sources
'           tsdbValues      - Range of values
'           tsdbDateTime    - Date of the data (this version only handles a single DateTime per file
'
'Outputs:   TRUE if successful (as in comlpeted running), otherwise error.


'Set up error trapping
'On Error GoTo errorTrap
'errorTrap:
'    tsdbWriteTimeSeriesFileDate = False
'    Exit Function
    
    Dim size As Integer, count As Integer
    Dim headerLine As String, valueLine As String, currentValue As Variant
    Dim oFileSystem As Scripting.FileSystemObject
    Dim oFile As Scripting.TextStream
        
    'Get the size of the ranges
    If (TypeName(tsdbNames) = "Range") Then
        size = tsdbNames.count
    Else
        size = UBound(tsdbNames)
    End If
    
    'Populate the header line
    headerLine = "Date"
    For count = 1 To size
        headerLine = headerLine + "," + tsdbNames(count) + ":" + tsdbSources(count)
    Next

    'Populate the value line
    valueLine = Format(tsdbDateTime, "yyyy-mm-dd hh:mm:ss")
    For count = 1 To size
        currentValue = tsdbValues(count)
        If TypeName(currentValue) = "String" Then
            valueLine = valueLine + "," + currentValue
        Else
            valueLine = valueLine + "," + Str$(currentValue)
        End If
    Next
    
    'Write output to file
    Set oFileSystem = New Scripting.FileSystemObject
    Set oFile = oFileSystem.CreateTextFile(outFilename, True)
    oFile.WriteLine (headerLine)
    oFile.WriteLine (valueLine)
    oFile.Close
    
    tsdbWriteTimeSeriesFileDate = True
End Function

