//#pragma once

#ifndef TSENUMS_H
#define TSENUMS_H

#include "tsBase.h"

enum tagTSFIELDID
{
  FID_DESCRIPTION                       =   62,
  FID_HOLIDAY                           = 1000,
  FID_HOLIDAY_NAME                      = 1001,
  FID_HOLIDAY_START_DATE                = 1002,
  FID_HOLIDAY_FINISH_DATE               = 1003,
  FID_SETTING                           = 1004,
  FID_SETTING_TYPE                      = 1005,
  FID_PRICE_SCALE                       =    5,
  FID_POINT_VALUE                       =  217,
  FID_DAILY_LIMIT                       =  230,
  FID_MIN_MOVEMENT                      =  218,
  FID_SESSION                           = 1006,
  FID_SESSION_TYPE                      = 1007,
  FID_SESSION_NAME                      = 1008,
  FID_CUSTOM_SESSION                    = 1009,
  FID_TEMPLATE_SESSION                  = 1010,
  FID_TIME_ZONE                         = 1011,
  FID_SESSIONS                          =  219,
  FID_SESSIONS_TIME_ZONE_INFORMATION    = 1012,
  FID_EXCHANGE                          = 1013,
  FID_EXCHANGE_NAME                     =   17,
  FID_MAP_ID                            = 1014,
  FID_EXCHANGE_TIME_ZONE_INFORMATION    = 1015,
  FID_SYMBOL                            = 1016,
  FID_SYMBOL_NAME                       =   31,
  FID_SYMBOL_ROOT                       =  209,
  FID_CUSIP                             =  199,
  FID_CATEGORY                          =  190,
  FID_DATA_FEED                         =  207,
  FID_DATA_FEED_NAME                    =  208,
  FID_DATA_FEED_STATUS                  =  335,
  FID_CONTRACT_MONTH                    =  240,
  FID_CONTRACT_YEAR                     =  241,
  FID_STRIKE_PRICE                      =   32,
  FID_CALL_PUT                          =  224,
  FID_MARGIN                            =  225,
  FID_EXPIRATION_DATE                   =  102,
  FID_EXPIRED                           =  294,
  FID_EXPIRATION_RULE                   =  222,
  FID_FIRST_NOTICE_DATE                 =  213,
  FID_DELIVERY                          =   66,

  FID_EXACT_MATCH                       =  258,

  FID_BACK_TYPE                         = 1017,
  FID_DAYS_BACK                         =  274,
  FID_BARS_BACK                         =  275,
  FID_START_DATE                        =  276,
  FID_FINISH_DATE                       =  228,
  FID_BACK_CAPACITY                     = 1018,

  FID_GROUP_BY                          = 1019,
  FID_QUERY_TYPE                        = 1020,
  FID_FIELD                             = 1021,
  FID_RESOLUTION                        = 1022,
  FID_RESOLUTION_SIZE                   = 1023,

  FID_EXCHANGE_TIME_ZONE                =  297,
  FID_LOCAL_TIME_ZONE                   =  298,

  FID_BROWSE_DIRECTION                  =  312,
  FID_BROWSE_TYPE                       =  313,
  FID_LIST_TYPE                         =  359,
  FID_SYMBOL_LIST_TYPE                  =  366,

  FID_BAR_DATE                          =  421,
  FID_BAR_TIME                          =  422,
  FID_BAR_OPEN                          =  232,
  FID_BAR_HIGH                          =  233,
  FID_BAR_LOW                           =  234,
  FID_BAR_CLOSE                         =  235,
  FID_BAR_UP_VOLUME                     =  238,
  FID_BAR_DOWN_VOLUME                   =  239,
  FID_BAR_UNCHANGED_VOLUME              =  248,
  FID_BAR_TOTAL_VOLUME                  =  249,
  FID_BAR_UP_TICKS                      =  321,
  FID_BAR_DOWN_TICKS                    =  322,
  FID_BAR_UNCHANGED_TICKS               =  323,
  FID_BAR_TOTAL_TICKS                   =  324,
  FID_BAR_OPEN_INTEREST                 =  253,
  FID_BAR_STATUS                        =  256,

