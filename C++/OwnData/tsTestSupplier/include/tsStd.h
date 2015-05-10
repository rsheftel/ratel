// Created by Microsoft (R) C/C++ Compiler Version 12.00.8964.0 (b85b1cbe).
//
// d:\testcontrdetails\debug\tsStd.tlh
//
// C++ source equivalent of Win32 type library D:\Program Files\TS Support\TSServer\tsStd.dll
// compiler-generated file created 11/24/04 at 10:52:33 - DO NOT EDIT!

#pragma once
#pragma pack(push, 8)

#include <comdef.h>

//
// Forward references and typedefs
//

struct /* coclass */ CtsCompanyInfo;
struct __declspec(uuid("6040064a-8006-4ada-9415-168c92b9307e"))
/* dual interface */ ItsCompanyInfo;
struct /* coclass */ CtsDumper;
struct __declspec(uuid("f082c903-f963-464b-abaa-f99719d18653"))
/* dual interface */ ItsDumper;
struct /* coclass */ CtsPlatformInfo;
struct __declspec(uuid("9515dcde-18bb-495e-99a8-9114a46692fc"))
/* dual interface */ ItsPlatformInfo;
struct /* coclass */ CtsProcessInfo;
struct __declspec(uuid("d3706757-aab9-473d-89ae-3808d8f86e0b"))
/* dual interface */ ItsProcessInfo;

//
// Smart pointer typedef declarations
//

_COM_SMARTPTR_TYPEDEF(ItsCompanyInfo, __uuidof(ItsCompanyInfo));
_COM_SMARTPTR_TYPEDEF(ItsDumper, __uuidof(ItsDumper));
_COM_SMARTPTR_TYPEDEF(ItsPlatformInfo, __uuidof(ItsPlatformInfo));
_COM_SMARTPTR_TYPEDEF(ItsProcessInfo, __uuidof(ItsProcessInfo));

//
// Type library items
//

struct __declspec(uuid("52affcf2-ccae-4874-a920-65f215718865"))
CtsCompanyInfo;
    // [ default ] interface ItsCompanyInfo

struct __declspec(uuid("6040064a-8006-4ada-9415-168c92b9307e"))
ItsCompanyInfo : IDispatch
{
    //
    // Raw methods provided by interface
    //

    virtual HRESULT __stdcall get_Name (
        BSTR * Value ) = 0;
    virtual HRESULT __stdcall get_Registry (
        BSTR * Value ) = 0;
};

struct __declspec(uuid("b6b05571-4f63-426a-8895-186f6cf0f75e"))
CtsDumper;
    // [ default ] interface ItsDumper

struct __declspec(uuid("f082c903-f963-464b-abaa-f99719d18653"))
ItsDumper : IDispatch
{
    //
    // Raw methods provided by interface
    //

    virtual HRESULT __stdcall get_Name (
        BSTR * pVal ) = 0;
    virtual HRESULT __stdcall get_Path (
        BSTR * pVal ) = 0;
    virtual HRESULT __stdcall get_Registry (
        BSTR * pVal ) = 0;
    virtual HRESULT __stdcall Dump (
        BSTR Value ) = 0;
    virtual HRESULT __stdcall BinaryDump (
        unsigned char * Buffer,
        long Size ) = 0;
};

struct __declspec(uuid("b207f085-6864-4321-8b5c-89506ebfaecd"))
CtsPlatformInfo;
    // [ default ] interface ItsPlatformInfo

struct __declspec(uuid("9515dcde-18bb-495e-99a8-9114a46692fc"))
ItsPlatformInfo : IDispatch
{
    //
    // Raw methods provided by interface
    //

    virtual HRESULT __stdcall get_Name (
        BSTR * Value ) = 0;
    virtual HRESULT __stdcall get_Version (
        BSTR * Value ) = 0;
    virtual HRESULT __stdcall get_SharedRegistry (
        BSTR * Value ) = 0;
    virtual HRESULT __stdcall get_Registry (
        BSTR * Value ) = 0;
    virtual HRESULT __stdcall get_SharedDirectory (
        BSTR * Value ) = 0;
};

struct __declspec(uuid("62656012-5fc3-4958-9539-bb2919a7fea5"))
CtsProcessInfo;
    // [ default ] interface ItsProcessInfo

struct __declspec(uuid("d3706757-aab9-473d-89ae-3808d8f86e0b"))
ItsProcessInfo : IDispatch
{
    //
    // Raw methods provided by interface
    //

    virtual HRESULT __stdcall get_Name (
        BSTR * Value ) = 0;
    virtual HRESULT __stdcall get_Version (
        BSTR * Value ) = 0;
    virtual HRESULT __stdcall get_Registry (
        BSTR * Value ) = 0;
};

//
// Named GUID constants initializations
//

extern "C" const GUID __declspec(selectany) LIBID_tsStdProject =
    {0xffe6355b,0x814f,0x44fa,{0x8c,0x1e,0x30,0x02,0x65,0x4a,0xa5,0xdc}};
extern "C" const GUID __declspec(selectany) CLSID_CtsCompanyInfo =
    {0x52affcf2,0xccae,0x4874,{0xa9,0x20,0x65,0xf2,0x15,0x71,0x88,0x65}};
extern "C" const GUID __declspec(selectany) IID_ItsCompanyInfo =
    {0x6040064a,0x8006,0x4ada,{0x94,0x15,0x16,0x8c,0x92,0xb9,0x30,0x7e}};
extern "C" const GUID __declspec(selectany) CLSID_CtsDumper =
    {0xb6b05571,0x4f63,0x426a,{0x88,0x95,0x18,0x6f,0x6c,0xf0,0xf7,0x5e}};
extern "C" const GUID __declspec(selectany) IID_ItsDumper =
    {0xf082c903,0xf963,0x464b,{0xab,0xaa,0xf9,0x97,0x19,0xd1,0x86,0x53}};
extern "C" const GUID __declspec(selectany) CLSID_CtsPlatformInfo =
    {0xb207f085,0x6864,0x4321,{0x8b,0x5c,0x89,0x50,0x6e,0xbf,0xae,0xcd}};
extern "C" const GUID __declspec(selectany) IID_ItsPlatformInfo =
    {0x9515dcde,0x18bb,0x495e,{0x99,0xa8,0x91,0x14,0xa4,0x66,0x92,0xfc}};
extern "C" const GUID __declspec(selectany) CLSID_CtsProcessInfo =
    {0x62656012,0x5fc3,0x4958,{0x95,0x39,0xbb,0x29,0x19,0xa7,0xfe,0xa5}};
extern "C" const GUID __declspec(selectany) IID_ItsProcessInfo =
    {0xd3706757,0xaab9,0x473d,{0x89,0xae,0x38,0x08,0xd8,0xf8,0x6e,0x0b}};

#pragma pack(pop)
