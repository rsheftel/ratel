#ifndef _CRIT_SECTION_OBJ_
#define _CRIT_SECTION_OBJ_

class CCriticalSectionObj
{
public:

	CCriticalSectionObj()
	{
		InitializeCriticalSection(&m_cs);
	}

	~CCriticalSectionObj()
	{
		DeleteCriticalSection(&m_cs);
	}

	void Lock()
	{
		EnterCriticalSection(&m_cs);
	}

	void UnLock()
	{
		LeaveCriticalSection(&m_cs);
	}

protected:

	CRITICAL_SECTION m_cs;
};


#endif