  FID_BIT_FLAGS                         =  387,
  FID_8_UP_DOWN_BIT_PAIRS               =  388,
  FID_LAST_BID_ARROW                    =  409,
  FID_12_HIGH                           =  410,
  FID_12_LOW                            =  411,

  FID_ASK                               =   10,
  FID_ASK_SIZE                          =   11,
  FID_ASK_EXG                           =   12,
  FID_DATE_LAST_ASK                     =  309,
  FID_TIME_LAST_ASK                     =  311,

  FID_BID                               =    7,
  FID_BID_SIZE                          =    8,
  FID_BID_EXG                           =    9,
  FID_DATE_LAST_BID                     =  308,
  FID_TIME_LAST_BID                     =  310,

  FID_EXCHANGE_GMT                      =  440,
  FID_LOCAL_GMT                         =  441,
  FID_VWAP                              =  443,

  FID_CLSID                             = 1024,
  FID_ID                                = 1025,
  FID_DELETED                           = 1026,
  FID_OPEN_DAY                          = 1027,
  FID_OPEN_TIME                         = 1028,
  FID_CLOSE_DAY                         = 1029,
  FID_CLOSE_TIME                        = 1030,
  FID_SEOC                              = 1031,
  FID_DRIBBLE                           = 1032,
  FID_SETTLEMENT                        = 1033,
  FID_DATA_FEED_DLL_NAME                = 1034,
  FID_ACTIVE                            = 1035,

  FID_RESOLUTION_NAME                   = 1036,
  FID_RESOLUTION_SIZE_NAME              = 1037,
  FID_CATEGORY_NAME                     = 1038,
  FID_FIELD_NAME                        = 1039,
  FID_BACK_TYPE_NAME                    = 1040,
  FID_TIME_ZONE_NAME                    = 1041,

  FID_HISTORY_DATA_FEED                 = FID_DATA_FEED,
  FID_HISTORY_SYMBOL                    = FID_SYMBOL,
  FID_HISTORY_SYMBOL_NAME               = FID_SYMBOL_NAME,
  FID_HISTORY_CATEGORY                  = FID_CATEGORY,

  FID_REALTIME_DATA_FEED                = 1042,
  FID_REALTIME_SYMBOL                   = 1043,
  FID_REALTIME_SYMBOL_NAME              = 1044,
  FID_REALTIME_CATEGORY                 = 1045
}; 

enum tagTSFIELDTYPE
{
  TS_TYPE_NONE   = -1,
  TS_TYPE_TIME   = 0, // seconds since midnight
  TS_TYPE_DATE   = 1, // julian date
  TS_TYPE_STRING = 2, // string
  TS_TYPE_PIECES = 3, // number in pieces
  TS_TYPE_DOUBLE = 4, // double value
  TS_TYPE_FLOAT  = 5, // float value
  TS_TYPE_LONG   = 6, // long signed value
  TS_TYPE_ULONG  = 7, // long unsigned value
  TS_TYPE_SHORT  = 8, // short signed value
  TS_TYPE_USHORT = 9, // short unsigned value
  TS_TYPE_CHAR   = 10,// char signed value
  TS_TYPE_BYTE   = 11 // char unsigned value
};

enum tagTSBACKTYPE
{ 
  TS_NOTHING_BACK = 0,
  TS_START_DAY = 1,    // Выбрать данные за период с StartDate по EndDate
  TS_BARS_BACK = 2,    // Выбрать количество записей BackNumber назад с даты EndDate
  TS_DAYS_BACK = 3     // Выбрать с EndDate-DayBack по EndDate 
};

enum tagTIMEZONE
{
  LOCAL_TIME_ZONE     = 0,
  GMT_TIME_ZONE       = 1,
  EXCHANGE_TIME_ZONE  = 2
};

enum tagTSINTERVALSTATUS
{
  TS_EMPTY_INTERVAL = 0x0000,
  TS_INCOMPLETE_INTERVAL = 0x0001,
  TS_COMPLETE_INTERVAL = 0x0002,
};

