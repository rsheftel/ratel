#ifndef _TS_CONNECTION_STATUS_
#define _TS_CONNECTION_STATUS_

#pragma warning (disable : 4786)

#include "SynchCont.h"
#include "ThrRefs.h"
#include "tsbase.h"
#include <vector>
#include "ToProcessWrapper.h"
#include "CritSectionObj.h"
#include <string>


using namespace std;
using namespace ts;

//-----------------------------------------------------------------------
class CTSConnectionStatus
{
public:
//-----------------------------------------------------------------------
	typedef synch_map<TSenderCaller*, tagTSCONNECTIONSTATUS> CSenderConnectionsStatus;
//-----------------------------------------------------------------------
	CTSConnectionStatus()
	{
		m_ConnectionStatus = TS_CONNECTION_OFFLINE;
	}
//-----------------------------------------------------------------------
	void ChangeStatus(TSenderCaller* Caller, SAFEARRAY* pStatusInfo);
	void SendLastConnectionStatus();
//-----------------------------------------------------------------------
	LPVOID AllocBufferForStatus(tagTSCONNECTIONSTATUS status, CUDFDateTime& dtStatus, SAFEARRAY*& pStatusStruct, DWORD& szData);
	BOOL AddStatusListener(TSenderCaller* Caller);
	BOOL DeleteStatusListener(TSenderCaller* Caller);
//-----------------------------------------------------------------------
	tagTSCONNECTIONSTATUS GetConnectionStatus(CUDFDateTime& dt)
	{
		tagTSCONNECTIONSTATUS Status;
		m_ConnectionStatusCS.Lock();
		Status = m_ConnectionStatus;
		dt = m_ConnectionStatusTime;
		m_ConnectionStatusCS.UnLock();
		return Status;
	}
	tagTSCONNECTIONSTATUS SetConnectionStatus(tagTSCONNECTIONSTATUS status, INT Date, INT Time)
	{
		tagTSCONNECTIONSTATUS PrevStatus;
		m_ConnectionStatusCS.Lock();
		PrevStatus = m_ConnectionStatus;
		m_ConnectionStatus = status;
		m_ConnectionStatusTime.SetDateTime(Date, Time);
		m_ConnectionStatusCS.UnLock();
		return PrevStatus;
	}
//-----------------------------------------------------------------------
	virtual BOOL OnAdviseConnectionStatus() = 0;
//-----------------------------------------------------------------------
protected:
	CSenderConnectionsStatus m_ConnectionStatusListeners;
	tagTSCONNECTIONSTATUS m_ConnectionStatus;
	CUDFDateTime m_ConnectionStatusTime;
	CCriticalSectionObj m_ConnectionStatusCS;
};
//-----------------------------------------------------------------------

#endif