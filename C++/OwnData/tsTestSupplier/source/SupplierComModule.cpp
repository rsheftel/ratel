#include "SupplierComModule.h"
#include "ToProcessWrapper.h"

//-----------------------------------------------------------------------
LONG CSupplierComModule::m_SuppliersObjectCount = 0;

//-----------------------------------------------------------------------
HRESULT CSupplierComModule::CanUnloadNow(void)
{
	HRESULT hr = S_FALSE;
	LONG lLockCount;
	LONG ObjsCount;

	COneObjectInProcess<CToProcessWrapper> *ProcessWrapper = NULL;
	ProcessWrapper = new COneObjectInProcess<CToProcessWrapper>(true);

	ProcessWrapper->AnythingLock();
  
	try
	{
		ObjsCount = GetSuppliersCount();
		lLockCount = GetLockCount();

		if (lLockCount <= ObjsCount)
		{
			CToProcessWrapper::CloseListenerThread();
		}
	}
	catch(...)
	{
	}
	
	ProcessWrapper->AnythingUnlock();
	delete ProcessWrapper;
	ProcessWrapper = NULL;
	
	if (!ObjsCount)
	{
		hr = (GetLockCount()==0) ? S_OK : S_FALSE;
	} 

	return hr;
}
//-----------------------------------------------------------------------
HRESULT CSupplierComModule::CanUnloadNow2()
{
	HRESULT hr = S_FALSE;
	hr = (GetLockCount()==0) ? S_OK : S_FALSE;
	return hr;
}
//-----------------------------------------------------------------------