enum tagTSTRANSACTIONSTATUS
{
  // LOWORD contains status information : 1 or multiple ORed together
  TS_TRANSACTION_SUCCESS         = 0x00000000,
  TS_TRANSACTION_FAILED          = 0x00000001,
  TS_TRANSACTION_RESET           = 0x00000002,
  TS_TRANSACTION_ABORT           = 0x00000004,
  TS_TRANSACTION_VALID           = 0x00000000,
  TS_TRANSACTION_INVALID         = 0x00000008,
  TS_TRANSACTION_DATA_TOO_LARGE  = 0x00000010,
  TS_TRANSACTION_END_OF_HISTORY  = 0x00000020,

  // HIWORD contains an error code (number)
  TS_TRANSACTION_SYMBOL_NOT_IN_PORTFOLIO  = 0x00010000,
  TS_TRANSACTION_DATAFEED_NOT_CONNECTED   = 0x00020000,
  TS_TRANSACTION_MEMORY_ERROR             = 0x00030000,
  TS_TRANSACTION_NO_DATA_AVAILABLE        = 0x00040000,
  TS_TRANSACTION_COMMAND_NOT_AVAILABLE    = 0x00050000,
  TS_TRANSACTION_DATAFEED_OFFLINE_MODE    = 0x00060000,
  TS_TRANSACTION_NEWS_SERVER_DISCONNECTED = 0x00070000
};

enum tagTSCONTENTS
{
  TS_CONTENTS_NOTHING = 0,
  TS_CONTENTS_DATA = 1,
  TS_CONTENTS_INFO = 2
};

enum tagTSREQUESTINFO
{
  TS_REQUEST_UNKNOWN           = -1,
  TS_REQUEST_DATA_FEED_LIST    = 1, 
  TS_REQUEST_DATA_FEED_STATUS  = 3,
  TS_REQUEST_SYMBOL_LIST       = 0, 
  TS_REQUEST_SYMBOL_INFO       = 2, 
  TS_REQUEST_SYMBOL_DATA       = 4,
  TS_REQUEST_SYMBOL_SNAP_SHOT  = 5,
  TS_REQUEST_SYMBOL_ENTITLEMENT= 6,
  TS_REQUEST_SYMBOL_MESSAGE    = 7
};

enum tagTSQUERYTYPE
{ 
  TS_QUERY_UNKNOWN = 0, 
  TS_QUERY_REQUEST = 1, 
  TS_QUERY_ADVISE  = 2 
};      

enum tagTSCHARTTYPE
{ 
  TS_CHART_NOTHING = 0,
  TS_CHART_TICK    = 1,
  TS_CHART_VOLUME  = 2,
  TS_CHART_BAR     = 3,
  TS_CHART_PFIGURE = 4
};

enum tagTSANSWERTYPE
{ 
  TS_IMMEDIATELY_ANSWER = 0,
  TS_CHART_ANSWER = 1,
  TS_RADAR_SCREEN_ANSWER = 2
};

enum tagEXCHANGETIMEZONE
{
  TS_CUSTOM_EXCHANGE_TIME_ZONE=0,
  TS_REGISTRY_EXCHANGE_TIME_ZONE=1
};

enum tagSESSIONTYPE
{
  TS_CUSTOM_SESSION=0,
  TS_EXCHANGE_SESSION=1,
  TS_TEMPLATE_SESSION=2
};

enum tagTSSESSIONTYPE 
{
  TS_REGULAR_SESSION=0,
  TS_24_HOUR_SESSION=1,
  TS_PRE_MARKET_SESSION=2,
  TS_POST_MARKET_SESSION=3,
  TS_PRE_AND_POST_MARKET_SESSION=4
};

enum tagSETTINGTYPE
{
  TS_CUSTOM_SETTING=0,
  TS_EXCHANGE_SETTING=1
};

