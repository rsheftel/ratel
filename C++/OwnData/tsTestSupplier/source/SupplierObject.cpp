//#include "StdAfx.h" 
#include "SupplierObject.h"
#include "comutil.h"
#include "tsEnums.h"
//-----------------------------------------------------------------------
HRESULT CSupplierObject::FinalConstruct(TSupplierThread pSupplierThread)
{
	HRESULT Result = S_OK;

	m_Listener = NULL;
	m_CallObject = new COneObjectInProcess<CToProcessWrapper>(true);
	m_pSinkSender = new TSenderCaller;
	m_pSinkSender->AddRef();
	m_pSinkSender->CallerBind(this);

	m_SupplierThreadProc = pSupplierThread;

	m_CallObject->AnythingLock();
	m_CallObject->GetObject()->StartSupplierThread(pSupplierThread);
	m_CallObject->AnythingUnlock();

	return Result;
}
//-----------------------------------------------------------------------
void CSupplierObject::FinalRelease()
{
	_ASSERT(m_Listener == NULL);
	if (m_Listener != NULL)
	{
		m_Listener->Release();
		m_Listener = NULL;
	}

	CSupplierConnections::iterator itSupplierConnections;

	m_Connections.enter_section();

	for(itSupplierConnections = m_Connections.begin(); itSupplierConnections != m_Connections.end(); itSupplierConnections++)
	{
		itSupplierConnections->first->Release();
	}

	m_Connections.clear();

	m_Connections.leave_section();

	if (m_CallObject != NULL)
	{
		delete m_CallObject;
		m_CallObject = NULL;
	}

	if (m_pSinkSender != NULL)
	{
		m_pSinkSender->Clear();
		m_pSinkSender->Release();
		m_pSinkSender = NULL;
	}
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::get_Name(BSTR* pVal)
{
	HRESULT Result = S_OK;
	_bstr_t txtName = "QuantysSupplier";
	*pVal = txtName.copy();
	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::get_ShortName(BSTR* pVal)
{
	HRESULT Result = S_OK;
	_bstr_t txtName = "H";
	*pVal = txtName.copy();
	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::get_Description(BSTR* pVal)
{
	USES_CONVERSION;
	HRESULT Result = S_OK;
	_bstr_t txtDescr;
	txtDescr = "Copyright 2007, Malbec Partners";

	*pVal = txtDescr.copy();
	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::get_Version(BSTR* pVal)
{
	HRESULT Result = S_OK;
	_bstr_t txtDescr;
	txtDescr = "1.0.0.0";
	*pVal = txtDescr.copy();
	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::get_Vendor(BSTR* pVal)
{
	HRESULT Result = S_OK;
	_bstr_t txtDescr;
	txtDescr = "Malbec Partners";
	*pVal = txtDescr.copy();
	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::ShowProperties(long Datafeed)
{
	HRESULT Result = S_OK;
	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::get_ConnectionsNumber(long* pVal)
{
	HRESULT Result = S_OK;
	*pVal = 0;
	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::get_ConnectionName(long Index, BSTR* pVal)
{
	HRESULT Result = S_OK;
	_bstr_t txtConnName;
	txtConnName = "";
	*pVal = txtConnName.copy();
	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::AdviseConnectionsStatus(long Datafeed, IUnknown* pSink)
{
	BOOL res = FALSE;
	HRESULT Result = S_FALSE;
	ItsConnectionsListener *Connections = NULL;

	_ASSERT(pSink != NULL);
	if (pSink == NULL)
		return E_POINTER;

    Result = pSink->QueryInterface(IID_ItsConnectionsListener, (void**)&Connections);
    _ASSERT(S_OK == Result);

	if (Result == S_OK)
	{			

		if (IsActiveConnectionsListener(Connections))
		{
			Connections->Release();
			Connections = NULL;
			Result = S_FALSE;
			return Result;
		}

		AddRef();

		res = TRUE;

		m_Connections.enter_section();

		if (m_Connections.empty())
			res = CToProcessWrapper::AdviseConnectionStatus(m_pSinkSender);

		if (!res)
		{
			Connections->Release();
			Connections = NULL;
			Result = S_OK;
		} else
		{
			m_Connections.insert(CSupplierConnections::value_type(Connections, Connections));
			Result = S_OK;
		}

		m_Connections.leave_section();
	}

	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::UnadviseConnectionsStatus(long Datafeed, IUnknown* pSink)
{
	BOOL res = FALSE;
	HRESULT Result = S_FALSE;
	ItsConnectionsListener *Connections = NULL;

	_ASSERT(pSink != NULL);
	if (pSink == NULL)
		return E_POINTER;


    Result = pSink->QueryInterface(IID_ItsConnectionsListener, (void**)&Connections);
    _ASSERT(S_OK == Result);

	if (Result == S_OK)
	{	
		m_Connections.enter_section();
		if (IsActiveConnectionsListener(Connections))
		{
			m_Connections.erase(Connections);
			Connections->Release();
		}

		Connections->Release();

		if (m_Connections.empty())
		{
			BOOL res;
			res = CToProcessWrapper::UnadviseConnectionStatus(m_pSinkSender);

			if (!res)
			{
				Release();
			}
		}

		m_Connections.leave_section();

		Result = S_OK;
	}

	if (Result == S_OK)
	{
		if (!IsActiveAdvise())
		{
			CToProcessWrapper::CloseListenerThread();
		}
	}

	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::IsProvide(long Mode, long Symbol, long Category, long ResolutionSize, long Resolution, long Field, BOOL* pVal)
{
	HRESULT Result = S_OK;
	*pVal = TRUE;
	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::Advise(IUnknown* pSink)
{
	HRESULT Result = S_FALSE;
	_ASSERT(m_Listener == NULL);
	if (m_Listener != NULL)
		return Result;

    Result = pSink->QueryInterface(IID_ItsSupplierListener, (void**)&m_Listener);
    _ASSERT(m_Listener != NULL);
    _ASSERT(S_OK==Result);

	if (Result == S_OK)
	{			
		AddRef();
	}

	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::Unadvise(void)
{
	BOOL res = FALSE;
	HRESULT Result = S_FALSE;

	_ASSERT(m_Listener != NULL);
	if (m_Listener == NULL)
		return Result;

	CTransInfo::iterator itTransInfo;
	vector<DWORD> vTransIds, vStatusLineTransIds;
	vector<DWORD>::iterator itTransIds;

	for(itTransInfo = m_AdviseTrans.begin(); itTransInfo != m_AdviseTrans.end(); itTransInfo++)
	{
		vTransIds.push_back(itTransInfo->first);
	}

	for(itTransInfo = m_AdviseStatusLineTrans.begin(); itTransInfo != m_AdviseStatusLineTrans.end(); itTransInfo++)
	{
		vStatusLineTransIds.push_back(itTransInfo->first);
	}

	for(itTransIds = vTransIds.begin(); itTransIds != vTransIds.end(); itTransIds++)
	{
		UnadviseTransaction(*itTransIds);
	}

	for(itTransIds = vStatusLineTransIds.begin(); itTransIds != vStatusLineTransIds.end(); itTransIds++)
	{
		UnadviseStatusLine(*itTransIds);
	}

	vTransIds.clear();
	vStatusLineTransIds.clear();

	if (m_Listener != NULL)
	{
		m_Listener->Release();
		m_Listener = NULL;
		res = CToProcessWrapper::Unadvise(m_pSinkSender);
		if (!res)
			Release();

	}

	Result = S_OK;

	if (Result == S_OK)
	{
		if (!IsActiveAdvise())
		{
			CToProcessWrapper::CloseListenerThread();
		}
	}

	return Result;
}
//-----------------------------------------------------------------------
BOOL CSupplierObject::CreateConnection()
{
	BOOL res = FALSE;
	SConnectionParams *pConnectionParams = NULL;
//------------
/*
	if (!IsLastVersion())
		LoadParams();
*/

	pConnectionParams = new SConnectionParams("", "");
//------------
	/*
	CConfigParams pars;
	CConfigParams::iterator itConfigParams;
	GetConfigParams(pars);
	for(itConfigParams = pars.begin(); itConfigParams != pars.end(); itConfigParams++)
	{
		pConnectionParams->AddParam(itConfigParams->first.c_str(), itConfigParams->second.m_value.c_str(), itConfigParams->second.m_type);
	}
	*/

	pConnectionParams->AddRef();
	res = CToProcessWrapper::CreateConnection(pConnectionParams);
	if (!res)
	{
		pConnectionParams->Release();
	}

	return res;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::AdviseTransaction(long Id, long DatafeedId, long Symbol, BSTR SymbolName, BSTR Description, long Category, long ResolutionSize, long Resolution, long Field, long Exchange, BSTR ExchangeName, long StartDate, long StartTime, long FinishDate, long FinishTime, BSTR Sessions, BSTR SessionsTimeZoneInformation, BSTR ExchangeTimeZoneInformation)
{
	BOOL res = FALSE;
	HRESULT Result = S_OK;
	STransactionParams *pTransactionParams = NULL;

	CUDFDateTime stTime, finTime;
	stTime.SetDateTime(StartDate, StartTime);
	finTime.SetDateTime(FinishDate, FinishTime);
	_ASSERT(stTime <= finTime);

	_ASSERT(m_Listener != NULL);
	if (m_Listener == NULL)
		return S_FALSE;

	BOOL r;
	r = CreateConnection();
	if (!r)
		return S_FALSE;

	m_AdviseTrans.insert(CTransInfo::value_type(Id, Id));
	_ASSERT(m_pSinkSender != NULL);
	m_pSinkSender->AddRef();

	pTransactionParams = new STransactionParams(m_pSinkSender, Id, 
		DatafeedId, 
		SymbolName, Description, ExchangeName, Category, 
		ResolutionSize, Resolution, Field, 
		StartDate, StartTime, FinishDate, FinishTime, 
		0, 0,
		Sessions, SessionsTimeZoneInformation, ExchangeTimeZoneInformation);

	pTransactionParams->AddRef();

	res = CToProcessWrapper::AdviseTransaction(pTransactionParams);
	if (!res)
	{
		pTransactionParams->Release();
		m_pSinkSender->Release();
		Result = S_FALSE;
	}

	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::UnadviseTransaction(long Id)
{
	BOOL res = FALSE;
	HRESULT Result = S_OK;
	m_AdviseTrans.erase(Id);

	res = CToProcessWrapper::UnadviseTransaction(Id);
	if (!res)
	{
		Result = S_FALSE;
	}

	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::GetResolutions(long Mode, long DataFeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long Exchange, BSTR ExchangeName, VARIANT* Answer)
{
	HRESULT Result = S_FALSE;

	long *Resolutions = NULL;
	long ResolutionsNumber = 0;
	if (GetResolutions(Mode, Resolutions, ResolutionsNumber))
	{
		CreateVectorLongElem(Answer, Resolutions, ResolutionsNumber);
		Result = S_OK;
	} else
	{
		CreateVectorLongElem(Answer, NULL, 0);
		Result = S_OK;
	}

	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::GetResolutionSizes(long Mode, long DataFeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long Resolution, long Exchange, BSTR ExchangeName, VARIANT* Answer)
{
	HRESULT Result = S_FALSE;

	long *ResolutionSizes = NULL;
	long ResolutionSizesNumber = 0;
	if (GetResolutionSizes(Mode, Resolution, ResolutionSizes, ResolutionSizesNumber))
	{
		CreateVectorLongElem(Answer, ResolutionSizes, ResolutionSizesNumber);
		Result = S_OK;
	} else
	{
		CreateVectorLongElem(Answer, NULL, 0);
		Result = S_OK;
	}

	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::GetFields(long Mode, long DataFeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long ResolutionSize, long Resolution, long Exchange, BSTR ExchangeName, VARIANT* Answer)
{
	HRESULT Result = S_FALSE;

	long *Fields = NULL;
	long FieldsNumber = 0;
	if (GetFields(Mode, Category, Fields, FieldsNumber))
	{
		CreateVectorLongElem(Answer, Fields, FieldsNumber);
		Result = S_OK;
	} else
	{
		CreateVectorLongElem(Answer, NULL, 0);
		Result = S_OK;
	}

	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::get_ProvideStatusLine(long* pVal)
{
//------------*pVal = 0;
	*pVal = TS_STATUS_LINE_DATE|TS_STATUS_LINE_TIME|
		TS_STATUS_LINE_OPEN|TS_STATUS_LINE_HIGH|
		TS_STATUS_LINE_LOW|TS_STATUS_LINE_CLOSE|
		TS_STATUS_LINE_TOTAL_VOLUME|TS_STATUS_LINE_ASK|
		TS_STATUS_LINE_BID|TS_STATUS_LINE_PREV|
		TS_STATUS_LINE_OPEN_INTEREST|TS_STATUS_LINE_LAST;

	return S_OK;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::AdviseStatusLine(long Id, long Datafeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long ResolutionSize, long Resolution, long Field, long Exchange, BSTR ExchangeName, BSTR Sessions, BSTR SessionsTimeZoneInformation)
{
	BOOL res = FALSE;
	HRESULT Result = S_OK;
	SStatusLineParams *pStatusLineParams = NULL;

	_ASSERT(m_Listener != NULL);
	if (m_Listener == NULL)
		return S_FALSE;

	BOOL r;
	r = CreateConnection();
	if (!r)
		return S_FALSE;

	m_AdviseStatusLineTrans.insert(CTransInfo::value_type(Id, Id));
	_ASSERT(m_pSinkSender != NULL);
	m_pSinkSender->AddRef();

	pStatusLineParams = new SStatusLineParams(m_pSinkSender, Id, 
		Datafeed,
		SymbolName, Description, ExchangeName, Category, 
		ResolutionSize, Resolution, Field, 
		0, 0);

	pStatusLineParams->AddRef();

	res = CToProcessWrapper::AdviseStatusLine(pStatusLineParams);
	if (!res)
	{
		pStatusLineParams->Release();
		m_pSinkSender->Release();
		Result = S_FALSE;
	}

	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::UnadviseStatusLine(long Id)
{
	BOOL res = FALSE;
	HRESULT Result = S_OK;
	m_AdviseStatusLineTrans.erase(Id);

	res = CToProcessWrapper::UnadviseStatusLine(Id);
	if (!res)
	{
		Result = S_FALSE;
	}

	return Result;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::IsProvideAddingSymbols(BOOL* pVal)
{
	*pVal = FALSE;
	return S_OK;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::CanSaveToStorage(BOOL* pVal)
{
	*pVal = TRUE;
	return S_OK;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::CanReadFromStorage(BOOL* pVal)
{
	*pVal = TRUE;
	return S_OK;
}
//-----------------------------------------------------------------------
STDMETHODIMP CSupplierObject::get_IsAscDataFeed(BOOL* pVal)
{
	*pVal = FALSE;
	return S_OK;
}
//-----------------------------------------------------------------------
#include "UserMsgs.h"

STDMETHODIMP CSupplierObject::DoAddingSymbols(long Datafeed, IDispatch* Portfolio)
{
	return S_OK;
}
//-----------------------------------------------------------------------
BOOL CSupplierObject::IsActiveTransaction(long Id)
{
	BOOL res = FALSE;
	CTransInfo::iterator itTransInfo;
	itTransInfo = m_AdviseTrans.blocking_find(Id);
	res = (itTransInfo != m_AdviseTrans.end());
	m_AdviseTrans.leave_section();
	return res;
}
//-----------------------------------------------------------------------
BOOL CSupplierObject::IsActiveStatusLineTransaction(long Id)
{
	BOOL res = FALSE;
	CTransInfo::iterator itTransInfo;
	itTransInfo = m_AdviseStatusLineTrans.blocking_find(Id);
	res = (itTransInfo != m_AdviseStatusLineTrans.end());
	m_AdviseStatusLineTrans.leave_section();
	return res;
}
//-----------------------------------------------------------------------
BOOL CSupplierObject::IsActiveConnectionsListener(ItsConnectionsListener *ConnectionsListener)
{
	BOOL res = FALSE;
	CSupplierConnections::iterator itSupplierConnections;
	itSupplierConnections = m_Connections.blocking_find(ConnectionsListener);
	res = (itSupplierConnections != m_Connections.end());
	m_Connections.leave_section();
	return res;
}
//-----------------------------------------------------------------------
BOOL CSupplierObject::IsActiveAdvise()
{
	BOOL res = FALSE;

	res = TRUE;

	return res;
}
//-----------------------------------------------------------------------
HRESULT __stdcall CSupplierObject::OnHistoryData(long Id, SAFEARRAY* psa)
{
	HRESULT Result = S_FALSE;
	VARIANT vData;

	try
	{

	VariantInit(&vData);

	_ASSERT(psa != NULL);
	if (psa == NULL)
		return E_POINTER;

	vData.vt = VT_ARRAY|VT_UI1;
	vData.parray = psa;

	if (m_Listener != NULL)
	{
		if (IsActiveTransaction(Id))
		{
			Result = m_Listener->DataChanged(Id, vData);
		}
	}

	if (Result != S_OK)
	{
		HRESULT hrSAD;
		hrSAD = SafeArrayDestroy(vData.parray);
	}

	}
	catch(...)
	{
	}

	return Result;
}
//-----------------------------------------------------------------------
HRESULT __stdcall CSupplierObject::OnStartRealTime(long Id, SAFEARRAY* psa)
{
	HRESULT Result = S_FALSE;

	try
	{
	_ASSERT(psa == NULL);
	if (m_Listener != NULL)
	{
		if (IsActiveTransaction(Id))
		{
			Result = m_Listener->Update(Id);
		}
	}
	}
	catch(...)
	{
	}

	return Result;
}
//-----------------------------------------------------------------------
HRESULT __stdcall CSupplierObject::OnRealTimeData(long Id, SAFEARRAY* psa)
{
	HRESULT Result = S_FALSE;
	HRESULT Result2 = S_FALSE;
	VARIANT vData;

	try
	{
	VariantInit(&vData);

	_ASSERT(psa != NULL);
	if (psa == NULL)
		return E_POINTER;

	vData.vt = VT_ARRAY|VT_UI1;
	vData.parray = psa;

	if (m_Listener != NULL)
	{

		if (IsActiveTransaction(Id))
		{
			Result = m_Listener->DataChanged(Id, vData);
			if (Result == S_OK)
			{
				Result2 = m_Listener->Update(Id);
			}
		}
	}

	if (Result != S_OK)
	{
		HRESULT hrSAD;
		hrSAD = SafeArrayDestroy(vData.parray);
	}

	}
	catch(...)
	{
	}


	return Result2;
}
//-----------------------------------------------------------------------
HRESULT __stdcall CSupplierObject::OnCompleted(long Id, SAFEARRAY* psa)
{
	HRESULT Result = S_FALSE;

	try
	{
	_ASSERT(psa == NULL);
	if (m_Listener != NULL)
	{
		if (IsActiveTransaction(Id))
		{
			Result = m_Listener->Complete(Id);
		}
	}
	}
	catch(...)
	{
	}

	return Result;
}
//-----------------------------------------------------------------------
HRESULT __stdcall CSupplierObject::OnStatusLine(long Id, SAFEARRAY* psa)
{
	HRESULT Result = S_FALSE;
	VARIANT vData;
	SStatusLineMessage *pStatusLineMessage = NULL;
	SStatusLine *pStatusLine = NULL;
	HRESULT hr;
	SAFEARRAY *psaStatusLine = NULL;

	try
	{
	VariantInit(&vData);

	_ASSERT(psa != NULL);
	if (psa == NULL)
		return E_POINTER;

	if (m_Listener != NULL)
	{

		if (IsActiveStatusLineTransaction(Id))
		{
			hr = SafeArrayAccessData(psa, (void**)&pStatusLineMessage);
			if (hr == S_OK)
			{
				psaStatusLine = SafeArrayCreateVector(VT_UI1, 0, sizeof(SStatusLine));
				_ASSERT(psaStatusLine != NULL);
				if (psaStatusLine != NULL)
				{
					hr = SafeArrayAccessData(psaStatusLine, (void**)&pStatusLine);
					if (hr == S_OK)
					{
						memcpy(pStatusLine, &pStatusLineMessage->m_StatusLine, sizeof(SStatusLine));

						hr = SafeArrayUnaccessData(psaStatusLine);

						vData.vt = VT_ARRAY|VT_UI1;
						vData.parray = psaStatusLine;

						if (pStatusLineMessage->m_Mask)
							Result = m_Listener->StatusLine(Id, vData, pStatusLineMessage->m_Mask);
					}

					
					if (Result != S_OK)
					{
						SafeArrayDestroy(psaStatusLine);
					}
					
				}				
				hr = SafeArrayUnaccessData(psa);
			}
		}
	}

	HRESULT hrSAD;
	hrSAD = SafeArrayDestroy(psa);

	}
	catch(...)
	{
	}

	return Result;
}
//-----------------------------------------------------------------------
void __stdcall CSupplierObject::ReleaseObject()
{
	try
	{
	Release();
	}
	catch(...)
	{
	}

}
//-----------------------------------------------------------------------
HRESULT __stdcall CSupplierObject::OnStatusChanged(long Id, SAFEARRAY* psa)
{
	HRESULT Result = S_FALSE;

	try
	{
	_ASSERT(psa != NULL);
	if (psa == NULL)
		return E_POINTER;

	if (Id == -1)
	{
		if (!m_Connections.empty())
		{
			SConnectionStatus *pStatusInfo = NULL;
			Result = SafeArrayAccessData(psa, (void**)&pStatusInfo);
			if (Result == S_OK)
			{				
				CSupplierConnections::iterator itSupplierConnections;

				m_Connections.enter_section();

				for(itSupplierConnections = m_Connections.begin(); itSupplierConnections != m_Connections.end(); itSupplierConnections++)
				{
					Result = itSupplierConnections->first->Status(pStatusInfo->m_Date, pStatusInfo->m_Time, 0,
						(tagTSCONNECTIONSTATUS)pStatusInfo->m_Status);
				}

				m_Connections.leave_section();
	  
				SafeArrayUnaccessData(psa);
			}
		}
	} else
	if (IsActiveTransaction(Id))
	{
		if (m_Listener != NULL)
		{
			SConnectionStatus *pStatusInfo = NULL;
			Result = SafeArrayAccessData(psa, (void**)&pStatusInfo);
			if (Result == S_OK)
			{				
				Result = m_Listener->Status(Id, pStatusInfo->m_Mode, pStatusInfo->m_Date, pStatusInfo->m_Time,
					pStatusInfo->m_Status);
	  
				SafeArrayUnaccessData(psa);
			}
		}
	}

	HRESULT hrSAD;
	hrSAD = SafeArrayDestroy(psa);
	}
	catch(...)
	{
	}


	return Result;
}
//-----------------------------------------------------------------------
void CSupplierObject::CreateVectorLongElem(VARIANT* Answer, LONG* Data, LONG Number)
{
    Answer->vt=VT_ARRAY|VT_UI4;
    Answer->parray=SafeArrayCreateVector(VT_UI4,0,Number);
    _ASSERT(Answer->parray);
    LONG* Buffer;
	if ((Data != NULL) && (Number > 0))
    if(S_OK==SafeArrayAccessData(Answer->parray,(void**)&Buffer))
    {
        memcpy(Buffer, Data, Number*sizeof(LONG));
        SafeArrayUnaccessData(Answer->parray);
    }
}
//-----------------------------------------------------------------------