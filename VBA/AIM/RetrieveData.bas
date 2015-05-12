Attribute VB_Name = "RetrieveData"
Option Explicit
Option Base 1

Function aimGetFieldValue(aimIDrange As Range, aimID As String, aimTagLevel1Name As String, aimField As String, volatileCell As Variant) As Variant
'Returns the value of a field for a given ID and TageLevel1Name.
'The ID is from a specific range

Dim rFields As Range
Dim aIDs As Variant
Dim aValues As Variant
Dim fieldValue As Variant

Dim colTagLevel1Name As Integer, colField As Integer, rowCount As Integer

    Set rFields = Range("aim.fields")
    aIDs = aimIDrange
    aValues = Range("aim.values")

    colField = Application.WorksheetFunction.Match(aimField, rFields, 0)
    colTagLevel1Name = Application.WorksheetFunction.Match("Level1TagName", rFields, 0)

    fieldValue = "#Error ID/Tag Not Found!"
    For rowCount = LBound(aIDs) To UBound(aIDs)
        If IsError(aIDs(rowCount, 1)) Then
            Exit For
        End If
        If aIDs(rowCount, 1) = aimID Then
            If aValues(rowCount, colTagLevel1Name) = aimTagLevel1Name Then
                fieldValue = aValues(rowCount, colField)
                Exit For
            End If
        End If
    Next
    
    aimGetFieldValue = fieldValue
    
End Function
Function aimGetFieldValueFromFieldName(aimID As String, aimIDField As String, aimTagLevel1Name As String, aimField As String, volatileCell As Variant) As Variant
'Returns the value of a field for a given ID and TageLevel1Name.
'The ID is from the column specified by the aimIDField in the values range

Dim rFields As Range
Dim colTicker As Integer

    Set rFields = Range("aim.fields")

    colTicker = Application.WorksheetFunction.Match(aimIDField, rFields, 0)
    
    aimGetFieldValueFromFieldName = aimGetFieldValue(Range("aim.values").Offset(, colTicker - 1).Resize(, 1), aimID, aimTagLevel1Name, aimField, volatileCell)
End Function

Function aimGetFieldValueFromID(aimID As String, aimTagLevel1Name As String, aimField As String, volatileCell As Variant) As Variant
'Returns the value of a field for a given ID and TageLevel1Name.
'The ID is fromt the aimID range

    aimGetFieldValueFromID = aimGetFieldValueFromKey(aimID, aimTagLevel1Name, Range("aim.book").Value, aimField, volatileCell)
End Function

Function aimGetFieldValueFromTicker(aimTicker As String, aimTagLevel1Name As String, aimField As String, volatileCell As Variant) As Variant
'Returns the value of a field for a given ID and TageLevel1Name.
'The ID is from the ticker column in the values range
   
    aimGetFieldValueFromTicker = aimGetFieldValueFromFieldName(aimTicker, "ticker", aimTagLevel1Name, aimField, volatileCell)
End Function

Function aimGetFieldValueFromKey(aimID As String, aimTagLevel1Name As String, aimBook As String, aimField As String, volatileCell As Variant) As Variant
Dim keyRow As Integer, fieldCol As Integer
Dim key As String
Dim returnValue As Variant

    key = "account=" + aimBook + "|securityId=" + aimID + "|level1TagName=" + aimTagLevel1Name
    keyRow = -1
    fieldCol = -1
    returnValue = "#Error ID/Tag/Field Not Found!"
    
    On Error GoTo ExitFunc
    keyRow = Application.WorksheetFunction.Match(key, Range("aim.keys"), 0)
    fieldCol = Application.WorksheetFunction.Match(aimField, Range("aim.fields"), 0)
    
    returnValue = Range("aim.values")(keyRow, fieldCol)

ExitFunc:
    aimGetFieldValueFromKey = returnValue
    Exit Function

End Function


