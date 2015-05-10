#ifndef _UDF_DATE_
#define _UDF_DATE_

#include <wtypes.h>
#include <string>

using namespace std;

enum EDayOfWeek
{
	DAY_MONDAY = 1,
	DAY_TUESDAY = 2,
	DAY_WEDNESDAY = 3,
	DAY_THURSDAY = 4,
	DAY_FRIDAY = 5,
	DAY_SATURDAY = 6,
	DAY_SUNDAY = 0
};

class CUDFTime
{
public:
	CUDFTime() : m_time(0) {}
	CUDFTime(const CUDFTime& time) { *this = time; }
	CUDFTime(DWORD time) { SetTime(time); }
	CUDFTime(UINT hours, UINT minutes, UINT seconds) { SetTime(hours, minutes, seconds); }

	void SetTime(DWORD time)
	{
		m_time = time;
	}
	void SetTime(UINT hours, UINT minutes, UINT seconds)
	{
		m_time = hours*3600 + minutes*60 + seconds;
	}
	void GetTime(UINT& hours, UINT& minutes, UINT& seconds) const
	{
		hours = m_time / 3600;
		minutes = m_time % 3600 / 60;
		seconds = m_time % 3600 % 60;
	}

	DWORD GetTime() const
	{
		return m_time; 
	}
	string GetText() const
	{
		UINT h, m, s;
		char buf[20];
		string tText;
		GetTime(h, m, s);
		itoa(h, buf, 10);
		if (strlen(buf) == 1) tText += "0";
		tText += buf;
		tText += ":";
		itoa(m, buf, 10);
		if (strlen(buf) == 1) tText += "0";
		tText += buf;
		tText += ":";
		itoa(s, buf, 10);
		if (strlen(buf) == 1) tText += "0";
		tText += buf;
		return tText;
	}	

	CUDFTime& operator=(const CUDFTime& time)
	{
		m_time = time.m_time;
		return *this;
	}
	CUDFTime& operator=(UINT time)
	{
		m_time = time;
		return *this;
	}
	CUDFTime& operator=(DWORD time)
	{
		m_time = time;
		return *this;
	}

	bool operator==(const CUDFTime& time) const
	{
		return m_time == time.m_time;
	}
	bool operator!=(const CUDFTime& time) const
	{
		return m_time != time.m_time;
	}
	bool operator<(const CUDFTime& time) const
	{
		return m_time < time.m_time;
	}
	bool operator==(DWORD time) const
	{
		return m_time == time;
	}
	bool operator!=(DWORD time) const
	{
		return m_time != time;
	}
	bool operator<(DWORD time) const
	{
		return m_time < time;
	}

	operator DWORD() const
	{
		return m_time;
	}
	operator UINT() const
	{
		return m_time;
	}

protected:
	DWORD m_time;
};

class CUDFDate
{
public:
	CUDFDate() : m_date(0) {}
	CUDFDate(const CUDFDate& date) { *this = date; }
	CUDFDate(UINT day, UINT month, UINT year) { SetDate(day, month, year); }
	CUDFDate(WORD date) { SetDate(date); }

	WORD GetDate() const
	{
		return m_date;
	}
	void GetDate(UINT& day, UINT& month, UINT& year) const
	{
		GetDateForNumDays(m_date, day, month, year);
	}
	void SetDate(WORD date)
	{
		m_date = date;
	}
	void SetDate(UINT day, UINT month, UINT year)
	{
		m_date = GetNumDaysForDate(day, month, year); 
	}
	operator WORD() const
	{
		return m_date;
	}
	CUDFDate& operator=(const CUDFDate& date)
	{
		m_date = date.m_date;
		return *this;
	}
	CUDFDate& operator=(const WORD date)
	{
		m_date = date;
		return *this;
	}
	bool operator==(const CUDFDate& date) const
	{
		return m_date == date.m_date;
	}
	bool operator==(WORD date) const
	{
		return m_date == date;
	}
	bool operator!=(const CUDFDate& date) const
	{
		return m_date != date.m_date;
	}
	bool operator!=(WORD date) const
	{
		return m_date != date;
	}
	bool operator<(const CUDFDate& date) const
	{
		return m_date < date.m_date;
	}
	bool operator<(WORD date) const
	{
		return m_date < date;
	}

