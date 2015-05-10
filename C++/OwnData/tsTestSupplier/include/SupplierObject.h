#ifndef _SUPPLIER_OBJECT_
#define _SUPPLIER_OBJECT_

//++++++++++++
#include <comdef.h>
#include <comutil.h>

#include "tsbase.h"
#include "tsSupplierListener.h"
#include "ProxySender.h"
#include "ToProcessWrapper.h"
#include "SynchCont.h"
//------------#include "SupplierRegistry.h"

//-----------------------------------------------------------------------
//------------class CSupplierObject : public IUnknown, public CSupplierRegistry, public CProxyTSSupplier
class CSupplierObject : public IUnknown, public CProxyTSSupplier
{
public:

	CSupplierObject()
	{
	}

	HRESULT FinalConstruct(TSupplierThread pSupplierThread);
	void FinalRelease();

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

	HRESULT __stdcall OnHistoryData(long Id, SAFEARRAY* psa);
	HRESULT __stdcall OnStartRealTime(long Id, SAFEARRAY* psa);
	HRESULT __stdcall OnRealTimeData(long Id, SAFEARRAY* psa);
	HRESULT __stdcall OnCompleted(long Id, SAFEARRAY* psa);
	HRESULT __stdcall OnStatusLine(long Id, SAFEARRAY* psa);
	void __stdcall  ReleaseObject();
	HRESULT __stdcall OnStatusChanged(long Id, SAFEARRAY* psa);

	BOOL CreateConnection();

public:

	typedef synch_map<long, long> CTransInfo;
	typedef synch_map<ItsConnectionsListener*, ItsConnectionsListener*> CSupplierConnections;

	BOOL IsActiveTransaction(long Id);
	BOOL IsActiveStatusLineTransaction(long Id);
	BOOL IsActiveConnectionsListener(ItsConnectionsListener *ConnectionsListener);

	static void CreateVectorLongElem(VARIANT* Answer, LONG* Data, LONG Number);
	virtual BOOL GetResolutions(long Mode, long*& Resolutions, long& ResolutionsNumber) = 0;
	virtual BOOL GetResolutionSizes(long Mode, long Resolution, long*& ResolutionsSizes, long& ResolutionSizesNumber) = 0;
	virtual BOOL GetFields(long Mode, long Category, long*& Fields, long& FieldsNumber) = 0;

	BOOL IsActiveAdvise();

protected:

TSenderCaller *m_pSinkSender;
COneObjectInProcess<CToProcessWrapper> *m_CallObject;
ItsSupplierListener *m_Listener;
CSupplierConnections m_Connections;
CTransInfo m_AdviseTrans;
CTransInfo m_AdviseSymbolListTrans;
CTransInfo m_AdviseStatusLineTrans;
TSupplierThread m_SupplierThreadProc;
};
//-----------------------------------------------------------------------

#endif