inline CONST CHAR* GetResolutionName(tagTSRESOLUTIONS Resolution)
{
  CONST CHAR* Result;
  switch(Resolution)
  {
    case TS_RESOLUTION_TICK: Result="Tick"; break;
    case TS_RESOLUTION_VALUE: Result="Range"; break;
    case TS_RESOLUTION_SECOND: Result="Second"; break;
    case TS_RESOLUTION_MINUTE: Result="Minute"; break;
    case TS_RESOLUTION_DAY: Result="Day"; break;
    default: Result="";
  }
  return Result;
}

inline CONST CHAR* GetFieldName(tagTSFIELDS Field)
{
  CONST CHAR* Result;
  switch(Field)
  {
    case TS_ASK_FIELD: Result="Ask Record"; break;
    case TS_BID_FIELD: Result="Bid Record"; break;
    case TS_TRADE_FIELD: Result="Trade Record"; break;
    default: Result=""; break;
  }
  return Result;
}

inline CONST CHAR* GetTransactionStatusName(tagTSTRANSACTIONSTATUS Status)
{
  CONST CHAR* Result="";
  if(Status&TS_TRANSACTION_FAILED)
   Result="FAILED";
  else
   if(Status&TS_TRANSACTION_END_OF_HISTORY)
    Result="END OF HISTORY";
   else
    if(Status&TS_TRANSACTION_SYMBOL_NOT_IN_PORTFOLIO)
     Result="SYMBOL NOT IN PORTFOLIO";
   else
    if(TS_TRANSACTION_SUCCESS==Status)
     Result="SUCCESS";
   else
     Result="UNKNOWN";

  return Result;
}

inline CONST CHAR* GetBarStatusName(tagTSBARSTATUS Value)
{
  CONST CHAR* Result;
  switch(Value)
  {
    case TS_BAR_NONE: Result="NONE"; break;
    case TS_BAR_OPEN: Result="OPEN"; break;
    case TS_BAR_INSIDE: Result="INSIDE"; break;
    case TS_BAR_CLOSE: Result="CLOSE"; break;
    default: Result="UNKNOWN"; break;
  }
  return Result;
}

inline CONST CHAR* GetRequestTypeName(tagTSREQUESTINFO Value)
{
  CONST CHAR* Result;

  switch(Value)
  {
    case TS_REQUEST_UNKNOWN: Result="UNKNOWN"; break;
    case TS_REQUEST_DATA_FEED_LIST: Result="DATA_FEED_LIST"; break;
    case TS_REQUEST_DATA_FEED_STATUS: Result="DATA_FEED_STATUS"; break;
    case TS_REQUEST_SYMBOL_LIST: Result="SYMBOL_LIST"; break;
    case TS_REQUEST_SYMBOL_INFO: Result="SYMBOL_INFO"; break;
    case TS_REQUEST_SYMBOL_DATA: Result="SYMBOL_DATA"; break;
    case TS_REQUEST_SYMBOL_SNAP_SHOT: Result="SYMBOL_SHAP_SHOT"; break;
    case TS_REQUEST_SYMBOL_ENTITLEMENT: Result="ENTITLEMENTS"; break;
    case TS_REQUEST_SYMBOL_MESSAGE: Result="MESSAGE"; break;
    default: Result=""; break;
  }

  return Result;
};

inline CONST CHAR* GetQueryTypeName(tagTSQUERYTYPE Value)
{
  CONST CHAR* Result;

  switch(Value)
  {
    case TS_QUERY_UNKNOWN: Result="UNKNOWN"; break;
    case TS_QUERY_REQUEST: Result="REQUEST"; break;
    case TS_QUERY_ADVISE: Result="ADVISE"; break;
    default: Result=""; break;
  }

  return Result;
};