	static WORD GetNumDaysForDate(UINT day, UINT month, UINT year)
	{
		if (!month || !year) return day;
		UINT numYear = year - 1899;
		UINT numLongYear = (numYear - 1) / 4;
		if (!((numYear - 1) % 4) && (numYear != 1)) numLongYear--;
		UINT numDays = (numYear - numLongYear - 1)*365 + numLongYear*366 + day;
		if (year != 1899) numDays--;
		if (month > 1)
			numDays += daysForMonthsSum[month - 2];
//		for(UINT i = 0; i < month - 1; i++) numDays += daysForMonths[i];
		if ((month > 2) && !(year % 4) && (year != 1900)) numDays++;
		return numDays + 2;
	}

	static void GetDateForNumDays(DWORD nDays, UINT& day, UINT& month, UINT& year)
	{
		UINT numYear;
		if (nDays > 367) numYear = ((nDays - 367) / 1461) * 4 + ((nDays - 367) % 1461) / 365 + 2; else 
		if (nDays > 1) numYear = 1; else numYear = 0;
		if (!((nDays - 366) % 1461) && (nDays > 367)) numYear--;
		if (nDays == 367) numYear++;
		UINT numLongYear = (numYear - 1)/ 4;
		UINT numDays;
		if (nDays >= 2) numDays = nDays - (numYear - numLongYear - 1)*365 - numLongYear*366;
		else numDays = 365 + nDays;
		if ((numYear - 1) % 4) numDays--;
		if (numYear == 1) numDays--;
		UINT d;
		int i;
		for(i = 0; i < 12; i++)
		{
			if ((i == 1) && !((numYear - 1) % 4) && (numYear - 1)) d = 1; else d = 0;
			if (numDays <= daysForMonths[i] + d)
			{
				day  = numDays;
				break;
			} 
			numDays -= daysForMonths[i] + d;
		}
		month = i + 1;
		year = numYear + 1899;
	}

	static BOOL IsLongYear(UINT year)
	{
		return ((year != 1900) && !(year % 4));
	}
	
	static UINT GetNumDaysForMonth(UINT month, UINT year)
	{
		UINT incDays = 0;
		if ((month == 2) && (IsLongYear(year))) incDays++;
		return daysForMonths[month - 1] + incDays;
	}

	string GetText() const
	{
		UINT d, m, y;
		char buf[20];
		string dText;
		GetDate(d, m, y);
		itoa(m, buf, 10);
		if (strlen(buf) == 1) dText += "0";
		dText += buf;
		dText += "/";
		itoa(d, buf, 10);
		if (strlen(buf) == 1) dText += "0";
		dText += buf;
		dText += "/";
		itoa(y, buf, 10);
		dText += buf;
		return dText;
	}
	BOOL SetText(const char* dateText)
	{
		UINT d, m, y;
		char buf[5];
		if (strlen(dateText) < 10) return FALSE;
		buf[0] = dateText[0];
		buf[1] = dateText[1];
		buf[2] = 0;
		m = atoi(buf);
		if (!m || (m > 12)) return FALSE;
		buf[0] = dateText[3];
		buf[1] = dateText[4];
		buf[2] = 0;
		d = atoi(buf);
		if (!d || (d > daysForMonths[m-1])) return FALSE;
		buf[0] = dateText[6];
		buf[1] = dateText[7];
		buf[2] = dateText[8];
		buf[3] = dateText[9];
		buf[4] = 0;
		y = atoi(buf);
		if ((y < 1899) || (y > 2079)) return FALSE;
		SetDate(d, m, y);
		return TRUE;
	}

protected:
	WORD m_date;
public:
	static BYTE daysForMonths[12];
	static WORD daysForMonthsSum[12];	
};

class CUDFDateTime : protected CUDFDate, protected CUDFTime
{
public:

