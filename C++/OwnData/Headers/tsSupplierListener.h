#pragma once

// ItsConnectionsListener

[
    object,
    uuid("8DB1FA16-9900-43da-B1D8-C9374877FD72"),
    dual,helpstring("ItsConnectionsListener Interface"),
    pointer_default(unique)
]
__interface ItsConnectionsListener : IDispatch
{
    [id(1), helpstring("method Status")] HRESULT Status(long Date, long Time, long Index, long Status);
};


// ItsSupplierListener
[
  object,
  uuid("595D6668-198C-4376-8EAD-A1B704E37A4F"),
  dual, helpstring("ItsSupplierListener Interface"),
  pointer_default(unique)
]
__interface ItsSupplierListener : IDispatch
{
    [id(1), helpstring("method DataChanged")] HRESULT DataChanged(long Id, VARIANT Data);
    [id(2), helpstring("method Update")] HRESULT Update(long Id);
    [id(3), helpstring("method Complete")] HRESULT Complete(long Id);
    [id(4), helpstring("method Status")] HRESULT Status(long Id, long Mode, long Date, long Time, long Status);
    [id(5), helpstring("method StatusLine")] HRESULT StatusLine(long Id, VARIANT Data, long Mask);
};

