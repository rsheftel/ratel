

/* this ALWAYS GENERATED file contains the definitions for the interfaces */


 /* File created by MIDL compiler version 6.00.0366 */
/* at Wed Oct 24 15:22:36 2007
 */
/* Compiler settings for .\tsTestSupplier.idl:
    Oicf, W1, Zp8, env=Win32 (32b run)
    protocol : dce , ms_ext, c_ext, robust
    error checks: allocation ref bounds_check enum stub_data 
    VC __declspec() decoration level: 
         __declspec(uuid()), __declspec(selectany), __declspec(novtable)
         DECLSPEC_UUID(), MIDL_INTERFACE()
*/
//@@MIDL_FILE_HEADING(  )

#pragma warning( disable: 4049 )  /* more than 64k source lines */


/* verify that the <rpcndr.h> version is high enough to compile this file*/
#ifndef __REQUIRED_RPCNDR_H_VERSION__
#define __REQUIRED_RPCNDR_H_VERSION__ 475
#endif

#include "rpc.h"
#include "rpcndr.h"

#ifndef __RPCNDR_H_VERSION__
#error this stub requires an updated version of <rpcndr.h>
#endif // __RPCNDR_H_VERSION__

#ifndef COM_NO_WINDOWS_H
#include "windows.h"
#include "ole2.h"
#endif /*COM_NO_WINDOWS_H*/

#ifndef __tsTestSupplier_h__
#define __tsTestSupplier_h__

#if defined(_MSC_VER) && (_MSC_VER >= 1020)
#pragma once
#endif

/* Forward Declarations */ 

#ifndef __ItsSupplier_FWD_DEFINED__
#define __ItsSupplier_FWD_DEFINED__
typedef interface ItsSupplier ItsSupplier;
#endif 	/* __ItsSupplier_FWD_DEFINED__ */


#ifndef __IqmAddingSymbol_FWD_DEFINED__
#define __IqmAddingSymbol_FWD_DEFINED__
typedef interface IqmAddingSymbol IqmAddingSymbol;
#endif 	/* __IqmAddingSymbol_FWD_DEFINED__ */


#ifndef __TestSupplier_FWD_DEFINED__
#define __TestSupplier_FWD_DEFINED__

#ifdef __cplusplus
typedef class TestSupplier TestSupplier;
#else
typedef struct TestSupplier TestSupplier;
#endif /* __cplusplus */

#endif 	/* __TestSupplier_FWD_DEFINED__ */


/* header files for imported files */
#include "oaidl.h"
#include "ocidl.h"

