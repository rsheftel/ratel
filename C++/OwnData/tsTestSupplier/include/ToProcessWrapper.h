#pragma once

//#include "StdAfx.h"
#pragma warning (disable : 4786)

#include <crtdbg.h>
#include <map>
#include "STAOtherAppartCaller.h"
#include "OneObjectInProcess.h"
#include "TSenderCaller.h"
#include <vector>

//-----------------------------------------------------------------------
using namespace std;

#include "ProxySender.h"
#include "ThrRefs.h"
#include "udfdate.h"
#include "dateutils.h"
#include "strutils.h"
#include <string>
#include <map>
#include <atlconv.h>
#include "tsbase.h"
#include "SystemType.h"

using namespace ts;

//-------------------------------------------------------------------------
enum EProvideMode
{
	EHistoryDataProvide = 0,
	ERealTimeDataProvide = 1
};
//-----------------------------------------------------------------------
typedef UINT (WINAPI *TSupplierThread)(LPVOID lpParameter);
//-------------------------------------------------------------------
class CToProcessWrapper;
//-----------------------------------------------------------------------
struct SSupplierThreadParams : public CThreadRefs
{
	SSupplierThreadParams(CToProcessWrapper *procWrapper, HANDLE hStartEvent)
	{
		m_procWrapper = procWrapper;
		m_hStartEvent = hStartEvent;
		m_hwndSupplierThreadMsgsWindow = NULL;
	}
//-----------------------------------------------------------------------
	void SetSupplierThreadMsgsWindow(HWND hwndSupplierThreadMsgsWindow)
	{
		m_hwndSupplierThreadMsgsWindow = hwndSupplierThreadMsgsWindow;
	}
//-----------------------------------------------------------------------
	HWND GetSupplierThreadMsgsWindow()
	{
		return m_hwndSupplierThreadMsgsWindow;
	}
//-----------------------------------------------------------------------
	CToProcessWrapper *m_procWrapper;
	HANDLE m_hStartEvent;
	HWND m_hwndSupplierThreadMsgsWindow;
};
//-----------------------------------------------------------------------
#pragma pack(push, tsSupplierDataStruct, 1)

struct SConnectionStatus
{
	long m_Mode;
	long m_Date;
	long m_Time;
	long m_Status;
};
//-----------------------------------------------------------------------
struct SConnectionMessage
{
	long m_Date;
	long m_Time;
	long m_code;
	char m_message[250];
};
//-----------------------------------------------------------------------
struct SStatusLineMessage
{
	SStatusLineMessage()
	{
		m_StatusLine.Open = m_StatusLine.High = m_StatusLine.Low = m_StatusLine.Close = m_StatusLine.Ask = m_StatusLine.Bid = 0;
		m_StatusLine.Date = 0;
		m_StatusLine.Time = 0;
		m_StatusLine.TotalVolume = 0;
		m_StatusLine.OpenInterest = 0;
		m_StatusLine.Prev = 0;
		m_Mask = 0;
	}

	SStatusLine m_StatusLine;
	long m_Mask;
};

#pragma pack(pop, tsSupplierDataStruct)
//-----------------------------------------------------------------------
struct STransactionParams : public CThreadRefs
{
	TSenderCaller *m_pSenderCaller;
	long m_TransactionId;
	long m_DataFeedId;
	string m_SymbolName;
	string m_SymbolDescription;
	string m_ExchangeName;
	long m_Category;
	long m_ResolutionSize;
	long m_Resolution;
	long m_Field;
	long m_StartDate; 
	long m_StartTime;
	long m_FinishDate;
	long m_FinishTime;
	long m_NowDate;
	long m_NowTime;
	string m_Sessions;
	string m_SessionsTimeZoneInformation;
	string m_ExchangeTimeZoneInformation;
//-----------------------------------------------------------------------
	STransactionParams()
	{
		m_pSenderCaller = NULL;
		m_TransactionId = -1;
		m_DataFeedId = 0;
		m_Category = 0;
		m_ResolutionSize = 0;
		m_Resolution = 0;
		m_Field = 0;
		m_StartDate = m_FinishDate = m_NowDate = 0;
		m_StartTime = m_FinishTime = m_NowTime = 0;
	}

