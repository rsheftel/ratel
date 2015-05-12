Attribute VB_Name = "modExport"
Option Explicit

Private Const INTERFACE_NAME As String = "RInterface"

Private Const EVAL As String = "#Evaluating Expression#"
Private Const EVAL_FAIL As String = "#Failed To Evaluate Expression#"
Private Const EVAL_SUCCESS As String = "#Expression Evaluated Successfully#"
Private Const INIT_SUCCESS As String = "#Successfully Connected To R#"
Private Const INIT_FAIL As String = "#Failed To Connect To R#"
Private Const UNINIT_SUCCESS As String = "#Successfully Terminated R Connection#"
Private Const UNINIT_FAIL As String = "#Failed To Disconnect From R#"
Private Const SETSYMBOL As String = "#Setting Symbol#"
Private Const SETSYMBOL_SUCCESS As String = "#Successfully Set Symbol#"
Private Const SETSYMBOL_FAIL As String = "#Failed To Set Symbol#"
Private Const GETSYMBOL As String = "#Getting Symbol#"
Private Const GETSYMBOL_SUCCESS As String = "#Successfully Retrieved Symbol#"
Private Const GETSYMBOL_FAIL As String = "#Failed To Retrieve Symbol#"
Private Const PARAM_PASS_BYVAL As String = "#Converting ByVal Parameter From ANSI To Unicode#"
Private Const PARAM_PASS_BYVAL_SUCCESS As String = "#Successfully Converted Parameter From ANSI To Unicode#"
Private Const PARAM_PASS_BYVAL_FAIL As String = "#Failed To Convert Parameter From ANSI To Unicode#"

Private m_StatConnectorObj As StatConnector

Private Declare Function StrLen Lib "kernel32" Alias "lstrlenA" (ByVal Ptr As Long) As Long
Private Declare Function GetAddrOf Lib "kernel32" Alias "MulDiv" (nNumber As Any, Optional ByVal nNumerator As Long = 1, Optional ByVal nDenominator As Long = 1) As Long

Private Declare Function GetProcessHeap Lib "kernel32" () As Long
Private Declare Function HeapAlloc Lib "kernel32" (ByVal hHeap As Long, ByVal dwFlags As Long, ByVal dwBytes As Long) As Long
Private Declare Function HeapFree Lib "kernel32" (ByVal hHeap As Long, ByVal dwFlags As Long, lpMem As Any) As Long
    
Public Function DllMain(ByVal theDLLHandleLng As Long, ByVal theFunctionReasonLng As Long, ByVal theReservedValLng As Long) As Long
    
    Const DLL_PROCESS_ATTACH As Long = 1
    
    If theFunctionReasonLng = DLL_PROCESS_ATTACH Then
        
        'Initialize the VB Runtime when this DLL is first
        'loaded by a process:
        InitializeRuntime theDLLHandleLng
            
        'Must return TRUE for success:
        DllMain = 1
        
    End If
    
End Function

Public Function R_OpenConnection() As String
    
    On Error GoTo errHandler
    
    Dim PROC_NAME As String: PROC_NAME = "R_OpenConnection"
    
    Dim strStatus As String, strRetVal As String
    
    Set m_StatConnectorObj = New StatConnector
    
    modLogger.OpenLog
    
    m_StatConnectorObj.Init "R"
    
    strRetVal = 1
    strRetVal = StrConv(strRetVal, vbFromUnicode)
    
    R_OpenConnection = strRetVal
                
    WriteLog PROC_NAME, INIT_SUCCESS
    
    Exit Function
    
errHandler:

    strRetVal = 0
    strRetVal = StrConv(strRetVal, vbFromUnicode)
    
    R_OpenConnection = strRetVal
    
    WriteLog PROC_NAME, INIT_FAIL, "Exception Thrown: " & Err.Description
    
    modLogger.CloseLog
    
End Function

