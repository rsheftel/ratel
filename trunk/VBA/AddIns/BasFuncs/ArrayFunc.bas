Attribute VB_Name = "ArrayFunc"
Option Explicit
Option Base 1  'It is important that base=1 for the LinkArray function

Function Interp(Search As Range, result As Range, Value As Double) As Double
'New version of interpolate function

Dim size As Integer, count As Integer, lower_bound As Double, upper_bound As Double, lower_value As Double, upper_value As Double

    size = Search.Rows.count
    
    If size = 1 Then size = Search.Columns.count
    
    If Value <= Search(1) Then
        Interp = result(1)
    ElseIf Value >= Search(size) Then
        Interp = result(size)
    Else
        count = 1
        Do While (count < size) And (Value >= Search(count + 1))
            count = count + 1
        Loop
    
        lower_bound = Search(count)
        upper_bound = Search(count + 1)
        lower_value = result(count)
        upper_value = result(count + 1)
        
        Interp = lower_value + (upper_value - lower_value) * (Value - lower_bound) / (upper_bound - lower_bound)
    End If
End Function

Function Interpolate(Search_Array As Range, Result_Array As Range, Value As Double) As Double
'Performs piecewise linear interpolation

Dim lower_index As Integer, lower_bound As Double, lower_value As Double
Dim upper_index As Integer, upper_bound As Double, upper_value As Double
    
    With Application
    If Search_Array.Columns.count = 1 Then
        If Value < .Index(Search_Array, 1, 1) Then
            Interpolate = .Index(Result_Array, 1, 1)
        Else
            lower_index = .Match(Value, Search_Array, 1)
            lower_bound = .Index(Search_Array, lower_index, 1)
            lower_value = .Index(Result_Array, lower_index, 1)
            If lower_index = Search_Array.count Then
                Interpolate = .Index(Result_Array, lower_index, 1)
            Else
                upper_bound = .Index(Search_Array, lower_index + 1, 1)
                upper_value = .Index(Result_Array, lower_index + 1, 1)
                Interpolate = lower_value + (upper_value - lower_value) * (Value - lower_bound) / (upper_bound - lower_bound)
            End If
        End If
    Else
        If Value < .Index(Search_Array, 1, 1) Then
            Interpolate = .Index(Result_Array, 1, 1)
        Else
            lower_index = .Match(Value, Search_Array, 1)
            lower_bound = .Index(Search_Array, 1, lower_index)
            lower_value = .Index(Result_Array, 1, lower_index)
            If lower_index = Search_Array.count Then
                Interpolate = .Index(Result_Array, 1, lower_index)
            Else
                upper_bound = .Index(Search_Array, 1, lower_index + 1)
                upper_value = .Index(Result_Array, 1, lower_index + 1)
                Interpolate = lower_value + (upper_value - lower_value) * (Value - lower_bound) / (upper_bound - lower_bound)
            End If
        End If
    End If
    End With
End Function

Function InterpolateXY(ArrayX As Range, ValueX As Double, ArrayY As Range, ValueY As Double, Grid As Range)
'Performs piecewise linear interpolation in 2-dimensions

Dim lower_index As Integer, lower_bound As Double, lower_value As Double
Dim upper_index As Integer, upper_bound As Double, upper_value As Double

Dim index_loX As Integer, index_hiX As Integer, index_loY As Integer, index_hiY As Integer
Dim weight_loX As Double, weight_hiX As Double, weight_loY As Double, weight_hiY As Double
    
    With Application
    'Search the Y column
    If ValueY < .Index(ArrayY, 1, 1) Then
        index_loY = 1
        index_hiY = 1
        weight_loY = 1
        weight_hiY = 0
    Else
        index_loY = .Match(ValueY, ArrayY, 1)
        lower_bound = .Index(ArrayY, index_loY, 1)
        index_hiY = .Min(index_loY + 1, ArrayY.Rows.count)
        upper_bound = .Index(ArrayY, index_hiY, 1)
            
        If index_loY = index_hiY Then
            weight_loY = 1
            weight_hiY = 0
        Else
            weight_hiY = (ValueY - lower_bound) / (upper_bound - lower_bound)
            weight_loY = 1 - weight_hiY
        End If
    End If
            
    'Search the X row
    If ValueX < .Index(ArrayX, 1, 1) Then
        index_loX = 1
        index_hiX = 1
        weight_loX = 1
        weight_hiX = 0
    Else
        index_loX = .Match(ValueX, ArrayX, 1)
        lower_bound = .Index(ArrayX, 1, index_loX)
        index_hiX = .Min(index_loX + 1, ArrayX.Columns.count)
        upper_bound = .Index(ArrayX, 1, index_hiX)
            
        If index_loX = index_hiX Then
            weight_loX = 1
            weight_hiX = 0
        Else
            weight_hiX = (ValueX - lower_bound) / (upper_bound - lower_bound)
            weight_loX = 1 - weight_hiX
        End If
    End If
                
    'Get value
    InterpolateXY = weight_hiX * weight_hiY * .Index(Grid, index_hiY, index_hiX)
    InterpolateXY = weight_hiX * weight_loY * .Index(Grid, index_loY, index_hiX) + InterpolateXY
    InterpolateXY = weight_loX * weight_hiY * .Index(Grid, index_hiY, index_loX) + InterpolateXY
    InterpolateXY = weight_loX * weight_loY * .Index(Grid, index_loY, index_loX) + InterpolateXY
    
    End With
End Function

Function LinkArrays(ParamArray InputArrays() As Variant) As Variant
'This function takes several multi dimensional arrays and links them together into one long
'array. The reulting array is the Input arrays lined up in the order they are passed.
'
'Inputs :  InputArrays  -  The multi dimensional arrays to be linked, in order
'          All the arrays MUST have the same number of columns.
'Result :  A single dimension array that is the linking of passed arrays.

Dim count As Integer, ColCount As Integer, Last As Integer, ArrayCount As Integer
Dim CurRange As Range
Dim result() As Variant
Dim ResultLen As Integer, ResultWidth As Integer

    'Set up the initial Result array
    ResultLen = 0
    For ArrayCount = 0 To UBound(InputArrays)
        ResultLen = ResultLen + InputArrays(ArrayCount).Rows.count
    Next
    ResultWidth = InputArrays(0).Columns.count
    ReDim result(ResultLen, ResultWidth)
    Last = 0
    
    'Fill up the Result array with the passed array arguments
    For ArrayCount = 0 To UBound(InputArrays)
        Set CurRange = InputArrays(ArrayCount)
        For ColCount = 1 To CurRange.Columns.count
            For count = 1 To CurRange.Rows.count
                result(Last + count, ColCount) = CurRange.Cells(count, ColCount).Value
            Next
        Next
        Last = Last + count - 1
    Next
    
    LinkArrays = result
End Function