inline CONST CHAR* GetCategoryName(tagTSCATEGORIES Value)
{
  CONST CHAR* Result;

  switch(Value)
  {
    case TS_CATEGORY_NOTHING: Result="All"; break;
    case TS_CATEGORY_FUTURE: Result="Future"; break;
    case TS_CATEGORY_FUTUREOPTION: Result="Future Option"; break;
    case TS_CATEGORY_STOCK: Result="Stock"; break;
    case TS_CATEGORY_STOCKOPTION: Result="Stock Option"; break;
    case TS_CATEGORY_INDEX: Result="Index"; break;
    case TS_CATEGORY_CURROPTION: Result="CURROPTION"; break;
    case TS_CATEGORY_MUTUALFUND: Result="Mutual Fund"; break;
    case TS_CATEGORY_MONEYMKTFUND: Result="Money Market Fund"; break;
    case TS_CATEGORY_INDEXOPTION: Result="Index Option"; break;
    case TS_CATEGORY_CASH: Result="Cash"; break;
    case TS_CATEGORY_BOND: Result="Bond"; break;
    case TS_CATEGORY_SPREAD: Result="Spread"; break;
    case TS_CATEGORY_FOREX: Result="Forex"; break;
    case TS_CATEGORY_CPCSYMBOL: Result="CPCSYMBOL"; break;
    case TS_CATEGORY_COMPOSITE: Result="Composite"; break;
    case TS_CATEGORY_FUTUREROOT: Result="FUTURE ROOT"; break;
    case TS_CATEGORY_FUTUREOPTIONROOT: Result="FUTURE OPTION ROOT"; break;
    case TS_CATEGORY_STOCKOPTIONROOT: Result="STOCK OPTION ROOT"; break;
    default: Result=""; break;
  }

  return Result;
};

inline CONST CHAR* GetBackTypeName(tagTSBACKTYPE Value)
{
  CONST CHAR* Result;

  switch(Value)
  {
    case TS_NOTHING_BACK: Result="NOTHING"; break;
    case TS_START_DAY: Result="INTERVAL"; break;
    case TS_BARS_BACK: Result="BARS BACK"; break;
    case TS_DAYS_BACK: Result="DAYS BACK"; break;
    default: Result=""; break;
  }

  return Result;
};

inline CONST CHAR* GetTimeZoneName(tagTIMEZONE Value)
{
  CONST CHAR* Result;

  switch(Value)
  {
    case LOCAL_TIME_ZONE: Result="Local"; break;
    case GMT_TIME_ZONE: Result="GMT"; break;
    case EXCHANGE_TIME_ZONE: Result="Exchange"; break;
    default: Result=""; break;
  }

  return Result;
};

inline CONST CHAR* GetFieldTypeName(tagTSFIELDTYPE Value)
{
  CONST CHAR* Result;
  
  switch(Value)
  {
    case TS_TYPE_NONE   : Result="NONE"; break;
    case TS_TYPE_TIME   : Result="TIME"; break;
    case TS_TYPE_DATE   : Result="DATE"; break;
    case TS_TYPE_STRING : Result="STRING"; break;
    case TS_TYPE_PIECES : Result="PIECES"; break;
    case TS_TYPE_DOUBLE : Result="DOUBLE"; break;
    case TS_TYPE_FLOAT  : Result="FLOAT"; break;
    case TS_TYPE_LONG   : Result="LONG"; break;
    case TS_TYPE_ULONG  : Result="ULONG"; break;
    case TS_TYPE_SHORT  : Result="SHORT"; break;
    case TS_TYPE_USHORT : Result="USHORT"; break;
    case TS_TYPE_CHAR   : Result="CHAR"; break;
    case TS_TYPE_BYTE   : Result="BYTE"; break;
    default: Result=""; break;
  }

  return Result;
}

inline CONST CHAR* GetSessionTypeName(tagTSSESSIONTYPE Value)
{
    CONST CHAR* Result;

    switch(Value)
    {
        case TS_REGULAR_SESSION : Result="Regular"; break;
        case TS_24_HOUR_SESSION : Result="24 Hour"; break;
        case TS_PRE_MARKET_SESSION : Result="Pre-market"; break;
        case TS_POST_MARKET_SESSION : Result="Post-market"; break;
        case TS_PRE_AND_POST_MARKET_SESSION : Result="Pre & Post Market"; break;
        default: Result=""; break;
    }

    return Result;
}

#endif