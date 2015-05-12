Attribute VB_Name = "modLogger"
Private Const LOG_FILE As String = "C:\TS_DCOMLog.txt"

Private m_FileHandleLng As Long

Public Sub OpenLog()
    
    m_FileHandleLng = FreeFile
    
    Open LOG_FILE For Output As #m_FileHandleLng
    
    Print #m_FileHandleLng, vbNullString
    Print #m_FileHandleLng, "Date: " & Date
    Print #m_FileHandleLng, "Time: " & Time
    Print #m_FileHandleLng, "########################################################"
    Print #m_FileHandleLng, "Log Opened..."
    Print #m_FileHandleLng, "########################################################"
    Print #m_FileHandleLng, vbNullString
    
End Sub

Public Sub WriteLogEntry(ByVal theEntryStr As String)
    
    Print #m_FileHandleLng, vbNullString
    Print #m_FileHandleLng, "Date: " & Date
    Print #m_FileHandleLng, "Time: " & Time
    Print #m_FileHandleLng, "########################################################"
    Print #m_FileHandleLng, theEntryStr
    Print #m_FileHandleLng, "########################################################"
    Print #m_FileHandleLng, vbNullString
    
End Sub

Public Sub CloseLog()
    
    Print #m_FileHandleLng, vbNullString
    Print #m_FileHandleLng, "Date: " & Date
    Print #m_FileHandleLng, "Time: " & Time
    Print #m_FileHandleLng, "########################################################"
    Print #m_FileHandleLng, "Log Closed..."
    Print #m_FileHandleLng, "########################################################"
    Print #m_FileHandleLng, vbNullString
    
    Close #m_FileHandleLng
    
End Sub
