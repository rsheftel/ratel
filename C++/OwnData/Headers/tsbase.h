#pragma once

#include "tsBaseEnums.h"

namespace ts
{

#pragma pack(push,enter_ts)
#pragma pack(1)

// --------------------------------------------------------------------------------------
// SDT - Date/Time
// --------------------------------------------------------------------------------------
struct SDT
{
  INT Date;
  INT Time;
  
  SDT(INT d=0, INT t=0)
  {
    Date=d; Time=t;
  }

  inline void Adjust()
  {
    const INT SecondsInDay=60*60*24;

        while(Time>=SecondsInDay)
        {
          Date++;
          Time-=SecondsInDay;
        }
  
        while(Time<0)
        {
          Date--;
          Time+=SecondsInDay;
        }
    
    if(Date<0)
         Date=0;
  }

  inline SDT& operator+=(int Delta)
  {
    Time+=Delta;
    Adjust();  
    return *this;
  }

  inline SDT& operator-=(int Delta)
  {
    Time-=Delta;
    Adjust();  
    return *this;
  }

  inline SDT& operator++()
  {
    return operator+=(1);
  }

  inline SDT& operator++(int)
  {
    return operator++();
  }

  inline SDT& operator--()
  {
    return operator-=(1);
  }

  inline SDT& operator--(int)
  {
    return operator--();
  }

  inline BOOL operator<(CONST SDT& Value)CONST
  {
    BOOL Result;
    if(Value.Date==Date)
     Result=(Time<Value.Time);
    else
     Result=(Date<Value.Date);
    return Result;
  }

  inline BOOL operator<=(CONST SDT& Value)CONST
  {
    BOOL Result;
    if(Value.Date==Date)
     Result=(Time<=Value.Time);
    else
     Result=(Date<Value.Date);
    return Result;
  }

  inline BOOL operator>=(CONST SDT& Value)CONST
  {
    BOOL Result;
    if(Value.Date==Date)
     Result=(Time>=Value.Time);
    else
     Result=(Date>Value.Date);
    return Result;
  }
};

inline INT operator-(CONST SDT& Value1, CONST SDT& Value2)
{
  const INT SecondsInDay=60*60*24;
  double v1=Value1.Date*SecondsInDay+Value1.Time;
  double v2=Value2.Date*SecondsInDay+Value2.Time;
  double Result=v1-v2;

  return (INT)Result;
}

// --------------------------------------------------------------------------------------
// STick - [Tick]
// --------------------------------------------------------------------------------------
template<class T>
struct STick : public T
{
  DOUBLE Price;
  FLOAT  Volume;
  tagTSBARSTATUS Status;
};

// --------------------------------------------------------------------------------------
// SBar - [TickBar, VolumeBar, SecondsBar, IntradayBar, HourBar]
// --------------------------------------------------------------------------------------
template<class T>
struct SBar : public T
{
  DOUBLE  Open;
  DOUBLE  High;
  DOUBLE  Low;
  DOUBLE  Close;
  FLOAT   UpVolume;
  FLOAT   DownVolume;
  FLOAT   UnchangedVolume;   
  FLOAT   TotalVolume;   
  FLOAT   UpTicks;
  FLOAT   DownTicks;
  FLOAT   UnchangedTicks;
  FLOAT   TotalTicks;  
  tagTSBARSTATUS Status;
};

// --------------------------------------------------------------------------------------
// SBarEx - [DailyBar, MonthlyBar, WeeklyBar, YearlyBar]
// --------------------------------------------------------------------------------------
template<class T>
struct SBarEx : public SBar<T>
{
  FLOAT OpenInterest;
};

// --------------------------------------------------------------------------------------
// SStatusLine
// --------------------------------------------------------------------------------------
struct SStatusLine
{
  INT     Date;
  INT     Time;

  DOUBLE  Open;
  DOUBLE  High;
  DOUBLE  Low;
  DOUBLE  Close;
  FLOAT   TotalVolume;   
  DOUBLE  Ask;
  DOUBLE  Bid;

  DOUBLE  Prev;
  FLOAT   OpenInterest;
};

// --------------------------------------------------------------------------------------
// StatusLineBits
// --------------------------------------------------------------------------------------
#define TS_STATUS_LINE_DATE 0x0001
#define TS_STATUS_LINE_TIME 0x0002
#define TS_STATUS_LINE_OPEN 0x0004
#define TS_STATUS_LINE_HIGH 0x0008
#define TS_STATUS_LINE_LOW  0x0010
#define TS_STATUS_LINE_CLOSE 0x0020
#define TS_STATUS_LINE_TOTAL_VOLUME 0x0040
#define TS_STATUS_LINE_ASK 0x0080
#define TS_STATUS_LINE_BID 0x0100
#define TS_STATUS_LINE_PREV 0x0200
#define TS_STATUS_LINE_OPEN_INTEREST 0x0400

// --------------------------------------------------------------------------------------
// SSupplierTick - [Tick]
// --------------------------------------------------------------------------------------
typedef STick<SDT> SSupplierTick;

// --------------------------------------------------------------------------------------
// SSupplierBar - [TickBar, VolumeBar, SecondsBar, IntradayBar, HourBar]
// --------------------------------------------------------------------------------------
typedef SBar<SDT> SSupplierBar;

// --------------------------------------------------------------------------------------
// SSupplierBarEx - [DailyBar, MonthlyBar, WeeklyBar, YearlyBar]
// --------------------------------------------------------------------------------------
typedef SBarEx<SDT> SSupplierBarEx;




#pragma pack( pop, enter_ts)

};
