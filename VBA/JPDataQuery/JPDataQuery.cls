VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "JPDataQuery"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = True
Option Explicit

Private Type contextClass
    frequency As String
    frequencyConversion As String
    naCode As String
    calendar As String
    connectionString As String
End Type

Private contextObject As contextClass

Private Sub Class_Initialize()
'Initialize the Context object
    With contextObject
        .frequency = "FREQ_DAY"
        .frequencyConversion = "CONV_LASTBUS_ABS"
        .naCode = "NA_NOTHING"
        .calendar = "CAL_WEEKDAYS"
    End With
    
    initiateContext
End Sub

Private Sub initiateContext()
'Initaiate the context object
    With contextObject
        contextObject.connectionString = Application.Run("DQ_CREATE_CONTEXT", .frequency, .frequencyConversion, .naCode, .calendar)
    End With
End Sub

Private Sub Class_Terminate()
'Terminate the object
With contextObject
        .frequency = ""
        .frequencyConversion = ""
        .naCode = ""
        .calendar = ""
        .connectionString = ""
    End With
End Sub

Public Function getValue(ByVal dqTicker As String, ByVal dataDate As Variant) As Variant
'Retrieve a single date
Dim dqData As Variant

    dqData = Application.Run("DQ_GET_SERIES", dataDate, dataDate, dqTicker, contextObject.connectionString, "NO_DATES", "DO_ASCENDING")
    If IsError(dqData(0, 0)) Then
        getValue = "NA"
    Else
        getValue = dqData(0, 0)
    End If

End Function

Public Sub setCalendar(ByVal dqCalendarType As String)
'Set the calendar type
    contextObject.calendar = dqCalendarType
    initiateContext
End Sub

Public Function getValuesDateRange(ByVal dqTicker As String, ByVal startDate As Variant, ByVal endDate As Variant) As Variant
'Retrieve a range of dates from start to end

    getValuesDateRange = Application.Run("DQ_GET_SERIES", startDate, endDate, dqTicker, contextObject.connectionString, "DATES_IN_COLS", "DO_ASCENDING")
    
End Function
