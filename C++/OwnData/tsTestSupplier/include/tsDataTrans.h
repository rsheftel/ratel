#ifndef _TSDATATRANS_
#define _TSDATATRANS_

#pragma warning (disable : 4786)

#include "SynchCont.h"
#include "ThrRefs.h"
#include "tsbase.h"
#include <vector>
#include "ToProcessWrapper.h"
#include "dateutils.h"
#include "tsDataSender.h"
#include "tsStatusLineSender.h"
#include "CritSectionObj.h"
#include "tsConnectionStatus.h"
#include "TestData.h"
#include <string>
#include <jni.h>
#include <iostream>
#include "LogFile.h" 
#include <fstream>
#include <stdlib.h>
#include <direct.h>
#include <tchar.h> 
#include <strsafe.h>


extern CLogFile fff;	

#define BUFFER_SIZE 10000

using namespace std;
using namespace ts;

//-----------------------------------------------------------------------
class CTSDataTrans : public CTSConnectionStatus, public CNowDateTime
{
public:
//-----------------------------------------------------------------------
	typedef synch_map<DWORD, CStatusLineSender*> CStatusLineSenders;
	typedef synch_map<DWORD, DWORD> CTSTransStatusLineInfo;
//-----------------------------------------------------------------------
	typedef synch_map<DWORD, CTSTransDataSender*> CTransDataSenders;
	typedef synch_map<DWORD, DWORD> CTSTransDataInfo;
//-----------------------------------------------------------------------
	typedef vector<STransactionParams*> CTransactionParamsVector;
	typedef synch_map<string, CTransactionParamsVector> CTransactionsParams;
//-----------------------------------------------------------------------
	typedef synch_map<TSenderCaller*, TSenderCaller*> CSenderCallersMap;
//-----------------------------------------------------------------------

	JNIEnv *env;
	jclass cls;
	//JavaVM *jvm;

	CTSDataTrans()
	{		
		char CurrentPath[BUFFER_SIZE]; 
		getcwd(CurrentPath, BUFFER_SIZE);
		fff << CurrentPath <<"\r\n"; 
		
		TCHAR classpath1[BUFFER_SIZE]; 
		string classpath2;
		ifstream pathFile ("classpath.conf");
		if (pathFile.is_open())
		{
//			while (!pathFile.eof() )
			{
				getline(pathFile, classpath2);
			}
			pathFile.close();
		}
		else 
			fff << "Unable to open file"<<"\r\n"; 

			// Add the systematic integration
	char * basePath = getenv("MAIN");
	if (basePath) {
		TCHAR path[] = "\\Java\\systematic\\lib\\";
		TCHAR systematic[BUFFER_SIZE];
		TCHAR jarBase[BUFFER_SIZE];
		StringCchCopy(jarBase, BUFFER_SIZE, basePath);
		StringCchCat(jarBase, BUFFER_SIZE, path);
		StringCchCopy(systematic, BUFFER_SIZE, jarBase);

		StringCchCat(systematic, BUFFER_SIZE, "\\*.jar");

		WIN32_FIND_DATA ffd;
		HANDLE hFind = FindFirstFile(systematic, &ffd);
		StringCchCopy(classpath1, BUFFER_SIZE, jarBase);
		StringCchCat(classpath1, BUFFER_SIZE, TEXT(ffd.cFileName));
		StringCchCat(classpath1, BUFFER_SIZE, ";");

		do
		{
			StringCchCat(classpath1, BUFFER_SIZE, jarBase);
			StringCchCat(classpath1, BUFFER_SIZE, TEXT(ffd.cFileName));
			StringCchCat(classpath1, BUFFER_SIZE, ";");
			//fff << classpath1 << "\r\n"; 
		}
		while (FindNextFile(hFind, &ffd) != 0);
	}


		m_CurRealTimeTickNumber = 0;
		m_SenderThreadId = 0;
		m_Created = FALSE;

		const int optionsLen = 7;
		JavaVMOption options[optionsLen];
		JavaVMInitArgs vm_args;
		char buf[BUFFER_SIZE];
		StringCchCopy(buf, BUFFER_SIZE, "-Djava.class.path=");
		StringCchCat(buf, BUFFER_SIZE, classpath1);
		StringCchCat(buf, BUFFER_SIZE, ";");
		strcat(buf, classpath2.c_str());

		fff << buf <<" \r\n"; 

		int i = 0;
		//options[i++].optionString = "-server"; 
		options[i++].optionString = buf;//(char*)(("-Djava.class.path=" + classpath).c_str());
		options[i++].optionString = "-Djava.compiler=NONE";
		options[i++].optionString = "-Djava.util.logging.config.file=logging.properties";

		options[i++].optionString = "-Dcom.sun.management.jmxremote.port=9797";
		options[i++].optionString = "-Dcom.sun.management.jmxremote.authenticate=false";
		options[i++].optionString = "-Dcom.sun.management.jmxremote.ssl=false";
		//options[i++].optionString = "-verbose:jni";
		options[i++].optionString = "-Xmx256m";


		vm_args.version = JNI_VERSION_1_6;
		vm_args.options = options;
		vm_args.nOptions = optionsLen;
		vm_args.ignoreUnrecognized = JNI_FALSE;
		JavaVM *jvm;

		if(JNI_CreateJavaVM(&jvm, (void **)&env, &vm_args) == JNI_ERR ) 
		{
			fff<<"Error invoking the JVM"<<"\r\n";
		}

		cls = env->FindClass("com/fftw/owndata/Loader");
		if( cls == NULL ) 
		{
			fff<<"can't find class"<<"\r\n";
			return;
		}
	}

