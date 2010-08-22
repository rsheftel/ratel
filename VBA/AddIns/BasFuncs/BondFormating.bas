Attribute VB_Name = "BondFormating"
Option Explicit


Function Dec2Ticks(dec As Double) As String
Attribute Dec2Ticks.VB_ProcData.VB_Invoke_Func = " \n14"
'Converts a decimal number to a Ticks format number
'Result - String in Tick format

Dim Points, N32nd As Double, N8th As Double, Ticks As Double

    Points = Fix(dec)
    Ticks = Abs((dec - Points) * 32)
    N32nd = Int(Ticks)
    N8th = Round((Ticks - N32nd) * 8, 0)
    
    If Points = 0 Then
        If Sgn(dec) = -1 Then
            Points = "-" & Points
        End If
    End If
    If N8th = 8 Then
        If N32nd < 9 Then
            Dec2Ticks = Points & "-0" & (N32nd + 1)
        Else
            Dec2Ticks = Points & "-" & (N32nd + 1)
        End If
    ElseIf N8th = 0 Then
        If N32nd < 10 Then
           Dec2Ticks = Points & "-0" & N32nd
        Else
            Dec2Ticks = Points & "-" & N32nd
        End If
    ElseIf N8th = 4 Then
        If N32nd < 10 Then
           Dec2Ticks = Points & "-0" & N32nd & "+"
        Else
            Dec2Ticks = Points & "-" & N32nd & "+"
        End If
    Else
        If N32nd < 10 Then
            Dec2Ticks = Points & "-0" & N32nd & N8th
        Else
            Dec2Ticks = Points & "-" & N32nd & N8th
        End If
    End If
End Function


Function Ticks2Dec(Ticks As String) As Double
'Converts Ticks to Decimal assuming a format of xxx-yyz, while xxx is points, yy is 32nds
'and z is 8ths of 32nds. The string must begin with a number
'
'10/4/07  -  Updated to new code in dash search logic

Dim Char, count As Integer, Length As Double, Points As Double, N32nd As Double, N8th As Double, Negative As Double

    Length = Len(Ticks)
    Negative = 1
    
    count = 1
    Points = 0
    Char = Mid$(Ticks, count, 1)
        
    If Char = "-" Then
        Negative = -1
        count = count + 1
        Char = Mid$(Ticks, count, 1)
    End If
    
    If IsNumeric(Char) Then
        Do Until Char = "-"
            Points = Points * 10 + Char
            count = count + 1
           Char = Mid$(Ticks, count, 1)
        Loop
        
        count = count + 1
        Char = Mid$(Ticks, count, 2)
        If (Length - count) >= 1 Then
            N32nd = Char / 32
            count = count + 2
            If count <= Length Then
                Char = Mid$(Ticks, count, 1)
                If Char = "+" Then
                    N8th = 4 / 256
                Else
                    N8th = Char / 256
                End If
            Else
                N8th = 0
            End If
            Ticks2Dec = (Points + N32nd + N8th) * Negative
        Else
            Ticks2Dec = "Error in 32nd Field"
        End If
    Else
        Ticks2Dec = "Must Start With Num"
    End If
End Function


Function n64th2Dec(Ticks As String) As Double
Attribute n64th2Dec.VB_ProcData.VB_Invoke_Func = " \n14"
'Converts Ticks to Decimal assuming a format of xxx-yy, while xxx is points, yy is 64ths

Dim Char, count, Length, Points, N64th

    Length = Len(Ticks)
    
    count = 1
    Points = 0
    Char = Mid$(Ticks, 1, 1)
        
    If IsNumeric(Char) Then
        Do Until Char = "-"
            Points = Points * 10 + Char
            count = count + 1
            Char = Mid$(Ticks, count, 1)
        Loop
        count = count + 1
        Char = Mid$(Ticks, count, 2)
        If (Length - count) >= 1 Then
            N64th = Char / 64
            count = count + 2
            n64th2Dec = Points + N64th
        Else
            n64th2Dec = "Error in 32nd Field"
        End If
    Else
        n64th2Dec = "Must Start With Num"
    End If
End Function


Function PriceAlign(TickInput As String) As String
    If Mid$(TickInput, Len(TickInput) - 2, 1) = "-" Then
        PriceAlign = TickInput + " "
    Else
        PriceAlign = TickInput
    End If
