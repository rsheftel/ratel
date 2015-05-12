
#include <windows.h>
#include <ATLComTime.h>
#include <atlconv.h>
#include <stdarg.h>

#include <atlsafe.h>

#import "C:\Program Files\TradeStation 8.2 (Build 3896)\Program\tskit.dll" no_namespace
#import "..\QExcel\bin\Debug\QExcel.tlb" raw_interfaces_only no_namespace



// DLL Main
BOOL __stdcall DllMain(HANDLE hModule,
					   DWORD ul_reason_for_call,
					   LPVOID lpReserved
					   )
{
	switch( ul_reason_for_call )
	{
		case DLL_PROCESS_ATTACH:
		case DLL_THREAD_ATTACH:
		case DLL_THREAD_DETACH:
		case DLL_PROCESS_DETACH:
		break; 
	}
	return TRUE ;
}

// Internal-use functions
BSTR __stdcall bstr( LPSTR szInString )
{
	int nSize ;
	nSize = MultiByteToWideChar(CP_ACP, 0, szInString, -1, NULL, 0) ;
	WCHAR* wszUniString ;
	wszUniString = new WCHAR[nSize] ;
	MultiByteToWideChar(CP_ACP, 0, szInString, -1, wszUniString, nSize) ;
	BSTR bstrOutString ;
	bstrOutString = SysAllocString( wszUniString ) ;
	delete[] wszUniString ;
	return bstrOutString ;
}

BSTR __stdcall bstrOrNull( LPSTR in ) {
	if(strcmp(in, "NULL") == 0)
		return NULL;
	return bstr(in);
}

class Bomb {
	const char* error;
public:
	Bomb(const char* error);
};

Bomb::Bomb(const char* message) : error(message) {}

//  Generate a run-time error in TradeStation
void error
( IEasyLanguageObject * pEL, const char* errorString)
{
	TSRuntimeErrorItem tsItem;
	int m_HistErr ;
	tsItem.sCompany = _bstr_t("Malbec Quantys Fund").copy();
	tsItem.sErrorLocation = _bstr_t("QExcel TS Library").copy();
	tsItem.sErrorCategory = _bstr_t("Error").copy();
	tsItem.sLongString = NULL;
	tsItem.nParameters = 0;
	tsItem.sShortString = _bstr_t("Error in QExcelTS.dll").copy();
	tsItem.sSourceString = _bstr_t(errorString).copy();
	tsItem.nErrorCode = -1;
	m_HistErr = pEL->Errors->RegisterError( &tsItem ) ;
	pEL->Errors->RaiseRuntimeError( m_HistErr ) ;
	throw Bomb(errorString);
}


void noComError(HRESULT rc, IEasyLanguageObject* pEL, int lineNum) {
	if(!FAILED(rc)) return;
	TCHAR error_msg[1024];
	USES_CONVERSION;
	FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM, 0, rc, 0, error_msg, 1024, NULL);
	TCHAR lineStr[128];
	swprintf_s(lineStr, 128, L"%d", lineNum);
	wcscat_s(error_msg, 1024, lineStr);
	error(pEL, T2A(error_msg));
}

#define CRAPPY_SENTINEL_VALUE -999999
#define COM(fn) { noComError(fn, pEL, __LINE__); }
#define TSDB_COM(fn, type) { \
	TSDB_COM_VOID(fn); \
	if(!(result.vt == (type))) { \
		char errorString[10240]; \
		if(result.vt == VT_BSTR) { \
			_bstr_t bstr(result); \
			sprintf_s(errorString, 10240, "error returned from " #fn ": %s", (char*) bstr); \
		} else { \
			sprintf_s(errorString, 10240, "something besides a " #type " returned from " #fn ": %i", result.vt); \
		} \
		error(pEL, errorString); \
	} \
}

#define TSDB_COM_VOID(fn) { noComError(tsdb->fn, pEL, __LINE__); }

#define INIT_TSDB _Tsdb* tsdb; _TsdbPtr p(__uuidof(Tsdb)); tsdb = p

#define NO_BOMB(doStuff) { \
	try { \
		doStuff \
	} catch (Bomb) { \
		return CRAPPY_SENTINEL_VALUE; \
	} \
}

