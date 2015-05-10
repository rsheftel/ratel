#ifndef _USERMSGS_
#define _USERMSGS_

#include <wtypes.h>

int OutErrorCodeMessage(const char* msg, HRESULT hr, HRESULT hrOK=S_OK);
int OutErrorMessage(const char* msg);
int OutWarningMessage(const char* msg);

#endif