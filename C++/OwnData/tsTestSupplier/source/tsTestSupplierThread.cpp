#include "tsTestSupplierThread.h"

#include "tsDataTrans.h"

#include "ToProcessWrapper.h"
#include "dateutils.h"
#include "TSenderCaller.h"
#include "MsgWait.h"
#include <process.h>
#include <string>

#include "SupplierThreadMsgs.h"
#include "SupplierComModule.h"
extern CSupplierComModule _Module;


#include "LogFile.h"
extern CLogFile fff;	


using namespace std;
//-----------------------------------------------------------------------
UINT WINAPI TestSupplierThread(LPVOID lpParameter)
{	
tagMSG msg;

SSupplierThreadParams *pSupplierThreadParams;
pSupplierThreadParams = (SSupplierThreadParams*)lpParameter;

fff<<"Start Supplier Thread id= "<<GetCurrentThreadId()<<"\r\n";

PeekMessage(&msg, NULL, WM_USER, WM_USER, PM_NOREMOVE);

//---------------------------------
CSupplierThreadMsgs::RegSupplierThreadMsgsWindowProc();

CSupplierThreadMsgs *pSupplierThreadMsgs = NULL;
pSupplierThreadMsgs = new CSupplierThreadMsgs;

pSupplierThreadParams->SetSupplierThreadMsgsWindow(pSupplierThreadMsgs->GetWindowHandle());
//---------------------------------
SetEvent(pSupplierThreadParams->m_hStartEvent);

pSupplierThreadParams->Release();

BOOL bRun = TRUE;
SConnectionParams LastSupplierParams;

CTSDataTrans *pTSDataTrans = NULL;

HANDLE hTimers[1];
hTimers[0] = CreateWaitableTimer(NULL, FALSE, NULL);
LARGE_INTEGER mTime;
mTime.HighPart = 0;
mTime.LowPart = 0; 
LONG lPeriod;
BOOL res;

lPeriod = 400;
res = SetWaitableTimer(hTimers[0], &mTime, lPeriod, NULL, NULL, TRUE);

DWORD wres;
CUDFDateTime nowTime;
SYSTEMTIME sysTime;
GetSystemTime(&sysTime);
nowTime = GetUdfDateTimeFromSystemTime(sysTime);
DWORD numSetTimer=0;

while (bRun)
{
	wres = MsgWait(1, hTimers, 100000, QS_ALLINPUT, CToProcessWrapper::IsSystemNT40());

	if (wres == WAIT_OBJECT_0)
	{
		numSetTimer++;
		if (pTSDataTrans != NULL)
		{
			if (pTSDataTrans->Created())
			if (pTSDataTrans->Connected())
			{
//-----------------------------------------------------------------------
//fff<<"BEFORE.."<<endl;
				if (!(numSetTimer%2))
				{
					GetSystemTime(&sysTime);
					nowTime = GetUdfDateTimeFromSystemTime(sysTime);
					pTSDataTrans->SetNowDateTime(nowTime);
					pTSDataTrans->SendLastConnectionStatus();
					pTSDataTrans->CompleteTimeOutedTransactions(nowTime);
				}

				// New Data
				pTSDataTrans->OnDataChanged();
//fff<<"AFTER.."<<endl;
//-----------------------------------------------------------------------
			}
		}
	} else
	{
		while (PeekMessage(&msg, NULL, 0, 0, PM_REMOVE))
		{		
			TranslateMessage(&msg);
			DispatchMessage(&msg);
		}

		while (pSupplierThreadMsgs->PeekMessage(msg))
		{			
				switch (msg.message)
				{
					case WM_ADVISE_TRANSACTION:
						{
							STransactionParams *pTransParams;
							pTransParams = (STransactionParams*)msg.wParam;
							if (pTransParams != NULL)
							{
								if (pTSDataTrans != NULL)
								{
									pTSDataTrans->SetNowDateTimeAsSystemTime(nowTime);
									pTransParams->m_NowDate = nowTime.GetDate();
									pTransParams->m_NowTime = nowTime.GetTime();
									pTSDataTrans->OnAdviseTransaction(pTransParams);
								}
								pTransParams->Release();
							}
						}
						break;

					case WM_UNADVISE_TRANSACTION:
						if (pTSDataTrans != NULL)
						{
							pTSDataTrans->OnUnadviseTransaction((DWORD)msg.wParam);
						}
						break;
					case WM_ADVISE_STATUS_LINE:
						{
							SStatusLineParams *pStatusLineParams;
							pStatusLineParams = (SStatusLineParams*)msg.wParam;
							if (pStatusLineParams != NULL)
							{
								if (pTSDataTrans != NULL)
								{
									pTSDataTrans->SetNowDateTimeAsSystemTime(nowTime);
									pStatusLineParams->m_NowDate = nowTime.GetDate();
									pStatusLineParams->m_NowTime = nowTime.GetTime();
									pTSDataTrans->OnAdviseStatusLine(pStatusLineParams);
								}
								pStatusLineParams->Release();
							}
						}
						break;
					case WM_UNADVISE_STATUS_LINE:
						if (pTSDataTrans != NULL)
						{
							pTSDataTrans->OnUnadviseStatusLine((DWORD)msg.wParam);
						}
						break;
					case WM_CREATE_CONNECTION:
					{
						SConnectionParams *pConnectionParams;
						pConnectionParams = (SConnectionParams*)msg.wParam;

						if (pConnectionParams != NULL)
						{
							if (LastSupplierParams != *pConnectionParams)
							{
								if (pTSDataTrans != NULL)
								{
									if (pTSDataTrans->Created())
									{
										pTSDataTrans->Disconnect();
										delete pTSDataTrans;
										pTSDataTrans = NULL;
									}
								}
							}

							if (pTSDataTrans == NULL)
							{
								pTSDataTrans = new CTSDataTrans;
								pTSDataTrans->SetNowDateTime(nowTime);
							}

							LastSupplierParams = *pConnectionParams;

							if (pTSDataTrans != NULL)
							if (!pTSDataTrans->Created())
							{
								pTSDataTrans->SetConnectionStatus(TS_CONNECTION_CONNECTING, nowTime.GetDate(), nowTime.GetTime());
								pTSDataTrans->SendLastConnectionStatus();

								pTSDataTrans->Create();
							}

							if (pTSDataTrans->Created())
							if (!pTSDataTrans->Connected())
							{
								if (pTSDataTrans->Connect())
								{
									pTSDataTrans->SetConnectionStatus(TS_CONNECTION_ONLINE, nowTime.GetDate(), nowTime.GetTime());
									pTSDataTrans->SendLastConnectionStatus();
								} else
								{
									pTSDataTrans->SetConnectionStatus(TS_CONNECTION_OFFLINE, nowTime.GetDate(), nowTime.GetTime());
									pTSDataTrans->SendLastConnectionStatus();
								}

								pTSDataTrans->OnAdviseConnectionStatus();
							}

							pConnectionParams->Release();

						}
						break;
					}
					case WM_ADVISE_CONNECTION_STATUS:
						{
							TSenderCaller *Caller = NULL;
							BOOL rOp = FALSE;
							Caller = (TSenderCaller*)msg.wParam;
							if (Caller != NULL)
							{
								if (pTSDataTrans == NULL)
								{
									pTSDataTrans = new CTSDataTrans;
									pTSDataTrans->SetNowDateTime(nowTime);
								}
								
								pTSDataTrans->SetNowDateTimeAsSystemTime(nowTime);
								rOp = pTSDataTrans->AddStatusListener(Caller);
								if (!rOp)
									Caller->Release();

								if (!pTSDataTrans->Created())
								{
									pTSDataTrans->SetConnectionStatus(TS_CONNECTION_OFFLINE, nowTime.GetDate(), nowTime.GetTime());
								}								

								pTSDataTrans->SendLastConnectionStatus();
							}
						}
						break;
					case WM_UNADVISE_CONNECTION_STATUS:
						{
							TSenderCaller *Caller = NULL;
							BOOL rOp = FALSE;
							Caller = (TSenderCaller*)msg.wParam;
							if (Caller != NULL)
							{
								if (pTSDataTrans != NULL)
								{
									pTSDataTrans->DeleteStatusListener(Caller);
								}
								CToProcessWrapper::SendRemove(*Caller);

								Caller->Release();
							}

						}
						break;
					case WM_UNADVISE:
						{
							TSenderCaller *Caller = NULL;
							Caller = (TSenderCaller*)msg.wParam;
							if (Caller != NULL)
							{
								if (pTSDataTrans != NULL)
								{
									//pTSDataTrans->OnUnadvise(*Caller);
								}
								CToProcessWrapper::SendRemove(*Caller);

								Caller->Release();
							}
						}
						break;
					case WM_EXIT_THREAD:
						{
fff<<"WM_EXIT_THREAD"<<"\r\n";
							if (_Module.CanUnloadNow2() == S_OK)
							{
								bRun = FALSE;
								break;
							} else
							{
								HANDLE hContinueEvent;
								hContinueEvent = (HANDLE)msg.wParam;
								if (hContinueEvent != NULL)
								{
									SetEvent(hContinueEvent);
								}
							}
							break;
						}
					case WM_TERM_THREAD:
						{
							{
								HANDLE hTermEvent;
								hTermEvent = (HANDLE)msg.wParam;
								if (hTermEvent != NULL)
								{
									SetEvent(hTermEvent);
									bRun = FALSE;
									break;
								}
							}

							break;
						}
				}
		}
	}
}

if (pTSDataTrans != NULL)
{
	if (pTSDataTrans->Created())
	{
		pTSDataTrans->Disconnect();
	}

	delete pTSDataTrans;
	pTSDataTrans = NULL;
}
//-----------------------------------------------------------
CancelWaitableTimer(hTimers[0]);
CloseHandle(hTimers[0]);
//---------------------------------
if (pSupplierThreadMsgs != NULL)
{
	delete pSupplierThreadMsgs;
	pSupplierThreadMsgs = NULL;
}

CSupplierThreadMsgs::UnRegSupplierThreadMsgsWindowProc();
//---------------------------------
fff<<"Finish Supplier Thread id= "<<GetCurrentThreadId()<<"\r\n";
return 0;
};
//-----------------------------------------------------------------------