	~CTSDataTrans();

//-----------------------------------------------------------------------
	BOOL OnAdviseTransaction(STransactionParams* transParams);
//-----------------------------------------------------------------------
	BOOL OnUnadviseTransaction(DWORD dwTransactionId);
//-----------------------------------------------------------------------
	BOOL OnAdviseStatusLine(SStatusLineParams* pStatusLineParams);
	BOOL OnUnadviseStatusLine(DWORD dwTransactionId);
//-----------------------------------------------------------------------
	BOOL OnUnadvise(TSenderCaller& Caller);
//-----------------------------------------------------------------------
	void enter_section_transinfo()
	{
		m_TSTransDataInfo.enter_section();
		m_TransDataSenders.enter_section();
	}
	void leave_section_transinfo()
	{
		m_TransDataSenders.leave_section();
		m_TSTransDataInfo.leave_section();
	}
//-----------------------------------------------------------------------
	void enter_section_statusinfo()
	{
		m_TSTransStatusLineInfo.enter_section();
		m_StatusLineSenders.enter_section();
	}
	void leave_section_statusinfo()
	{
		m_StatusLineSenders.leave_section();
		m_TSTransStatusLineInfo.leave_section();
	}
//-----------------------------------------------------------------------
	CTSTransDataSender* GetSenderByTransId(DWORD idTrans);
	CTSTransDataSender* GetSenderByTSTransId(DWORD idTrans);
//-----------------------------------------------------------------------
	CStatusLineSender* GetStatusSenderByTransId(DWORD idTrans);
	CStatusLineSender* GetStatusSenderByTSTransId(DWORD idTrans);
//-----------------------------------------------------------------------
	void CompleteTimeOutedTransactions(CUDFDateTime& nowTime);
//-----------------------------------------------------------------------
	void SetSenderThreadId(DWORD idSenderThread)
	{
		m_SenderThreadId = idSenderThread;
	}
	DWORD GetSenderThreadId()
	{
		return m_SenderThreadId;
	}
//-----------------------------------------------------------------------
	virtual BOOL OnAdviseConnectionStatus();
//-----------------------------------------------------------------------
	virtual BOOL Created()
	{
		return m_Created;
	}
	virtual BOOL Create()
	{
		m_Created = TRUE;
		return TRUE;
	}
//-----------------------------------------------------------------------
	virtual BOOL Connected()
	{
		return m_Connected;
	}
	virtual BOOL Connect()
	{
		m_Connected = TRUE;
		return TRUE;
	}
	virtual BOOL Disconnect()
	{
		m_Connected = FALSE;
		return TRUE;
	}
//-----------------------------------------------------------------------
	void CreateHistoryTickArray(CTransDataSenders::iterator itTransDataSenders, SSupplierTick*& Ticks, DWORD& TickCount)
	{
		CreateTick("getHistoryTicks", itTransDataSenders, Ticks, TickCount);
	}

