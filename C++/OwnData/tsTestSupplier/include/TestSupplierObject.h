#ifndef _TEST_SUPPLIER_OBJECT_
#define _TEST_SUPPLIER_OBJECT_

#include "SupplierObject.h"
//------------
//#include "SupplierRegistry.h"
#include "strutils.h"

//-----------------------------------------------------------------------
class CTestSupplierObject : public CSupplierObject
{
public:
//-----------------------------------------------------------------------
	CTestSupplierObject()
	{
//------------
		/*
		string ClassName, VersionMutexName, VersionMapName;
		ClassName = "CTestSupplierObject";

		VersionMutexName = ClassName + "_VersionMutex";
		VersionMapName = ClassName + "_VersionMap";
		InitVersion(VersionMutexName.c_str(), VersionMapName.c_str());

		Init();
		*/
//------------
	}
//-----------------------------------------------------------------------
//------------
	/*
	void Init()
	{
		string ver;
		CLibraryVersions libVers;
		CLibraryVersions::iterator itLibraryVersions;

		LoadParams();
		ver = GetLibraryVersions(libVers);

		itLibraryVersions = libVers.find(GetSupplierCurrentVersion());
		if (itLibraryVersions == libVers.end())
		{
			SetSupplierCurrentVersion(ver);
		}
	}
//-----------------------------------------------------------------------
	virtual string GetRegistryPath()
	{
		string path;
		path = GetSharedRegistry();
		path = DeleteEndChars(path, '\\');
		path += "\\Datafeeds\\tsTestDataFeed";
		return path;
	}
//-----------------------------------------------------------------------
	virtual string GetNativeLibraryName()
	{
		string libName;
		return libName;
	}
//-----------------------------------------------------------------------
	virtual string GetLibraryVersions(CLibraryVersions& libVers)
	{
		string defVers;

		defVers = "1.0.0.0";
		libVers.insert(CLibraryVersions::value_type(defVers.c_str(), ""));

		return defVers;
	}
//-----------------------------------------------------------------------
	virtual string GetSupplierCurrentVersion()
	{
		string ver;
		return ver;
	}
//-----------------------------------------------------------------------
	virtual string GetSupplierCurrentPath()
	{
		string path;
		return path;
	}
//-----------------------------------------------------------------------
	virtual BOOL IsVendorInstalled()
	{
		return TRUE;
	}
//-----------------------------------------------------------------------
	virtual string SetSupplierCurrentVersion(const string& ver)
	{
		string prevVer;
		return prevVer;
	}
//-----------------------------------------------------------------------
	virtual BOOL SaveCurrentParams();
//-----------------------------------------------------------------------
	virtual BOOL LoadParams();
//-----------------------------------------------------------------------
	virtual BOOL GetConfigParams(CConfigParams& pars)
	{
		BOOL res = TRUE;
		pars.clear();

		return res;
	}
//-----------------------------------------------------------------------
	virtual string GetConfigParam(const char* par)
	{
		return "";
	}
	*/
//-----------------------------------------------------------------------
	STDMETHODIMP IsProvide(long Mode, long Symbol, long Category, long ResolutionSize, long Resolution, long Field, BOOL* pVal)
	{
		BOOL bSuppField = ((Field == TS_ASK_FIELD) || (Field == TS_BID_FIELD)  || (Field == TS_TRADE_FIELD));
		BOOL bSuppRTMode = ((Mode == ERealTimeDataProvide) && (
			(Resolution == TS_RESOLUTION_TICK) && (ResolutionSize == 1) 
						)
					);

		BOOL bSuppHTMode = ((Mode == EHistoryDataProvide) && (
			(Resolution == TS_RESOLUTION_TICK) && (ResolutionSize == 1) 
			|| (Resolution == TS_RESOLUTION_DAY) && (ResolutionSize == 1)
						)
					);
 
		*pVal = (bSuppField && (bSuppRTMode || bSuppHTMode));

		return S_OK;
	}
//-----------------------------------------------------------------------
	virtual BOOL GetResolutions(long Mode, long*& Resolutions, long& ResolutionsNumber)
	{
		if (Mode == 0)
		{
			Resolutions = s_HistoryResolutions;
			ResolutionsNumber = sizeof(s_HistoryResolutions) / sizeof(s_HistoryResolutions[0]);
		} else
		{
			Resolutions = s_RealTimeResolutions;
			ResolutionsNumber = sizeof(s_RealTimeResolutions) / sizeof(s_RealTimeResolutions[0]);
		}

		return TRUE;
	}
//-----------------------------------------------------------------------
	virtual BOOL GetFields(long Mode, long Category, long*& Fields, long& FieldsNumber)
	{
		Fields = s_Fields;
		FieldsNumber = sizeof(s_Fields) / sizeof(s_Fields[0]);

		return TRUE;
	}
//-----------------------------------------------------------------------
	virtual BOOL GetResolutionSizes(long Mode, long Resolution, long*& ResolutionsSizes, long& ResolutionsSizesNumber)
	{
		return FALSE;
	}
//-----------------------------------------------------------------------
protected:

	static LONG s_HistoryResolutions[2];
	static LONG s_RealTimeResolutions[1];
	static LONG s_Fields[3];
};
//-----------------------------------------------------------------------


#endif