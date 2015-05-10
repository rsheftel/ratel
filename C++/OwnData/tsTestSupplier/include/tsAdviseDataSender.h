#ifndef _TS_ADVISE_DATA_SENDER_
#define _TS_ADVISE_DATA_SENDER_


#include <wtypes.h>
#include "ThrRefs.h"
#include "udfdate.h"
#include "dateutils.h"

//-------------------------------------------------------------------------
class CAdviseDataSender : public CThreadRefs, public CNowDateTime
{
public:
//-----------------------------------------------------------------------
	CAdviseDataSender()
	{
		m_SenderThreadId = 0;
		m_Caller = NULL;
	}
	virtual ~CAdviseDataSender()
	{
		if (m_Caller != NULL)
		{
			m_Caller->Release();
			m_Caller = NULL;
		}
	}
//-----------------------------------------------------------------------
	void SetSenderThreadId(DWORD idSenderThread)
	{
		m_SenderThreadId = idSenderThread;
	}
//-----------------------------------------------------------------------
	static string GetCategoryName(long Categ)
	{
		string Category;
		switch (Categ)
		{
			case TS_CATEGORY_FUTURE:
				Category = "FUT";
				break;
			case TS_CATEGORY_FUTUREOPTION:
				Category = "FOP";
				break;
			case TS_CATEGORY_STOCK:
				Category = "STK";
				break;
			case TS_CATEGORY_STOCKOPTION:
				Category = "OPT";
				break;
			case TS_CATEGORY_INDEX:
				Category = "IND";
				break;
			case TS_CATEGORY_MUTUALFUND:
				Category = "FND";
				break;
			case TS_CATEGORY_MONEYMKTFUND:
				Category = "MMF";
				break;
			case TS_CATEGORY_INDEXOPTION:
				Category = "IOP";
				break;
			case TS_CATEGORY_CASH:
				Category = "CASH";
				break;
			case TS_CATEGORY_BOND:
				Category = "BND";
				break;
			case TS_CATEGORY_SPREAD:
				Category = "SPR";
				break;
			case TS_CATEGORY_FOREX:
				Category = "FRX";
				break;
		}

		return Category;
	}
//-----------------------------------------------------------------------
	virtual string GetSymbolNameOfQuery() = 0;
	virtual string GetSymbolCategoryOfQuery() = 0;
	virtual string GetExchangeNameOfQuery() = 0;
	virtual DWORD GetTSTransId() = 0;
	virtual DWORD GetDataFeedID() = 0;
//-----------------------------------------------------------------------
protected:

	DWORD m_SenderThreadId;
	TSenderCaller *m_Caller;
};
//-----------------------------------------------------------------------
#endif