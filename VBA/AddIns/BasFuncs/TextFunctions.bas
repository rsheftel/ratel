Attribute VB_Name = "TextFunctions"
Option Explicit
Option Base 1

Function squish(sepChar As String, ParamArray textArray() As Variant) As Variant

'Combines a list of inputs into a text string seperated by the sep character

Dim squishText As Variant
Dim count As Integer
    
    squishText = textArray(0).Value
    For count = 1 To UBound(textArray)
        squishText = squishText & sepChar & textArray(count).Value
    Next
    
    squish = squishText

End Function