#ifdef __cplusplus
extern "C"{
#endif 

void * __RPC_USER MIDL_user_allocate(size_t);
void __RPC_USER MIDL_user_free( void * ); 

#ifndef __ItsSupplier_INTERFACE_DEFINED__
#define __ItsSupplier_INTERFACE_DEFINED__

/* interface ItsSupplier */
/* [unique][helpstring][dual][uuid][object] */ 


EXTERN_C const IID IID_ItsSupplier;

#if defined(__cplusplus) && !defined(CINTERFACE)
    
    MIDL_INTERFACE("0F228678-C8E4-42b6-98E1-9C2C641F9FA1")
    ItsSupplier : public IDispatch
    {
    public:
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_Name( 
            /* [retval][out] */ BSTR *pVal) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_ShortName( 
            /* [retval][out] */ BSTR *pVal) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_Description( 
            /* [retval][out] */ BSTR *pVal) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_Version( 
            /* [retval][out] */ BSTR *pVal) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_Vendor( 
            /* [retval][out] */ BSTR *pVal) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ShowProperties( 
            long Datafeed) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_ConnectionsNumber( 
            /* [retval][out] */ long *pVal) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_ConnectionName( 
            /* [in] */ long Index,
            /* [retval][out] */ BSTR *pVal) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE AdviseConnectionsStatus( 
            long Datafeed,
            IUnknown *pSink) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE UnadviseConnectionsStatus( 
            long Datafeed,
            IUnknown *pSink) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE IsProvide( 
            long Mode,
            long Symbol,
            long Category,
            long ResolutionSize,
            long Resolution,
            long Field,
            /* [out] */ BOOL *pVal) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE Advise( 
            IUnknown *pSink) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE Unadvise( void) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE AdviseTransaction( 
            long Id,
            long Datafeed,
            long Symbol,
            BSTR SymbolName,
            BSTR Description,
            long Category,
            long ResolutionSize,
            long Resolution,
            long Field,
            long Exchange,
            BSTR ExchangeName,
            long StartDate,
            long StartTime,
            long FinishDate,
            long FinishTime,
            BSTR Sessions,
            BSTR SessionsTimeZoneInformation,
            BSTR ExchangeTimeZoneInformation) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE UnadviseTransaction( 
            long Id) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE GetResolutions( 
            long Mode,
            long DataFeed,
            long Symbol,
            BSTR SymbolName,
            BSTR Description,
            long Category,
            long Exchange,
            BSTR ExchangeName,
            /* [out] */ VARIANT *Answer) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE GetResolutionSizes( 
            long Mode,
            long DataFeed,
            long Symbol,
            BSTR SymbolName,
            BSTR Description,
            long Category,
            long Resolution,
            long Exchange,
            BSTR ExchangeName,
            /* [out] */ VARIANT *Answer) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE GetFields( 
            long Mode,
            long DataFeed,
            long Symbol,
            BSTR SymbolName,
            BSTR Description,
            long Category,
            long ResolutionSize,
            long Resolution,
            long Exchange,
            BSTR ExchangeName,
            /* [out] */ VARIANT *Answer) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_ProvideStatusLine( 
            /* [retval][out] */ long *pVal) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE AdviseStatusLine( 
            long Id,
            long Datafeed,
            long Symbol,
            BSTR SymbolName,
            BSTR Description,
            long Category,
            long ResolutionSize,
            long Resolution,
            long Field,
            long Exchange,
            BSTR ExchangeName,
            BSTR Sessions,
            BSTR SessionsTimeZoneInformation) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE UnadviseStatusLine( 
            long Id) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE IsProvideAddingSymbols( 
            /* [out] */ BOOL *pVal) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE DoAddingSymbols( 
            long Datafeed,
            IDispatch *Portfolio) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE CanSaveToStorage( 
            /* [out] */ BOOL *pVal) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE CanReadFromStorage( 
            /* [out] */ BOOL *pVal) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_IsAscDataFeed( 
            /* [retval][out] */ BOOL *pVal) = 0;
        
    };
    
#else 	/* C style interface */

    typedef struct ItsSupplierVtbl
    {
        BEGIN_INTERFACE
        
        HRESULT ( STDMETHODCALLTYPE *QueryInterface )( 
            ItsSupplier * This,
            /* [in] */ REFIID riid,
            /* [iid_is][out] */ void **ppvObject);
        
        ULONG ( STDMETHODCALLTYPE *AddRef )( 
            ItsSupplier * This);
        
        ULONG ( STDMETHODCALLTYPE *Release )( 
            ItsSupplier * This);
        
        HRESULT ( STDMETHODCALLTYPE *GetTypeInfoCount )( 
            ItsSupplier * This,
            /* [out] */ UINT *pctinfo);
        
        HRESULT ( STDMETHODCALLTYPE *GetTypeInfo )( 
            ItsSupplier * This,
            /* [in] */ UINT iTInfo,
            /* [in] */ LCID lcid,
            /* [out] */ ITypeInfo **ppTInfo);
        
        HRESULT ( STDMETHODCALLTYPE *GetIDsOfNames )( 
            ItsSupplier * This,
            /* [in] */ REFIID riid,
            /* [size_is][in] */ LPOLESTR *rgszNames,
            /* [in] */ UINT cNames,
            /* [in] */ LCID lcid,
            /* [size_is][out] */ DISPID *rgDispId);
        
        /* [local] */ HRESULT ( STDMETHODCALLTYPE *Invoke )( 
            ItsSupplier * This,
            /* [in] */ DISPID dispIdMember,
            /* [in] */ REFIID riid,
            /* [in] */ LCID lcid,
            /* [in] */ WORD wFlags,
            /* [out][in] */ DISPPARAMS *pDispParams,
            /* [out] */ VARIANT *pVarResult,
            /* [out] */ EXCEPINFO *pExcepInfo,
            /* [out] */ UINT *puArgErr);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_Name )( 
            ItsSupplier * This,
            /* [retval][out] */ BSTR *pVal);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_ShortName )( 
            ItsSupplier * This,
            /* [retval][out] */ BSTR *pVal);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_Description )( 
            ItsSupplier * This,
            /* [retval][out] */ BSTR *pVal);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_Version )( 
            ItsSupplier * This,
            /* [retval][out] */ BSTR *pVal);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_Vendor )( 
            ItsSupplier * This,
            /* [retval][out] */ BSTR *pVal);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *ShowProperties )( 
            ItsSupplier * This,
            long Datafeed);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_ConnectionsNumber )( 
            ItsSupplier * This,
            /* [retval][out] */ long *pVal);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_ConnectionName )( 
            ItsSupplier * This,
            /* [in] */ long Index,
            /* [retval][out] */ BSTR *pVal);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *AdviseConnectionsStatus )( 
            ItsSupplier * This,
            long Datafeed,
            IUnknown *pSink);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *UnadviseConnectionsStatus )( 
            ItsSupplier * This,
            long Datafeed,
            IUnknown *pSink);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *IsProvide )( 
            ItsSupplier * This,
            long Mode,
            long Symbol,
            long Category,
            long ResolutionSize,
            long Resolution,
            long Field,
            /* [out] */ BOOL *pVal);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *Advise )( 
            ItsSupplier * This,
            IUnknown *pSink);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *Unadvise )( 
            ItsSupplier * This);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *AdviseTransaction )( 
            ItsSupplier * This,
            long Id,
            long Datafeed,
            long Symbol,
            BSTR SymbolName,
            BSTR Description,
            long Category,
            long ResolutionSize,
            long Resolution,
            long Field,
            long Exchange,
            BSTR ExchangeName,
            long StartDate,
            long StartTime,
            long FinishDate,
            long FinishTime,
            BSTR Sessions,
            BSTR SessionsTimeZoneInformation,
            BSTR ExchangeTimeZoneInformation);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *UnadviseTransaction )( 
            ItsSupplier * This,
            long Id);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *GetResolutions )( 
            ItsSupplier * This,
            long Mode,
            long DataFeed,
            long Symbol,
            BSTR SymbolName,
            BSTR Description,
            long Category,
            long Exchange,
            BSTR ExchangeName,
            /* [out] */ VARIANT *Answer);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *GetResolutionSizes )( 
            ItsSupplier * This,
            long Mode,
            long DataFeed,
            long Symbol,
            BSTR SymbolName,
            BSTR Description,
            long Category,
            long Resolution,
            long Exchange,
            BSTR ExchangeName,
            /* [out] */ VARIANT *Answer);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *GetFields )( 
            ItsSupplier * This,
            long Mode,
            long DataFeed,
            long Symbol,
            BSTR SymbolName,
            BSTR Description,
            long Category,
            long ResolutionSize,
            long Resolution,
            long Exchange,
            BSTR ExchangeName,
            /* [out] */ VARIANT *Answer);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_ProvideStatusLine )( 
            ItsSupplier * This,
            /* [retval][out] */ long *pVal);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *AdviseStatusLine )( 
            ItsSupplier * This,
            long Id,
            long Datafeed,
            long Symbol,
            BSTR SymbolName,
            BSTR Description,
            long Category,
            long ResolutionSize,
            long Resolution,
            long Field,
            long Exchange,
            BSTR ExchangeName,
            BSTR Sessions,
            BSTR SessionsTimeZoneInformation);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *UnadviseStatusLine )( 
            ItsSupplier * This,
            long Id);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *IsProvideAddingSymbols )( 
            ItsSupplier * This,
            /* [out] */ BOOL *pVal);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *DoAddingSymbols )( 
            ItsSupplier * This,
            long Datafeed,
            IDispatch *Portfolio);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *CanSaveToStorage )( 
            ItsSupplier * This,
            /* [out] */ BOOL *pVal);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *CanReadFromStorage )( 
            ItsSupplier * This,
            /* [out] */ BOOL *pVal);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_IsAscDataFeed )( 
            ItsSupplier * This,
            /* [retval][out] */ BOOL *pVal);
        
        END_INTERFACE
    } ItsSupplierVtbl;

    interface ItsSupplier
    {
        CONST_VTBL struct ItsSupplierVtbl *lpVtbl;
    };

    

