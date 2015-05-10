#ifndef _IPA_UTILS_
#define _IPA_UTILS_

//-----------------------------------------------------------------------
struct SIPAddr
{
	BYTE m_b1;
	BYTE m_b2;
	BYTE m_b3;
	BYTE m_b4;

	SIPAddr()
	{
		Clear();
	}

	SIPAddr(BYTE b1, BYTE b2, BYTE b3, BYTE b4)
	{
		Set(b1, b2, b3, b4);
	}

	BOOL Set(BYTE b1, BYTE b2, BYTE b3, BYTE b4)
	{
		if (!b1)
		{
			Clear();
			return FALSE;
		}

		m_b1 = b1;
		m_b2 = b2;
		m_b3 = b3;
		m_b4 = b4;

		return TRUE;
	}

	BOOL Set(const char* addr)
	{
		BYTE b1, b2, b3, b4;

		Clear();
		if (IsIPAddress(addr, b1, b2, b3, b4))
		{
			return Set(b1, b2, b3, b4);
		}

		return FALSE;
	}

	BOOL IsEqual(BYTE b1, BYTE b2, BYTE b3, BYTE b4)
	{
		return ((m_b1 == b1) && (m_b2 == b2) && (m_b3 == b3) && (m_b4 == b4));
	}

	void Clear()
	{
		m_b1 = 0;
		m_b2 = 0;
		m_b3 = 0;
		m_b4 = 0;
	}

	BOOL IsEmpty()
	{
		return (m_b1 == 0);
	}

	string GetAsString()
	{
		string str;
		char buf[50]="";
		itoa(m_b1, buf, 10);
		str += buf;
		str += ".";
		itoa(m_b2, buf, 10);
		str += buf;
		str += ".";
		itoa(m_b3, buf, 10);
		str += buf;
		str += ".";
		itoa(m_b4, buf, 10);
		str += buf;

		return str;
	}
};
//-----------------------------------------------------------------------

#endif