int elDate(const VARIANT& date) {
	COleDateTime dateInfo(date);
	return (dateInfo.GetYear() - 1900) * 10000 + dateInfo.GetMonth() * 100 + dateInfo.GetDay();
}

void elAssert(bool condition, IEasyLanguageObject* pEL, const char* message) {
	if(condition) return;
	error(pEL, message);
}

int populateELArray(IEasyLanguageObject* pEL, IEasyLanguageVariablePtr elArray, SAFEARRAY *comArray) {
	elAssert(SafeArrayGetDim(comArray) == 2, pEL, 
		"populateELArray only currently supports rectangular arrays");
	long rowsLower, rowsUpper, colsLower, colsUpper;
	COM(SafeArrayGetLBound(comArray, 1, &rowsLower));
	COM(SafeArrayGetUBound(comArray, 1, &rowsUpper));
	COM(SafeArrayGetLBound(comArray, 2, &colsLower));
	COM(SafeArrayGetUBound(comArray, 2, &colsUpper));
	for(long r = rowsLower; r <= rowsUpper; r++) {
		for(long c = colsLower; c <= colsUpper; c++) {
			VARIANT value;
			long at[] = { r, c };
			COM(SafeArrayGetElement(comArray, at, &value));
			elArray->SelectedIndex[0] = r - rowsLower;
			elArray->SelectedIndex[1] = c - colsLower;
			switch(value.vt) {
				case VT_DATE: elArray->Value[0] = elDate(value); break;
				case VT_R8: elArray->Value[0] = value.dblVal; break;
				case VT_I8: elArray->Value[0] = value.lVal; break;
				default: 
					char message[128];
					sprintf_s(message, 128, "unknown variant type in populateELArray: %d", value.vt);
					error(pEL, message);
			}
		}
	}
	return rowsUpper - rowsLower + 1;
}


// Exported function(s)
int __stdcall BusinessDaysAgo(IEasyLanguageObject* pEL, int days, LPSTR date, LPSTR center) {
	NO_BOMB(
		INIT_TSDB;
		VARIANT result;
		TSDB_COM(businessDaysAgo(days, bstr(date), bstr(center), &result), VT_DATE);
		return elDate(result);
	);
}

_bstr_t* string(IEasyLanguageObject* pEL, VARIANT *from, ...) {
	INIT_TSDB;
	VARIANT value;
	if(from->vt == (VT_ARRAY | VT_VARIANT)) {
		int dims = SafeArrayGetDim(from->parray);
		long* at = new long[dims];
		va_list args;
		va_start(args, from);
		for(int i = 0; i < dims; i++) at[i] = va_arg(args, long);
		va_end(args);
		COM(SafeArrayGetElement(from->parray, at, &value));
		delete[] at;
	} else 
		value = *from;
	return new _bstr_t(value);
}


int __stdcall LiveDescription(IEasyLanguageObject* pEL, LPSTR symbolName, LPSTR sourceName, LPSTR templateName, LPSTR topicName) {
	NO_BOMB(
		INIT_TSDB;
		VARIANT result;
		TSDB_COM(liveDescription(bstr(symbolName), &result), VT_ARRAY | VT_VARIANT);
		_bstr_t *source = string(pEL, &result, 0L);
		_bstr_t *templat = string(pEL, &result, 1L);
		_bstr_t *topic = string(pEL, &result, 2L);
		pEL->GetVariables(sourceName)->PutAsString(0, *source);
		pEL->GetVariables(templateName)->PutAsString(0, *templat);
		pEL->GetVariables(topicName)->PutAsString(0, *topic);
		delete source;
		delete templat;
		delete topic;
		return 0;
	);
}