#ifdef COBJMACROS


#define ItsSupplier_QueryInterface(This,riid,ppvObject)	\
    (This)->lpVtbl -> QueryInterface(This,riid,ppvObject)

#define ItsSupplier_AddRef(This)	\
    (This)->lpVtbl -> AddRef(This)

#define ItsSupplier_Release(This)	\
    (This)->lpVtbl -> Release(This)


#define ItsSupplier_GetTypeInfoCount(This,pctinfo)	\
    (This)->lpVtbl -> GetTypeInfoCount(This,pctinfo)

#define ItsSupplier_GetTypeInfo(This,iTInfo,lcid,ppTInfo)	\
    (This)->lpVtbl -> GetTypeInfo(This,iTInfo,lcid,ppTInfo)

#define ItsSupplier_GetIDsOfNames(This,riid,rgszNames,cNames,lcid,rgDispId)	\
    (This)->lpVtbl -> GetIDsOfNames(This,riid,rgszNames,cNames,lcid,rgDispId)

#define ItsSupplier_Invoke(This,dispIdMember,riid,lcid,wFlags,pDispParams,pVarResult,pExcepInfo,puArgErr)	\
    (This)->lpVtbl -> Invoke(This,dispIdMember,riid,lcid,wFlags,pDispParams,pVarResult,pExcepInfo,puArgErr)


