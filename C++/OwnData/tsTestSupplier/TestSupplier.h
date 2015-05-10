// TestSupplier.h: Definition of the CTestSupplier class
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_TESTSUPPLIER_H__16B20227_50F2_4348_8614_FE1080B58A17__INCLUDED_)
#define AFX_TESTSUPPLIER_H__16B20227_50F2_4348_8614_FE1080B58A17__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "resource.h"       // main symbols
#include <crtdbg.h>
#include "CNewComObject.h"
#include "TestSupplierObject.h"

/////////////////////////////////////////////////////////////////////////////
// CTestSupplier

class CTestSupplier : 
    public CComObjectRootEx<CComSingleThreadModel>,
	public CComCoClass<CTestSupplier, &CLSID_TestSupplier>,
	public IDispatchImpl<ItsSupplier, &IID_ItsSupplier, &LIBID_TSTESTSUPPLIERLib>,
	public CTestSupplierObject
{
public:
	CTestSupplier() {}

	typedef CComCreator2< CComCreator< CNewComObject< CTestSupplier > >, CComCreator< CComAggObject< CTestSupplier > > > _CreatorClass;

	HRESULT FinalConstruct();
	void FinalRelease();

BEGIN_COM_MAP(CTestSupplier)
	COM_INTERFACE_ENTRY(ItsSupplier)
	COM_INTERFACE_ENTRY2(IDispatch, ItsSupplier)
END_COM_MAP()

DECLARE_REGISTRY_RESOURCEID(IDR_TestSupplier)

// ItsSupplier
	STDMETHOD(get_Name)(BSTR* pVal);
	STDMETHOD(get_ShortName)(BSTR* pVal);
	STDMETHOD(get_Description)(BSTR* pVal);
	STDMETHOD(get_Version)(BSTR* pVal);
	STDMETHOD(get_Vendor)(BSTR* pVal);
	STDMETHOD(ShowProperties)(long Datafeed);
	STDMETHOD(get_ConnectionsNumber)(long* pVal);
	STDMETHOD(get_ConnectionName)(long Index, BSTR* pVal);
	STDMETHOD(AdviseConnectionsStatus)(long Datafeed, IUnknown* pSink);
	STDMETHOD(UnadviseConnectionsStatus)(long Datafeed, IUnknown* pSink);
	STDMETHOD(IsProvide)(long Mode, long Symbol, long Category, long ResolutionSize, long Resolution, long Field, BOOL* pVal);
	STDMETHOD(Advise)(IUnknown* pSink);
	STDMETHOD(Unadvise)(void);
	STDMETHOD(AdviseTransaction)(long Id, long DatafeedId, long Symbol, BSTR SymbolName, BSTR Description, long Category, long ResolutionSize, long Resolution, long Field, long Exchange, BSTR ExchangeName, long StartDate, long StartTime, long FinishDate, long FinishTime, BSTR Sessions, BSTR SessionsTimeZoneInformation, BSTR ExchangeTimeZoneInformation);
	STDMETHOD(UnadviseTransaction)(long Id);
	STDMETHOD(GetResolutions)(long Mode, long DataFeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long Exchange, BSTR ExchangeName, VARIANT* Answer);
	STDMETHOD(GetResolutionSizes)(long Mode, long DataFeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long Resolution, long Exchange, BSTR ExchangeName, VARIANT* Answer);
	STDMETHOD(GetFields)(long Mode, long DataFeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long ResolutionSize, long Resolution, long Exchange, BSTR ExchangeName, VARIANT* Answer);
	STDMETHOD(get_ProvideStatusLine)(long* pVal);
	STDMETHOD(AdviseStatusLine)(long Id, long Datafeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long ResolutionSize, long Resolution, long Field, long Exchange, BSTR ExchangeName, BSTR Sessions, BSTR SessionsTimeZoneInformation);
	STDMETHOD(UnadviseStatusLine)(long Id);
	STDMETHOD(IsProvideAddingSymbols)(BOOL* pVal);
	STDMETHOD(DoAddingSymbols)(long Datafeed, IDispatch* Portfolio);
	STDMETHOD(CanSaveToStorage)(BOOL* pVal);
	STDMETHOD(CanReadFromStorage)(BOOL* pVal);
	STDMETHOD(get_IsAscDataFeed)(BOOL* pVal);
};

#endif // !defined(AFX_TESTSUPPLIER_H__16B20227_50F2_4348_8614_FE1080B58A17__INCLUDED_)
