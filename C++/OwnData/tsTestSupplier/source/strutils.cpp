#include "strutils.h"
//-----------------------------------------------------------------------
string StringToLowerCase(const string& str)
{
	string s = str;
	for(string::size_type c = 0; c < s.length(); c++)
		s[c] = tolower(s[c]);
	return s;
}
//-----------------------------------------------------------------------
string StringToUpperCase(const string& str)
{
	string s = str;
	for(string::size_type c = 0; c < s.length(); c++)
		s[c] = toupper(s[c]);
	return s;
}
//-----------------------------------------------------------------------
const char monthNames3Letts[12][4] = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};


string YMDExpiryToMY(const string& str)
{
	string s;


	if (str.length() < 8)
		return s;

	int n;
	s = str.substr(4, 2);
	n = atoi(s.c_str());
	if (n < 13)
		s = monthNames3Letts[n-1];
	else
		s = "";

	s += str.substr(2, 2);

	return s;
}
//---------------------------------------------------------------------------
BOOL YMDToNums(const string& str, int& day, int& month, int& year)
{
	if (str.empty())
	{
		year = 1899;
		month = 12;
		day = 30;
		return TRUE;
	}

	if (str.length() < 8)
		return FALSE;

	char buf[20] = "";
	BOOL res = FALSE;

	year = atoi(str.substr(0, 4).c_str());
	month = atoi(str.substr(4, 2).c_str());
	day = atoi(str.substr(6, 2).c_str());

	res = (year > 1900) && (month >= 1) && (month <= 12) && (day >= 1) && (day <= 31);

	return res;
}
//---------------------------------------------------------------------------
BOOL HMSToNums(const string& str, int& h, int& m, int& s)
{
	if (str.empty())
	{
		h = 0;
		m = 0;
		s = 0;
		return TRUE;
	}

	if (str.length() < 8)
		return FALSE;

	char buf[20] = "";

	h = atoi(str.substr(0, 2).c_str());
	m = atoi(str.substr(3, 2).c_str());
	s = atoi(str.substr(6, 2).c_str());

	return TRUE;
}
//---------------------------------------------------------------------------
string YMDToStr(int day, int month, int year)
{
	string str;
	char buf[20] = "";

	itoa(year, buf, 10);
	str = buf;

	itoa(month, buf, 10);
	if (month < 10)
		str += "0";
	str += buf;

	itoa(day, buf, 10);
	if (day < 10)
		str += "0";
	str += buf;

	return str;
}
//---------------------------------------------------------------------------
string NumsToStr(int n1, int l1, int n2, int l2, int n3, int l3, char delim)
{
	string res;
	char strNum[20] = "";
	char strFmt[20] = "";
	char buf[50] = "";
	int numValue = 0;
	int numLen = 0;
	for(int n = 0; n < 3; n++)
	{
		switch (n)
		{
			case 0:
				numValue = n1;
				numLen = l1;
				break;
			case 1:
				numValue = n2;
				numLen = l2;
				break;
			case 2:
				numValue = n3;
				numLen = l3;
				break;
		}

		if (numLen > 0)
		{
			itoa(numLen, strNum, 10);
			strcpy(strFmt, "%.");
			strcat(strFmt, strNum);
			strcat(strFmt, "d");
			sprintf(buf, strFmt, numValue);
			res += buf;
			if (n < 2)
				res += delim;
		}
	}

	return res;
}
//---------------------------------------------------------------------------
BOOL IsIPAddress(const char* addr, BYTE& a1,  BYTE& a2, BYTE& a3, BYTE& a4)
{
char buf[50];
char *token;
int ap;
strcpy(buf, addr);
token = strtok(buf, ".");
int numAddrPart = 0;
while (token != NULL)
{
        ap = atoi(token);
        if ((ap > 255) || (ap < 0) || ((ap == 0) && (numAddrPart == 0)))
                break;

        token = strtok(NULL, ".");
        numAddrPart++;

        switch (numAddrPart)
        {
                case 1:
                        a1 = ap;
                        break;
                case 2:
                        a2 = ap;
                        break;
                case 3:
                        a3 = ap;
                        break;
                case 4:
                        a4 = ap;
                        break;
        }
}

if (numAddrPart == 4)
        return TRUE;
else
        return FALSE;
}
//-----------------------------------------------------------------------
string DeleteSpacesAroundChar(const string& str, char c)
{
	string s;
	string::size_type pos = 0, posf = 0, pos1 = 0, len;
	len = str.length();
	posf = str.find(c);

	while (1)
	{

		if (posf == string::npos)
		{
			s += str.substr(pos, len - pos);
			break;
		}

		pos1 = posf;
		while (pos1 > pos)
		{
			pos1--;
			if (str[pos1] != ' ')
			{
				pos1++;
				break;
			}
		}

		s += str.substr(pos, pos1 - pos) + c;
		
		pos1 = posf + 1;
		while (pos1 < len)
		{
			if (str[pos1] != ' ')
			{
				if (str[pos1] != c)
					break;
				else
					s += c;
			}

			pos1++;
		}

		pos = pos1;
		posf = str.find(c, pos);
	}

	return s;	
}
//-----------------------------------------------------------------------
void PackPathName(string& Path) 
{
	Path = DeleteSpacesAroundChar(Path, '\\');
	Path = DeleteSpacesAroundChar(Path, ':');
	Path = DeleteEndChars(Path, '\\');
}
//-----------------------------------------------------------------------
string DeleteBeginChars(const string& str, char c)
{
	string s;

	string::size_type pos = 0, len;
	len = str.length();
	while (pos < len)
	{
		if (str[pos] != c)
			break;

		pos++;
	}

	s = str.substr(pos, len - pos);

	return s;
}
//-----------------------------------------------------------------------
string DeleteEndChars(const string& str, char c)
{
	string s;

	string::size_type pos;
	pos = str.length();
	while (pos > 0)
	{
		if (str[pos-1] != c)
			break;

		pos--;
	}

	s = str.substr(0, pos);

	return s;
}
//-----------------------------------------------------------------------
string DeleteEndCharsAfterDelimiter(const char* str, char EndChar, char Delimiter, BOOL bDeleteDelimiter)
{
	string ResStr = str;
	string::size_type pos, pos1;
	pos = ResStr.find(Delimiter);
	if (pos != string::npos)
	{
		pos1 = ResStr.length() - 1;
		if (bDeleteDelimiter)
		{
			if (pos > 0)
				pos--;
		}
		else
		{
			if (pos < pos1)
				pos++;
		}
		while ((pos1 > 0) && (pos1 > pos))
		{
			if (ResStr[pos1] != EndChar)
				break;

			pos1--;
		}

		ResStr = ResStr.substr(0, pos1 + 1);
	}

	return ResStr;
}
//-----------------------------------------------------------------------
