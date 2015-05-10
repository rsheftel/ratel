#ifndef _STR_UTILS_
#define _STR_UTILS_

#include <wtypes.h>
#include <string>
using namespace std;

string StringToLowerCase(const string& str);
string StringToUpperCase(const string& str);
string YMDExpiryToMY(const string& str);
BOOL YMDToNums(const string& str, int& day, int& month, int& year);
BOOL HMSToNums(const string& str, int& h, int& m, int& s);
string YMDToStr(int day, int month, int year);
string NumsToStr(int n1, int l1, int n2, int l2, int n3, int l3, char delim);
BOOL IsIPAddress(const char* addr, BYTE& a1,  BYTE& a2, BYTE& a3, BYTE& a4);
void PackPathName(string& Path);
string DeleteSpacesAroundChar(const string& str, char c);
string DeleteBeginChars(const string& str, char c);
string DeleteEndChars(const string& str, char c);	
string DeleteEndCharsAfterDelimiter(const char* str, char EndChar = '0', char Delimiter = '.', BOOL bDeleteDelimiter = FALSE);

#endif