#include "tsConnectionStatus.h"

//-----------------------------------------------------------------------
LPVOID CTSConnectionStatus::
AllocBufferForStatus(tagTSCONNECTIONSTATUS status, CUDFDateTime& dtStatus, SAFEARRAY*& pStatusStruct, DWORD& szData)
{
	DWORD size;
	LPVOID pData = NULL;
	pStatusStruct = NULL;
	szData = 0;
	size = sizeof(SConnectionStatus);
	SConnectionStatus *pConnectionStatus = NULL;
	
	pStatusStruct = SafeArrayCreateVector(VT_UI1, 0, size);
	_ASSERT(pStatusStruct);
	if (SafeArrayAccessData(pStatusStruct, (void**)&pConnectionStatus) == S_OK)
	{
		szData = size;
		pData = pConnectionStatus;
		pConnectionStatus->m_Mode = 0;
		pConnectionStatus->m_Date = dtStatus.GetDate();
		pConnectionStatus->m_Time = dtStatus.GetTime();
		pConnectionStatus->m_Status = status;
	}

	return pData;
}
//-----------------------------------------------------------------------
BOOL CTSConnectionStatus::
AddStatusListener(TSenderCaller* Caller)
{
	BOOL res = FALSE;
	CSenderConnectionsStatus::iterator itSenderCallersMap;

	_ASSERT(Caller != NULL);
	if (Caller == NULL)
		return res;

	itSenderCallersMap = m_ConnectionStatusListeners.blocking_find(Caller);
	if (itSenderCallersMap == m_ConnectionStatusListeners.end())
	{
		m_ConnectionStatusListeners.insert(CSenderConnectionsStatus::value_type(Caller, (tagTSCONNECTIONSTATUS)-1));
		res = TRUE;
	}

	m_ConnectionStatusListeners.leave_section();

	return res;
}
//-----------------------------------------------------------------------
BOOL CTSConnectionStatus::
DeleteStatusListener(TSenderCaller* Caller)
{
	BOOL res = FALSE;
	CSenderConnectionsStatus::iterator itSenderCallersMap;

	_ASSERT(Caller != NULL);
	if (Caller == NULL)
		return res;

	itSenderCallersMap = m_ConnectionStatusListeners.blocking_find(Caller);
	if (itSenderCallersMap != m_ConnectionStatusListeners.end())
	{
		m_ConnectionStatusListeners.erase(Caller);
		LONG rc = Caller->Release();
		res = TRUE;
	}

	m_ConnectionStatusListeners.leave_section();

	return res;
}
//-----------------------------------------------------------------------
void CTSConnectionStatus::
ChangeStatus(TSenderCaller* Caller, SAFEARRAY* pStatusInfo)
{
	SafeArrayUnaccessData(pStatusInfo);

	_ASSERT(Caller != NULL);
	if (Caller == NULL)
		return;

	CToProcessWrapper::OnStatusChanged(*Caller, -1, pStatusInfo);
}
//-----------------------------------------------------------------------
void CTSConnectionStatus::
SendLastConnectionStatus()
{
	SAFEARRAY *pStatusStruct = NULL;
	DWORD szData;
	LPVOID pData = NULL;
	CUDFDateTime dt;
	CSenderConnectionsStatus::iterator itSenderConnectionsStatus;
	CSenderConnectionsStatus ConnectionsStatusSenders;

	tagTSCONNECTIONSTATUS status;
	status = GetConnectionStatus(dt);
	TSenderCaller *Caller = NULL;

	m_ConnectionStatusListeners.enter_section();
	for(itSenderConnectionsStatus = m_ConnectionStatusListeners.begin(); itSenderConnectionsStatus != m_ConnectionStatusListeners.end(); itSenderConnectionsStatus++)
	{
		if (itSenderConnectionsStatus->second != status)
		{
			itSenderConnectionsStatus->second = status;
			Caller = itSenderConnectionsStatus->first;
			Caller->AddRef();
			ConnectionsStatusSenders.insert(CSenderConnectionsStatus::value_type(Caller, m_ConnectionStatus));
		}		
	}
	m_ConnectionStatusListeners.leave_section();

	for(itSenderConnectionsStatus = ConnectionsStatusSenders.begin(); itSenderConnectionsStatus != ConnectionsStatusSenders.end(); itSenderConnectionsStatus++)
	{
		pData = AllocBufferForStatus(status, dt, pStatusStruct, szData);
		if (pData != NULL)
		{				
			ChangeStatus(itSenderConnectionsStatus->first, pStatusStruct);
		}
		itSenderConnectionsStatus->first->Release();
	}	
}
//-----------------------------------------------------------------------