#ifndef _TS_DATA_SENDER_
#define _TS_DATA_SENDER_

#pragma warning (disable : 4786)

#include <wtypes.h>
#include "ThrRefs.h"
#include "tsbase.h"
#include <vector>
#include "tsbase.h"
#include "ToProcessWrapper.h"
#include "tsAdviseDataSender.h"

#include "LogFile.h"
extern CLogFile fff;	


using namespace std;
using namespace ts;
//-----------------------------------------------------------------------
class CTSTransDataSender : public CAdviseDataSender
{
public:	
//-----------------------------------------------------------------------
	CTSTransDataSender(STransactionParams* transParams);
//-----------------------------------------------------------------------
	virtual string GetSymbolNameOfQuery()
	{
		return m_transParams.m_SymbolName;
	}
//-----------------------------------------------------------------------
	virtual DWORD GetTSTransId()
	{
		return m_transParams.m_TransactionId;
	}
//-----------------------------------------------------------------------
	virtual DWORD GetDataFeedID()
	{
		return m_transParams.m_DataFeedId;
	}
//-----------------------------------------------------------------------
	virtual ~CTSTransDataSender()
	{
	}
//-----------------------------------------------------------------------
	virtual string GetExchangeNameOfQuery()
	{
		return m_transParams.m_ExchangeName;
	}
//-----------------------------------------------------------------------
	virtual string GetSymbolCategoryOfQuery()
	{
		string Category;
		Category = GetCategoryName(m_transParams.m_Category);
		return Category;
	}
//-----------------------------------------------------------------------
	virtual long GetResolution()
	{
		return m_transParams.m_Resolution;
	}
//-----------------------------------------------------------------------
	virtual long GetResolutionSize()
	{
		return m_transParams.m_ResolutionSize;
	}
//-----------------------------------------------------------------------
	virtual CUDFDateTime GetStartDateTime()
	{
		CUDFDateTime dt;
		dt.SetDateTime(m_transParams.m_StartDate, m_transParams.m_StartTime);
		return dt;
	}
	virtual CUDFDateTime GetFinishDateTime()
	{
		CUDFDateTime dt;
		dt.SetDateTime(m_transParams.m_FinishDate, m_transParams.m_FinishTime);
		return dt;
	}
//-----------------------------------------------------------------------
	virtual BOOL RecvFullHistory()
	{
		return m_RecvFullHistory;
	}
	virtual BOOL OnRecvFullHistory()
	{
		m_RecvFullHistory = TRUE;
		return TRUE;
	}
//-----------------------------------------------------------------------
	void SendHistoryData(SAFEARRAY* pBarsArray);
	void SendRealTimeData(SAFEARRAY* pBarsArray);
	void StartRealTime();
	void Completed();

	BOOL IsCompleted()
	{
		return m_bCompleted;
	}
	BOOL IsStartRealTime()
	{
		return m_bStartRealTime;
	}
//-----------------------------------------------------------------------
	LPVOID AllocBufferForStatus(tagTSCONNECTIONSTATUS status, SAFEARRAY*& pStatusStruct, DWORD& szData);
//-----------------------------------------------------------------------
	template<class STSDataElement>
	static LPVOID AllocBufferForTSData(DWORD numRecs, SAFEARRAY*& pBarsArray, STSDataElement*& pFirstBar, DWORD& szData)
	{
		DWORD size;
		LPVOID pData = NULL;
		pBarsArray = NULL;
		pFirstBar = NULL;
		szData = 0;
		size = numRecs * sizeof(STSDataElement);

		if (size == 0)
			return NULL;
		
		pBarsArray = SafeArrayCreateVector(VT_UI1, 0, size);
		_ASSERT(pBarsArray);
		if (SafeArrayAccessData(pBarsArray, (void**)&pFirstBar) == S_OK)
		{
			szData = size;
			pData = pFirstBar;
		}

		return pData;
	}
//-----------------------------------------------------------------------
	void ChangeStatus(SAFEARRAY* pStatusInfo);
	void SendLastStatus();
//-----------------------------------------------------------------------
	BOOL OnUnadvise(TSenderCaller& Caller)
	{
		if (m_Caller == &Caller)
		{
			m_Caller->Release();
			m_Caller = NULL;
		}
		return TRUE;
	}
//-----------------------------------------------------------------------
	virtual void CheckCompletedStatus()
	{
		if (GetResolution() == TS_RESOLUTION_TICK)
		{
			if (m_NowDateTime >= GetFinishDateTime())
				Completed();
		} else
		if (GetResolution() == TS_RESOLUTION_MINUTE)
		{
			if (m_NowDateTime > GetFinishDateTime())
				Completed();
		} else
		{
			Completed();
		}
	}
//-----------------------------------------------------------------------
	virtual BOOL CheckTimeOuted(CUDFDateTime& nowTime)
	{
		BOOL res = FALSE;
		m_NowDateTime = nowTime;

		if (nowTime > m_FinishDateTime)
		{
			res = TRUE;
		}

		return res;
	}
//-----------------------------------------------------------------------
	tagTSCONNECTIONSTATUS SetStatus(tagTSCONNECTIONSTATUS status, CUDFDateTime& NowDT)
	{
		tagTSCONNECTIONSTATUS prevStatus;
		SetNowDateTime(NowDT);
		prevStatus = m_Status;
		m_Status = status;
		return prevStatus;
	}
//-----------------------------------------------------------------------
	void SetInvalidStatus()
	{
		m_bInvalidStatus = TRUE;
	}
	void ResetInvalidStatus()
	{
		m_bInvalidStatus = FALSE;
	}
	BOOL IsActiveInvalidStatus()
	{
		return m_bInvalidStatus;
	}
//-----------------------------------------------------------------------
	EProvideMode GetTransactionMode()
	{
		return m_TransactionMode;
	}
//-----------------------------------------------------------------------
	BOOL OnDataChanged(LPVOID pData, DWORD BarsCount);
//-----------------------------------------------------------------------
	EProvideMode m_TransactionMode;

protected:

	STransactionParams m_transParams;
	CUDFDateTime m_StartDateTime;
	CUDFDateTime m_FinishDateTime;

	BOOL m_bCompleted;
	BOOL m_RecvFullHistory;
	BOOL m_bStartRealTime;
	tagTSCONNECTIONSTATUS m_Status;
	BOOL m_bInvalidStatus;
};
//-----------------------------------------------------------------------

#endif
