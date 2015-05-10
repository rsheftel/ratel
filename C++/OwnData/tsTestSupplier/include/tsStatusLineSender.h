#ifndef _TS_STATUS_LINE_SENDER_
#define _TS_STATUS_LINE_SENDER_

#include "ToProcessWrapper.h"
#include "tsAdviseDataSender.h"
//-----------------------------------------------------------------------
class CStatusLineSender : public CAdviseDataSender
{
public:
//-----------------------------------------------------------------------
	CStatusLineSender(SStatusLineParams* StatusLineParams)
	{
		_ASSERT(StatusLineParams != NULL);
		if (StatusLineParams != NULL)
			m_StatusLineParams = *StatusLineParams;

		m_NowDateTime.SetDateTime(m_StatusLineParams.m_NowDate, m_StatusLineParams.m_NowTime);
		m_Caller = m_StatusLineParams.m_pSenderCaller;
		_ASSERT(m_Caller != NULL);
	}
//-----------------------------------------------------------------------
	virtual ~CStatusLineSender()
	{
	}
//-----------------------------------------------------------------------
	virtual string GetSymbolNameOfQuery()
	{
		return m_StatusLineParams.m_SymbolName;
	}
	virtual string GetExchangeNameOfQuery()
	{
		return m_StatusLineParams.m_ExchangeName;
	}
	virtual string GetSymbolCategoryOfQuery()
	{
		string Category;
		Category = GetCategoryName(m_StatusLineParams.m_Category);
		return Category;
	}
//-----------------------------------------------------------------------
	virtual DWORD GetTSTransId()
	{
		return m_StatusLineParams.m_TransactionId;
	}
	virtual DWORD GetDataFeedID()
	{
		return m_StatusLineParams.m_DataFeedId;
	}
//-----------------------------------------------------------------------
	LPVOID AllocBufferForStatusLineMessage(SAFEARRAY*& psaStatusLineMessage, SStatusLineMessage*& pStatusLineMessage, DWORD& szData)
	{
		DWORD size;
		LPVOID pData = NULL;
		pStatusLineMessage = NULL;
		psaStatusLineMessage = NULL;
		szData = 0;
		size = sizeof(SStatusLineMessage);
		
		psaStatusLineMessage = SafeArrayCreateVector(VT_UI1, 0, size);
		_ASSERT(psaStatusLineMessage != NULL);
		if (SafeArrayAccessData(psaStatusLineMessage, (void**)&pStatusLineMessage) == S_OK)
		{
			szData = size;
			pData = pStatusLineMessage;
			pStatusLineMessage->m_Mask = 0;
			pStatusLineMessage->m_StatusLine.Date = 0;
			pStatusLineMessage->m_StatusLine.Time = 0;
			pStatusLineMessage->m_StatusLine.Ask = pStatusLineMessage->m_StatusLine.Bid = 0;
			pStatusLineMessage->m_StatusLine.Open = pStatusLineMessage->m_StatusLine.High = 
				pStatusLineMessage->m_StatusLine.Low = pStatusLineMessage->m_StatusLine.Close = 0;
			pStatusLineMessage->m_StatusLine.Prev = 0;
			pStatusLineMessage->m_StatusLine.TotalVolume = 0;
			pStatusLineMessage->m_StatusLine.OpenInterest = 0;
		}

		return pData;
	}
//-----------------------------------------------------------------------
	void SendStatusLineData(SAFEARRAY* pStatusLineData)
	{
		_ASSERT(pStatusLineData != NULL);
		if (pStatusLineData == NULL)
			return;

		SafeArrayUnaccessData(pStatusLineData);

		_ASSERT(m_Caller != NULL);
		if (m_Caller == NULL)
		{
			SafeArrayDestroy(pStatusLineData);
			return;
		}

		CToProcessWrapper::OnStatusLine(*m_Caller, GetTSTransId(), pStatusLineData);
	}
//-----------------------------------------------------------------------
protected:

	SStatusLineParams m_StatusLineParams;
};
//-----------------------------------------------------------------------

#endif