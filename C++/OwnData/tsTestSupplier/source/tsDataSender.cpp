#include "tsDataSender.h"

//-----------------------------------------------------------------------
void CTSTransDataSender::SendHistoryData(SAFEARRAY* pBarsArray)
{
	_ASSERT(pBarsArray != NULL);
	if (pBarsArray == NULL)
		return;

	SafeArrayUnaccessData(pBarsArray);

	_ASSERT(m_Caller != NULL);
	if (m_Caller == NULL)
	{
		SafeArrayDestroy(pBarsArray);
		return;
	}

	if (!IsStartRealTime() && !IsCompleted())
	{
		CToProcessWrapper::OnHistoryData(*m_Caller, GetTSTransId(), pBarsArray);
	} else
	{
		SafeArrayDestroy(pBarsArray);
	}
}
//-----------------------------------------------------------------------
void CTSTransDataSender::SendRealTimeData(SAFEARRAY* pBarsArray)
{
	_ASSERT(pBarsArray != NULL);
	if (pBarsArray == NULL)
		return;

	SafeArrayUnaccessData(pBarsArray);

	_ASSERT(m_Caller != NULL);
	if (m_Caller == NULL)
	{
		SafeArrayDestroy(pBarsArray);
		return;
	}

	if (IsStartRealTime() && !IsCompleted())
	{
		CToProcessWrapper::OnRealTimeData(*m_Caller, GetTSTransId(), pBarsArray);
	} else
	{
		SafeArrayDestroy(pBarsArray);
	}
}
//-----------------------------------------------------------------------
void CTSTransDataSender::StartRealTime()
{
	_ASSERT(m_Caller != NULL);
	if (m_Caller == NULL)
		return;

	if (!IsStartRealTime() && !IsCompleted())
	{
		m_bStartRealTime = TRUE;
		CToProcessWrapper::OnStartRealTime(*m_Caller, GetTSTransId());
	}
}
//-----------------------------------------------------------------------
void CTSTransDataSender::Completed()
{
	_ASSERT(m_Caller != NULL);
	if (m_Caller == NULL)
		return;

	if (!IsCompleted())
	{	
		m_bCompleted = TRUE;

		CToProcessWrapper::OnCompleted(*m_Caller, GetTSTransId());
	}
}
//-----------------------------------------------------------------------
LPVOID CTSTransDataSender::AllocBufferForStatus(tagTSCONNECTIONSTATUS status, SAFEARRAY*& pStatusStruct, DWORD& szData)
{
	DWORD size;
	LPVOID pData = NULL;
	pStatusStruct = NULL;
	szData = 0;
	size = sizeof(SConnectionStatus);
	SConnectionStatus *pConnectionStatus = NULL;
	CUDFDateTime dtNow;
	GetNowDateTime(dtNow);
	
	pStatusStruct = SafeArrayCreateVector(VT_UI1, 0, size);
	_ASSERT(pStatusStruct);
	if (SafeArrayAccessData(pStatusStruct, (void**)&pConnectionStatus) == S_OK)
	{
		szData = size;
		pData = pConnectionStatus;
		pConnectionStatus->m_Mode = GetTransactionMode();
		pConnectionStatus->m_Date = dtNow.GetDate();
		pConnectionStatus->m_Time = dtNow.GetTime();
		pConnectionStatus->m_Status = status;
	}

	return pData;
}
//-----------------------------------------------------------------------
void CTSTransDataSender::SendLastStatus()
{
	SAFEARRAY *pStatusStruct = NULL;
	DWORD szData;
	LPVOID pData = NULL;

	pData = AllocBufferForStatus(m_Status, pStatusStruct, szData);
	if (pData != NULL)
	{
		ChangeStatus(pStatusStruct);
	}
}
//-----------------------------------------------------------------------
void CTSTransDataSender::ChangeStatus(SAFEARRAY* pStatusInfo)
{
	SafeArrayUnaccessData(pStatusInfo);

	_ASSERT(m_Caller != NULL);
	if (m_Caller == NULL)
		return;

	CToProcessWrapper::OnStatusChanged(*m_Caller, GetTSTransId(), pStatusInfo);
}
//-----------------------------------------------------------------------
CTSTransDataSender::CTSTransDataSender(STransactionParams* transParams)
{
	m_RecvFullHistory = FALSE;
	m_bCompleted = FALSE;
	m_bStartRealTime = FALSE;
	m_transParams = *transParams;
//-----------------------------------------------------------------------
	m_StartDateTime.SetDateTime(m_transParams.m_StartDate, m_transParams.m_StartTime);
	m_FinishDateTime.SetDateTime(m_transParams.m_FinishDate, m_transParams.m_FinishTime);
//-----------------------------------------------------------------------
	CUDFDateTime NowDateTime;
	NowDateTime.SetDateTime(m_transParams.m_NowDate, m_transParams.m_NowTime);
	SetNowDateTime(NowDateTime);

	if (m_FinishDateTime <= NowDateTime)
		m_TransactionMode = EHistoryDataProvide;
	else
		m_TransactionMode = ERealTimeDataProvide;

	m_Caller = m_transParams.m_pSenderCaller;
	_ASSERT(m_Caller != NULL);

	SetStatus(TS_CONNECTION_OFFLINE, NowDateTime);
	ResetInvalidStatus();
}
//-----------------------------------------------------------------------
BOOL CTSTransDataSender::OnDataChanged(LPVOID pData, DWORD BarsCount)
{
	BOOL res = FALSE;
	SAFEARRAY* pBarsArray = NULL;
	DWORD sizeData = 0;

	if (!RecvFullHistory())
	{
		OnRecvFullHistory();

		if (GetResolution() == TS_RESOLUTION_TICK)
		{
			SSupplierTick* ticks = (SSupplierTick*)pData;
			SSupplierTick* CurTick = NULL;

			fff<<"OnDataChanged History Ticks Count= "<<BarsCount<<" TransId= "<<GetTSTransId()<<"\r\n";
			fff<<"Start= "<<GetStartDateTime().GetText().c_str()<<"  Finish= "<<GetFinishDateTime().GetText().c_str()<<"\r\n";
			CurTick = ticks;
			CUDFDateTime dt;
			/*
			for(DWORD t = 0; t < BarsCount; t++)
			{
				dt.SetDateTime(CurTick->Date, CurTick->Time);
				fff<<" Date= "<<dt.GetText().c_str()<<" Price= "<<CurTick->Price<<endl;				
				CurTick++;
			}*/

			AllocBufferForTSData(BarsCount, pBarsArray, CurTick, sizeData);
			if (CurTick != NULL)
			{
				memcpy(CurTick, pData, BarsCount*sizeof(SSupplierTick));
				SendHistoryData(pBarsArray);
			}			
		} else
		if (GetResolution() == TS_RESOLUTION_DAY)
		{
			SSupplierBarEx* bars = (SSupplierBarEx*)pData;
			SSupplierBarEx* CurBar = NULL;

			fff<<"OnDataChanged History Day Count= "<<BarsCount<<" TransId= "<<GetTSTransId()<<"\r\n";
			fff<<"Start= "<<GetStartDateTime().GetText().c_str()<<"  Finish= "<<GetFinishDateTime().GetText().c_str()<<"\r\n";
			CurBar = bars;
			CUDFDateTime dt;
			/*
			for(DWORD b = 0; b < BarsCount; b++)
			{
				dt.SetDateTime(CurBar->Date, CurBar->Time);
				fff<<" Date= "<<dt.GetText().c_str()<<" Open= "<<CurBar->Open<<" Close= "<<CurBar->Close<<" High= "<<CurBar->High<<" Low= "<<CurBar->Low<<" Volumn= "<<CurBar->TotalVolume<<endl;				
				CurBar++;
			}*/

			AllocBufferForTSData(BarsCount, pBarsArray, CurBar, sizeData);
			if (CurBar != NULL)
			{
				memcpy(CurBar, pData, BarsCount*sizeof(SSupplierBarEx));
				SendHistoryData(pBarsArray);
			}			
		}

		if (RecvFullHistory())
		{
			if (GetTransactionMode() == EHistoryDataProvide)
				Completed();
			else
				CheckCompletedStatus();
		}

		if (RecvFullHistory() && !IsCompleted())
		{
			StartRealTime();
		}

	} else
	{
		if (GetResolution() == TS_RESOLUTION_TICK)
		{
			SSupplierTick* tick = (SSupplierTick*)pData;
			CUDFDateTime dt;
			dt.SetDateTime(tick->Date, tick->Time);

			if (dt >= m_FinishDateTime)
			{
				Completed();
			} else
			{
				if (dt >= m_StartDateTime)
				{
					//fff<<"*****^^^^^^^^OnDataChanged RT Day Count= "<<BarsCount<<" TransId= "<<GetTSTransId()<<endl;
					SSupplierTick* CurTick = NULL;
					AllocBufferForTSData(BarsCount, pBarsArray, CurTick, sizeData);
					if (CurTick != NULL)
					{
						memcpy(CurTick, pData, BarsCount*sizeof(SSupplierTick));
						SendRealTimeData(pBarsArray);
					}			
				}
			}
		}
	}

	res = TRUE;

	return res;
}
//-----------------------------------------------------------------------