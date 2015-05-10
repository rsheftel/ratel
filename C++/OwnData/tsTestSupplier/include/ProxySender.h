#pragma once

class CProxyTSSupplier
{
public:
	virtual HRESULT __stdcall OnHistoryData(long Id, SAFEARRAY* psa) = 0;
	virtual HRESULT __stdcall OnStartRealTime(long Id, SAFEARRAY* psa) = 0;
	virtual HRESULT __stdcall OnRealTimeData(long Id, SAFEARRAY* psa) = 0;
	virtual HRESULT __stdcall OnCompleted(long Id, SAFEARRAY* psa) = 0;
	virtual HRESULT __stdcall OnStatusLine(long Id, SAFEARRAY* psa) = 0;
	virtual void __stdcall  ReleaseObject() = 0;
	virtual HRESULT __stdcall  OnStatusChanged(long Id, SAFEARRAY* psa) = 0;
};

struct SDataParams
{
	long Id;
	SAFEARRAY* psa;
};
