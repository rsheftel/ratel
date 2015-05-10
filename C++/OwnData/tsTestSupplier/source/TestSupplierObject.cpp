//#include "StdAfx.h"
#include "TestSupplierObject.h"

//-----------------------------------------------------------------------
LONG CTestSupplierObject::s_HistoryResolutions[2] =
{TS_RESOLUTION_TICK, TS_RESOLUTION_DAY};

LONG CTestSupplierObject::s_RealTimeResolutions[1] =
{TS_RESOLUTION_TICK};

LONG CTestSupplierObject::s_Fields[3] =
{TS_BID_FIELD, TS_TRADE_FIELD, TS_ASK_FIELD};
/*
//-----------------------------------------------------------------------
BOOL CTestSupplierObject::SaveCurrentParams()
{
	return TRUE;
}
//-----------------------------------------------------------------------
BOOL CTestSupplierObject::LoadParams()
{
	return TRUE;
}
//-----------------------------------------------------------------------
*/