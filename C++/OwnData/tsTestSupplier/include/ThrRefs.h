#ifndef _THREAD_REFS_
#define _THREAD_REFS_


class CThreadRefs
{
public:

	CThreadRefs()
	{
		m_RefsCount = 0;
	}
	virtual ~CThreadRefs()
	{
	}

	LONG AddRef()
	{
		return InterlockedIncrement(&m_RefsCount);
	}

	LONG Release()
	{
		LONG rc;
		rc = InterlockedDecrement(&m_RefsCount);
		if (rc <= 0)
			delete this;
		return rc;
	}

protected:

	LONG m_RefsCount;
};


#endif