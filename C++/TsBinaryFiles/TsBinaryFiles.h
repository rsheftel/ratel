
// The following ifdef block is the standard way of creating macros which make exporting 
// from a DLL simpler. All files within this DLL are compiled with the TSTEST2_EXPORTS
// symbol defined on the command line. this symbol should not be defined on any project
// that uses this DLL. This way any other project whose source files include this file see 
// TSTEST2_API functions as being imported from a DLL, wheras this DLL sees symbols
// defined with this macro as being exported.
#ifdef TSTEST2_EXPORTS
#define TSTEST2_API __declspec(dllexport)
#else
#define TSTEST2_API __declspec(dllimport)
#endif

#import "C:\Program Files\TradeStation 8.2 (Build 3896)\Program\tskit.dll" no_namespace
#include <atlbase.h>


/*
// This class is exported from the TsTest2.dll
class TSTEST2_API CTsTest2 {
public:
	CTsTest2(void);
	// TODO: add your methods here.
};

extern TSTEST2_API int nTsTest2;

TSTEST2_API int fnTsTest2(void);

TSTEST2_API double __stdcall MovAvg( IEasyLanguageObject * pELObj, int iAvgLength );
*/