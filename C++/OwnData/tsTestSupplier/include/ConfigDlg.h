#ifndef _TEST_SUPPLIER_CONFIG_DLG_
#define _TEST_SUPPLIER_CONFIG_DLG_

#include <string>
#include <wtypes.h>
#include <tchar.h>
#include <vector>
#include <string>

using namespace std;
//-----------------------------------------------------------------------
class CTestSupplierConfigDlg
{
	static BOOL CALLBACK ConfigDialogProc( HWND hwndDlg, UINT uMsg, WPARAM wParam, LPARAM lParam);
	static BOOL OnInitDialog( HWND hwndDlg);
	static void GetConfigDlg( HWND hwndDlg);

	static CTestSupplierConfigDlg* m_pTestSupplierConfigDlg;

public:
	CTestSupplierConfigDlg();
	virtual ~CTestSupplierConfigDlg();

	int DoModal();
//-----------------------------------------------------------------------
protected:

	BOOL m_bStartDialog;

};
//-----------------------------------------------------------------------
#endif