Attribute VB_Name = "DateFunctions"
Option Explicit

Function old_businessDaysAgo(days As Integer, thisDate As Date, financialCalendar As String) As Variant


'########## Depricated and now in QExcel library ##############


Dim result As Variant

    On Error Resume Next
    businessDaysAgo = "#Error"
    
    'Check if R is running, if not then start it. Note this uses default start logic
    If Not (rconnected) Then
        rinterface.StartRServer
    End If
    
    'Load GSFCore
    result = rEval("library(GSFCore)")
    
    result = rinterface.GetArrayToVBA("as.character(businessDaysAgo(" & days & ",'" & Format(thisDate, "yyyy-mm-dd") & "',center='" + financialCalendar & "'))")
    
    businessDaysAgo = CDate(result)
    
End Function


