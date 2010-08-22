Attribute VB_Name = "ReferenceUtils"
Option Explicit
'These functions allow adding and removing references from VBA. In this way you can write applications
'that do not require any addins be pre-installed and can load them all in the Workbook_Open() sub


Function ReferenceAddByFile(referenceFile As String, Optional fromSubversion As Boolean = False) As Variant
'Adds a reference using the referenceFile filename
'If the fromSubversion = TRUE then the path of the referenceFile is assumed to be in subversion

Dim svnDir As String
       
    'If fromSubversion then get make the full filename
    If fromSubversion Then
        svnDir = Environ("Main")            'Get the subversion directory
        If Right$(svnDir, 1) = "/" Then
            If Left$(referenceFile, 1) = "\" Then
                referenceFile = svnDir & Right(referenceFile, Len(referenceFile) - 1)
            Else
                referenceFile = svnDir & referenceFile
            End If
        Else
            If Left$(referenceFile, 1) = "\" Then
                referenceFile = svnDir & referenceFile
            Else
                referenceFile = svnDir & "\" & referenceFile
            End If
        End If
    End If
    
    'Load the reference
    If Dir(referenceFile) <> "" Then
        On Error Resume Next
        Err.Clear
        ThisWorkbook.VBProject.References.AddFromFile (referenceFile)
    
        Select Case Err.Number
        Case Is = 32813
            'Reference already in use nothing to do
            ReferenceAddByFile = True
        Case Is = vbNullString
            'No errors
            ReferenceAddByFile = True
        Case Else
            ReferenceAddByFile = Err.Description
        End Select
        On Error GoTo 0
    Else
        ReferenceAddByFile = "#Error File not found!"
    End If
End Function


Function ReferenceCloseByName(referenceName As String)
'Close a reference by using the name

Dim countReference As Integer

    With ThisWorkbook.VBProject.References
        For countReference = 1 To .Count
            If .Item(countReference).Name = referenceName Then
                .Remove .Item(referenceName)
            End If
        Next
   End With

End Function
