// TestSupplier.cpp : Implementation of CTsTestSupplierApp and DLL registration.

#include "stdafx.h"
#include "tsTestSupplier.h"
#include "TestSupplier.h"
#include "SupplierComModule.h"
#include "tsTestSupplierThread.h"
#include "SupplierThreadMsgs.h"
#include "UserMsgs.h"
#include <comutil.h>
#include <wchar.h>

const char* TSTAThreadObjectWndClass = "TSTAThreadObject_TestDataFeed";
const TCHAR CSupplierThreadMsgs::s_SupplierThreadMsgsWindowClassName[] = "TestSupplierThreadMsgsWindowClass";

#include "LogFile.h"
CLogFile fff("TestSupplier");

//-----------------------------------------------------------------------
HRESULT CTestSupplier::FinalConstruct()
{
	CSupplierComModule::AddSupplier();
	return CSupplierObject::FinalConstruct(TestSupplierThread);
}
//-----------------------------------------------------------------------
void CTestSupplier::FinalRelease()
{
	CSupplierComModule::ReleaseSupplier();
	CSupplierObject::FinalRelease();
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::get_Name(BSTR* pVal)
{
	HRESULT Result = S_OK;
	_bstr_t txtName = "QuantysSupplier"; 
	*pVal = txtName.copy();
	return S_OK;
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::get_ShortName(BSTR* pVal)
{
	HRESULT Result = S_OK;
	_bstr_t txtName = "H";
	*pVal = txtName.copy();
	return S_OK;
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::get_Description(BSTR* pVal)
{
	return CSupplierObject::get_Description(pVal);
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::get_Version(BSTR* pVal)
{
	return CSupplierObject::get_Version(pVal);
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::get_Vendor(BSTR* pVal)
{
	return CSupplierObject::get_Vendor(pVal);
}
//-----------------------------------------------------------------------
#include "ConfigDlg.h"

STDMETHODIMP CTestSupplier::ShowProperties(long Datafeed)
{
	CTestSupplierConfigDlg ConfigDlg;

	ConfigDlg.DoModal();

	return S_OK;
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::get_ConnectionsNumber(long* pVal)
{
	_ASSERT(pVal != NULL);
	if (pVal == NULL)
		return E_POINTER;

	*pVal = 1;
	return S_OK;
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::get_ConnectionName(long Index, BSTR* pVal)
{
	if (Index != 0)
		return S_FALSE;

	_ASSERT(pVal != NULL);
	if (pVal == NULL)
		return E_POINTER;

	_bstr_t ConnName = "HF Connection";
	*pVal = ConnName.copy();
	return S_OK;
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::AdviseConnectionsStatus(long Datafeed, IUnknown* pSink)
{
	return CSupplierObject::AdviseConnectionsStatus(Datafeed, pSink);
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::UnadviseConnectionsStatus(long Datafeed, IUnknown* pSink)
{
	return CSupplierObject::UnadviseConnectionsStatus(Datafeed, pSink);
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::IsProvide(long Mode, long Symbol, long Category, long ResolutionSize, long Resolution, long Field, BOOL* pVal)
{
	return CTestSupplierObject::IsProvide(Mode, Symbol, Category, ResolutionSize, Resolution, Field, pVal);
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::Advise(IUnknown* pSink)
{
	return CSupplierObject::Advise(pSink);
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::Unadvise(void)
{
	return CSupplierObject::Unadvise();
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::AdviseTransaction(long Id, long DatafeedId, long Symbol, BSTR SymbolName, BSTR Description, long Category, long ResolutionSize, long Resolution, long Field, long Exchange, BSTR ExchangeName, long StartDate, long StartTime, long FinishDate, long FinishTime, BSTR Sessions, BSTR SessionsTimeZoneInformation, BSTR ExchangeTimeZoneInformation)
{
	return CSupplierObject::AdviseTransaction(Id, DatafeedId, Symbol, SymbolName, Description, Category, ResolutionSize, Resolution, Field, Exchange, ExchangeName, StartDate, StartTime, FinishDate, FinishTime, Sessions, SessionsTimeZoneInformation, ExchangeTimeZoneInformation);
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::UnadviseTransaction(long Id)
{
	return CSupplierObject::UnadviseTransaction(Id);
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::GetResolutions(long Mode, long DataFeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long Exchange, BSTR ExchangeName, VARIANT* Answer)
{
	return CSupplierObject::GetResolutions(Mode, DataFeed, Symbol, SymbolName, Description, Category, Exchange, ExchangeName, Answer);
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::GetResolutionSizes(long Mode, long DataFeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long Resolution, long Exchange, BSTR ExchangeName, VARIANT* Answer)
{
	return CSupplierObject::GetResolutionSizes(Mode, DataFeed, Symbol, SymbolName, Description, Category, Resolution, Exchange, ExchangeName, Answer);
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::GetFields(long Mode, long DataFeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long ResolutionSize, long Resolution, long Exchange, BSTR ExchangeName, VARIANT* Answer)
{
	return CSupplierObject::GetFields(Mode, DataFeed, Symbol, SymbolName, Description, Category, ResolutionSize, Resolution, Exchange, ExchangeName, Answer);
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::get_ProvideStatusLine(long* pVal)
{
	*pVal = //0;
		TS_STATUS_LINE_DATE|TS_STATUS_LINE_TIME|TS_STATUS_LINE_LAST;  
	/*TS_STATUS_LINE_DATE|TS_STATUS_LINE_TIME| 

	TS_STATUS_LINE_OPEN|TS_STATUS_LINE_HIGH| 

	TS_STATUS_LINE_LOW|TS_STATUS_LINE_CLOSE| 

	TS_STATUS_LINE_TOTAL_VOLUME|TS_STATUS_LINE_ASK| 

	TS_STATUS_LINE_BID|TS_STATUS_LINE_PREV| 

	TS_STATUS_LINE_OPEN_INTEREST|TS_STATUS_LINE_LAST; */

	return S_OK; 
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::AdviseStatusLine(long Id, long Datafeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long ResolutionSize, long Resolution, long Field, long Exchange, BSTR ExchangeName, BSTR Sessions, BSTR SessionsTimeTimeZoneInformation)
{
	return CSupplierObject::AdviseStatusLine(Id, Datafeed, Symbol, SymbolName, Description, Category, ResolutionSize, Resolution, Field, Exchange, ExchangeName, Sessions, SessionsTimeTimeZoneInformation);
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::UnadviseStatusLine(long Id)
{
	return CSupplierObject::UnadviseStatusLine(Id);
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::IsProvideAddingSymbols(BOOL* pVal)
{
	*pVal = TRUE;
	return S_OK;
}
//-----------------------------------------------------------------------
#include "AddSymbolDlg.h"

STDMETHODIMP CTestSupplier::DoAddingSymbols(long Datafeed, IDispatch* Portfolio)
{
	CAddSymbolDlg AddSymbolDlg;

	INT CtrlId = 0;
	CtrlId = AddSymbolDlg.DoModal();
	if (CtrlId == IDOK)
	{
		HRESULT hr = S_FALSE;
		IqmAddingSymbol *qmAddingSymbolInterface = NULL;
		hr = Portfolio->QueryInterface(IID_IqmAddingSymbol, (void**)&qmAddingSymbolInterface);
		if (hr == S_OK)
		{
			_bstr_t Symbol = AddSymbolDlg.m_Symbol.c_str();
			_bstr_t SymbolRoot = AddSymbolDlg.m_SymbolRoot.c_str();
			_bstr_t Description = AddSymbolDlg.m_Description.c_str();
			_bstr_t CUSIP = "0";
			long Category = CAddSymbolDlg::GetCategoryByName(AddSymbolDlg.m_Category.c_str());
			long DataFeedId = Datafeed;
			_bstr_t ExchangeName = AddSymbolDlg.m_Exchange.c_str();
			long ContractMonth = 0;
			long ContractYear = 0;
			double StrikePrice = 0;
			long CallPut = 0;
			long Margin = 0;
			long ExpirationDate = 0;
			long Expired = 0;
			long ExpirationRule = 0;
			long FirstNoticeDate = 0;
			long Delivery = 0;

			long SymbolId = 0;

			try
			{
				hr = qmAddingSymbolInterface->AddSymbol(Symbol, SymbolRoot, Description,
					CUSIP, Category, DataFeedId, ExchangeName, ContractMonth, ContractYear,
					StrikePrice, CallPut, Margin, ExpirationDate, Expired, ExpirationRule,
					FirstNoticeDate, Delivery, &SymbolId);
				if (hr != S_OK)
				{
					OutErrorCodeMessage("AddSymbol HRESULT", hr);
				} else
				{
					string Message;
					Message = "Symbol ";
					Message += Symbol;
					Message += " added successfully";
					::MessageBox(0, Message.c_str(), "Information", MB_OK|MB_ICONINFORMATION);
				}
			}
			catch(...)
			{
				OutErrorMessage("AddSymbol exception");
			}


			qmAddingSymbolInterface->Release();
			qmAddingSymbolInterface = NULL;
		}
	}

	return S_OK;
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::CanSaveToStorage(BOOL* pVal)
{
	*pVal = FALSE;
	return S_OK;
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::CanReadFromStorage(BOOL* pVal)
{
	*pVal = TRUE;
	return S_OK;
}
//-----------------------------------------------------------------------
STDMETHODIMP CTestSupplier::get_IsAscDataFeed(BOOL* pVal)
{
	return CSupplierObject::get_IsAscDataFeed(pVal);
}
//-----------------------------------------------------------------------