Public Function R_Evaluate(ByVal theExpressionStr As String) As String
    
    On Error GoTo errHandler
    
    Dim PROC_NAME As String: PROC_NAME = "R_Evaluate"
    
    Dim strExpression As String, strResult As String, strStatus As String
    Dim bytTmpArr() As Byte
    Dim lngBufferLen As Long
    
    bytTmpArr = theExpressionStr
    lngBufferLen = StrLen(VarPtr(ByVal theExpressionStr))
    
    If PassByVal(strExpression, bytTmpArr, lngBufferLen) Then
        WriteLog PROC_NAME, EVAL, "Expression: " & strExpression
        
        strResult = StrConv(m_StatConnectorObj.Evaluate(strExpression), vbFromUnicode)
        
        R_Evaluate = strResult
        
        WriteLog PROC_NAME, strResult, "Result: " & strResult
    Else
        GoTo errHandler
    End If
    
    Exit Function
    
errHandler:
    
    R_Evaluate = StrConv("#ERR#", vbFromUnicode)

    WriteLog PROC_NAME, strResult, "Exception Thrown: " & Err.Description
    modLogger.CloseLog
    
End Function

Public Function R_EvaluateNoReturn(ByVal theExpressionStr As String) As String
    
    On Error GoTo errHandler
    
    Dim PROC_NAME As String: PROC_NAME = "R_EvaluateNoReturn"
    
    Dim strExpression As String, strResult As String, strStatus As String
    Dim bytTmpArr() As Byte
    Dim lngBufferLen As Long, strRetVal As String
    
    bytTmpArr = theExpressionStr
    lngBufferLen = StrLen(VarPtr(ByVal theExpressionStr))
    
    If PassByVal(strExpression, bytTmpArr, lngBufferLen) Then
        WriteLog PROC_NAME, EVAL, "Expression: " & strExpression
        
        m_StatConnectorObj.Evaluate strExpression
        
        strRetVal = 1
        strRetVal = StrConv(strRetVal, vbFromUnicode)
        
        R_EvaluateNoReturn = strRetVal
        
        WriteLog PROC_NAME, EVAL_SUCCESS, "Result: " & strResult
    Else
        GoTo errHandler
    End If
    
    Exit Function
    
errHandler:
    
    R_EvaluateNoReturn = StrConv("#ERR#", vbFromUnicode)

    WriteLog PROC_NAME, EVAL_FAIL, "Exception Thrown: " & Err.Description
    modLogger.CloseLog
    
End Function

Public Function R_SetSymbol(ByVal theSymbolNameStr As String, ByRef theSymbolDataStr As String) As String
    
    On Error GoTo errHandler
    
    Dim PROC_NAME As String: PROC_NAME = "R_SetSymbol"
    
    Dim strSymbolName As String, strSymbolData As String, strRetVal As String
    Dim bytNameArr() As Byte, bytDataArr(0 To 10000) As Byte
    Dim varSymbolData As Variant
    Dim lngBufferLen As Long, lngdataptr As Long
    
    bytNameArr = theSymbolNameStr
    lngBufferLen = StrLen(VarPtr(ByVal theSymbolNameStr))
    
    If PassByVal(strSymbolName, bytNameArr, lngBufferLen) Then
        WriteLog PROC_NAME, SETSYMBOL, "Symbol Name: " & strSymbolName
        
        CopyMemory lngdataptr, VarPtr(theSymbolDataStr), 4
        CopyMemory bytDataArr(0), ByVal lngdataptr, StrLen(lngdataptr)
        
        lngBufferLen = StrLen(VarPtr(bytDataArr(0)))
        
        If PassByVal(strSymbolData, bytDataArr, lngBufferLen) Then
            varSymbolData = strSymbolData
            
            m_StatConnectorObj.SETSYMBOL strSymbolName, varSymbolData
            
            strRetVal = 1
            strRetVal = StrConv(strRetVal, vbFromUnicode)
            
            R_SetSymbol = strRetVal
            
            WriteLog PROC_NAME, SETSYMBOL_SUCCESS, "Symbol: " & strSymbolName
        Else
            GoTo errHandler
        End If
    Else
        GoTo errHandler
    End If
    
    Exit Function
    
errHandler:
    
    R_SetSymbol = StrConv("#ERR#", vbFromUnicode)
    
    WriteLog PROC_NAME, SETSYMBOL_FAIL, "Exception Thrown: " & Err.Description
    modLogger.CloseLog
    
End Function