#define ItsSupplier_get_Name(This,pVal)	\
    (This)->lpVtbl -> get_Name(This,pVal)

#define ItsSupplier_get_ShortName(This,pVal)	\
    (This)->lpVtbl -> get_ShortName(This,pVal)

#define ItsSupplier_get_Description(This,pVal)	\
    (This)->lpVtbl -> get_Description(This,pVal)

#define ItsSupplier_get_Version(This,pVal)	\
    (This)->lpVtbl -> get_Version(This,pVal)

#define ItsSupplier_get_Vendor(This,pVal)	\
    (This)->lpVtbl -> get_Vendor(This,pVal)

#define ItsSupplier_ShowProperties(This,Datafeed)	\
    (This)->lpVtbl -> ShowProperties(This,Datafeed)

#define ItsSupplier_get_ConnectionsNumber(This,pVal)	\
    (This)->lpVtbl -> get_ConnectionsNumber(This,pVal)

#define ItsSupplier_get_ConnectionName(This,Index,pVal)	\
    (This)->lpVtbl -> get_ConnectionName(This,Index,pVal)

#define ItsSupplier_AdviseConnectionsStatus(This,Datafeed,pSink)	\
    (This)->lpVtbl -> AdviseConnectionsStatus(This,Datafeed,pSink)

#define ItsSupplier_UnadviseConnectionsStatus(This,Datafeed,pSink)	\
    (This)->lpVtbl -> UnadviseConnectionsStatus(This,Datafeed,pSink)

#define ItsSupplier_IsProvide(This,Mode,Symbol,Category,ResolutionSize,Resolution,Field,pVal)	\
    (This)->lpVtbl -> IsProvide(This,Mode,Symbol,Category,ResolutionSize,Resolution,Field,pVal)

#define ItsSupplier_Advise(This,pSink)	\
    (This)->lpVtbl -> Advise(This,pSink)

#define ItsSupplier_Unadvise(This)	\
    (This)->lpVtbl -> Unadvise(This)

#define ItsSupplier_AdviseTransaction(This,Id,Datafeed,Symbol,SymbolName,Description,Category,ResolutionSize,Resolution,Field,Exchange,ExchangeName,StartDate,StartTime,FinishDate,FinishTime,Sessions,SessionsTimeZoneInformation,ExchangeTimeZoneInformation)	\
    (This)->lpVtbl -> AdviseTransaction(This,Id,Datafeed,Symbol,SymbolName,Description,Category,ResolutionSize,Resolution,Field,Exchange,ExchangeName,StartDate,StartTime,FinishDate,FinishTime,Sessions,SessionsTimeZoneInformation,ExchangeTimeZoneInformation)

#define ItsSupplier_UnadviseTransaction(This,Id)	\
    (This)->lpVtbl -> UnadviseTransaction(This,Id)

#define ItsSupplier_GetResolutions(This,Mode,DataFeed,Symbol,SymbolName,Description,Category,Exchange,ExchangeName,Answer)	\
    (This)->lpVtbl -> GetResolutions(This,Mode,DataFeed,Symbol,SymbolName,Description,Category,Exchange,ExchangeName,Answer)

