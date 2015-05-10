// tsSupplier.h : Declaration of the CtsSupplier
#pragma once

[
    object,
    uuid("370B4079-40BB-47c9-B797-33B3B5422685"),
    dual, helpstring("ItsSupplier Interface"),
    pointer_default(unique)
]
__interface ItsSupplier : IDispatch
{
    [propget, id(1), helpstring("property Name")] HRESULT Name([out, retval] BSTR* pVal);
    [propget, id(2), helpstring("property ShortName")] HRESULT ShortName([out, retval] BSTR* pVal);
    [propget, id(3), helpstring("property Description")] HRESULT Description([out, retval] BSTR* pVal);
    [propget, id(4), helpstring("property Version")] HRESULT Version([out, retval] BSTR* pVal);
    [propget, id(5), helpstring("property Vendor")] HRESULT Vendor([out, retval] BSTR* pVal);
    [id(6), helpstring("method ShowProperties")] HRESULT ShowProperties(long Datafeed);

    [propget, id(7), helpstring("property ConnectionsNumber")] HRESULT ConnectionsNumber([out, retval] long* pVal);
    [propget, id(8), helpstring("property ConnectionName")] HRESULT ConnectionName([in] long Index, [out, retval] BSTR* pVal);
    [id(9), helpstring("method AdviseConnectionsStatus")] HRESULT AdviseConnectionsStatus(long Datafeed,IUnknown* pSink);
    [id(10), helpstring("method UnadviseConnectionsStatus")] HRESULT UnadviseConnectionsStatus(long Datafeed,IUnknown* pSink);

    [id(11), helpstring("method IsProvide")] HRESULT IsProvide(long Mode, long Category, long ResolutionSize, long Resolution, long Field, [out] BOOL* pVal);

    [id(12), helpstring("method Advise")] HRESULT Advise(IUnknown* pSink);
    [id(13), helpstring("method Unadvise")] HRESULT Unadvise();

    [id(14), helpstring("method AdviseTransaction")] HRESULT AdviseTransaction(long Id, long Datafeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long ResolutionSize, long Resolution, long Field, long Exchange, BSTR ExchangeName, long StartDate, long StartTime, long FinishDate, long FinishTime, BSTR Sessions, BSTR SessionsTimeZoneInformation, BSTR ExchangeTimeZoneInformation);
    [id(15), helpstring("method UnadviseTransaction")] HRESULT UnadviseTransaction(long Id);

    [id(16), helpstring("method GetResolutions")] HRESULT GetResolutions(long Mode, long DataFeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long Exchange, BSTR ExchangeName, [out] VARIANT* Answer);
    [id(17), helpstring("method GetResolutionSizes")] HRESULT GetResolutionSizes(long Mode, long DataFeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long Resolution, long Exchange, BSTR ExchangeName, [out] VARIANT* Answer);
    [id(18), helpstring("method GetFields")] HRESULT GetFields(long Mode, long DataFeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long ResolutionSize, long Resolution, long Exchange, BSTR ExchangeName, [out] VARIANT* Answer);

    [propget, id(19), helpstring("property ProvideStatusLine")] HRESULT ProvideStatusLine([out, retval] long* pVal);
    [id(20), helpstring("method AdviseStatusLine")] HRESULT AdviseStatusLine(long Id, long Datafeed, long Symbol, BSTR SymbolName, BSTR Description, long Category, long ResolutionSize, long Resolution, long Field, long Exchange, BSTR ExchangeName);
    [id(21), helpstring("method UnadviseStatusLine")] HRESULT UnadviseStatusLine(long Id);

    [id(22), helpstring("method IsProvideAddingSymbols")] HRESULT IsProvideAddingSymbols([out] BOOL* pVal);
    [id(23), helpstring("method DoAddingSymbols")] HRESULT DoAddingSymbols(long Datafeed);

    [id(24), helpstring("method CanSaveToStorage")] HRESULT CanSaveToStorage([out, retval] BOOL* pVal);
    [id(25), helpstring("method CanReadFromStorage")] HRESULT CanReadFromStorage([out, retval] BOOL* pVal);
};
