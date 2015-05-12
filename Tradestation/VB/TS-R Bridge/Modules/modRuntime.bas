Attribute VB_Name = "modRuntime"
Option Explicit

Private Const DELAGATE_ASM_DBL As Currency = -368956918007638.6215@

Private Type typ_DelegatorVTables
    itsVTableLng(7) As Long
End Type

Public Type typ_FunctionDelegator
    itsVTablePtr As Long
    itsFunctionPtr As Long
End Type

Private m_udtVTables As typ_DelegatorVTables

Private m_DelegateAsmDbl As Currency
Private m_VTableSuccessValPtr As Long
Private m_VTableFailValPtr As Long

'Object reference which keeps runtime alive:
Private m_InitObj As clsRuntime


Public Sub InitializeRuntime(ByVal theModuleHandleLng As Long)
    
    Dim ITypeLibObj As ITypeLib
    Dim ITypeInfoObj As ITypeInfo
    Dim ICallDLLClassObj As ICallDLLGetClassObject
    Dim IClassFactoryObj As IClassFactory
    Dim IUnknownObj As IUnknown
    Dim udtTypeAttrib As TYPEATTR
    Dim udtClassFactory As VBGUID, udtIUnknown As VBGUID
    Dim udtFunctionDelegator As typ_FunctionDelegator
    Dim strFile As String, strName As String
    Dim lngLen As Long, lngRetVal As Long, i As Long, lngTypeLibPtr As Long, lngTypeInfoPtr As Long, lngAttribPtr As Long, lngClassAddressPtr As Long

    'Make sure parent process is not VB IDE:
    If GetModuleHandle("VBA6.DLL") <> 0 Then Exit Sub
    If GetModuleHandle("VBA5.DLL") <> 0 Then Exit Sub
    
    strFile = Space$(260)
    lngLen = Len(strFile)
    lngRetVal = GetModuleFileName(theModuleHandleLng, strFile, lngLen)
    
    If lngRetVal Then
        strFile = Left$(strFile, lngLen - 1)
        
        lngTypeLibPtr = LoadTypeLibEx(strFile, REGKIND_NONE)
        
        CopyMemory ITypeLibObj, lngTypeLibPtr, 4
        
        For i = 0 To ITypeLibObj.GetTypeInfoCount - 1
            If ITypeLibObj.GetTypeInfoType(i) = TKIND_COCLASS Then
                lngTypeInfoPtr = ITypeLibObj.GetTypeInfo(i)
                
                CopyMemory ITypeInfoObj, lngTypeInfoPtr, 4
                
                ITypeInfoObj.GetDocumentation DISPID_UNKNOWN, strName, "", 0, ""
                
                If lstrcmp(strName, "clsRuntime") = 0 Then
                    lngAttribPtr = ITypeInfoObj.GetTypeAttr
                    
                    CopyMemory udtTypeAttrib, ByVal lngAttribPtr, Len(udtTypeAttrib)
                    
                    ITypeInfoObj.ReleaseTypeAttr lngAttribPtr
                    
                    If udtTypeAttrib.wTypeFlags Then
                        Exit For
                    End If
                End If
            End If
        Next
        
        With udtClassFactory
            .Data1 = 1
            .Data4(0) = &HC0
            .Data4(7) = &H46
        End With
        
        With udtIUnknown
            .Data4(0) = &HC0
            .Data4(7) = &H46
        End With
        
        lngClassAddressPtr = GetProcAddress(theModuleHandleLng, "DllGetClassObject")
        
        If lngClassAddressPtr Then
            CopyMemory ICallDLLClassObj, InitializeDelegator(udtFunctionDelegator, lngClassAddressPtr), 4
            
            lngRetVal = ICallDLLClassObj.Call(udtTypeAttrib.iid, udtClassFactory, IClassFactoryObj)
            
            If lngRetVal <> CLASS_E_CLASSNOTAVAILABLE Then
                lngRetVal = IClassFactoryObj.CreateInstance(0&, udtIUnknown, IUnknownObj)
                
                If lngRetVal = S_OK Then
                    Set m_InitObj = IUnknownObj
                        m_InitObj.InitVBCall
                    
                        CopyMemory ICallDLLClassObj, 0&, 4
                    Set IClassFactoryObj = Nothing
                    Set IUnknownObj = Nothing
                End If
            End If
        End If
    End If
    
End Sub

Public Function InitializeDelegator(theDelegatorUdt As typ_FunctionDelegator, Optional ByVal theFunctionPtrLng As Long) As IUnknown
    
    If m_VTableSuccessValPtr = 0 Then InitializeVTables
    
    With theDelegatorUdt
        .itsVTablePtr = m_VTableSuccessValPtr
        .itsFunctionPtr = theFunctionPtrLng
    End With
    
    CopyMemory InitializeDelegator, VarPtr(theDelegatorUdt), 4
    
End Function

Private Sub InitializeVTables()

    Dim lngAddrRefReleasePtr As Long
    
    With m_udtVTables
        .itsVTableLng(0) = GetFunctionAddress(AddressOf QueryInterfaceSuccess)
        .itsVTableLng(4) = GetFunctionAddress(AddressOf QueryInterfaceFail)
        
        lngAddrRefReleasePtr = GetFunctionAddress(AddressOf AddRefRelease)
        
        .itsVTableLng(1) = lngAddrRefReleasePtr
        .itsVTableLng(5) = lngAddrRefReleasePtr
        .itsVTableLng(2) = lngAddrRefReleasePtr
        .itsVTableLng(6) = lngAddrRefReleasePtr
        
        m_DelegateAsmDbl = DELAGATE_ASM_DBL
        
        .itsVTableLng(3) = VarPtr(m_DelegateAsmDbl)
        .itsVTableLng(7) = .itsVTableLng(3)
        
        m_VTableSuccessValPtr = VarPtr(.itsVTableLng(0))
        m_VTableFailValPtr = VarPtr(.itsVTableLng(4))
    End With
    
End Sub

Private Function QueryInterfaceSuccess(ByRef theFunctionDelegatorUdt As typ_FunctionDelegator, ByRef theReferenceIDLng As Long, ByRef theVTableObjPtr As Long) As Long
    
    theVTableObjPtr = VarPtr(theFunctionDelegatorUdt)
    
    theFunctionDelegatorUdt.itsVTablePtr = m_VTableFailValPtr
    
End Function

Private Function AddRefRelease(ByVal theReferenceReleaseLng As Long) As Long
End Function

Private Function QueryInterfaceFail(ByVal theThisPtrLng As Long, ByRef theReferenceIDLng As Long, ByRef theVTableObjPtr As Long) As Long
    
    theVTableObjPtr = 0
    QueryInterfaceFail = E_NOINTERFACE
    
End Function

Private Function GetFunctionAddress(ByVal theFunctionPtr As Long) As Long

    GetFunctionAddress = theFunctionPtr
    
End Function
