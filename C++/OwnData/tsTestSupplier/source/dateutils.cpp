#include "dateutils.h"
#include "strutils.h"

//-----------------------------------------------------------------------
int GetNumOfMonth(const char* month)
{
	string m;
	int NumMonth = 0;
	m = StringToLowerCase(month);

	if (!strcmp(m.c_str(), "jan"))
		 NumMonth = 0;
	else
	if (!strcmp(m.c_str(), "feb"))
		 NumMonth = 1;
	else
	if (!strcmp(m.c_str(), "mar"))
		 NumMonth = 2;
	else
	if (!strcmp(m.c_str(), "apr"))
		 NumMonth = 3;
	else
	if (!strcmp(m.c_str(), "may"))
		 NumMonth = 4;
	else
	if (!strcmp(m.c_str(), "jun"))
		 NumMonth = 5;
	else
	if (!strcmp(m.c_str(), "jul"))
		 NumMonth = 6;
	else
	if (!strcmp(m.c_str(), "aug"))
		 NumMonth = 7;
	else
	if (!strcmp(m.c_str(), "sep"))
		 NumMonth = 8;
	else
	if (!strcmp(m.c_str(), "oct"))
		 NumMonth = 9;
	else
	if (!strcmp(m.c_str(), "nov"))
		 NumMonth = 10;
	else
	if (!strcmp(m.c_str(), "dec"))
		 NumMonth = 11;

	return NumMonth;
}
//-----------------------------------------------------------------------
