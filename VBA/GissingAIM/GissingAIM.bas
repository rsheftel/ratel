Attribute VB_Name = "AIM"
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
        If aIDs(rowCount, 1) = aimID Then
            If aValues(rowCount, colTagLevel1Name) = aimTagLevel1Name Then
                fieldValue = aValues(rowCount, colField)
                Exit For
            End If
        End If
    Next
    
    aimGetFieldValue = fieldValue
    
End Function

Function aimGetFieldValueFromID(aimID As String, aimTagLevel1Name As String, aimField As String, volatileCell As Variant) As Variant
'Returns the value of a field for a given ID and TageLevel1Name.
'The ID is fromt the aimID range

    aimGetFieldValueFromID = aimGetFieldValue(Range("aim.IDs"), aimID, aimTagLevel1Name, aimField, volatileCell)
End Function

Function aimGetFieldValueFromTicker(aimTicker As String, aimTagLevel1Name As String, aimField As String, volatileCell As Variant) As Variant
'Returns the value of a field for a given ID and TageLevel1Name.
'The ID is from the ticker column in the values range

Dim rFields As Range
Dim colTicker As Integer

    Set rFields = Range("aim.fields")

    colTicker = Application.WorksheetFunction.Match("ticker", rFields, 0)
    
    aimGetFieldValueFromTicker = aimGetFieldValue(Range("aim.values").Offset(, colTicker - 1).Resize(, 1), aimTicker, aimTagLevel1Name, aimField, volatileCell)
End Function


Function aimGetNetPosition(aimID As String, aimTagLevel1Name As String, volatileCell As Variant) As Variant
'Returns the position (holding) for a given position
Dim livePosition As Variant

    livePosition = aimGetFieldValueFromID(aimID, aimTagLevel1Name, "currentPosition", volatileCell)
    If IsNumeric(livePosition) Then
        aimGetNetPosition = livePosition
    Else
        aimGetNetPosition = aimGetFieldValueFromID(aimID, aimTagLevel1Name, "fullCurrentNetPosition", volatileCell)
    End If
    
End Function

Function aimGetEquityNetPosition(aimTicker As String, aimTagLevel1Name As String, volatileCell As Variant) As Variant
'Returns the position (holding) for a given position
Dim livePosition As Variant

    livePosition = aimGetFieldValueFromTicker(aimTicker, aimTagLevel1Name, "currentPosition", volatileCell)
    If IsNumeric(livePosition) Then
        aimGetEquityNetPosition = livePosition
    Else
        aimGetEquityNetPosition = aimGetFieldValueFromTicker(aimTicker, aimTagLevel1Name, "fullCurrentNetPosition", volatileCell)
    End If
    
End Function


Function aimGetFuturesNetContracts(aimID As String, aimTagLevel1Name As String, volatileCell As Variant) As Variant
'Returns the futures position (holding) for a given position in contracts

Dim fullNetPosition As Variant, contractSize As Variant

    contractSize = aimGetFieldValueFromID(aimID, aimTagLevel1Name, "contractSize", volatileCell)
    
    'Replace below with a call to aimGetNetPosition once bberg fixes
    Dim livePosition As Variant

    livePosition = aimGetFieldValueFromID(aimID, aimTagLevel1Name, "currentPosition", volatileCell)
    If IsNumeric(livePosition) Then
        Select Case UCase(Left(aimID, 2))
        Case "TU", "FV", "TY", "US"
            fullNetPosition = livePosition * contractSize
        Case Else
            fullNetPosition = livePosition
        End Select
    Else
        fullNetPosition = aimGetFieldValueFromID(aimID, aimTagLevel1Name, "fullCurrentNetPosition", volatileCell)
    End If
    'End replace
    
    'fullNetPosition = aimGetNetPosition(aimID, aimTagLevel1Name, volatileCell)
    
    If (IsNumeric(fullNetPosition) And IsNumeric(contractSize)) Then
        If (contractSize > 0) Then
            aimGetFuturesNetContracts = fullNetPosition / contractSize
        Else
            aimGetFuturesNetContracts = "#Error - ContractSize is zero!"
        End If
    Else
        aimGetFuturesNetContracts = "#Error"
    End If
End Function


Function aimGetFuturesAllNetContracts(ticker As String, aimTagLevel1Name As String, monthYearCodes As Variant, volatileCell As Variant) As Variant
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
        thisContract = aimGetFuturesNetContracts(tickerFull, aimTagLevel1Name, volatileCell)
        If IsNumeric(thisContract) Then
            contractSum = contractSum + thisContract
        End If
    Next

    aimGetFuturesAllNetContracts = contractSum

End Function
