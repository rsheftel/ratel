#include "ConfigDlg.h"
#include "..\\resource.h"

CTestSupplierConfigDlg* CTestSupplierConfigDlg::m_pTestSupplierConfigDlg = NULL;

//-----------------------------------------------------------------------
CTestSupplierConfigDlg::CTestSupplierConfigDlg()
{
	if (m_pTestSupplierConfigDlg != NULL)
	{
		m_bStartDialog = FALSE;
	} else
	{
		m_pTestSupplierConfigDlg = this;
		m_bStartDialog = TRUE;
	}
}
//-----------------------------------------------------------------------
CTestSupplierConfigDlg::~CTestSupplierConfigDlg()
{
	if (m_bStartDialog)
		m_pTestSupplierConfigDlg = NULL;
}
//-----------------------------------------------------------------------
BOOL CALLBACK CTestSupplierConfigDlg::ConfigDialogProc( HWND hwndDlg, UINT uMsg, WPARAM wParam, LPARAM lParam)
{
	switch (uMsg) 
    { 
        case WM_COMMAND: 
		{	
            switch (LOWORD(wParam)) 
            { 
				case IDOK:
				{
				}
				case IDCANCEL: 
					EndDialog( hwndDlg, LOWORD(wParam));
					return TRUE; 
            } 
			break;
			};
		case WM_INITDIALOG: 
			return OnInitDialog(hwndDlg);
    } 

	return FALSE; 
}
//-----------------------------------------------------------------------
int CTestSupplierConfigDlg::DoModal()
{
	if (!m_bStartDialog)
	{
		HWND hwnd;
		hwnd = FindWindow("#32770", "Test Supplier Configuration");
		if (hwnd != NULL)
		{
			SetForegroundWindow(hwnd);
		}
		return IDCANCEL;
	}

	HMODULE hMod = GetModuleHandle("tsTestSupplier.dll");
	int cid = DialogBox(GetModuleHandle("tsTestSupplier.dll"), MAKEINTRESOURCE(IDD_SUPPLIER_CONFIG),
		GetActiveWindow(), &ConfigDialogProc);

	return cid;
}
//-----------------------------------------------------------------------
BOOL CTestSupplierConfigDlg::OnInitDialog(HWND hwndDlg) 
{
	// TODO: Add extra initialization here

	return TRUE;
}
//-----------------------------------------------------------------------
