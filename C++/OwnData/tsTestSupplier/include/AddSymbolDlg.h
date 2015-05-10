#ifndef _ADD_SYMBOL_CONFIG_DLG_
#define _ADD_SYMBOL_CONFIG_DLG_

#include <string>
#include <wtypes.h>
#include <tchar.h>
#include <vector>
#include <string>

using namespace std;
//-----------------------------------------------------------------------
class CAddSymbolDlg
{
	static BOOL CALLBACK ConfigDialogProc( HWND hwndDlg, UINT uMsg, WPARAM wParam, LPARAM lParam);
	static BOOL OnInitDialog( HWND hwndDlg);
	static void GetConfigDlg( HWND hwndDlg);

	static CAddSymbolDlg* m_pAddSymbolDlg;

public:
	CAddSymbolDlg();
	virtual ~CAddSymbolDlg();

	int DoModal();
//-----------------------------------------------------------------------
	static LONG GetCategoryByName(const char* CategoryName);
//-----------------------------------------------------------------------
	string m_Symbol;
	string m_SymbolRoot;
	string m_Exchange;
	string m_Category;
	string m_Description;

//-----------------------------------------------------------------------
protected:

	BOOL m_bStartDialog;

};
//-----------------------------------------------------------------------
#endif