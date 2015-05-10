#pragma once

#include <atlbase.h>
#include "tsBase.h"

EXTERN_C const IID IID_ItsSupplierListener;
EXTERN_C const IID IID_ItsConnectionsListener;

//MIDL_INTERFACE("8DB1FA16-9900-43da-B1D8-C9374877FD72") //MC
MIDL_INTERFACE("B7E265FD-6F78-43ee-B42E-E7705CDA2C04")   //OD
ItsConnectionsListener : public IDispatch
{
public:

    STDMETHOD(Status)(long Date, long Time, long Index, tagTSCONNECTIONSTATUS Status);
};

EXTERN_C const IID IID_ItsSupplierListener;

//MIDL_INTERFACE("595D6668-198C-4376-8EAD-A1B704E37A4F") //MC
MIDL_INTERFACE("876D5FE7-912E-4b8a-B80C-4B9B194617A1") //new OD
ItsSupplierListener : public IDispatch
{
public:

    virtual HRESULT STDMETHODCALLTYPE DataChanged(long Id, VARIANT Data)=0;
    virtual HRESULT STDMETHODCALLTYPE Update(long Id)=0;
    virtual HRESULT STDMETHODCALLTYPE Complete(long Id)=0;
    virtual HRESULT STDMETHODCALLTYPE Status(long Id, long Mode, long Date, long Time, long Status) = 0;
	virtual HRESULT STDMETHODCALLTYPE StatusLine(long Id, VARIANT Data, long Mask)=0;
};