End Function


Function Dec2Quarters(dec As Double) As String
'Converts a decimal number to a Quarters "-1'2" format number
'Result - String in Tick format

Dim N32nd, N4th As Double, Ticks As Double

    Ticks = dec * 32
    N32nd = Fix(Ticks)
    N4th = Abs(Round((Ticks - N32nd) * 4, 0))
    
    If N32nd = 0 Then
        N32nd = ""
    End If
    
    Select Case N4th
    Case 4
        If N32nd = "" Then
            Dec2Quarters = Sgn(dec)
        Else
            Dec2Quarters = N32nd + Sgn(dec)
        End If
    Case 0
        Dec2Quarters = N32nd
    Case 2
        If (Sgn(dec) = -1) And (N32nd = "") Then
            Dec2Quarters = "-+"
        Else
            Dec2Quarters = N32nd & "+"
        End If
    Case Else
        If (Sgn(dec) = -1) And (N32nd = "") Then
            Dec2Quarters = "-'" & (N4th * 2)
        Else
            Dec2Quarters = N32nd & "'" & (N4th * 2)
        End If
    End Select
End Function


Function Quarters2Dec(Ticks As String) As Double
'Converts Ticks to Decimal assuming a format of "xx'y", while xx is ticks, y quarters
'The string must begin with a number

Dim Char, count As Double, Length As Double, N32nd As Double, N4th As Double, Negative As Double

    Length = Len(Ticks)
    Negative = 1
    
    count = 1
    N32nd = 0
    N4th = 0
    Char = Mid$(Ticks, count, 1)
        
    If Char = "-" Then
        Negative = -1
        count = count + 1
        Char = Mid$(Ticks, count, 1)
    End If
    
    Do Until ((Char = "'" Or Char = "+") Or count = Length + 1)
        N32nd = N32nd * 10 + Char / 32
        count = count + 1
        Char = Mid$(Ticks, count, 1)
    Loop
        
    
    Select Case Char
    Case "+"
        N4th = 1 / 64
    Case "'"
        N4th = Mid$(Ticks, count + 1, 1) / 256
    End Select
    
    Quarters2Dec = (N32nd + N4th) * Negative
End Function

Function Dec2Eigths(dec As Double) As String
'Converts a decimal number to a Eights "-1'2" format number
'Result - String in Tick format

Dim N32nd, N8th As Double, Ticks As Double

    Ticks = dec * 32
    N32nd = Fix(Ticks)
    N8th = Abs(Round((Ticks - N32nd) * 8, 0))
    
    If N32nd = 0 Then
        N32nd = ""
    End If
    
    Select Case N8th
    Case 8
        If N32nd = "" Then
            Dec2Eigths = Sgn(dec)
        Else
            Dec2Eigths = N32nd + Sgn(dec)
        End If
    Case 0
        Dec2Eigths = N32nd
    Case 4
        If (Sgn(dec) = -1) And (N32nd = "") Then
            Dec2Eigths = "-+"
        Else
            Dec2Eigths = N32nd & "+"
        End If
    Case Else
        If (Sgn(dec) = -1) And (N32nd = "") Then
            Dec2Eigths = "-'" & (N8th)
        Else
            Dec2Eigths = N32nd & "'" & (N8th)
        End If
    End Select
End Function


Function Eigths2Dec(Ticks As String) As Double
'Converts Ticks to Decimal assuming a format of "xx'y", while xx is ticks, y quarters
'The string must begin with a number

Dim Char, count As Double, Length As Double, N32nd As Double, N8th As Double, Negative As Double

    Length = Len(Ticks)
    Negative = 1
    
    count = 1
    N32nd = 0
    N8th = 0
    Char = Mid$(Ticks, count, 1)
        
    If Char = "-" Then
        Negative = -1
        count = count + 1
        Char = Mid$(Ticks, count, 1)
    End If
    
    Do Until ((Char = "'" Or Char = "+") Or count = Length + 1)
        N32nd = N32nd * 10 + Char / 32
        count = count + 1
        Char = Mid$(Ticks, count, 1)
    Loop
        
    
    Select Case Char
    Case "+"
        N8th = 1 / 64
    Case "'"
        N8th = Mid$(Ticks, count + 1, 1) / 256
    End Select
    
    Eigths2Dec = (N32nd + N8th) * Negative
End Function
