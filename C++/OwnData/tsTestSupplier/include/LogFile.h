#ifndef _LOGFILE_
#define _LOGFILE_
//-----------------------------------------------------------------------
#include <wtypes.h>
#include <ostream>
#include <tchar.h>

using namespace std;

typedef basic_ostream<char, char_traits<char> >&
	__cdecl Func_endl(basic_ostream<char, char_traits<char> >& _O);


//#define endl ""
//-----------------------------------------------------------------------
class CLogFile
{
public:

	static const TCHAR LOG_FILE_DIR[];

	CLogFile()
	{
	}
	CLogFile(const char* fName, BOOL bRewr=TRUE);
	~CLogFile();

	void Close();

	CLogFile& operator<<(const char* str);
	CLogFile& operator<<(DWORD num);
	CLogFile& operator<<(int num);
	CLogFile& operator<<(double num);
	CLogFile& operator<<(UINT num);
	CLogFile& operator<<(Func_endl* f);

protected:

	HANDLE m_hFile;
};
//-----------------------------------------------------------------------
class CTimeLogFile : public CLogFile
{
public:
//-----------------------------------------------------------------------
	CTimeLogFile()
	{
	}
	CTimeLogFile(const char* fName, BOOL bRewr=TRUE)
		: CLogFile(fName, bRewr)
	{
	}
//-----------------------------------------------------------------------
	CTimeLogFile& PrintString(const char* str);
	CTimeLogFile& operator<<(const char* str);
	CTimeLogFile& operator<<(DWORD num);
	CTimeLogFile& operator<<(int num);
	CTimeLogFile& operator<<(double num);
	CTimeLogFile& operator<<(UINT num);
	CTimeLogFile& operator<<(Func_endl* f);
//-----------------------------------------------------------------------
};
//-----------------------------------------------------------------------
#endif