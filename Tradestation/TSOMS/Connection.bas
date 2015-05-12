Attribute VB_Name = "Connection"
Option Explicit
Option Base 1

Function tsomsGetSenderID() As String
'Returns a standard formated sender ID
    
    tsomsGetSenderID = Environ("USERNAME") & "_" & Environ("COMPUTERNAME")

End Function

Function tsomsGetAccountForTag(aimAccount As String, aimTag As String, accountType As String) As String
'For a given account and TAG in AIM the TS account

'Dim dbConnection As ADODB.Connection
Dim rsData As ADODB.Recordset
Dim sConnect As String
Dim sSQL As String

On Error GoTo adoError

    'Create the connection string
    sConnect = "Provider=SQLOLEDB;" & _
                "Data Source=SQLPRODTS;" & _
                "Initial Catalog=BADB;" & _
                "Integrated Security=sspi"
                
    'Create the SQL statement
    sSQL = "select TagValue " & _
            "from TRADING_STRATEGY " & _
            "where " & _
            "PLATFORM_ID = 'TS' and " & _
            "AccountType = '" & accountType & "' and " & _
            "BloombergAccount = '" & aimAccount & "' and " & _
            "BloombergStrategy = '" & aimTag & "'"
            
    'Execute the SQL command
    Set rsData = New ADODB.Recordset
    rsData.Open sSQL, sConnect, adOpenForwardOnly, adLockReadOnly, adCmdText
    
    'Set the value
    tsomsGetAccountForTag = rsData.Fields("TagValue").Value
    
Done:
    'Clean up
    rsData.Close
    Set rsData = Nothing
    Exit Function
                
adoError:
    tsomsGetAccountForTag = "#Error " & Err.Description
    
End Function

Function tsomsPrepareOrder(order As String, sender As String, receiver As String, omsCounter As Integer) As Variant
'Prepare an order to the Tradestation TSOMS
'This function should NOT be called directly, only through a sub or flow control code
'Returns the orderID

Const ordersDir = "\\nyux51\data\STProcess\TSOMS\Orders\"

Dim fileName As String
Dim valueLine As String
Dim todayDate As String, strResult As String

Dim oFileSystem As Scripting.FileSystemObject
Dim oFile As Scripting.TextStream
       
    'Set up the file object
    todayDate = Format(Now, "YYYYMMDD")
    fileName = ordersDir & "orders_" & todayDate & ".csv"
    Set oFileSystem = New Scripting.FileSystemObject
    
    'If the file doesn't exist make it
    If (oFileSystem.FileExists(fileName) = False) Then
        Set oFile = oFileSystem.CreateTextFile(fileName, True)
        oFile.WriteLine (",sender,receiver,order")
        oFile.WriteLine ("#0,buffer,buffer,buffer")
    Else
        Set oFile = oFileSystem.OpenTextFile(fileName, ForAppending, False)
    End If
    
    order = Replace(order, ",", "#")
    
    oFile.WriteLine (todayDate & omsCounter & "," & sender & "," & receiver & "," & order)
    oFile.Close
    tsomsPrepareOrder = omsCounter
End Function

Sub tsomsSendOrders(orderRange As Range, senderRange As Range, receiverRange As Range, activationRange As Range, resultRange As Range)
'Given a range of cells will prepare and send orders to TSOMS

Const gissingWaitSeconds = 2

Dim order As Range
Dim omsCounters() As Double, orderCount As Integer
Dim count As Integer, activeTrueCount As Integer
Dim strResult As String
Dim omsCounterStart As Double, lastActual As Double, lastPending As Double

    activeTrueCount = Application.WorksheetFunction.CountIf(activationRange, True)
    
    'Get the pending order counter and reserve on Gissing
    lastActual = RTXLGetSnapshot("MARKETDATA|omsOrderCounter", "LastPrice", 1000)
    lastPending = RTXLGetSnapshot("MARKETDATA|omsPendingOrder", "LastPrice", 1000)
    If lastActual >= lastPending Then
        omsCounterStart = lastActual
    Else
        omsCounterStart = lastPending
    End If
    'Put the new counter on Gissing in the omsPendingOrder
    strResult = Excel.Evaluate("=RTXLPub( ""MARKETDATA|omsPendingOrder"", ""LastPrice"", """ & omsCounterStart + activeTrueCount & """)")

    If activeTrueCount > 0 Then
        ReDim omsCounters(activeTrueCount)
        
        orderCount = 1
        For count = 1 To orderRange.count
            If activationRange(count) = True Then
                omsCounters(orderCount) = tsomsPrepareOrder(orderRange(count), senderRange(count), receiverRange(count), omsCounterStart + orderCount)
                resultRange(count).Value = omsCounters(orderCount) & " | " & Format(Now, "hh:mm:ss")
                orderCount = orderCount + 1
            End If
        Next

        'set the activation range to FALSE
        activationRange.Value = False

        '*********** Fire off the orders **********************
        For count = 1 To activeTrueCount
            'Tick the LastPrice on Gissing, this generates the order
            'strResult = Excel.Evaluate("=RTXLPub( ""MARKETDATA|omsOrderCounter"", ""Timestamp"", """ & Format(Now, "yyyy/mm/dd hh:mm:ss") & """)")
            strResult = Excel.Evaluate("=RTXLPub( ""MARKETDATA|omsOrderCounter"", ""LastPrice"", """ & omsCounters(count) & """)")
            'Pause exection for 1 second to prevent conflation
            Application.Wait (TimeSerial(Hour(Now), Minute(Now()), Second(Now()) + gissingWaitSeconds))
        Next
    End If
End Sub

Sub tsomsResetToday()
'Restarts the service for today to recover

    strResult = Excel.Evaluate("=RTXLPub( ""MARKETDATA|omsOrderCounter"", ""LastPrice"", """ & 0 & """)")
    strResult = Excel.Evaluate("=RTXLPub( ""MARKETDATA|omsPendingOrder"", ""LastPrice"", """ & 0 & """)")
End Sub