int __stdcall RetrieveOneTimeSeriesByName(IEasyLanguageObject* pEL, LPSTR target, LPSTR series, LPSTR source, LPSTR start, LPSTR end) {
	NO_BOMB(
		INIT_TSDB;
		VARIANT result;
		TSDB_COM(
			retrieveOneTimeSeriesByName(bstr(series), bstr(source), bstrOrNull(start), bstrOrNull(end), &result),
			VT_ARRAY | VT_VARIANT
		);
		return populateELArray(pEL, pEL->GetVariables(target), result.parray);
	);
}



int __stdcall RetrieveOneSymbol(IEasyLanguageObject* pEL, LPSTR target, LPSTR market, LPSTR start, LPSTR end) {
	NO_BOMB(
		INIT_TSDB;
		VARIANT result;
		TSDB_COM(
			retrieveOneSymbol(bstr(market), bstrOrNull(start), bstrOrNull(end), &result),
			VT_ARRAY | VT_VARIANT
		);
		return populateELArray(pEL, pEL->GetVariables(target), result.parray);
	);
}


int __stdcall RetrieveOneSymbolClose(IEasyLanguageObject* pEL, LPSTR target, LPSTR market, LPSTR start, LPSTR end) {
	NO_BOMB(
		INIT_TSDB;
		VARIANT result;
		TSDB_COM(
			retrieveOneSymbolClose(bstr(market), bstrOrNull(start), bstrOrNull(end), &result),
			VT_ARRAY | VT_VARIANT
		);
		return populateELArray(pEL, pEL->GetVariables(target), result.parray);
	);
}


// http://ocean.stanford.edu/research/idl_doc/2D_Array_Examples.html
void setPair(CComSafeArray<BSTR> *multiDimSafeArray, long row, BSTR key, BSTR value) {

	long ndx[2]; // row, column
	ndx[0] = row;

	ndx[1] = 0;
	multiDimSafeArray->MultiDimSetAt(ndx,key);

	ndx[1] = 1;
	multiDimSafeArray->MultiDimSetAt(ndx,value);
}

int __stdcall AmqPublish(IEasyLanguageObject* pEL, LPSTR topicName, LPSTR keyValues, int numFields) {
	
	NO_BOMB(
        IEasyLanguageVariablePtr myELArray = pEL->GetVariables(keyValues);

		CComSafeArrayBound bound[2];  
		bound[0].SetCount(numFields); // numFields rows  
		bound[1].SetCount(2); // 2 columns 

		CComSafeArray<BSTR> twoDim(bound,2);

		for (int row = 0; row < numFields; row++) {

            myELArray->SelectedIndex[0] = row;
            myELArray->SelectedIndex[1] = 0;  // key column
			_bstr_t key = myELArray->AsString[0];

            myELArray->SelectedIndex[0] = row;
            myELArray->SelectedIndex[1] = 1; // value column
			_bstr_t value = myELArray->AsString[0];

			// add to the multi-dimensional array
			setPair(&twoDim, row, key, value);
        }

        INIT_TSDB;
        VARIANT result;
        // make your COM call with TSDB_COM
		TSDB_COM(
			publish(bstr(topicName), twoDim, &result), VT_BOOL
		);
		return 0;
	);
}

int __stdcall AmqPublishOne(IEasyLanguageObject* pEL, LPSTR topicName, LPSTR key, LPSTR value) {
	NO_BOMB(
        INIT_TSDB;
        VARIANT result;
        // make your COM call with TSDB_COM
		TSDB_COM(
			publishOne(bstr(topicName), bstr(key), bstr(value), &result), VT_BOOL
		);
		return 0;
	);
}

int __stdcall AmqGetValue(IEasyLanguageObject* pEL, LPSTR topicName, LPSTR key, LPSTR valueRef) {
	NO_BOMB(
        INIT_TSDB;
        VARIANT result;

		TSDB_COM(
			getFieldValue(bstr(topicName), bstr(key), &result), VT_BSTR  
		);
		_bstr_t *value = string(pEL, &result);
		pEL->GetVariables(valueRef)->PutAsString(0, *value);
		return 0;
	);
}