Function aimGetEquityNetPosition(securityId As String, aimTagLevel1Name As String, volatileCell As Variant) As Variant
'Returns the position (holding) for a given position
Dim livePosition As Variant, closePosition As Variant

    livePosition = aimGetFieldValueFromID(securityId, aimTagLevel1Name, "currentPosition", volatileCell)
    If IsNumeric(livePosition) Then
        aimGetEquityNetPosition = livePosition
    Else
        closePosition = aimGetFieldValueFromID(securityId, aimTagLevel1Name, "fullCurrentNetPosition", volatileCell)
        If IsNumeric(closePosition) Then
            aimGetEquityNetPosition = closePosition
        Else
            aimGetEquityNetPosition = 0
        End If
    End If
End Function


Function aimGetFuturesNetContracts(aimID As String, aimTagLevel1Name As String, volatileCell As Variant, Optional futureContractSize As Variant = Null) As Variant
'Returns the futures position (holding) for a given position in contracts

Dim netPosition As Variant, contractSize As Variant
Dim livePosition As Variant, batchPosition As Variant

On Error Resume Next

    If IsNull(futureContractSize) Then
        contractSize = aimGetFieldValueFromID(aimID, aimTagLevel1Name, "contractSize", volatileCell)
    Else
        contractSize = futureContractSize
    End If
    
    livePosition = aimGetFieldValueFromID(aimID, aimTagLevel1Name, "totalBuyVolume", volatileCell)
    livePosition = livePosition - aimGetFieldValueFromID(aimID, aimTagLevel1Name, "totalSellVolume", volatileCell)
    If Not (IsNumeric(livePosition)) Then
        livePosition = 0
    End If
        
    batchPosition = aimGetFieldValueFromID(aimID, aimTagLevel1Name, "currentLongPosition", volatileCell)
    If Not (IsNumeric(batchPosition)) Then
        batchPosition = 0
    End If
    Select Case UCase(Left(aimID, 2))
    Case "TU", "FV", "TY", "US", "ED"
        If IsNumeric(batchPosition) Then
            batchPosition = batchPosition * 1000
        End If
    End Select
    
    netPosition = batchPosition + livePosition
        
    If (IsNumeric(netPosition) And IsNumeric(contractSize)) Then
        If (contractSize > 0) Then
            Select Case UCase(Left(aimID, 2))
            Case "ES", "UX"
                aimGetFuturesNetContracts = netPosition
            Case Else
                aimGetFuturesNetContracts = netPosition / contractSize
            End Select
        Else
            aimGetFuturesNetContracts = "#Error - ContractSize is zero!"
        End If
    Else
        aimGetFuturesNetContracts = "#Error"
    End If
End Function


Function aimGetFuturesAllNetContracts(ticker As String, aimTagLevel1Name As String, monthYearCodes As Variant, volatileCell As Variant, Optional contractSize As Variant = Null) As Variant
'Returns the cummulative position for a given futures ticker
'The monthYearCodes can either be an range or an array. It should have

Dim monthCount As Integer, indexLow As Integer, indexHigh As Integer
Dim contractSum As Double, thisContract As Variant
Dim myCodes As Variant
Dim tickerFull As String

    If TypeName(monthYearCodes) = "Range" Then
        If monthYearCodes.Rows.Count = 1 Then
            myCodes = Application.Transpose(monthYearCodes)
        Else
            myCodes = monthYearCodes
        End If
    Else
        myCodes = monthYearCodes
    End If
    
    indexLow = LBound(myCodes)
    indexHigh = UBound(myCodes)
    contractSum = 0
    For monthCount = indexLow To indexHigh
        If TypeName(monthYearCodes) = "Range" Then
            tickerFull = ticker & myCodes(monthCount, 1)
        Else
            tickerFull = ticker & myCodes(monthCount)
        End If
        thisContract = aimGetFuturesNetContracts(tickerFull, aimTagLevel1Name, volatileCell, contractSize)
        If IsNumeric(thisContract) Then
            contractSum = contractSum + thisContract
        End If
    Next

    aimGetFuturesAllNetContracts = contractSum

