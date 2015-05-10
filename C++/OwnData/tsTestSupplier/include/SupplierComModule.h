#ifndef _SUPPLIER_COM_MODULE_
#define _SUPPLIER_COM_MODULE_

#include "atlbase.h"

class CSupplierComModule : public CComModule
{
public:

	CSupplierComModule()
	{
	}

	static void AddSupplier()
	{
		InterlockedIncrement(&m_SuppliersObjectCount);
	}

	static void ReleaseSupplier()
	{
		InterlockedDecrement(&m_SuppliersObjectCount);
	}

	LONG GetSuppliersCount()
	{
		return m_SuppliersObjectCount;
	}

	HRESULT CanUnloadNow(void);
	HRESULT CanUnloadNow2(void);

protected:

	static LONG m_SuppliersObjectCount;
};

#endif