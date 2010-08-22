VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "BloombergData"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = True
Option Explicit

Private WithEvents oBlpData As BLP_DATA_CTRLLib.BlpData
Attribute oBlpData.VB_VarHelpID = -1


Private Sub Class_Initialize()
'Initialize object
    If oBlpData Is Nothing Then
        Set oBlpData = New BlpData
    End If
    'Set default params
    'oblddata.Timeout = TimeoutNone
    
End Sub

Private Sub Class_Terminate()
'Terminate
    Set oBlpData = Nothing
End Sub


'=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
'Synchronous Data Retrieval Methods

Public Function getDataFromLiveSnapshot(ByVal bbergTicker As String, ByVal bbergField As String) As Variant
'Get a live snapshot of a value
    
    getDataFromLiveSnapshot = oBlpData.BLPSubscribe(Security:=bbergTicker, Fields:=bbergField)(0, 0)
End Function


Public Function getDataFromDate(ByVal bbergTicker As String, ByVal bbergField As String, ByVal bbergDate As Date) As Variant
'Get historical close/last value for a given date

    oBlpData.NonTradingDayValue = ShowNoNumber
    oBlpData.DisplayNonTradingDays = Omit
    oBlpData.ShowHistoricalDates = False
    
    getDataFromDate = oBlpData.BLPGetHistoricalData(bbergTicker, bbergField, bbergDate, bbergDate)(0, 0, 0)

End Function

Public Function getDataFromDateTime(ByVal bbergTicker As String, ByVal bbergField As String, ByVal bbergDateTime As Date) As Variant
'Get a value for a given DateTime. This is truncated to the nearest minute
Dim endDateTime As Date

    oBlpData.NonTradingDayValue = ShowNoNumber
    oBlpData.DisplayNonTradingDays = Omit
    oBlpData.ShowHistoricalDates = False
    oBlpData.ReverseChronological = False
    
    endDateTime = bbergDateTime + TimeSerial(0, 1, 0)
    
    getDataFromDateTime = oBlpData.BLPGetHistoricalData(bbergTicker, bbergField, bbergDateTime, endDateTime, 1)(0, 0, 1)
End Function

Public Function getTickDataFromDateTime(ByVal bbergTicker As String, ByVal bbergField As String, ByVal bbergDateTime As Date) As Variant
'Get tick data for a given DateTime.

    oBlpData.NonTradingDayValue = ShowNoNumber
    oBlpData.DisplayNonTradingDays = Omit
    oBlpData.ShowHistoricalDates = False
    
    getTickDataFromDateTime = oBlpData.BLPGetHistoricalData(bbergTicker, bbergField, bbergDateTime, bbergDateTime, 0)(0, 0, 1)
End Function

Public Function getDataFromDateRange(ByVal bbergTicker As String, ByVal bbergField As String, ByVal startDate As Date, ByVal endDate As Date) As Variant
'Get historical close/last value for a given date

    oBlpData.NonTradingDayValue = ShowNoNumber
    oBlpData.DisplayNonTradingDays = Omit
    oBlpData.ShowHistoricalDates = True
    
    getDataFromDateRange = oBlpData.BLPGetHistoricalData(bbergTicker, bbergField, startDate, endDate)

End Function