End Function

Function aimGetBondNetPosition(aimID As String, aimTagLevel1Name As String, volatileCell As Variant) As Variant
'Returns the position (holding) for a given TBA or Bond position

Dim netPosition As Variant, livePosition As Variant, batchPosition As Variant

On Error Resume Next

    livePosition = aimGetFieldValueFromID(aimID, aimTagLevel1Name, "totalBuyVolume", volatileCell)
    livePosition = livePosition - aimGetFieldValueFromID(aimID, aimTagLevel1Name, "totalSellVolume", volatileCell)
    If IsNumeric(livePosition) Then
        livePosition = livePosition / 1000000
    Else
        livePosition = 0
    End If
    
    batchPosition = aimGetFieldValueFromID(aimID, aimTagLevel1Name, "currentLongPosition", volatileCell) / 1000
    If Not (IsNumeric(batchPosition)) Then
        batchPosition = 0
    End If
    
    aimGetBondNetPosition = livePosition + batchPosition
    
End Function

Function aimGetNetFXPosition(fxID As String, aimTagLevel1Name As String, volatileCell As Variant) As Variant
'Returns the value of a field for a given fxID and TageLevel1Name.

Dim rFields As Range, thisCell As Range, thisRow As Integer
Dim colBuy As Integer, colSell As Integer, colLivePosition As Integer, colSecurityId As Integer, colTag As Integer
Dim totalPosition As Variant, newPosition As Variant, livePosition As Variant
Dim firstAddress As Variant, exitState As Boolean
Dim buyVolume As Variant, sellVolume As Variant
Dim tagName As String

    Set rFields = Range("aim.fields")
    colSecurityId = Application.WorksheetFunction.Match("securityId", rFields, 0)
    colBuy = Application.WorksheetFunction.Match("totalBuyVolume", rFields, 0)
    colSell = Application.WorksheetFunction.Match("totalSellVolume", rFields, 0)
    colLivePosition = Application.WorksheetFunction.Match("currentLongPosition", rFields, 0)
    colTag = Application.WorksheetFunction.Match("level1TagName", rFields, 0)
    
    totalPosition = 0
    With Range("aim.values").Offset(, colSecurityId - 1).Resize(columnsize:=1)
        Set thisCell = .Find(fxID, LookIn:=xlValues, LookAt:=xlPart, SearchOrder:=xlByColumns)
        If Not thisCell Is Nothing Then
        firstAddress = thisCell.Address
        exitState = False
            Do
                thisRow = thisCell.Row - .Row + 1
                tagName = Range("aim.values").Cells(thisRow, colTag).Value
                If (tagName = aimTagLevel1Name) Then
                    buyVolume = Range("aim.values").Cells(thisRow, colBuy).Value
                    sellVolume = Range("aim.values").Cells(thisRow, colSell).Value
                    newPosition = 0
                    If IsNumeric(buyVolume) Then
                        newPosition = newPosition + buyVolume
                    End If
                    If IsNumeric(sellVolume) Then
                        newPosition = newPosition - sellVolume
                    End If

                    livePosition = Range("aim.values").Cells(thisRow, colLivePosition).Value
                    If IsNumeric(livePosition) Then
                        totalPosition = totalPosition + livePosition / 2 / 10
                    ElseIf IsNumeric(newPosition) Then
                        totalPosition = totalPosition + newPosition / 2 / 10000
                    End If
                End If
                Set thisCell = .Find(fxID, LookIn:=xlValues, LookAt:=xlPart, SearchOrder:=xlByColumns, after:=thisCell)
                If (thisCell Is Nothing) Then
                    exitState = True
                ElseIf (thisCell.Address = firstAddress) Then
                    exitState = True
                End If
            Loop While Not exitState
        End If
    End With
    aimGetNetFXPosition = totalPosition
End Function

