#include "AddSymbolDlg.h"
#include "tsBaseEnums.h"
#include "..\\resource.h"

CAddSymbolDlg* CAddSymbolDlg::m_pAddSymbolDlg = NULL;

//-----------------------------------------------------------------------
CAddSymbolDlg::CAddSymbolDlg()
{
	if (m_pAddSymbolDlg != NULL)
	{
		m_bStartDialog = FALSE;
	} else
	{
		m_pAddSymbolDlg = this;
		m_bStartDialog = TRUE;
	}
}
//-----------------------------------------------------------------------
CAddSymbolDlg::~CAddSymbolDlg()
{
	if (m_bStartDialog)
		m_pAddSymbolDlg = NULL;
}
//-----------------------------------------------------------------------
BOOL CALLBACK CAddSymbolDlg::ConfigDialogProc( HWND hwndDlg, UINT uMsg, WPARAM wParam, LPARAM lParam)
{
	switch (uMsg) 
    { 
        case WM_COMMAND: 
		{	
            switch (LOWORD(wParam)) 
            { 
				case IDOK:
				{
					HWND hWnd=NULL;
					TCHAR szTemp[MAX_PATH];

					if (!(hWnd = GetDlgItem( hwndDlg, IDC_SYMBOL)))
						return FALSE;
					GetWindowText(hWnd, szTemp, sizeof(szTemp));
					if (!strcmp(szTemp, ""))
					{
						MessageBox(0, "SymbolName is empty!", "Error", MB_ICONERROR|MB_OK);
						return TRUE;
					}

					m_pAddSymbolDlg->m_Symbol = szTemp;

					if (!(hWnd = GetDlgItem( hwndDlg, IDC_SYMBOL_ROOT)))
						return FALSE;
					GetWindowText(hWnd, szTemp, sizeof(szTemp));
					if (!strcmp(szTemp, ""))
					{
						m_pAddSymbolDlg->m_Symbol = "";
						MessageBox(0, "SymbolRoot is empty!", "Error", MB_ICONERROR|MB_OK);
						return TRUE;
					}

					m_pAddSymbolDlg->m_SymbolRoot = szTemp;

					if (!(hWnd = GetDlgItem( hwndDlg, IDC_EXCHANGE)))
						return FALSE;
					GetWindowText(hWnd, szTemp, sizeof(szTemp));
					m_pAddSymbolDlg->m_Exchange = szTemp;

					if (!(hWnd = GetDlgItem( hwndDlg, IDC_CATEGORY)))
						return FALSE;
					GetWindowText(hWnd, szTemp, sizeof(szTemp));
					m_pAddSymbolDlg->m_Category = szTemp;

					if (!(hWnd = GetDlgItem( hwndDlg, IDC_DESCRIPTION)))
						return FALSE;
					GetWindowText(hWnd, szTemp, sizeof(szTemp));
					m_pAddSymbolDlg->m_Description = szTemp;
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
int CAddSymbolDlg::DoModal()
{
	if (!m_bStartDialog)
	{
		HWND hwnd;
		hwnd = FindWindow("#32770", "Add Symbol");
		if (hwnd != NULL)
		{
			SetForegroundWindow(hwnd);
		}
		return IDCANCEL;
	}

	HMODULE hMod = GetModuleHandle("tsTestSupplier.dll");
	int cid = DialogBox(GetModuleHandle("tsTestSupplier.dll"), MAKEINTRESOURCE(IDD_SUPPLIER_ADD_SYMBOL),
		GetActiveWindow(), &ConfigDialogProc);

	return cid;
}
//-----------------------------------------------------------------------
LONG CAddSymbolDlg::GetCategoryByName(const char* CategoryName)
{
	if (!strcmp(CategoryName, "Stock"))
	{
		return TS_CATEGORY_STOCK;
	} else
	if (!strcmp(CategoryName, "Stock Option"))
	{
		return TS_CATEGORY_STOCKOPTION;
	} else
	if (!strcmp(CategoryName, "Future"))
	{
		return TS_CATEGORY_FUTURE;
	} else
	if (!strcmp(CategoryName, "Future Option"))
	{
		return TS_CATEGORY_FUTUREOPTION;
	} else
	if (!strcmp(CategoryName, "Index"))
	{
		return TS_CATEGORY_INDEX;
	} else
	if (!strcmp(CategoryName, "Index Option"))
	{
		return TS_CATEGORY_INDEXOPTION;
	} else
	if (!strcmp(CategoryName, "Forex"))
	{
		return TS_CATEGORY_FOREX;
	} else
	if (!strcmp(CategoryName, "Cash"))
	{
		return TS_CATEGORY_CASH;
	}

	return TS_CATEGORY_NOTHING;
}
//-----------------------------------------------------------------------
BOOL CAddSymbolDlg::OnInitDialog(HWND hwndDlg) 
{
	HWND hWnd = NULL;

	if (!(hWnd = GetDlgItem(hwndDlg, IDC_EXCHANGE)))
		return FALSE;

	SendMessage(hWnd, CB_ADDSTRING, 0, (LPARAM)"NASDAQ");
	SendMessage(hWnd, CB_ADDSTRING, 1, (LPARAM)"NYSE");
	SendMessage(hWnd, CB_ADDSTRING, 2, (LPARAM)"CME");
	SendMessage(hWnd, CB_ADDSTRING, 3, (LPARAM)"GLOBEX");
	SendMessage(hWnd, CB_ADDSTRING, 4, (LPARAM)"FOREX");
	SendMessage(hWnd, CB_SETCURSEL, 0, 0);

	if (!(hWnd = GetDlgItem(hwndDlg, IDC_CATEGORY)))
		return FALSE;

	SendMessage(hWnd, CB_ADDSTRING, 0, (LPARAM)"Stock");
	SendMessage(hWnd, CB_ADDSTRING, 1, (LPARAM)"Stock Option");
	SendMessage(hWnd, CB_ADDSTRING, 2, (LPARAM)"Future");
	SendMessage(hWnd, CB_ADDSTRING, 3, (LPARAM)"Future Option");
	SendMessage(hWnd, CB_ADDSTRING, 4, (LPARAM)"Index");
	SendMessage(hWnd, CB_ADDSTRING, 5, (LPARAM)"Index Option");
	SendMessage(hWnd, CB_ADDSTRING, 6, (LPARAM)"Forex");
	SendMessage(hWnd, CB_ADDSTRING, 7, (LPARAM)"Cash");
	SendMessage(hWnd, CB_SETCURSEL, 0, 0);

	return TRUE;
}
//-----------------------------------------------------------------------