	void CreateRealTimeTickArray(CTransDataSenders::iterator itTransDataSenders, SSupplierTick*& Ticks, DWORD& TickCount)
	{
		CreateTick("getLiveTicks", itTransDataSenders, Ticks, TickCount);
	}

	void CreateTick(const char* methodName, CTransDataSenders::iterator itTransDataSenders, SSupplierTick*& Ticks, DWORD& TickCount)
	{
		CTSTransDataSender *pTransDataSender = itTransDataSenders->second;
		long tranId = pTransDataSender->GetTSTransId();

		env->ExceptionClear();
		jmethodID mid = env->GetStaticMethodID(cls, methodName, "(I)[[D");
		jobjectArray objArray = (jobjectArray)env->CallStaticObjectMethod(cls, mid, pTransDataSender->GetTSTransId());	
		TickCount = env->GetArrayLength(objArray);

		Ticks = NULL;
		SSupplierTick* CurTick = NULL;
		Ticks = (SSupplierTick*)new char[sizeof(SSupplierTick)*TickCount];
		ZeroMemory(Ticks, sizeof(SSupplierTick)*TickCount);
		CurTick = Ticks;
		DWORD b;
		for(b = 0; b < TickCount; b++)
		{	
			jboolean isCopy;
			jdoubleArray row = (jdoubleArray)env->GetObjectArrayElement(objArray, b);
			jdouble *TickData = env->GetDoubleArrayElements(row, &isCopy);///

			//fff<<"********* Date= "<<dt.GetText().c_str()<<endl;		
			CUDFDateTime *timestamp = new CUDFDateTime(TickData[0]); 
//			string tmp = ttt->GetText();

			CurTick->Date = timestamp->GetDate();
			CurTick->Time = timestamp->GetTime();
			CurTick->Price = TickData[1];
			CurTick->Volume = TickData[2];
			CurTick->Status = (tagTSBARSTATUS)(TS_BAR_OPEN|TS_BAR_CLOSE|TS_BAR_REAL_TIME_DATA);
			CurTick++;

			env->ReleaseDoubleArrayElements(row, TickData, 0);
			delete timestamp;
			timestamp = NULL;
		}
	}
//-----------------------------------------------------------------------
//	void CreateHistoryBarArray(CUDFDateTime& StartDateTime, CUDFDateTime& FinishDateTime, SSupplierBarEx*& Bars, DWORD& BarsCount)
	void CreateHistoryBarArray(CTransDataSenders::iterator itTransDataSenders, SSupplierBarEx*& Bars, DWORD& BarsCount)
	{
		CTSTransDataSender *pTransDataSender = itTransDataSenders->second;

		env->ExceptionClear();
		//jmethodID mid = env->GetMethodID(cls, "<init>", "()V");
		//jobject jobj = env->NewObject(cls, mid);
		//jmethodID mid1 = env->GetMethodID(cls, "getHistoryBar", "([Ljava/lang/String;)[[D");
		jmethodID mid1 = env->GetStaticMethodID(cls, "getHistoryBar", "([Ljava/lang/String;)[[D");

		jobjectArray str_array = env->NewObjectArray(
			6, 
			env->FindClass("java/lang/String"), 
			env->NewStringUTF(""));

		CUDFDateTime StartDateTime = pTransDataSender->GetStartDateTime();
		CUDFDateTime FinishDateTime = pTransDataSender->GetFinishDateTime();

		int i = 0;
		env->SetObjectArrayElement(str_array, i++, env->NewStringUTF(pTransDataSender->GetSymbolCategoryOfQuery().c_str()));
		env->SetObjectArrayElement(str_array, i++, env->NewStringUTF(pTransDataSender->GetExchangeNameOfQuery().c_str()));
		env->SetObjectArrayElement(str_array, i++, env->NewStringUTF(pTransDataSender->GetSymbolNameOfQuery().c_str()));
		env->SetObjectArrayElement(str_array, i++, env->NewStringUTF(StartDateTime.GetText().c_str()));
		env->SetObjectArrayElement(str_array, i++, env->NewStringUTF(FinishDateTime.GetText().c_str()));

		char ResolutionStr[33];
		itoa(pTransDataSender->GetResolution(), ResolutionStr, 10);
		env->SetObjectArrayElement(str_array, i++, env->NewStringUTF(ResolutionStr));
		//env->SetObjectArrayElement(str_array, i++, env->NewStringUTF(pTransDataSender->GetCategoryName()));

		jobjectArray objArray = (jobjectArray)env->CallStaticObjectMethod(cls, mid1, str_array);	
		BarsCount = env->GetArrayLength(objArray);

		Bars = NULL;
		CUDFDateTime dt, dt1;
		SSupplierBarEx* CurBar = NULL;
		//BarsCount = sizeof(HistoryDataMinutes) / sizeof(HistoryDataMinutes[0]);
		Bars = (SSupplierBarEx*)new char[sizeof(SSupplierBarEx)*BarsCount];
		ZeroMemory(Bars, sizeof(SSupplierBarEx)*BarsCount);
		CurBar = Bars;
		int offset = (FinishDateTime.GetTime() - StartDateTime.GetTime());
		dt = StartDateTime;
		//dt.SetInterval(dt.GetInterval()/interval*interval);
		dt += (offset);

		dt1 = dt;
		DWORD b;
		/*
		fff<<"@@@@@@@@@ Date= "<<m_NowDateTime.GetText().c_str()<<endl;	
		fff<<"@@@@@@@@@ Interval=%d "<<m_NowDateTime.GetInterval()<<endl;	
		fff<<"@@@@@@@@@ Date= "<<m_NowDateTime.GetDate()<<endl;	
		//fff<<"@@@@@@@@@ Date= "<<m_NowDateTime.getGetDate()<<endl;		
		//fff<<"@@@@@@@@@ Date= "<<FinishDateTime.GetText().c_str()<<endl;	
*/
		for(b = 0; b < BarsCount; b++)
		{	
			jdoubleArray row = (jdoubleArray)env->GetObjectArrayElement(objArray, b);
			jdouble *HistoryData = env->GetDoubleArrayElements(row, 0);///

			//fff<<"********* Date= "<<dt.GetText().c_str()<<endl;		
			CUDFDateTime *timestamp = new CUDFDateTime(HistoryData[0]); 
//			string tmp = ttt->GetText();

			CurBar->Date = timestamp->GetDate();
			CurBar->Time = timestamp->GetTime();
//  Open,  High, Low, Close, UpVol, DownVol, UnchVol, TotalVol, UpTicks, DownTicks, UnchTicks, TotalTicks
			int i = 1;
			CurBar->Open = HistoryData[i++];
			CurBar->High = HistoryData[i++];
			CurBar->Low = HistoryData[i++];
			CurBar->Close = HistoryData[i++];
			CurBar->UpVolume = HistoryData[i++];
			CurBar->DownVolume = HistoryData[i++];
			CurBar->UnchangedVolume = HistoryData[i++];
			CurBar->TotalVolume = HistoryData[i++];
			CurBar->UpTicks = HistoryData[i++];
			CurBar->DownTicks = HistoryData[i++];
			CurBar->UnchangedTicks = HistoryData[i++];
			CurBar->TotalTicks = HistoryData[i++];
			CurBar->Status = (tagTSBARSTATUS)(TS_BAR_OPEN|TS_BAR_CLOSE|TS_BAR_HISTORICAL_DATA);
			CurBar->OpenInterest = HistoryData[i++];

			CurBar++;
			env->ReleaseDoubleArrayElements(row, HistoryData, 0);
			delete timestamp;
			timestamp = NULL;
		}

		BarsCount = b;
	}
//-----------------------------------------------------------------------
	void OnDataChanged()
	{
		CTransDataSenders::iterator itTransDataSenders;
		CTSTransDataSender *sender = NULL;

		enter_section_transinfo();
		enter_section_statusinfo();

//--------------------- SymbolData ---------------------
		itTransDataSenders = m_TransDataSenders.end();

		if (!m_TransDataSenders.empty())
		do
		{
			itTransDataSenders--;
			sender = itTransDataSenders->second;
			sender->AddRef();

			if (!sender->RecvFullHistory())
			{
				if (sender->GetResolution() == TS_RESOLUTION_TICK)
				{
					SSupplierTick* HistoryTicks = NULL;
					DWORD HistoryTicksCount = 0;
					CreateHistoryTickArray(itTransDataSenders, HistoryTicks, HistoryTicksCount);
					if (HistoryTicksCount > 0 || itTransDataSenders->second->m_TransactionMode == EHistoryDataProvide)
					{
						sender->OnDataChanged(HistoryTicks, HistoryTicksCount);
					}

					if (HistoryTicks != NULL)
					{
						delete[] HistoryTicks;
						HistoryTicks = NULL;
					}
				} else
				if (sender->GetResolution() == TS_RESOLUTION_DAY)
				{
					SSupplierBarEx* HistoryBars = NULL;
					DWORD HistoryBarsCount = 0;
					CreateHistoryBarArray(itTransDataSenders, HistoryBars, HistoryBarsCount);
					sender->OnDataChanged(HistoryBars, HistoryBarsCount);

					if (HistoryBars != NULL)
					{
						delete[] HistoryBars; 
						HistoryBars = NULL;
					}
				}
			} else
			{
				if (sender->GetResolution() == TS_RESOLUTION_TICK)
				{
					SSupplierTick* RealTimeTicks = NULL;
					DWORD RealTimeTickCount = 0;
					CreateRealTimeTickArray(itTransDataSenders, RealTimeTicks, RealTimeTickCount);
					
					if (RealTimeTickCount > 0)
					{
						sender->OnDataChanged(RealTimeTicks, RealTimeTickCount);

						CStatusLineSenders::iterator itStatusLineSenders;
						CStatusLineSender *StatusLineSender = NULL;
						SAFEARRAY *pStatusDataArray = NULL;
						SStatusLineMessage *StatusLineMessage = NULL;
						DWORD szStatusData = 0;
						LPVOID pStatusData = NULL;

						for(itStatusLineSenders = m_StatusLineSenders.begin();
							itStatusLineSenders != m_StatusLineSenders.end();
							itStatusLineSenders++)
						{
							StatusLineSender = itStatusLineSenders->second;
							//if (sender->GetTSTransId() == StatusLineSender->GetTSTransId())
							if (sender->GetSymbolNameOfQuery() == StatusLineSender->GetSymbolNameOfQuery())
							{
								StatusLineSender->AddRef();

								pStatusDataArray = NULL;
								pStatusData = StatusLineSender->AllocBufferForStatusLineMessage(pStatusDataArray, StatusLineMessage, szStatusData);
								if (pStatusDataArray != NULL)
								{								
									StatusLineMessage->m_Mask = TS_STATUS_LINE_DATE|TS_STATUS_LINE_TIME|TS_STATUS_LINE_LAST;
									StatusLineMessage->m_StatusLine.Date = RealTimeTicks->Date;
									StatusLineMessage->m_StatusLine.Time = RealTimeTicks->Time;
									StatusLineMessage->m_StatusLine.Last = RealTimeTicks->Price;
									//StatusLineMessage->m_StatusLine.High = RealTimeTicks->Volume;

									StatusLineSender->SendStatusLineData(pStatusDataArray);
								}

								StatusLineSender->Release();
							}
						}
					}

					if (RealTimeTicks != NULL)
					{
						delete[] RealTimeTicks;
						RealTimeTicks = NULL;
					}
				}
			}

			sender->Release();
		} while (itTransDataSenders != m_TransDataSenders.begin());
				
		leave_section_statusinfo();
		leave_section_transinfo();
	}
//-----------------------------------------------------------------------
protected:

