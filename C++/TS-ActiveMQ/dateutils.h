#ifndef _DATE_UTILS_
#define _DATE_UTILS_

#include "udfdate.h"
#include "CritSectionObj.h"

//-----------------------------------------------------------------------
inline CUDFDateTime GetUdfDateTimeFromSystemTime(SYSTEMTIME& dt)
{
	CUDFDateTime dt1;
	dt1.SetDateTime(dt.wDay, dt.wMonth, dt.wYear, dt.wHour, dt.wMinute, dt.wSecond);
	return dt1;
}
//-----------------------------------------------------------------------
int GetNumOfMonth(const char* month);
//-----------------------------------------------------------------------
class CNowDateTime
{
public:
//-----------------------------------------------------------------------
	CNowDateTime()
	{
	}
//-----------------------------------------------------------------------
	BOOL GetNowDateTime(CUDFDateTime& nowDateTime)
	{
		m_TimeCS.Lock();
		nowDateTime = m_NowDateTime;
		m_TimeCS.UnLock();
		return (nowDateTime.GetDate() != 0);
	}
	BOOL GetNowDateTime(INT& nowDate, INT& nowTime)
	{
		m_TimeCS.Lock();
		nowDate = m_NowDateTime.GetDate();
		nowTime = m_NowDateTime.GetTime();
		m_TimeCS.UnLock();
		return (nowDate != 0);
	}
	void SetNowDateTime(CUDFDateTime& nowDateTime)
	{
		m_TimeCS.Lock();
		if (nowDateTime > m_NowDateTime)
			m_NowDateTime = nowDateTime;
		m_TimeCS.UnLock();
	}
	void SetNowDateTime(INT nowDate, INT nowTime)
	{
		CUDFDateTime dt;
		dt.SetDateTime(nowDate, nowTime);
		m_TimeCS.Lock();
		if (dt > m_NowDateTime)
			m_NowDateTime = dt;
		m_TimeCS.UnLock();
	}
	void SetNowDateTimeAsSystemTime(CUDFDateTime& nowTime)
	{
		SYSTEMTIME sysTime;
		GetSystemTime(&sysTime);
		nowTime = GetUdfDateTimeFromSystemTime(sysTime);
		SetNowDateTime(nowTime);
	}
//-----------------------------------------------------------------------
protected:

	CUDFDateTime m_NowDateTime;
	CCriticalSectionObj m_TimeCS;
};
//-----------------------------------------------------------------------

#endif