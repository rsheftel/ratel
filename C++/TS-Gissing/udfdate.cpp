#include "stdafx.h"
#include "udfdate.h"


BYTE CUDFDate::daysForMonths[12] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
WORD CUDFDate::daysForMonthsSum[12] = {31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365};