	CUDFDateTime() {}
	CUDFDateTime(const CUDFDateTime& dateTime) { *this = dateTime; }
	CUDFDateTime(const CUDFDate& date, const CUDFTime& time) { SetDate((WORD)date); SetTime((DWORD)time); }
	CUDFDateTime(const CUDFDate& date) { *this = date; }
	CUDFDateTime(const CUDFTime& time) { *this = time; }
	CUDFDateTime(WORD date) { SetDate(date); }
	CUDFDateTime(DWORD time) { SetTime(time); }
	CUDFDateTime(WORD date, DWORD time) { SetDateTime(date, time); }
	CUDFDateTime(double interv) { SetInterval(interv); }
	CUDFDateTime(UINT day, UINT month, UINT year, UINT hours, UINT minutes, UINT seconds)
	{
		SetDateTime(day, month, year, hours, minutes, seconds);
	}

	void SetDateTime(UINT day, UINT month, UINT year, UINT hours, UINT minutes, UINT seconds)
	{
		CUDFDate::SetDate(day, month, year);
		CUDFTime::SetTime(hours, minutes, seconds);
	}

	void SetDate(WORD date)
	{
		CUDFDate::SetDate(date);
	}

	void SetTime(DWORD time)
	{
		CUDFTime::SetTime(time);
	}

	void SetDateTime(WORD date, DWORD time)
	{
		CUDFDate::SetDate(date);
		CUDFTime::SetTime(time);
	}

	void SetDateTime(INT date, INT time)
	{
		CUDFDate::SetDate((WORD)date);
		CUDFTime::SetTime((DWORD)time);
	}

	void GetDate(UINT& day, UINT& month, UINT& year) const
	{
		CUDFDate::GetDate(day, month, year);
	}

	void GetTime(UINT& hours, UINT& minutes, UINT& seconds) const
	{
		CUDFTime::GetTime(hours, minutes, seconds);
	}	

	WORD GetDate() const
	{
		return CUDFDate::GetDate();
	}

	DWORD GetTime() const
	{
		return CUDFTime::GetTime();
	}
	
	DWORD GetInterval() const
	{
		DWORD interv = m_date;
		interv *= 86400;
		return interv += m_time;
	}

	void SetInterval(DWORD interv)
	{
		m_date = (WORD)(interv / 86400);
		m_time = interv % 86400;
	}

	void SetInterval(double interv)
	{
		DWORD interval = (DWORD)interv;
		SetInterval(interval);
	}

	operator WORD() const
	{
		return CUDFDate::operator WORD();
	}

	operator DWORD() const
	{
		return CUDFTime::operator DWORD();
	}
	operator UINT() const
	{
		return CUDFTime::operator UINT();
	}

	operator CUDFDate() const
	{
		return CUDFDate(m_date);
	}

	operator CUDFTime() const
	{
		return CUDFTime(m_time);
	}

	CUDFDateTime& operator=(const CUDFDateTime& dateTime)
	{
		m_date = dateTime.m_date;
		m_time = dateTime.m_time;
		return *this;
	}

	CUDFDateTime& operator=(const CUDFDate& date)
	{
		m_date = date;
		m_time = 0;
		return *this;
	}

	CUDFDateTime& operator=(const CUDFTime& time)
	{
		m_date = 0;
		m_time = time;
		return *this;
	}

	CUDFDateTime& operator=(WORD date)
	{
		m_date = date;
		return *this;
	}
	CUDFDateTime& operator=(UINT time)
	{
		m_time = time;
		return *this;
	}
	CUDFDateTime& operator=(DWORD time)
	{
		m_time = time;
		return *this;
	}