#define ItsSupplier_GetResolutionSizes(This,Mode,DataFeed,Symbol,SymbolName,Description,Category,Resolution,Exchange,ExchangeName,Answer)	\
    (This)->lpVtbl -> GetResolutionSizes(This,Mode,DataFeed,Symbol,SymbolName,Description,Category,Resolution,Exchange,ExchangeName,Answer)

#define ItsSupplier_GetFields(This,Mode,DataFeed,Symbol,SymbolName,Description,Category,ResolutionSize,Resolution,Exchange,ExchangeName,Answer)	\
    (This)->lpVtbl -> GetFields(This,Mode,DataFeed,Symbol,SymbolName,Description,Category,ResolutionSize,Resolution,Exchange,ExchangeName,Answer)

#define ItsSupplier_get_ProvideStatusLine(This,pVal)	\
    (This)->lpVtbl -> get_ProvideStatusLine(This,pVal)

#define ItsSupplier_AdviseStatusLine(This,Id,Datafeed,Symbol,SymbolName,Description,Category,ResolutionSize,Resolution,Field,Exchange,ExchangeName,Sessions,SessionsTimeZoneInformation)	\
    (This)->lpVtbl -> AdviseStatusLine(This,Id,Datafeed,Symbol,SymbolName,Description,Category,ResolutionSize,Resolution,Field,Exchange,ExchangeName,Sessions,SessionsTimeZoneInformation)

#define ItsSupplier_UnadviseStatusLine(This,Id)	\
    (This)->lpVtbl -> UnadviseStatusLine(This,Id)

#define ItsSupplier_IsProvideAddingSymbols(This,pVal)	\
    (This)->lpVtbl -> IsProvideAddingSymbols(This,pVal)

#define ItsSupplier_DoAddingSymbols(This,Datafeed,Portfolio)	\
    (This)->lpVtbl -> DoAddingSymbols(This,Datafeed,Portfolio)

#define ItsSupplier_CanSaveToStorage(This,pVal)	\
    (This)->lpVtbl -> CanSaveToStorage(This,pVal)

#define ItsSupplier_CanReadFromStorage(This,pVal)	\
    (This)->lpVtbl -> CanReadFromStorage(This,pVal)

#define ItsSupplier_get_IsAscDataFeed(This,pVal)	\
    (This)->lpVtbl -> get_IsAscDataFeed(This,pVal)

#endif /* COBJMACROS */


#endif 	/* C style interface */



/* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE ItsSupplier_get_Name_Proxy( 
    ItsSupplier * This,
    /* [retval][out] */ BSTR *pVal);


void __RPC_STUB ItsSupplier_get_Name_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE ItsSupplier_get_ShortName_Proxy( 
    ItsSupplier * This,
    /* [retval][out] */ BSTR *pVal);


void __RPC_STUB ItsSupplier_get_ShortName_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE ItsSupplier_get_Description_Proxy( 
    ItsSupplier * This,
    /* [retval][out] */ BSTR *pVal);


void __RPC_STUB ItsSupplier_get_Description_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE ItsSupplier_get_Version_Proxy( 
    ItsSupplier * This,
    /* [retval][out] */ BSTR *pVal);


void __RPC_STUB ItsSupplier_get_Version_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE ItsSupplier_get_Vendor_Proxy( 
    ItsSupplier * This,
    /* [retval][out] */ BSTR *pVal);


void __RPC_STUB ItsSupplier_get_Vendor_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_ShowProperties_Proxy( 
    ItsSupplier * This,
    long Datafeed);


void __RPC_STUB ItsSupplier_ShowProperties_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE ItsSupplier_get_ConnectionsNumber_Proxy( 
    ItsSupplier * This,
    /* [retval][out] */ long *pVal);


void __RPC_STUB ItsSupplier_get_ConnectionsNumber_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE ItsSupplier_get_ConnectionName_Proxy( 
    ItsSupplier * This,
    /* [in] */ long Index,
    /* [retval][out] */ BSTR *pVal);


void __RPC_STUB ItsSupplier_get_ConnectionName_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_AdviseConnectionsStatus_Proxy( 
    ItsSupplier * This,
    long Datafeed,
    IUnknown *pSink);


void __RPC_STUB ItsSupplier_AdviseConnectionsStatus_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_UnadviseConnectionsStatus_Proxy( 
    ItsSupplier * This,
    long Datafeed,
    IUnknown *pSink);


