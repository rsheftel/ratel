#ifndef _SUPPLIER_REGISTRY_
#define _SUPPLIER_REGISTRY_

#pragma warning (disable : 4786)

#include <string>
#include <vector>
#include <map>
#include "ToProcessWrapper.h"
#include "tsStd.h"
using namespace std;
//-----------------------------------------------------------------------
class CSupplierRegistry
{
public:
//-----------------------------------------------------------------------
	typedef SConnectionParams::SConnectionParam SConnectionParam;
	typedef SConnectionParams::EConnectionParamType EConnectionParamType;

	typedef map<string, string> CLibraryVersions;
	typedef map<string, SConnectionParam> CConfigParams;

	CSupplierRegistry();
	~CSupplierRegistry();

	string GetSharedDirectory();
	string GetSharedRegistry();
	virtual string GetRegistryPath() = 0;
	virtual string GetNativeLibraryName() = 0;
	virtual string GetLibraryVersions(CLibraryVersions& libVers) = 0;
	virtual string GetSupplierCurrentVersion() = 0;
	virtual string GetSupplierCurrentPath() = 0;
	virtual string SetSupplierCurrentVersion(const string& ver) = 0;
	virtual BOOL SaveCurrentParams() = 0;
	virtual BOOL LoadParams() = 0;
	virtual BOOL GetConfigParams(CConfigParams& pars) = 0;
	virtual string GetConfigParam(const char* par) = 0;
	virtual BOOL IsVendorInstalled() = 0;
//-----------------------------------------------------------------------
	BOOL InitVersion(const char* VersionMutex, const char* VersionMap);
	BOOL CloseVersion();
	BOOL IsLastVersion();
	BOOL GetLastVersion(DWORD& Ver);
	BOOL SetLastVersion(DWORD Ver);
	BOOL StartVersionUpdate();
	BOOL FinishVersionUpdate();
//-----------------------------------------------------------------------

protected:

    ItsPlatformInfo* m_IPlatformInfo;
	HANDLE m_hVersionMutex;
	HANDLE m_hVersionMap;
	DWORD m_Version;
};
//-----------------------------------------------------------------------

#endif