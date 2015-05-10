#include "SupplierThreadMsgs.h"
#include "ToProcessWrapper.h"
//-----------------------------------------------------------------------

//-----------------------------------------------------------------------
void CSupplierThreadMsgs::RegSupplierThreadMsgsWindowProc()
{
	WNDCLASSEX wcex;
	wcex.cbSize = sizeof(WNDCLASSEX);

	wcex.style			= 0;
	wcex.lpfnWndProc	= (WNDPROC)SupplierThreadMsgsWindowProc;
	wcex.cbClsExtra		= 0;
	wcex.cbWndExtra		= 0;
	wcex.hInstance		= GetModuleHandle(NULL);
	wcex.hIcon			= 0;
	wcex.hCursor		    = 0;
	wcex.hbrBackground	= 0;
	wcex.lpszMenuName	    = 0;
	wcex.lpszClassName	= s_SupplierThreadMsgsWindowClassName;
	wcex.hIconSm		    = 0;

	RegisterClassEx(&wcex);
}
//-----------------------------------------------------------------------
void CSupplierThreadMsgs::UnRegSupplierThreadMsgsWindowProc()
{
	UnregisterClass(s_SupplierThreadMsgsWindowClassName, GetModuleHandle(NULL));
}
//-----------------------------------------------------------------------
LRESULT CALLBACK CSupplierThreadMsgs::SupplierThreadMsgsWindowProc(HWND hWnd, UINT msg, WPARAM wPar, LPARAM lPar)
{
	LRESULT res = 0;
	char *ptrData = (char*)wPar;
	char *ptrData2 = (char*)lPar;
	CSupplierThreadMsgs* ptrObject = NULL;

	ptrObject = (CSupplierThreadMsgs*)GetWindowLong(hWnd, GWL_USERDATA);

	switch (msg)
	{
		case WM_EXIT_THREAD:
		case WM_ADVISE_TRANSACTION:
		case WM_UNADVISE_TRANSACTION:
		case WM_UNADVISE:
		case WM_ADVISE_CONNECTION_STATUS:
		case WM_UNADVISE_CONNECTION_STATUS:
		case WM_ADVISE_SYMBOL_LIST_TRANSACTION:
		case WM_UNADVISE_SYMBOL_LIST_TRANSACTION:
		case WM_UNADVISE_SYMBOL_LIST:
		case WM_CREATE_CONNECTION:
		case WM_ADVISE_STATUS_LINE:
		case WM_UNADVISE_STATUS_LINE:
		case WM_TERM_THREAD:
			if (ptrObject != NULL)
			{
				MSG message;
				ZeroMemory(&message, sizeof(MSG));
				message.hwnd = hWnd;
				message.message = msg;
				message.wParam = wPar;
				message.lParam = lPar;
				
				ptrObject->PushMessage(message);
				res = TRUE;
			}
			break;
		default:
			res = DefWindowProc(hWnd, msg, wPar, lPar);
	}

	return res;
}
//-----------------------------------------------------------------------