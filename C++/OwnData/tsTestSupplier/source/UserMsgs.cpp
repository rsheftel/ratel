#include "UserMsgs.h"
#include <stdio.h>

int OutErrorCodeMessage(const char* msg, HRESULT hr, HRESULT hrOK)
{
	int res;
	char outMsg[255] = "";

	if (hr != hrOK)
		sprintf(outMsg, "%s\nErrorCode: %X", msg, hr);
	else
		strcpy(outMsg, msg);

	res = MessageBox(0, outMsg, "Error", MB_OK|MB_ICONERROR);
	return res;
}

int OutErrorMessage(const char* msg)
{
	int res;
	res = MessageBox(0, msg, "Error", MB_OK|MB_ICONERROR);
	return res;
}

int OutWarningMessage(const char* msg)
{
	int res;
	res = MessageBox(0, msg, "Warning", MB_OK|MB_ICONWARNING);
	return res;
}