void __RPC_STUB ItsSupplier_UnadviseConnectionsStatus_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_IsProvide_Proxy( 
    ItsSupplier * This,
    long Mode,
    long Symbol,
    long Category,
    long ResolutionSize,
    long Resolution,
    long Field,
    /* [out] */ BOOL *pVal);


void __RPC_STUB ItsSupplier_IsProvide_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_Advise_Proxy( 
    ItsSupplier * This,
    IUnknown *pSink);


void __RPC_STUB ItsSupplier_Advise_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_Unadvise_Proxy( 
    ItsSupplier * This);


void __RPC_STUB ItsSupplier_Unadvise_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_AdviseTransaction_Proxy( 
    ItsSupplier * This,
    long Id,
    long Datafeed,
    long Symbol,
    BSTR SymbolName,
    BSTR Description,
    long Category,
    long ResolutionSize,
    long Resolution,
    long Field,
    long Exchange,
    BSTR ExchangeName,
    long StartDate,
    long StartTime,
    long FinishDate,
    long FinishTime,
    BSTR Sessions,
    BSTR SessionsTimeZoneInformation,
    BSTR ExchangeTimeZoneInformation);


void __RPC_STUB ItsSupplier_AdviseTransaction_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_UnadviseTransaction_Proxy( 
    ItsSupplier * This,
    long Id);


void __RPC_STUB ItsSupplier_UnadviseTransaction_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_GetResolutions_Proxy( 
    ItsSupplier * This,
    long Mode,
    long DataFeed,
    long Symbol,
    BSTR SymbolName,
    BSTR Description,
    long Category,
    long Exchange,
    BSTR ExchangeName,
    /* [out] */ VARIANT *Answer);


void __RPC_STUB ItsSupplier_GetResolutions_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_GetResolutionSizes_Proxy( 
    ItsSupplier * This,
    long Mode,
    long DataFeed,
    long Symbol,
    BSTR SymbolName,
    BSTR Description,
    long Category,
    long Resolution,
    long Exchange,
    BSTR ExchangeName,
    /* [out] */ VARIANT *Answer);


void __RPC_STUB ItsSupplier_GetResolutionSizes_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_GetFields_Proxy( 
    ItsSupplier * This,
    long Mode,
    long DataFeed,
    long Symbol,
    BSTR SymbolName,
    BSTR Description,
    long Category,
    long ResolutionSize,
    long Resolution,
    long Exchange,
    BSTR ExchangeName,
    /* [out] */ VARIANT *Answer);


void __RPC_STUB ItsSupplier_GetFields_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE ItsSupplier_get_ProvideStatusLine_Proxy( 
    ItsSupplier * This,
    /* [retval][out] */ long *pVal);


void __RPC_STUB ItsSupplier_get_ProvideStatusLine_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_AdviseStatusLine_Proxy( 
    ItsSupplier * This,
    long Id,
    long Datafeed,
    long Symbol,
    BSTR SymbolName,
    BSTR Description,
    long Category,
    long ResolutionSize,
    long Resolution,
    long Field,
    long Exchange,
    BSTR ExchangeName,
    BSTR Sessions,
    BSTR SessionsTimeZoneInformation);


void __RPC_STUB ItsSupplier_AdviseStatusLine_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_UnadviseStatusLine_Proxy( 
    ItsSupplier * This,
    long Id);


void __RPC_STUB ItsSupplier_UnadviseStatusLine_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_IsProvideAddingSymbols_Proxy( 
    ItsSupplier * This,
    /* [out] */ BOOL *pVal);


void __RPC_STUB ItsSupplier_IsProvideAddingSymbols_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_DoAddingSymbols_Proxy( 
    ItsSupplier * This,
    long Datafeed,
    IDispatch *Portfolio);


void __RPC_STUB ItsSupplier_DoAddingSymbols_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_CanSaveToStorage_Proxy( 
    ItsSupplier * This,
    /* [out] */ BOOL *pVal);


void __RPC_STUB ItsSupplier_CanSaveToStorage_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE ItsSupplier_CanReadFromStorage_Proxy( 
    ItsSupplier * This,
    /* [out] */ BOOL *pVal);


void __RPC_STUB ItsSupplier_CanReadFromStorage_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);


/* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE ItsSupplier_get_IsAscDataFeed_Proxy( 
    ItsSupplier * This,
    /* [retval][out] */ BOOL *pVal);


void __RPC_STUB ItsSupplier_get_IsAscDataFeed_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);