	STransactionParams(
		TSenderCaller *pSenderCaller,
		long Id, long DatafeedId, BSTR SymbolName, BSTR Description, BSTR ExchangeName,
		long Category, long ResolutionSize, long Resolution, long Field, 
		long StartDate, long StartTime, long FinishDate, long FinishTime, 
		long NowDate, long NowTime,
		BSTR Sessions, 
		BSTR SessionsTimeZoneInformation, BSTR ExchangeTimeZoneInformation)
	{
		USES_CONVERSION;
		_ASSERT(pSenderCaller != NULL);
		m_pSenderCaller = pSenderCaller;
		m_TransactionId = Id;
		m_DataFeedId = DatafeedId;
		m_SymbolName = W2A(SymbolName);
		m_SymbolDescription = W2A(Description);
		m_ExchangeName = W2A(ExchangeName);
		m_Category = Category;
		m_Resolution = Resolution;
		m_ResolutionSize = ResolutionSize;
		m_Field = Field;
		m_StartDate = StartDate; 
		m_StartTime = StartTime;
		m_FinishDate = FinishDate;
		m_FinishTime = FinishTime;
		m_NowDate = NowDate;
		m_NowTime = NowTime;
		m_Sessions = W2A(Sessions);
		m_SessionsTimeZoneInformation = W2A(SessionsTimeZoneInformation);
		m_ExchangeTimeZoneInformation = W2A(ExchangeTimeZoneInformation);
	}
//-----------------------------------------------------------------------
	~STransactionParams()
	{
	}
};
//-----------------------------------------------------------------------
struct SStatusLineParams : public CThreadRefs
{
	TSenderCaller *m_pSenderCaller;
	long m_TransactionId;
	long m_DataFeedId;
	string m_SymbolName;
	string m_SymbolDescription;
	string m_ExchangeName;
	long m_Category;
	long m_ResolutionSize;
	long m_Resolution;
	long m_Field;
	long m_NowDate;
	long m_NowTime;
//-----------------------------------------------------------------------
	SStatusLineParams()
	{
		m_pSenderCaller = NULL;
		m_TransactionId = -1;
		m_DataFeedId = 0;
		m_Category = 0;
		m_ResolutionSize = 0;
		m_Resolution = 0;
		m_Field = 0;
		m_NowDate = 0;
		m_NowTime = 0;
	}

	SStatusLineParams(
		TSenderCaller *pSenderCaller,
		long Id, long DatafeedId, BSTR SymbolName, BSTR Description, BSTR ExchangeName,
		long Category, long ResolutionSize, long Resolution, long Field, 
		long NowDate, long NowTime)
	{
		USES_CONVERSION;
		_ASSERT(pSenderCaller != NULL);
		m_pSenderCaller = pSenderCaller;
		m_TransactionId = Id;
		m_DataFeedId = DatafeedId;
		m_SymbolName = W2A(SymbolName);
		m_SymbolDescription = W2A(Description);
		m_ExchangeName = W2A(ExchangeName);
		m_Category = Category;
		m_Resolution = Resolution;
		m_ResolutionSize = ResolutionSize;
		m_Field = Field;
		m_NowDate = NowDate;
		m_NowTime = NowTime;
	}
//-----------------------------------------------------------------------
	~SStatusLineParams()
	{
	}
};
//-----------------------------------------------------------------------
#include "ipautils.h"

struct SConnectionParams : public CThreadRefs
{
//-----------------------------------------------------------------------
	enum EConnectionParamType
	{
		EStringConnectionParam = 0,
		ECSStringConnectionParam = 1,
		ENumericConnectionParam = 2,
		EFloatConnectionParam = 3,
		EIPAddrConnectionParam = 4
	};

	struct SConnectionParam
	{
		SConnectionParam(const char* ParamValue="", EConnectionParamType ParamType=EStringConnectionParam)
		{
			m_value = ParamValue;
			m_type = ParamType;
		}