Public Function R_GetSymbol(ByVal theSymbolNameStr As String) As String
    
    On Error GoTo errHandler
    
    Dim PROC_NAME As String: PROC_NAME = "R_GetSymbol"
    
    Dim strSymbolName As String, strSymbolData As String
    Dim varSymbolData As Variant
    Dim bytNameArr() As Byte
    Dim lngBufferLen As Long
    
    bytNameArr = theSymbolNameStr
    lngBufferLen = StrLen(VarPtr(ByVal theSymbolNameStr))
    
    If PassByVal(strSymbolName, bytNameArr, lngBufferLen) Then
        WriteLog PROC_NAME, GETSYMBOL, "Symbol Name: " & strSymbolName
        
        varSymbolData = m_StatConnectorObj.GETSYMBOL(strSymbolName)
        
        strSymbolData = varSymbolData
        
        R_GetSymbol = StrConv(strSymbolData, vbFromUnicode)
        
        WriteLog PROC_NAME, GETSYMBOL_SUCCESS, "Symbol Name: " & strSymbolName
    Else
        GoTo errHandler
    End If
    
    Exit Function
    
errHandler:
                    
    R_GetSymbol = StrConv("#ERR#", vbFromUnicode)
    
    WriteLog PROC_NAME, GETSYMBOL_FAIL, "Exception Thrown: " & Err.Description
    CloseLog
    
End Function

Public Function R_CloseConnection() As Long

    On Error GoTo errHandler

    Dim PROC_NAME As String: PROC_NAME = "R_CloseConnection"
    
    Dim strRetVal As String
    
    m_StatConnectorObj.Close

    Set m_StatConnectorObj = Nothing
    
    strRetVal = 1
    strRetVal = StrConv(strRetVal, vbFromUnicode)
    
    R_CloseConnection = strRetVal
    
    WriteLog PROC_NAME, UNINIT_SUCCESS
    
    Exit Function

errHandler:

    WriteLog PROC_NAME, UNINIT_FAIL, Err.Description
                  
    strRetVal = 0
    strRetVal = StrConv(strRetVal, vbFromUnicode)
    
    R_CloseConnection = strRetVal

End Function

Private Function PassByVal(ByRef theDestStr As String, ByRef theSourceBytArr() As Byte, ByVal theBufferLenLng As Long) As Byte
    
    On Error GoTo errHandler
    
    Dim PROC_NAME As String: PROC_NAME = "PassByVal"
    
    Dim strVal As String, strTmp As String
    Dim bytArr() As Byte
    Dim i As Long
    
    strTmp = StrConv(theSourceBytArr, vbUnicode)
    
    If InStr(1, strTmp, vbNullChar, vbBinaryCompare) Then
        strTmp = Left$(strTmp, InStr(1, strTmp, vbNullChar, vbBinaryCompare) - 1)
    End If
    
    WriteLog PROC_NAME, PARAM_PASS_BYVAL, "Parameter Name: " & strTmp
    
    ReDim bytArr(UBound(theSourceBytArr))

    For i = 0 To theBufferLenLng - 1
        bytArr(i) = theSourceBytArr(i)
    Next

    strVal = StrConv(bytArr, vbUnicode)

    If InStr(1, strVal, vbNullChar, vbBinaryCompare) Then
        theDestStr = Left$(strVal, InStr(1, strVal, vbNullChar, vbBinaryCompare) - 1)
    Else
        theDestStr = strVal
    End If

    WriteLog PROC_NAME, PARAM_PASS_BYVAL_SUCCESS, "Parameter Name: " & strVal
    
    PassByVal = 1
    
    Exit Function
    
errHandler:
      
    PassByVal = 0
    
    WriteLog PROC_NAME, PARAM_PASS_BYVAL_FAIL, Err.Description
    
End Function

Private Sub WriteLog(ByVal theMethodStr As String, ByVal theMessageStr As String, Optional ByVal theExtendedMessageStr As String)
    
    Dim strMessage As String
    
    strMessage = "Source: " & INTERFACE_NAME & vbCrLf & _
                 "Method: " & theMethodStr & vbCrLf & _
                 "Message: " & theMessageStr & vbCrLf & _
                 theExtendedMessageStr
                 
    modLogger.WriteLogEntry strMessage
    
End Sub
