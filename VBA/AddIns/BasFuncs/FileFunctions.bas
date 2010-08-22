Attribute VB_Name = "FileFunctions"
Option Explicit
Option Base 1

 
Function FileOrDirExists(pathName As String) As Boolean
     'Macro Purpose: Function returns TRUE if the specified file
     '               or folder exists, false if not.
     'PathName     : Supports Windows mapped drives or UNC
     '             : Supports Macintosh paths
     'File usage   : Provide full file path and extension
     'Folder usage : Provide full folder path
     '               Accepts with/without trailing "\" (Windows)
     '               Accepts with/without trailing ":" (Macintosh)
     
    Dim iTemp As Integer
     
     'Ignore errors to allow for error evaluation
    On Error Resume Next
    iTemp = GetAttr(pathName)
     
     'Check if error exists and set response appropriately
    Select Case Err.Number
    Case Is = 0
        FileOrDirExists = True
    Case Else
        FileOrDirExists = False
    End Select
     
     'Resume error checking
    On Error GoTo 0
End Function

 
Function StripFileOrPath(FullPath As String, ReturnType As String) As String
     '   =====================================================================
     '   Returns either the FileName or the Path from a given Full FileName
     '   1st Arg = Pass a files full name (C:\Example\MyFile.xls)
     '   2nd Arg = What to return (either the file name or the path
     '             either "path" or "file"
     '   =====================================================================
    Dim szPathSep As String
    szPathSep = Application.PathSeparator
     
    Dim szCut As String
    szCut = CStr(Empty)
     
    Dim i As Long
    i = Len(FullPath)
     
    Dim szPath As String
    Dim szFile As String
     
    If i > 0 Then
         
        Do While szCut <> szPathSep
             
            szCut = Mid$(FullPath, i, 1)
             
            If szCut = szPathSep Then
                 
                szPath = Left$(FullPath, i)
                szFile = Right$(FullPath, Len(FullPath) - i)
                 
            End If
             
            i = i - 1
        Loop
         
        Select Case UCase(ReturnType)
        Case "PATH"
            StripFileOrPath = szPath
        Case "FILE"
            StripFileOrPath = szFile
        Case Else
            StripFileOrPath = "#Return type must be either FILE or PATH"
        End Select
         
    Else
         
        StripFileOrPath = CStr(Empty)
         
    End If
     
End Function

Function MakeDir(pathName As String) As Boolean

    If FileOrDirExists(pathName) = False Then
        MkDir pathName
    End If
    
End Function