		BOOL operator==(const SConnectionParam& Param) const
		{
			BOOL res = FALSE;

			if (m_type != Param.m_type)
				return FALSE;

			switch(m_type)
			{
				case EStringConnectionParam:
					res = (StringToUpperCase(Param.m_value) == StringToUpperCase(m_value));
					break;
				case ECSStringConnectionParam:
					res = (Param.m_value == m_value);
					break;
				case ENumericConnectionParam:
					res = (atoi(Param.m_value.c_str()) == atoi(m_value.c_str()));
					break;
				case EFloatConnectionParam:
					res = (atof(Param.m_value.c_str()) == atof(m_value.c_str()));
					break;
				case EIPAddrConnectionParam:
					{
						BOOL bIPAddr, bIPAddr2;
						BYTE b1, b2, b3, b4;
						BYTE _b1, _b2, _b3, _b4;
						bIPAddr = IsIPAddress(Param.m_value.c_str(), b1, b2, b3, b4);
						bIPAddr2 = IsIPAddress(m_value.c_str(), _b1, _b2, _b3, _b4);

						if (bIPAddr == bIPAddr2)
						{
							if (bIPAddr)
							{
								SIPAddr	addr;
								addr.Set(m_value.c_str());
								res = addr.IsEqual(b1, b2, b3, b4);
							} else
							{
								res = (DeleteEndChars(DeleteBeginChars(StringToUpperCase(Param.m_value), ' '), ' ') == DeleteEndChars(DeleteBeginChars(StringToUpperCase(m_value), ' '), ' '));
							}
						}
					}
					break;
			}

			return res;
		}

		BOOL operator!=(const SConnectionParam& Param) const
		{
			return !(*this == Param);
		}

		EConnectionParamType m_type;
		string m_value;
	};
//-----------------------------------------------------------------------
	typedef map<string, SConnectionParam> CConnectionParams;
//-----------------------------------------------------------------------
	SConnectionParams()
	{
	}

	SConnectionParams(const char* libPath, const char* libName)
	{
		m_LibPath = libPath;
		m_LibName = libName;
	}

	~SConnectionParams()
	{
	}
//-----------------------------------------------------------------------
	void AddParam(const char* parName, const char* parVal, EConnectionParamType parType=EStringConnectionParam)
	{
		m_Params.insert(CConnectionParams::value_type(parName, SConnectionParam(parVal, parType)));
	}
	string GetParam(const char* parName) const
	{
		string par;
		CConnectionParams::const_iterator itConnectionParams;

		itConnectionParams = m_Params.find(parName);
		if (itConnectionParams != m_Params.end())
			par = itConnectionParams->second.m_value;

		return par;
	}
	char GetCharParam(const char* parName) const
	{
		string par;
		char val = 0;
		par = GetParam(parName);
		if (!par.empty())
			val = par[0];
		return val;
	}
	int GetNumericParam(const char* parName) const
	{
		string par;
		int val;
		par = GetParam(parName);
		val = atoi(par.c_str());
		return val;
	}
	double GetFloatParam(const char* parName) const
	{
		string par;
		double val;
		par = GetParam(parName);
		val = atof(par.c_str());
		return val;
	}
	BOOL GetParam(const char* parName, SConnectionParam& param) const
	{
		BOOL res = FALSE;
		string par;
		CConnectionParams::const_iterator itConnectionParams;

		itConnectionParams = m_Params.find(parName);
		if (itConnectionParams != m_Params.end())
		{
			param.m_type = itConnectionParams->second.m_type;
			param.m_value = itConnectionParams->second.m_value;
			res = TRUE;
		}

		return res;
	}
//-----------------------------------------------------------------------
	string GetFullLibName() const
	{
		string librName;
		librName = m_LibPath;
		librName = DeleteEndChars(librName, '\\');
		librName = DeleteSpacesAroundChar(librName, '\\');
		librName = DeleteSpacesAroundChar(librName, ':');
		librName = StringToUpperCase(librName);
		librName += "\\";
		librName += StringToUpperCase(m_LibName);
		return librName;		
	}
//-----------------------------------------------------------------------
	BOOL operator==(const SConnectionParams& params) const
	{
		BOOL res = FALSE;
		SConnectionParam Param;

		res = (GetFullLibName() == params.GetFullLibName());

		if (res)
		{
			res = (m_Params.size() == params.m_Params.size());
		}

		if (res)
		{
			res = FALSE;
			CConnectionParams::const_iterator itConnectionParams;
			for(itConnectionParams = m_Params.begin(); itConnectionParams != m_Params.end(); itConnectionParams++)
			{
				if (params.GetParam(itConnectionParams->first.c_str(), Param))
				{
					if (Param != itConnectionParams->second)
					{						
						break;
					}
				} else
				{
					break;
				}
			}

			if (itConnectionParams == m_Params.end())
				res = TRUE;
		}

		return res;
	}
//-----------------------------------------------------------------------
	BOOL operator!=(const SConnectionParams& params) const
	{
		return !(*this == params);
	}
//-----------------------------------------------------------------------
	string m_LibPath;
	string m_LibName;
	CConnectionParams m_Params;
};
//-----------------------------------------------------------------------
#define WM_EXIT_THREAD (WM_USER + 1000)
#define WM_ADVISE_TRANSACTION (WM_USER + 1001)
#define WM_UNADVISE_TRANSACTION (WM_USER + 1002)
#define WM_UNADVISE (WM_USER + 1004)
#define WM_ADVISE_CONNECTION_STATUS (WM_USER + 1005)
#define WM_UNADVISE_CONNECTION_STATUS (WM_USER + 1006)
#define WM_ADVISE_SYMBOL_LIST_TRANSACTION (WM_USER + 1007)
#define WM_UNADVISE_SYMBOL_LIST_TRANSACTION (WM_USER + 1008)
#define WM_UNADVISE_SYMBOL_LIST (WM_USER + 1009)
#define WM_CREATE_CONNECTION (WM_USER + 1010)
#define WM_ADVISE_STATUS_LINE (WM_USER + 1011)
#define WM_UNADVISE_STATUS_LINE (WM_USER + 1012)
#define WM_TERM_THREAD (WM_USER + 1013)
//-----------------------------------------------------------------------
class CToProcessWrapper
{
public:

	static HANDLE m_SupplierThread;
	static DWORD m_SupplierThreadId;
	static ESystemType s_SystemType;
	static HWND m_SupplierThreadMsgsWindow;

public:
//-----------------------------------------------------------------------
	CToProcessWrapper();
	~CToProcessWrapper();
//-----------------------------------------------------------------------
	void StartSupplierThread(TSupplierThread pSupplierThread);
	static BOOL CloseListenerThread();
	static BOOL TerminateSupplierThread();
//-----------------------------------------------------------------------
	static ESystemType GetSystemWType()
	{
		return s_SystemType;
	}
	static BOOL IsSystemNT40()
	{
		return (s_SystemType == EWindowsNT);
	}
//-----------------------------------------------------------------------
	static void OnHistoryData(TSenderCaller& Caller, long Id, SAFEARRAY* psa);
	static void OnStartRealTime(TSenderCaller& Caller, long Id);
	static void OnRealTimeData(TSenderCaller& Caller, long Id, SAFEARRAY* psa);
	static void OnCompleted(TSenderCaller& Caller, long Id);
	static void OnStatusLine(TSenderCaller& Caller, long Id, SAFEARRAY* psa);
	static void SendRemove(TSenderCaller& Caller);
	static void OnStatusChanged(TSenderCaller& Caller, long Id, SAFEARRAY* psa);
	static void OnSymbolListData(TSenderCaller& Caller, long Id, SAFEARRAY* psa);
	static void OnSymbolListStatusChanged(TSenderCaller& Caller, long Id, SAFEARRAY* psa);
//-----------------------------------------------------------------------
	static BOOL AdviseTransaction(STransactionParams *pTransParams);
	static BOOL UnadviseTransaction(long Id);
	static BOOL Unadvise(TSenderCaller* Caller);
	static BOOL AdviseConnectionStatus(TSenderCaller* Caller);
	static BOOL UnadviseConnectionStatus(TSenderCaller* Caller);
//-----------------------------------------------------------------------
	static BOOL AdviseStatusLine(SStatusLineParams *pStatusLineParams);
	static BOOL UnadviseStatusLine(long Id);
//-----------------------------------------------------------------------
	static BOOL CreateConnection(SConnectionParams *pConnectionParams);
//-----------------------------------------------------------------------
protected:
};
//-----------------------------------------------------------------------