// TsBinaryFiles.cpp : Defines the entry point for the DLL application.
//


#include "stdafx.h"
#include "TsBinaryFiles.h"
#include "stdio.h"
#include <windows.h>
#include <time.h>

BOOL APIENTRY DllMain( HANDLE hModule, DWORD  ul_reason_for_call, LPVOID lpReserved) {
    switch (ul_reason_for_call) {
		case DLL_PROCESS_ATTACH:
		case DLL_THREAD_ATTACH:
		case DLL_THREAD_DETACH:
		case DLL_PROCESS_DETACH:
			break;
    }
    return TRUE;
}

HANDLE curveFilehandle = 0;
char curveFilename[8192] = "";
double result[180000];
int count;

void closeCurveFilehandleNoErrorChecking() {
	CloseHandle(curveFilehandle);
	curveFilename[0] = '\0';
	count = 0;		
}

LPSTR handleError(DWORD error) {
		char* errorString = new char[8096];
		FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM, 0, error, 0, errorString, 8095, 0);
		closeCurveFilehandleNoErrorChecking();
		return errorString;
}

LPSTR __stdcall closeCurveFile(LPSTR outfile) {
	DWORD nWrote, error;
	if(strcmp(curveFilename, outfile) == 0) {
		WriteFile(curveFilehandle, result, count * sizeof(double) * 3, &nWrote, NULL);
		error = GetLastError();
		if(error != NO_ERROR)
			return handleError(error);
		
		CloseHandle(curveFilehandle);
		error = GetLastError();
		if(error != NO_ERROR)
			return handleError(error);
		curveFilehandle = 0;
	}
    return "";
}

double getTime(int date, int time)
{
	struct tm time_str;
    time_str.tm_mday = date % 100;
	date /= 100;
	time_str.tm_mon = (date % 100) - 1;
	date /= 100;
	time_str.tm_year = date;

	time_str.tm_hour = time / 100;
	time_str.tm_min = time % 100;
	time_str.tm_sec = 0;
	
	time_str.tm_isdst = -1;
	return (double) mktime(&time_str);
}

// Need to close files in error conditions.
LPSTR __stdcall writeToCurveFile(LPSTR outfile, int date, double pnl, double position) {
	DWORD error;
	if(strcmp(curveFilename, outfile) != 0) {
		if (curveFilehandle) {
			LPSTR errorString = closeCurveFile(curveFilename);
			if(strcmp(errorString, "") != 0)
				return errorString;
		}

		curveFilehandle = CreateFile(outfile, GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, 0, NULL);
		error = GetLastError();
		if(error != NO_ERROR)
			return handleError(error);

		strcpy(curveFilename, outfile);
		count = 0;
	}

	result[count*3 + 0] = getTime(date, 0);;
	result[count*3 + 1] = pnl;
	result[count*3 + 2] = position;
    count++;

    if(count >= 20000) {
		closeCurveFilehandleNoErrorChecking();
		return "Too many entries in curve file!  Only 20000 entries are allowed per file in this version.";
	}

	return "";
}

LPSTR __stdcall writeToTradeCurveFile(LPSTR outfile, int entryDate, int entryTime, double entryPrice, 
									  int exitDate, int exitTime, double exitPrice,
									  double profit, double marketPosition, double maxContracts, 
									  double maxPositionProfit, double maxPositionLoss) 
{
	DWORD error;
	if(strcmp(curveFilename, outfile) != 0) {
		if (curveFilehandle) {
			LPSTR errorString = closeCurveFile(curveFilename);
			if(strcmp(errorString, "") != 0)
				return errorString;
		}

		curveFilehandle = CreateFile(outfile, GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, 0, NULL);
		error = GetLastError();
		if(error != NO_ERROR)
			return handleError(error);

		strcpy(curveFilename, outfile);
		count = 0;
	}	

	int numOfElement = 9;
	int i = 0;
	result[count*numOfElement + i++] = getTime(entryDate, entryTime);
	result[count*numOfElement + i++] = entryPrice;
	result[count*numOfElement + i++] = getTime(exitDate, exitTime);
	result[count*numOfElement + i++] = exitPrice;
	result[count*numOfElement + i++] = profit;
	result[count*numOfElement + i++] = marketPosition;
	result[count*numOfElement + i++] = maxContracts;
	result[count*numOfElement + i++] = maxPositionProfit;
	result[count*numOfElement + i++] = maxPositionLoss;
    count++;

    if(count >= 20000) {
		closeCurveFilehandleNoErrorChecking();
		return "Too many entries in curve file!  Only 20000 entries are allowed per file in this version.";
	}

	return "";
}