	bool operator<(const CUDFDateTime& dateTime) const
	{
		if (m_date < dateTime.m_date) return true; else if (m_date > dateTime.m_date) return false;
		if (m_time < dateTime.m_time) return true;
		return false;
	}
	bool operator>(const CUDFDateTime& dateTime) const
	{
		return dateTime < *this;
	}
	bool operator<=(const CUDFDateTime& dateTime) const
	{
		if (m_date < dateTime.m_date) return true; else if (m_date > dateTime.m_date) return false;
		if (m_time <= dateTime.m_time) return true;
		return false;
	}
	bool operator>=(const CUDFDateTime& dateTime) const
	{
		return dateTime <= *this;
	}
	bool operator==(const CUDFDateTime& dateTime) const
	{
		return ((m_date == dateTime.m_date) && (m_time == dateTime.m_time));
	}
	bool operator!=(const CUDFDateTime& dateTime) const
	{
		return ((m_date != dateTime.m_date) || (m_time != dateTime.m_time));
	}
	CUDFDateTime operator+(CUDFDateTime& dateTime)
	{
		return CUDFDateTime(m_date + dateTime.m_date + (WORD)((m_time + dateTime.m_time) / 86400), (m_time + dateTime.m_time) % 86400);
	}
	CUDFDateTime operator-(CUDFDateTime& dateTime)
	{
		WORD date = (m_date > dateTime.m_date) ? m_date - dateTime.m_date : 0;
		DWORD time;
		if (m_time >= dateTime.m_time) 
		{
			time = m_time - dateTime.m_time;
		} else
		{
			if (date)
			{
				date--;
				time = m_time + 86400 - dateTime.m_time;
			} else time = 0;
		}
		return CUDFDateTime(date, time);
	}
	CUDFDateTime& operator+=(CUDFDateTime& dateTime)
	{
		m_date += dateTime.m_date + (WORD)((m_time + dateTime.m_time) / 86400);
		m_time = (m_time + dateTime.m_time) % 86400;
		return *this;
	}
	CUDFDateTime& operator-=(CUDFDateTime& dateTime)
	{
		m_date = (m_date > dateTime.m_date) ? m_date - dateTime.m_date : 0;
		if (m_time >= dateTime.m_time) 
		{
			m_time -= dateTime.m_time;
		} else
		{
			if (m_date)
			{
				m_date--;
				m_time += 86400 - dateTime.m_time;
			} else m_time = 0;
		}
		return *this;
	}

	CUDFDateTime& operator+=(DWORD numSecs)
	{
		SetInterval(GetInterval() + numSecs);
		return *this;
	}
	CUDFDateTime& operator-=(DWORD numSecs)
	{
		SetInterval(GetInterval() - numSecs);
		return *this;
	}

	string GetText(BOOL inclTime=TRUE) const
	{
		string dText;
		UINT d, m, y;
		UINT ho, mi, se;
		GetDate(d, m, y);
		GetTime(ho, mi, se);
		switch (m)
		{
			case 1: dText += "January "; break;
			case 2: dText += "February "; break;
			case 3: dText += "March "; break;
			case 4: dText += "April "; break;
			case 5: dText += "May "; break;
			case 6: dText += "June "; break;
			case 7: dText += "July "; break;
			case 8: dText += "August "; break;
			case 9: dText += "September "; break;
			case 10: dText += "October "; break;
			case 11: dText += "November "; break;
			default: dText += "December ";
		}
		char buf[20];
		itoa(d, buf, 10);
		dText += buf;
		d = m_date;
		if (d > 1) d -= 2; else d += 5;
		d %= 7;
		switch (d)
		{
			case 0: dText += ", Monday "; break;
			case 1: dText += ", Tuesday "; break;
			case 2: dText += ", Wednesday "; break;
			case 3: dText += ", Thursday "; break;
			case 4: dText += ", Friday "; break;
			case 5: dText += ", Saturday "; break;
			default: dText += ", Sunday ";
		}
		itoa(y, buf, 10);
		dText += buf;
		if (inclTime)
		{
			dText += " ";
			itoa(ho, buf, 10);
			if (strlen(buf) == 1) dText += "0";
			dText += buf;
			dText += ":";
			itoa(mi, buf, 10);
			if (strlen(buf) == 1) dText += "0";
			dText += buf;
			dText += ":";
			itoa(se, buf, 10);
			if (strlen(buf) == 1) dText += "0";
			dText += buf;
		}
		return dText;
	}

	EDayOfWeek GetDayOfWeek() const
	{
		UINT d = m_date;
		if (d > 1) d -= 2; else d += 5;
		d %= 7;
		switch (d)
		{
			case 0: return DAY_MONDAY;
			case 1: return DAY_TUESDAY;
			case 2: return DAY_WEDNESDAY;
			case 3: return DAY_THURSDAY;
			case 4: return DAY_FRIDAY;
			case 5: return DAY_SATURDAY;
			default: return DAY_SUNDAY;
		}
	}
};


#endif