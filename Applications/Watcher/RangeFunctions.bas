Attribute VB_Name = "RangeFunctions"
Option Explicit
Option Base 1

Sub MakeRange(rangeName As String)
'Make the range to be defined as the range starting the first line
'after the header and going to the last contiguous row.

    Dim oRange As Range
    Dim oHeader As Range
    
    Dim startCol As Integer, startRow As Integer
    
    Set oHeader = Range(rangeName & ".header")
    
    startCol = oHeader.column
    startRow = oHeader.row
    
    Set oRange = Cells(startRow + 1, startCol)
    
    oRange.Select
    oRange.Resize(1, oHeader.Columns.Count).Select
    If IsEmpty(Cells(startRow + 2, startCol).Value) = False Then
        Range(Selection, Selection.End(xlDown)).Select
    End If
    Names.Add Name:=rangeName, RefersTo:=Selection

End Sub

Sub OutlineRange(rangeName As String)

    Dim oRange As Range
    
    Set oRange = Range(rangeName)

    oRange.Borders(xlDiagonalDown).LineStyle = xlNone
    oRange.Borders(xlDiagonalUp).LineStyle = xlNone
    oRange.Borders(xlEdgeLeft).LineStyle = xlNone
    oRange.Borders(xlEdgeTop).LineStyle = xlNone
    oRange.Borders(xlEdgeBottom).LineStyle = xlNone
    oRange.Borders(xlEdgeRight).LineStyle = xlNone
    oRange.Borders(xlInsideVertical).LineStyle = xlNone
    oRange.Borders(xlInsideHorizontal).LineStyle = xlNone
    oRange.BorderAround LineStyle:=xlContinuous, Weight:=xlMedium
    
    If oRange.Columns.Count > 1 Then
        With oRange.Borders(xlInsideVertical)
            .LineStyle = xlContinuous
            .Weight = xlThin
            .ColorIndex = 15
        End With
        With oRange.Borders(xlInsideHorizontal)
            .LineStyle = xlContinuous
            .Weight = xlThin
            .ColorIndex = 15
        End With
    End If
    oRange.Interior.ColorIndex = 2
        
End Sub

Sub MakeRangeFromTopLeft(rangeName As String)
'Create a range from a continuous area given the top-left of the range(1,1)
    
    Range(rangeName).Worksheet.Activate
    Range(rangeName)(1, 1).Select
    Range(Selection, Selection.End(xlDown)).Select
    Range(Selection, Selection.End(xlToRight)).Select
    ActiveWorkbook.Names.Add Name:=rangeName, RefersTo:=Selection

End Sub

Sub MakeRangeFromTop(rangeName As String)
'Create a range from a continuous area given the top of the range(1,1)
    
    Range(rangeName).Worksheet.Activate
    Range(rangeName)(1, 1).Select
    Range(Selection, Selection.End(xlDown)).Select
    ActiveWorkbook.Names.Add Name:=rangeName, RefersTo:=Selection

End Sub