#endif 	/* __ItsSupplier_INTERFACE_DEFINED__ */


#ifndef __IqmAddingSymbol_INTERFACE_DEFINED__
#define __IqmAddingSymbol_INTERFACE_DEFINED__

/* interface IqmAddingSymbol */
/* [unique][helpstring][dual][uuid][object] */ 


EXTERN_C const IID IID_IqmAddingSymbol;

#if defined(__cplusplus) && !defined(CINTERFACE)
    
    MIDL_INTERFACE("D102088D-7CBC-42b5-93C8-2476D54DE8BD")
    IqmAddingSymbol : public IDispatch
    {
    public:
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE AddSymbol( 
            BSTR SymbolName,
            BSTR SymbolRoot,
            BSTR Description,
            BSTR CUSIP,
            long Category,
            long DataFeed,
            BSTR ExchangeName,
            long ContractMonth,
            long ContractYear,
            double StrikePrice,
            long CallPut,
            long Margin,
            long ExpirationDate,
            long Expired,
            long ExpirationRule,
            long FirstNoticeDate,
            long Delivery,
            long *pVal) = 0;
        
    };
    
#else 	/* C style interface */

    typedef struct IqmAddingSymbolVtbl
    {
        BEGIN_INTERFACE
        
        HRESULT ( STDMETHODCALLTYPE *QueryInterface )( 
            IqmAddingSymbol * This,
            /* [in] */ REFIID riid,
            /* [iid_is][out] */ void **ppvObject);
        
        ULONG ( STDMETHODCALLTYPE *AddRef )( 
            IqmAddingSymbol * This);
        
        ULONG ( STDMETHODCALLTYPE *Release )( 
            IqmAddingSymbol * This);
        
        HRESULT ( STDMETHODCALLTYPE *GetTypeInfoCount )( 
            IqmAddingSymbol * This,
            /* [out] */ UINT *pctinfo);
        
        HRESULT ( STDMETHODCALLTYPE *GetTypeInfo )( 
            IqmAddingSymbol * This,
            /* [in] */ UINT iTInfo,
            /* [in] */ LCID lcid,
            /* [out] */ ITypeInfo **ppTInfo);
        
        HRESULT ( STDMETHODCALLTYPE *GetIDsOfNames )( 
            IqmAddingSymbol * This,
            /* [in] */ REFIID riid,
            /* [size_is][in] */ LPOLESTR *rgszNames,
            /* [in] */ UINT cNames,
            /* [in] */ LCID lcid,
            /* [size_is][out] */ DISPID *rgDispId);
        
        /* [local] */ HRESULT ( STDMETHODCALLTYPE *Invoke )( 
            IqmAddingSymbol * This,
            /* [in] */ DISPID dispIdMember,
            /* [in] */ REFIID riid,
            /* [in] */ LCID lcid,
            /* [in] */ WORD wFlags,
            /* [out][in] */ DISPPARAMS *pDispParams,
            /* [out] */ VARIANT *pVarResult,
            /* [out] */ EXCEPINFO *pExcepInfo,
            /* [out] */ UINT *puArgErr);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *AddSymbol )( 
            IqmAddingSymbol * This,
            BSTR SymbolName,
            BSTR SymbolRoot,
            BSTR Description,
            BSTR CUSIP,
            long Category,
            long DataFeed,
            BSTR ExchangeName,
            long ContractMonth,
            long ContractYear,
            double StrikePrice,
            long CallPut,
            long Margin,
            long ExpirationDate,
            long Expired,
            long ExpirationRule,
            long FirstNoticeDate,
            long Delivery,
            long *pVal);
        
        END_INTERFACE
    } IqmAddingSymbolVtbl;

    interface IqmAddingSymbol
    {
        CONST_VTBL struct IqmAddingSymbolVtbl *lpVtbl;
    };

    

#ifdef COBJMACROS


#define IqmAddingSymbol_QueryInterface(This,riid,ppvObject)	\
    (This)->lpVtbl -> QueryInterface(This,riid,ppvObject)

#define IqmAddingSymbol_AddRef(This)	\
    (This)->lpVtbl -> AddRef(This)

#define IqmAddingSymbol_Release(This)	\
    (This)->lpVtbl -> Release(This)


#define IqmAddingSymbol_GetTypeInfoCount(This,pctinfo)	\
    (This)->lpVtbl -> GetTypeInfoCount(This,pctinfo)