	CTSTransStatusLineInfo m_TSTransStatusLineInfo;
	CStatusLineSenders m_StatusLineSenders;

	CTSTransDataInfo m_TSTransDataInfo;
	CTransDataSenders m_TransDataSenders;

	DWORD m_SenderThreadId;
	BOOL m_Created;
	BOOL m_Connected;
	DWORD m_CurRealTimeTickNumber;
};
//-----------------------------------------------------------------------
CTSDataTrans::~CTSDataTrans()
{
	CTransDataSenders::iterator it;

	enter_section_transinfo();

	for(it = m_TransDataSenders.begin(); it != m_TransDataSenders.end(); it++)
	{
		it->second->Release();
	}
	m_TransDataSenders.clear();
	m_TSTransDataInfo.clear();

	leave_section_transinfo();

	CStatusLineSenders::iterator itStatusLineSenders;

	enter_section_statusinfo();

	for(itStatusLineSenders = m_StatusLineSenders.begin(); itStatusLineSenders != m_StatusLineSenders.end(); itStatusLineSenders++)
	{
		itStatusLineSenders->second->Release();
	}
	m_StatusLineSenders.clear();
	m_TSTransStatusLineInfo.clear();

	leave_section_statusinfo();

	//jvm->DestroyJavaVM();
}

