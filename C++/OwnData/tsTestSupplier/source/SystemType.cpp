#include "SystemType.h"
#include <wtypes.h>
//-----------------------------------------------------------------------
ESystemType GetSystemVType()
{
ESystemType SType;
	DWORD dwVersion = GetVersion();
	DWORD MajorVersion =  (DWORD)(LOBYTE(LOWORD(dwVersion)));
	DWORD MinorVersion =  (DWORD)(HIBYTE(LOWORD(dwVersion)));

	SType= EUnknown;
	if ((MajorVersion ==5)&&(MinorVersion==0) ) 
		SType=EWindows2000;

	if ((MajorVersion ==5)&&(MinorVersion==1) ) 
		SType=EWindowsXP;


	if ((MajorVersion ==4) && (MinorVersion==10) ) 
		SType=EWindows98;

	if ((MajorVersion ==4) && (MinorVersion==0) )
		SType=EWindowsNT;
	return SType;
};
//-----------------------------------------------------------------------