#define IqmAddingSymbol_GetTypeInfo(This,iTInfo,lcid,ppTInfo)	\
    (This)->lpVtbl -> GetTypeInfo(This,iTInfo,lcid,ppTInfo)

#define IqmAddingSymbol_GetIDsOfNames(This,riid,rgszNames,cNames,lcid,rgDispId)	\
    (This)->lpVtbl -> GetIDsOfNames(This,riid,rgszNames,cNames,lcid,rgDispId)

#define IqmAddingSymbol_Invoke(This,dispIdMember,riid,lcid,wFlags,pDispParams,pVarResult,pExcepInfo,puArgErr)	\
    (This)->lpVtbl -> Invoke(This,dispIdMember,riid,lcid,wFlags,pDispParams,pVarResult,pExcepInfo,puArgErr)


#define IqmAddingSymbol_AddSymbol(This,SymbolName,SymbolRoot,Description,CUSIP,Category,DataFeed,ExchangeName,ContractMonth,ContractYear,StrikePrice,CallPut,Margin,ExpirationDate,Expired,ExpirationRule,FirstNoticeDate,Delivery,pVal)	\
    (This)->lpVtbl -> AddSymbol(This,SymbolName,SymbolRoot,Description,CUSIP,Category,DataFeed,ExchangeName,ContractMonth,ContractYear,StrikePrice,CallPut,Margin,ExpirationDate,Expired,ExpirationRule,FirstNoticeDate,Delivery,pVal)

#endif /* COBJMACROS */


#endif 	/* C style interface */



/* [helpstring][id] */ HRESULT STDMETHODCALLTYPE IqmAddingSymbol_AddSymbol_Proxy( 
    IqmAddingSymbol * This,
    BSTR SymbolName,
    BSTR SymbolRoot,
    BSTR Description,
    BSTR CUSIP,
    long Category,
    long DataFeed,
    BSTR ExchangeName,
    long ContractMonth,
    long ContractYear,
    double StrikePrice,
    long CallPut,
    long Margin,
    long ExpirationDate,
    long Expired,
    long ExpirationRule,
    long FirstNoticeDate,
    long Delivery,
    long *pVal);


void __RPC_STUB IqmAddingSymbol_AddSymbol_Stub(
    IRpcStubBuffer *This,
    IRpcChannelBuffer *_pRpcChannelBuffer,
    PRPC_MESSAGE _pRpcMessage,
    DWORD *_pdwStubPhase);



#endif 	/* __IqmAddingSymbol_INTERFACE_DEFINED__ */



#ifndef __TSTESTSUPPLIERLib_LIBRARY_DEFINED__
#define __TSTESTSUPPLIERLib_LIBRARY_DEFINED__

/* library TSTESTSUPPLIERLib */
/* [helpstring][version][uuid] */ 


EXTERN_C const IID LIBID_TSTESTSUPPLIERLib;

EXTERN_C const CLSID CLSID_TestSupplier;

#ifdef __cplusplus

class DECLSPEC_UUID("1BD005FD-6FB8-41d1-BD29-72CBEBFE33B2")
TestSupplier;
#endif
#endif /* __TSTESTSUPPLIERLib_LIBRARY_DEFINED__ */

/* Additional Prototypes for ALL interfaces */

unsigned long             __RPC_USER  BSTR_UserSize(     unsigned long *, unsigned long            , BSTR * ); 
unsigned char * __RPC_USER  BSTR_UserMarshal(  unsigned long *, unsigned char *, BSTR * ); 
unsigned char * __RPC_USER  BSTR_UserUnmarshal(unsigned long *, unsigned char *, BSTR * ); 
void                      __RPC_USER  BSTR_UserFree(     unsigned long *, BSTR * ); 

unsigned long             __RPC_USER  VARIANT_UserSize(     unsigned long *, unsigned long            , VARIANT * ); 
unsigned char * __RPC_USER  VARIANT_UserMarshal(  unsigned long *, unsigned char *, VARIANT * ); 
unsigned char * __RPC_USER  VARIANT_UserUnmarshal(unsigned long *, unsigned char *, VARIANT * ); 
void                      __RPC_USER  VARIANT_UserFree(     unsigned long *, VARIANT * ); 

/* end of Additional Prototypes */

#ifdef __cplusplus
}
#endif

#endif


