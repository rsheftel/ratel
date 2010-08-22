VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "FormFilter"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
Option Compare Database
Option Explicit

Private dFilters As Scripting.Dictionary
Private thisForm As Form

Private Sub Class_Initialize()
    Set dFilters = New Scripting.Dictionary
End Sub

Public Sub setForm(ByRef thisForm As Object)
'This does not work, learn how to set a form object and hold it

    Set thisForm = thisForm
End Sub

Private Sub Class_Terminate()
    Set dFilters = Nothing
End Sub

Public Function setFilterItem(fieldName As String, filterString As Variant)

    Me.removeFilterItem (fieldName)
    If Not (IsNull(filterString)) Then
        dFilters.Add fieldName, filterString
    End If
    
End Function

Public Function removeFilterItem(fieldName As String)
    
    If dFilters.Exists(fieldName) Then
        dFilters.Remove fieldName
    End If

End Function

Public Function filterOn(this As Object)
Dim keyCount As Integer, keys As Variant, items As Variant
Dim filterString As String

    filterString = ""
    keys = dFilters.keys
    items = dFilters.items
    For keyCount = 0 To dFilters.Count - 1
        If keyCount >= 1 Then
            filterString = filterString & " AND "
        End If
        filterString = filterString & keys(keyCount) & " LIKE """ & dFilters.Item(keys(keyCount)) & """"
    Next
    With this.Parent
        .filter = filterString
        .filterOn = True
    End With

End Function

Public Function filterOff(this As Object)

    this.Parent.filterOn = False
End Function

Public Function updateFilter(this As Object, fieldName As String, filterString As Variant)

    Me.setFilterItem fieldName, filterString
    If dFilters.Count > 0 Then
        Me.filterOn (this)
    Else
        Me.filterOff (this)
    End If
End Function

Public Function updateFilterFromActiveControl(this As Object, fieldName As String)

    Me.updateFilter this, fieldName, this.ActiveControl.Value

End Function

