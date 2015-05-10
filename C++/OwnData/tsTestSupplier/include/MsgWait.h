#ifndef _MSG_WAIT
#define _MSG_WAIT

#include <wtypes.h>

inline DWORD MsgWait(DWORD nCount, LPHANDLE pHandles, DWORD dwMilliseconds, DWORD dwWakeMask, BOOL bIsNT4)
{
	DWORD wres = WAIT_FAILED;
	MSG msg;
	if (!bIsNT4)
	{
		wres = MsgWaitForMultipleObjectsEx(nCount, pHandles, dwMilliseconds, dwWakeMask, MWMO_INPUTAVAILABLE);
	} else
	{
		if (PeekMessage(&msg, NULL, 0, 0, PM_NOREMOVE))
		{
			wres = WAIT_OBJECT_0 + nCount;
		} else
		{
			wres = MsgWaitForMultipleObjectsEx(nCount, pHandles, dwMilliseconds, dwWakeMask, 0);
		}
	}

	return wres;
}

#endif