//-----------------------------------------------------------------------
BOOL CTSDataTrans::OnAdviseTransaction(STransactionParams* transParams)
{///////////TODO
	BOOL res = FALSE;
	string qText;
	CTSTransDataSender *tsTransDataSender=NULL;

		fff<<"============= Start OnAdviseTransaction"<<"\r\n";

		fff<<"TransId= "<<transParams->m_TransactionId<<"\r\n";
		fff<<"Symbol= "<<transParams->m_SymbolName.c_str()<<"\r\n";
		//fff<<"AttrSymbol= "<<symbAttrs.
		fff<<"Exchange= "<<transParams->m_ExchangeName.c_str()<<"\r\n";
		fff<<"Sessions= "<<transParams->m_Sessions.c_str()<<"\r\n";

		CUDFDateTime dt;
		dt.SetDateTime(transParams->m_StartDate, transParams->m_StartTime);
		fff<<"StartTime= "<<dt.GetText().c_str()<<"\r\n";
		dt.SetDateTime(transParams->m_FinishDate, transParams->m_FinishTime);
		fff<<"FinishTime= "<<dt.GetText().c_str()<<"\r\n";

		fff<<"Resolution= "<<transParams->m_Resolution<<"\r\n";
		fff<<"ResolutionSize= "<<transParams->m_ResolutionSize<<"\r\n";

	tsTransDataSender = new CTSTransDataSender(transParams);

	if (tsTransDataSender == NULL)
		return FALSE;

	tsTransDataSender->AddRef();

	tsTransDataSender->SetSenderThreadId(m_SenderThreadId);

	enter_section_transinfo();

	m_TransDataSenders.insert(CTransDataSenders::value_type(transParams->m_TransactionId, tsTransDataSender));
	m_TSTransDataInfo.insert(CTSTransDataInfo::value_type(transParams->m_TransactionId, transParams->m_TransactionId));
	long tranId = transParams->m_TransactionId;

	if (transParams->m_Resolution == TS_RESOLUTION_TICK)
	{
		env->ExceptionClear();
		jmethodID mid = env->GetStaticMethodID(cls, "subscribe", "(ILjava/lang/String;)Z");
		jboolean isRT = env->CallStaticBooleanMethod(cls, mid, transParams->m_TransactionId, 
			env->NewStringUTF(transParams->m_SymbolName.c_str()));	

		if (!isRT)
			tsTransDataSender->m_TransactionMode = EHistoryDataProvide;
	}

	leave_section_transinfo();

	res = TRUE;

	fff<<"============= Finish OnAdviseTransaction"<<"\r\n";

	return res;
}
//-----------------------------------------------------------------------
BOOL CTSDataTrans::OnUnadviseTransaction(DWORD dwTransactionId)
{
	DWORD trId;
	vector<DWORD> vRemoveTrIds;

	fff<<"Start OnUnadviseTransaction transId= "<<dwTransactionId<<"\r\n";

	enter_section_transinfo();

	CTSTransDataSender *sender = NULL;

	CTSTransDataInfo::iterator itTSTransDataInfo;
	CTransDataSenders::iterator itTransDataSender;

	itTSTransDataInfo = m_TSTransDataInfo.blocking_find(dwTransactionId);

	if (itTSTransDataInfo != m_TSTransDataInfo.end())
	{
		trId = itTSTransDataInfo->second;
		itTransDataSender = m_TransDataSenders.blocking_find(trId);
		if (itTransDataSender != m_TransDataSenders.end())
		{
			sender = itTransDataSender->second;
			if (sender != NULL)
			{
				if (sender->GetResolution() == TS_RESOLUTION_TICK)
				{
					env->ExceptionClear();
					jmethodID mid = env->GetStaticMethodID(cls, "unsubscribe", "(ILjava/lang/String;)V");
					env->CallStaticVoidMethod(cls, mid, dwTransactionId, 
						env->NewStringUTF(sender->GetSymbolNameOfQuery().c_str()));	
				}

				m_TSTransDataInfo.erase(dwTransactionId);
				m_TransDataSenders.erase(trId);

				vRemoveTrIds.push_back(trId);
				sender->Release();
				sender = NULL;
			}
		}

		m_TransDataSenders.leave_section();
	}

	m_TSTransDataInfo.leave_section();

	leave_section_transinfo();

	fff<<"Finish OnUnadviseTransaction transId= "<<dwTransactionId<<"\r\n";

	return TRUE;
}
//-----------------------------------------------------------------------
BOOL CTSDataTrans::OnAdviseStatusLine(SStatusLineParams* pStatusLineParams)
{
	BOOL res = FALSE;
	string qText;
	CStatusLineSender *pStatusDataSender=NULL;

	pStatusDataSender = new CStatusLineSender(pStatusLineParams);

	if (pStatusDataSender == NULL)
		return FALSE;

	pStatusDataSender->AddRef();

	pStatusDataSender->SetSenderThreadId(m_SenderThreadId);

	enter_section_statusinfo();

	m_StatusLineSenders.insert(CStatusLineSenders::value_type(pStatusLineParams->m_TransactionId, pStatusDataSender));
	m_TSTransStatusLineInfo.insert(CTSTransStatusLineInfo::value_type(pStatusLineParams->m_TransactionId, pStatusLineParams->m_TransactionId));

	leave_section_statusinfo();

	res = TRUE;

	return res;
}
//-----------------------------------------------------------------------
BOOL CTSDataTrans::OnUnadviseStatusLine(DWORD dwTransactionId)
{
	DWORD trId;
	vector<DWORD> vRemoveTrIds;

	enter_section_statusinfo();

	CStatusLineSender *sender = NULL;

	CTSTransStatusLineInfo::iterator itTSTransStatusLineInfo;
	CStatusLineSenders::iterator itStatusLineSenders;

	itTSTransStatusLineInfo = m_TSTransStatusLineInfo.blocking_find(dwTransactionId);

	if (itTSTransStatusLineInfo != m_TSTransStatusLineInfo.end())
	{
		trId = itTSTransStatusLineInfo->second;
		itStatusLineSenders = m_StatusLineSenders.blocking_find(trId);
		if (itStatusLineSenders != m_StatusLineSenders.end())
		{
			sender = itStatusLineSenders->second;
			if (sender != NULL)
			{
				m_TSTransStatusLineInfo.erase(dwTransactionId);
				m_StatusLineSenders.erase(trId);

				vRemoveTrIds.push_back(trId);
				sender->Release();
				sender = NULL;
			}
		}

		m_StatusLineSenders.leave_section();
	}

	m_TSTransStatusLineInfo.leave_section();

	leave_section_statusinfo();

	return TRUE;
}
//-----------------------------------------------------------------------
void CTSDataTrans::CompleteTimeOutedTransactions(CUDFDateTime& nowTime)
{
	CTransDataSenders::iterator itTransDataSender;

	enter_section_transinfo();

	for(itTransDataSender = m_TransDataSenders.begin(); itTransDataSender != m_TransDataSenders.end(); itTransDataSender++)
	{
		if (itTransDataSender->second->CheckTimeOuted(nowTime))
		if (itTransDataSender->second->RecvFullHistory())
			itTransDataSender->second->Completed();
	}

	leave_section_transinfo();
}
//-----------------------------------------------------------------------
CTSTransDataSender* CTSDataTrans::GetSenderByTransId(DWORD idTrans)
{
	CTSTransDataSender *sender = NULL;	
	CTransDataSenders::iterator it;
	it = m_TransDataSenders.blocking_find(idTrans);

	if (it != m_TransDataSenders.end())
	{
		sender = it->second;
		if (sender != NULL)
			sender->AddRef();
	}

	m_TransDataSenders.leave_section();

	return sender;
}
//-----------------------------------------------------------------------
CTSTransDataSender* CTSDataTrans::GetSenderByTSTransId(DWORD idTrans)
{
	DWORD idTSTrans = 0;
	CTSTransDataSender *sender = NULL;	
	CTSTransDataInfo::iterator it;

	it = m_TSTransDataInfo.blocking_find(idTrans);

	if (it != m_TSTransDataInfo.end())
		idTSTrans = it->second;

	if (idTSTrans != 0)
		sender = GetSenderByTransId(idTSTrans);

	m_TSTransDataInfo.leave_section();

	return sender;
}
//-----------------------------------------------------------------------
CStatusLineSender* CTSDataTrans::GetStatusSenderByTransId(DWORD idTrans)
{
	CStatusLineSender *sender = NULL;	
	CStatusLineSenders::iterator it;
	it = m_StatusLineSenders.blocking_find(idTrans);

	if (it != m_StatusLineSenders.end())
	{
		sender = it->second;
		if (sender != NULL)
			sender->AddRef();
	}

	m_StatusLineSenders.leave_section();

	return sender;
}
//-----------------------------------------------------------------------
CStatusLineSender* CTSDataTrans::GetStatusSenderByTSTransId(DWORD idTrans)
{
	DWORD idTSTrans = 0;
	CStatusLineSender *sender = NULL;	
	CTSTransStatusLineInfo::iterator it;

	it = m_TSTransStatusLineInfo.blocking_find(idTrans);

	if (it != m_TSTransStatusLineInfo.end())
		idTSTrans = it->second;

	if (idTSTrans != 0)
		sender = GetStatusSenderByTransId(idTSTrans);

	m_TSTransStatusLineInfo.leave_section();

	return sender;
}
//-----------------------------------------------------------------------
BOOL CTSDataTrans::OnUnadvise(TSenderCaller& Caller)
{
	CTransDataSenders::iterator itTransDataSender;
	m_TransDataSenders.enter_section();
	for(itTransDataSender = m_TransDataSenders.begin(); itTransDataSender != m_TransDataSenders.end(); itTransDataSender++)
	{
		itTransDataSender->second->OnUnadvise(Caller);
	}
	m_TransDataSenders.leave_section();
	return TRUE;
}
//-----------------------------------------------------------------------
BOOL CTSDataTrans::OnAdviseConnectionStatus()
{
	return TRUE;
}
//-----------------------------------------------------------------------


#endif