Attribute VB_Name = "tsdbAccess"
Option Explicit
Option Base 0

Function tsdbGet(dataName As String, dataSource As String, dataDate As Date) As Variant

'Desc:      Gets a single value for a data series from tsdb
'
'Inputs:    dataName    - name of tsdb data series
'           dataSource  - name of tsdb data source
'           dataDate    - date of the requested data
'
'Output:    Value of the dataName for a dataSource on the dataDate.
'           Any errors as passed through as text or error
'
'10/3/07    - Initial version (Ryan Sheftel)
'
'6/17/08    - Refactored as a wrapper for the QExcel function
' Best to use direct excel call

    Dim tsdb As qExcel.tsdb
    
    On Error Resume Next
       
    Set tsdb = New qExcel.tsdb
    
    tsdbGet = "#Error"
    tsdbGet = tsdb.retrieveOneValueByTimeSeries(dataName, dataSource, dataDate)
    
End Function

Function tsdbGetDateRange(dataName As String, dataSource As String, startDate As Date, endDate As Date) As Variant

'Desc:      Gets a single value for a data series from tsdb
'
'Inputs:    dataName    - name of tsdb data series
'           dataSource  - name of tsdb data source
'           startDate   - start date of the requested data
'           endDate     - end date of the requested data
'
'Output:    Range of dates and values
'           Any errors as passed through as text or error
'           The return is string, so best to wrap the call in =VALUE() in excel

    Dim tsdbHandleExists As Boolean
    Dim startDateString As String, endDateString As String
    Dim result As Variant
    
    On Error Resume Next
    
    'Check if R is running, if not then start it. Note this uses default start logic
    If Not (rconnected) Then
        rinterface.StartRServer
    End If
    
    'Load GSFCore
    result = rEval("library('GSFCore')")
    
    'Check if SQL handle exists, if not then start ti
    tsdbHandleExists = rEval("exists('xl.tsdb')")
    If Not (tsdbHandleExists) Then
        result = rEval("xl.tsdb<-TimeSeriesDB()")
    End If

    'Request data from the tsdb
    startDateString = Format(startDate, "yyyy-mm-dd")
    endDateString = Format(endDate, "yyyy-mm-dd")
    
    rEval ("xl.zoo <- xl.tsdb$retrieveOneTimeSeriesByName('" + dataName + "','" + dataSource + "','" + startDateString + "','" + endDateString + "')")
    tsdbGetDateRange = getarraytovba("data.frame(index(xl.zoo),as.numeric(xl.zoo))")
    rEval ("rm(xl.zoo)")
    
End Function

Function tsdbGetDates(dataName As String, dataSource As String, dateVector As Variant) As Variant

'Desc:      Gets a single value for a data series from tsdb
'
'Inputs:    dataName    - name of tsdb data series
'           dataSource  - name of tsdb data source
'
'
'Output:    Range of values for the range of dates
'           Any errors as passed through as text or error
'           The return is string, so best to wrap the call in =VALUE() in excel

    Dim tsdbHandleExists As Boolean
    Dim startDateString As String, endDateString As String
    Dim result As Variant
    
    On Error Resume Next
    
    'Check if R is running, if not then start it. Note this uses default start logic
    If Not (rconnected) Then
        rinterface.StartRServer
    End If
    
    'Load GSFCore
    result = rEval("library('GSFCore')")
    
    'Check if SQL handle exists, if not then start ti
    tsdbHandleExists = rEval("exists('xl.tsdb')")
    If Not (tsdbHandleExists) Then
        result = rEval("xl.tsdb<-TimeSeriesDB()")
    End If

    'Request data from the tsdb
    startDateString = Format(Application.WorksheetFunction.Min(dateVector), "yyyy-mm-dd")
    endDateString = Format(Application.WorksheetFunction.Max(dateVector), "yyyy-mm-dd")
    
    tsdbGetDates = rEval("as.numeric(xl.tsdb$retrieveOneTimeSeriesByName('" + dataName + "','" + dataSource + "','" + startDateString + "','" + endDateString + "'))")
    
End Function

