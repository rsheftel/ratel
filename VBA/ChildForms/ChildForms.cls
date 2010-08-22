VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "ChildForms"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
Option Compare Database
Option Explicit
Option Base 1

Private Type ChildType
    formName As String
    indexField As String
    tableSource As String
End Type

Private children() As ChildType
Private childrenCount As Integer
Private parentIndexFieldName As String

Private Sub Class_Initialize()
    childrenCount = 0
    parentIndexFieldName = ""
End Sub

Public Function setParentIndexFieldName(fieldName As String)
    parentIndexFieldName = fieldName
End Function

Public Function addChild(formName As Variant, indexField As Variant, tableSource As Variant)

    childrenCount = childrenCount + 1
    ReDim Preserve children(childrenCount)
    children(childrenCount).formName = formName
    children(childrenCount).indexField = indexField
    children(childrenCount).tableSource = tableSource
    
End Function


Public Function updateChildren(this As Object)
Dim child As ChildType, childCount As Integer
Dim sqlstr As String
    
    For childCount = 1 To UBound(children)
        child = children(childCount)
        sqlstr = "SELECT dbo." & child.tableSource & ".* " & _
                    "FROM dbo." & child.tableSource & " " & _
                    "WHERE " & child.indexField & " LIKE '" & this(parentIndexFieldName) & "'"
                    
        If CurrentProject.AllForms(children(childCount).formName).IsLoaded = True Then
            Forms(children(childCount).formName).RecordSource = sqlstr
        End If
    Next

End Function
