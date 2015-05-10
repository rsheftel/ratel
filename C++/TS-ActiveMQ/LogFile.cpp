#include "stdafx.h"
#include "LogFile.h"
#include <crtdbg.h>
#include <string>
#include "strutils.h"

using namespace std;
//-----------------------------------------------------------------------
const TCHAR CLogFile::LOG_FILE_DIR[] = "C:\\TS_DATAFEEDS-AMQ.LOG";

//-----------------------------------------------------------------------
CLogFile::CLogFile(const char* fName, BOOL bRewr)
{
	DWORD createFlag;
	m_hFile = NULL;

	if (bRewr)
		createFlag = CREATE_ALWAYS;
	else
		createFlag = OPEN_ALWAYS;


	string ProcName;
	string::size_type pos = 0, pos1 = 0;

	CreateDirectory(LOG_FILE_DIR, NULL);

	char strProcName[MAX_PATH] = "";
	GetModuleFileName(NULL, strProcName, sizeof(strProcName));
	ProcName = StringToLowerCase(strProcName);

	while (1)
	{
		pos1 = ProcName.find('\\', pos);
		if (pos1 == string::npos)
			break;

		pos = pos1 + 1;
	}

	ProcName = ProcName.substr(pos, ProcName.length() - pos);

	if (ProcName == "regsvr32.exe")
		return;

	if (ProcName.substr(ProcName.length() - 4, 4) == ".exe")
	{
		ProcName = ProcName.substr(0, ProcName.length() - 4);
	}

	string FileName;
	FileName = LOG_FILE_DIR;
	FileName += "\\";
	FileName += fName;
	FileName += " - ";
	FileName += ProcName;
	FileName += ".log";	

	HANDLE hTestFile;
	hTestFile = CreateFile(FileName.c_str(), GENERIC_READ|GENERIC_WRITE, FILE_SHARE_READ, NULL, OPEN_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
	if (hTestFile == INVALID_HANDLE_VALUE)
	{
		DWORD err;
		err = GetLastError();
		//createFlag = OPEN_EXISTING;
	} else
	{
		CloseHandle(hTestFile);
		m_hFile = CreateFile(FileName.c_str(), GENERIC_READ|GENERIC_WRITE, FILE_SHARE_READ|FILE_SHARE_WRITE, NULL, createFlag, FILE_ATTRIBUTE_NORMAL, NULL);
		_ASSERT(m_hFile != INVALID_HANDLE_VALUE);
	}

	//m_hFile = CreateFile(fName, GENERIC_READ|GENERIC_WRITE, FILE_SHARE_READ|FILE_SHARE_WRITE, NULL, createFlag, FILE_ATTRIBUTE_NORMAL, NULL);
	//_ASSERT(m_hFile != INVALID_HANDLE_VALUE);
}
//-----------------------------------------------------------------------
void CLogFile::Close()
{
	if (m_hFile != NULL)
	{
		CloseHandle(m_hFile);
		m_hFile = NULL;
	}
}
//-----------------------------------------------------------------------
CLogFile::~CLogFile()
{
	Close();
}
//-----------------------------------------------------------------------
CLogFile& CLogFile::operator<<(const char* str)
{
	DWORD bytesWr;
	_ASSERT(m_hFile != INVALID_HANDLE_VALUE);
	if (m_hFile != NULL)
		WriteFile(m_hFile, str, strlen(str), &bytesWr, NULL);

	return *this;
}
//-----------------------------------------------------------------------
CLogFile& CLogFile::operator<<(DWORD num)
{
	char buf[20];
	ultoa(num, buf, 10);
	return *this<<buf;
}
//-----------------------------------------------------------------------
CLogFile& CLogFile::operator<<(int num)
{
	char buf[20];
	itoa(num, buf, 10);
	return *this<<buf;
}
//-----------------------------------------------------------------------
CLogFile& CLogFile::operator<<(UINT num)
{
	char buf[20];
	ultoa(num, buf, 10);
	return *this<<buf;
}
//-----------------------------------------------------------------------
CLogFile& CLogFile::operator<<(double num)
{
	char buf[20];
	_gcvt(num, 10, buf);
	return *this<<buf;
}
//-----------------------------------------------------------------------
CLogFile& CLogFile::operator<<(Func_endl* f)
{
	return *this<<"\n";
}
//-----------------------------------------------------------------------
#include "udfdate.h"
#include "dateutils.h"
//-----------------------------------------------------------------------
CTimeLogFile& CTimeLogFile::PrintString(const char* str)
{
	DWORD bytesWr;
	_ASSERT(m_hFile != INVALID_HANDLE_VALUE);
	if (m_hFile != NULL)
		WriteFile(m_hFile, str, strlen(str), &bytesWr, NULL);

	return *this;
}
//-----------------------------------------------------------------------
CTimeLogFile& CTimeLogFile::operator<<(const char* str)
{
	SYSTEMTIME st;
	CUDFTime dt11;
	CUDFDateTime dt22;
	GetLocalTime(&st);
	dt22 = GetUdfDateTimeFromSystemTime(st);
	dt11.SetTime(dt22.GetTime());
	PrintString("[ ");
	PrintString(dt11.GetText().c_str());
	PrintString(" ]");
	PrintString("  ");
	PrintString(str);
	//PrintString("\n");
	return *this;
}
//-----------------------------------------------------------------------
CTimeLogFile& CTimeLogFile::operator<<(DWORD num)
{
	char buf[20];
	ultoa(num, buf, 10);
	return PrintString(buf);
}
//-----------------------------------------------------------------------
CTimeLogFile& CTimeLogFile::operator<<(int num)
{
	char buf[20];
	itoa(num, buf, 10);
	return PrintString(buf);
}
//-----------------------------------------------------------------------
CTimeLogFile& CTimeLogFile::operator<<(UINT num)
{
	char buf[20];
	ultoa(num, buf, 10);
	return PrintString(buf);
}
//-----------------------------------------------------------------------
CTimeLogFile& CTimeLogFile::operator<<(double num)
{
	char buf[20];
	_gcvt(num, 10, buf);
	return PrintString(buf);
}
//-----------------------------------------------------------------------
CTimeLogFile& CTimeLogFile::operator<<(Func_endl* f)
{
	return PrintString("\n");
}
//-----------------------------------------------------------------------