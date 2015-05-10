#include "ToProcessWrapper.h"
#include <tchar.h>

using namespace std;
//-----------------------------------------------------------------------
#include <windows.h>
#include <process.h>
#include "MsgWait.h"
//-----------------------------------------------------------------------
HANDLE CToProcessWrapper::m_SupplierThread = NULL;
DWORD CToProcessWrapper::m_SupplierThreadId = 0;
ESystemType CToProcessWrapper::s_SystemType = EUnknown;
HWND CToProcessWrapper::m_SupplierThreadMsgsWindow = NULL;

#include <SupplierComModule.h>
extern CSupplierComModule _Module;


//-----------------------------------------------------------------------
CToProcessWrapper::CToProcessWrapper()
{
	s_SystemType = GetSystemVType();
};
//-----------------------------------------------------------------------
CToProcessWrapper::~CToProcessWrapper()
{
}; 
//-----------------------------------------------------------------------
void CToProcessWrapper::StartSupplierThread(TSupplierThread pSupplierThread)
{
	DWORD exCode;
	BOOL res;

	if (m_SupplierThread != NULL)
	{
		res = GetExitCodeThread(m_SupplierThread, &exCode);
		if (res && (exCode != STILL_ACTIVE))
		{
			CloseHandle(m_SupplierThread);
			m_SupplierThreadMsgsWindow = NULL;
			m_SupplierThread = NULL;
			m_SupplierThreadId = 0;
		}
	}
	
	if (m_SupplierThread == NULL)
	{
		SSupplierThreadParams *pSupplierThreadParams = NULL;
		HANDLE hStartEvent = NULL;
		hStartEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
		pSupplierThreadParams = new SSupplierThreadParams(this, hStartEvent);
		pSupplierThreadParams->AddRef();
		pSupplierThreadParams->AddRef();
		DWORD ThrId = 0;
		DWORD ThrHandle = 0;

		ThrHandle = _beginthreadex(NULL, 0, pSupplierThread, pSupplierThreadParams, 0, (UINT*)&ThrId);
		if (ThrHandle != 0)
		{
			DWORD wres;
			wres = WaitForSingleObject(hStartEvent, INFINITE);
			m_SupplierThread = (HANDLE)ThrHandle;
			m_SupplierThreadMsgsWindow = pSupplierThreadParams->GetSupplierThreadMsgsWindow();
			m_SupplierThreadId = ThrId;
		}

		pSupplierThreadParams->Release();
		CloseHandle(hStartEvent);
	}
};
//-----------------------------------------------------------------------
BOOL CToProcessWrapper::TerminateSupplierThread()
{
	BOOL res = FALSE;
	if (m_SupplierThreadMsgsWindow != NULL)
	{
		HANDLE hTerminateEvent;
		hTerminateEvent = CreateEvent(NULL, FALSE, FALSE, NULL);

		res = PostMessage(m_SupplierThreadMsgsWindow, WM_TERM_THREAD, (WPARAM)hTerminateEvent, 0);
		if (res)
		{
			DWORD wres;
			HANDLE WaitHandles[1];

			WaitHandles[0] = hTerminateEvent;

			wres = WaitForMultipleObjectsEx(1, WaitHandles, TRUE, 5000, FALSE);
		}
	
		if (m_SupplierThread != NULL)
		{
			CloseHandle(m_SupplierThread);
			m_SupplierThreadMsgsWindow = NULL;
			m_SupplierThread = NULL;
			m_SupplierThreadId = 0;
		}

		if (hTerminateEvent != NULL)
		{
			CloseHandle(hTerminateEvent);
			hTerminateEvent = NULL;
		}
	}

	return TRUE;
}
//-----------------------------------------------------------------------
BOOL CToProcessWrapper::CloseListenerThread()
{
	BOOL bCloseThread = FALSE;
	BOOL res;

	if (m_SupplierThreadMsgsWindow != NULL)
	{
		HANDLE hContinueEvent;
		hContinueEvent = CreateEvent(NULL, FALSE, FALSE, NULL);

		res = PostMessage(m_SupplierThreadMsgsWindow, WM_EXIT_THREAD, (WPARAM)hContinueEvent, 0);
		if (res)
		{
			DWORD wres;
			MSG msg;
			HANDLE WaitHandles[2];

			WaitHandles[0] = hContinueEvent;
			WaitHandles[1] = m_SupplierThread;

			while (1)
			{
				wres = MsgWait(2, WaitHandles, INFINITE, QS_ALLINPUT, IsSystemNT40());
				if (wres == WAIT_OBJECT_0)
				{
					break;
				} else
				if (wres == WAIT_OBJECT_0+1)
				{
					bCloseThread = TRUE;
					break;
				} else
				if (wres == WAIT_TIMEOUT)
				{
					//bCloseThread = TRUE;
					//TerminateThread(m_SupplierThread, 0);
					break;
				} else
				{
					while (PeekMessage(&msg, NULL, 0, 0, PM_REMOVE))
					{
						TranslateMessage(&msg);
						DispatchMessage(&msg);
					}
				}
			}
		}
	
		if (bCloseThread)
		{
			CloseHandle(m_SupplierThread);
			m_SupplierThreadMsgsWindow = NULL;
			m_SupplierThread = NULL;
			m_SupplierThreadId = 0;
		}

		if (hContinueEvent != NULL)
		{
			CloseHandle(hContinueEvent);
			hContinueEvent = NULL;
		}

	}

	return bCloseThread;
}
//-----------------------------------------------------------------------
void CToProcessWrapper::OnHistoryData(TSenderCaller& Caller, long Id, SAFEARRAY* psa)
{
	SDataParams P;
	P.Id = Id;
	P.psa = psa;
	Caller.CallMethodViaSend(0, P);
};
//-----------------------------------------------------------------------
void CToProcessWrapper::OnStartRealTime(TSenderCaller& Caller, long Id)
{
	SDataParams P;
	P.Id = Id;
	P.psa = NULL;
	Caller.CallMethodViaSend(1, P);
};
//-----------------------------------------------------------------------
void CToProcessWrapper::OnRealTimeData(TSenderCaller& Caller, long Id, SAFEARRAY* psa)
{
	SDataParams P;
	P.Id = Id;
	P.psa = psa;
	Caller.CallMethodViaSend(2, P);
};
//-----------------------------------------------------------------------
void CToProcessWrapper::OnCompleted(TSenderCaller& Caller, long Id)
{
	SDataParams P;
	P.Id = Id;
	P.psa = NULL;
	Caller.CallMethodViaSend(3, P);
};
//-----------------------------------------------------------------------
void CToProcessWrapper::OnStatusLine(TSenderCaller& Caller, long Id, SAFEARRAY* psa)
{
	SDataParams P;
	P.Id = Id;
	P.psa = psa;
	Caller.CallMethodViaSend(4, P);
}
//-----------------------------------------------------------------------
void CToProcessWrapper::SendRemove(TSenderCaller& Caller)
{
	Caller.CallMethodViaSendNoParams(5);
};
//-----------------------------------------------------------------------
void CToProcessWrapper::OnStatusChanged(TSenderCaller& Caller, long Id, SAFEARRAY* psa)
{
	SDataParams P;
	P.Id = Id;
	P.psa = psa;
	Caller.CallMethodViaSend(6, P);
}
//-----------------------------------------------------------------------
BOOL CToProcessWrapper::AdviseTransaction(STransactionParams *pTransParams)
{
	BOOL res = FALSE;
	if (m_SupplierThreadMsgsWindow != NULL)
	{
		res = PostMessage(m_SupplierThreadMsgsWindow, WM_ADVISE_TRANSACTION, (WPARAM)pTransParams, 0);
	}

	if (!res)
	{
		if (pTransParams->m_pSenderCaller != NULL)
			pTransParams->m_pSenderCaller->Release();
	}

	return res;
}
//-----------------------------------------------------------------------
BOOL CToProcessWrapper::UnadviseTransaction(long Id)
{
	BOOL res = FALSE;
	if (m_SupplierThreadMsgsWindow != NULL)
	{
		res = PostMessage(m_SupplierThreadMsgsWindow, WM_UNADVISE_TRANSACTION, Id, 0);
	}
	return res;
}
//-----------------------------------------------------------------------
BOOL CToProcessWrapper::AdviseConnectionStatus(TSenderCaller* Caller)
{
	BOOL res = FALSE;
	_ASSERT(Caller != NULL);
	if (m_SupplierThreadMsgsWindow != NULL)
	{
		Caller->AddRef();
		res = PostMessage(m_SupplierThreadMsgsWindow, WM_ADVISE_CONNECTION_STATUS, (WPARAM)Caller, 0);
		if (!res)
			Caller->Release();
	}
	return res;
}
//-----------------------------------------------------------------------
BOOL CToProcessWrapper::UnadviseConnectionStatus(TSenderCaller* Caller)
{
	BOOL res = FALSE;
	_ASSERT(Caller != NULL);
	if (m_SupplierThreadMsgsWindow != NULL)
	{
		Caller->AddRef();
		res = PostMessage(m_SupplierThreadMsgsWindow, WM_UNADVISE_CONNECTION_STATUS, (WPARAM)Caller, 0);
		if (!res)
			Caller->Release();
	}
	return res;
}
//-----------------------------------------------------------------------
BOOL CToProcessWrapper::Unadvise(TSenderCaller* Caller)
{
	BOOL res = FALSE;
	_ASSERT(Caller != NULL);
	if (m_SupplierThreadMsgsWindow != NULL)
	{
		Caller->AddRef();
		res = PostMessage(m_SupplierThreadMsgsWindow, WM_UNADVISE, (WPARAM)Caller, 0);
		if (!res)
			Caller->Release();
	}
	return res;
}
//-----------------------------------------------------------------------
BOOL CToProcessWrapper::AdviseStatusLine(SStatusLineParams *pStatusLineParams)
{
	BOOL res = FALSE;
	if (m_SupplierThreadMsgsWindow != NULL)
	{
		res = PostMessage(m_SupplierThreadMsgsWindow, WM_ADVISE_STATUS_LINE, (WPARAM)pStatusLineParams, 0);
	}

	if (!res)
	{
		if (pStatusLineParams->m_pSenderCaller != NULL)
			pStatusLineParams->m_pSenderCaller->Release();
	}

	return res;
}
//-----------------------------------------------------------------------
BOOL CToProcessWrapper::UnadviseStatusLine(long Id)
{
	BOOL res = FALSE;
	if (m_SupplierThreadMsgsWindow != NULL)
	{
		res = PostMessage(m_SupplierThreadMsgsWindow, WM_UNADVISE_STATUS_LINE, Id, 0);
	}
	return res;
}
//-----------------------------------------------------------------------
BOOL CToProcessWrapper::CreateConnection(SConnectionParams *pConnectionParams)
{
	BOOL res = FALSE;

	if (m_SupplierThreadMsgsWindow != NULL)
	{
		res = PostMessage(m_SupplierThreadMsgsWindow, WM_CREATE_CONNECTION, (WPARAM)pConnectionParams, 0);
	}

	return res;
}
//-----------------------------------------------------------------------
COneObjectData<CToProcessWrapper> *  COneObjectInProcess<CToProcessWrapper>::m_Object=NULL;
COneObjectCriticalSection<CToProcessWrapper>  COneObjectAccess<CToProcessWrapper>::crt;
COneObjectDataTerminator<CToProcessWrapper>  COneObjectInProcess<CToProcessWrapper>::m_Terminator;
//-----------------------------------------------------------------------
