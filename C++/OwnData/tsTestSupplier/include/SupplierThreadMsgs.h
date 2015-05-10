#ifndef _SUPPLIER_THREAD_MSGS_
#define _SUPPLIER_THREAD_MSGS_

#pragma warning (disable : 4786)

#include <wtypes.h>
#include <tchar.h>
#include <queue>

using namespace std;
//-----------------------------------------------------------------------
class CSupplierThreadMsgs
{
public:
//-----------------------------------------------------------------------
	typedef queue<tagMSG> CMessagesQueue;
//-----------------------------------------------------------------------
	static const TCHAR s_SupplierThreadMsgsWindowClassName[];
//-----------------------------------------------------------------------
	static LRESULT CALLBACK SupplierThreadMsgsWindowProc(HWND hWnd, UINT msg, WPARAM wPar, LPARAM lPar);
	static void RegSupplierThreadMsgsWindowProc();
	static void UnRegSupplierThreadMsgsWindowProc();
//-----------------------------------------------------------------------
	CSupplierThreadMsgs()
	{
		m_SupplierThreadMsgsWindow = CreateWindow(s_SupplierThreadMsgsWindowClassName, "", WS_POPUPWINDOW,
			0, 0, 0, 0, NULL, NULL, GetModuleHandle(NULL), NULL);
		SetWindowObject(this);
	}
	virtual ~CSupplierThreadMsgs()
	{
		SetWindowObject(NULL);
		DestroyWindow(m_SupplierThreadMsgsWindow);
		m_SupplierThreadMsgsWindow = NULL;
	}
//-----------------------------------------------------------------------
	HWND GetWindowHandle()
	{
		return m_SupplierThreadMsgsWindow;
	}
//-----------------------------------------------------------------------
	void PushMessage(tagMSG& msg)
	{
		m_MessagesQueue.push(msg);
	}
//-----------------------------------------------------------------------
	BOOL PeekMessage(tagMSG& msg)
	{
		BOOL res = FALSE;

		if (!m_MessagesQueue.empty())
		{
			msg = m_MessagesQueue.front();
			m_MessagesQueue.pop();
			res = TRUE;
		}

		return res;
	}
//-----------------------------------------------------------------------
protected:


	void SetWindowObject(void* Object)
	{
		if (m_SupplierThreadMsgsWindow != NULL)
			SetWindowLong(m_SupplierThreadMsgsWindow, GWL_USERDATA, (LONG)Object);
	}
//-----------------------------------------------------------------------
	HWND m_SupplierThreadMsgsWindow;
	CMessagesQueue m_MessagesQueue;
};

//-----------------------------------------------------------------------

#endif