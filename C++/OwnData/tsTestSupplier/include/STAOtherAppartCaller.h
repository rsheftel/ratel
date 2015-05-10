#pragma once
#pragma warning (disable :4786)
#include <typeinfo.h>
#include <map>

#include "COMCaller.h"

using namespace std;
#define WM_STA_CALLER_MESSAGE (WM_USER+0x7001)
#define WM_STA_CALLER_EXIT (WM_USER+0x7002)

class CSTABaseAppartCaller
{
  public:
  bool m_AppartmentLocked;

  CSTABaseAppartCaller()
  {
    m_AppartmentLocked=false;
  }
};

struct SCallerWindowMessage
{
CSTABaseAppartCaller* Caller;
void *Object;
int MethodNum;
int DataSize;
char Data[1];
};

extern const char* TSTAThreadObjectWndClass;


class TSTAThreadObject
{
  protected:
  int                m_Ref;  
  HWND               m_Wnd;  
  DWORD				 m_ThreadId;
  static const char* m_WndClassName;
  
  static LRESULT CALLBACK WindowProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
  {
    if(message==WM_STA_CALLER_MESSAGE)
    {
      SCallerWindowMessage* CallerMessage = (SCallerWindowMessage*)lParam;   

      HRESULT res=0;
      if(CallerMessage->Caller->m_AppartmentLocked)
       PostMessage(hWnd,message,wParam,lParam);
      else
      {
        CallerMessage->Caller->m_AppartmentLocked=true;
  	    res=ComCall(CallerMessage->MethodNum,CallerMessage->Object, CallerMessage->Data,CallerMessage->DataSize);
        CallerMessage->Caller->m_AppartmentLocked=false;
        delete CallerMessage;
      }

      return res;
    }
    else  
     return DefWindowProc(hWnd, message, wParam, lParam);
  }

  public:  
  static void RegisterWindow()
  {
    m_WndClassName = TSTAThreadObjectWndClass;
	//m_WndClassName = typeid(TSTAThreadObject).name();

    WNDCLASSEX wcex;
    wcex.cbSize = sizeof(WNDCLASSEX); 

    wcex.style			= 0;
    wcex.lpfnWndProc	= (WNDPROC)WindowProc;
    wcex.cbClsExtra		= 0;
    wcex.cbWndExtra		= 0;
    wcex.hInstance		= GetModuleHandle(NULL);
    wcex.hIcon			= 0;
    wcex.hCursor		= 0;
    wcex.hbrBackground	= 0;
    wcex.lpszMenuName	= 0;
    wcex.lpszClassName	= m_WndClassName;
    wcex.hIconSm		= 0;

    RegisterClassEx(&wcex);
  }
  
  static void UnRegisterWindow()
  {
	  UnregisterClass(m_WndClassName, GetModuleHandle(NULL));
  }
  protected:

  void Create()
  {
    m_Ref=1;
	m_ThreadId=GetCurrentThreadId();
    m_Wnd = CreateWindow(m_WndClassName, "", WS_POPUP, 0, 0, 0, 0, NULL, NULL, GetModuleHandle(NULL), NULL);
    if(m_Wnd==NULL)
     MessageBox(0,"m_Wnd is NULL","TSTAThreadObject",0);
  };

  void Destroy()
  {
    if(m_Wnd!=NULL)
    {
      DestroyWindow(m_Wnd);
      m_Wnd=NULL;
    }
  }

  TSTAThreadObject()
  {
    Create();
  };

  ~TSTAThreadObject()
  {
    Destroy();
  };

  int AddRef()
  {
    return ++m_Ref;
  };

  int Release()
  {
    int Result = --m_Ref;
    if(Result==0)
     delete this;
    return Result;
  }

  public:
  HWND GetWnd()const
  {
    return m_Wnd;
  }

  DWORD GetThreadID() const
  {
  return m_ThreadId;
  };

  friend class TSTAThreadObjectFactory;
};

typedef map<DWORD,TSTAThreadObject *> TThreaIdMap;

class TSTAThreadObjectFactory
{
  protected:
  CRITICAL_SECTION s;
  TThreaIdMap  Data;
  void Remove(DWORD ThreadID)
  {
    EnterCriticalSection(&s);   
    TThreaIdMap::iterator it = Data.find(ThreadID);
    if(it!=Data.end())
    {
      Data.erase(it);
    }
    LeaveCriticalSection(&s);  
  };

  public:

  TSTAThreadObjectFactory()
  {
    InitializeCriticalSection(&s);
    TSTAThreadObject::RegisterWindow();
  };

  ~TSTAThreadObjectFactory()
  {
    DeleteCriticalSection(&s);
	TSTAThreadObject::UnRegisterWindow();
  };

  TSTAThreadObject* CreateThreadObject()
  {
    EnterCriticalSection(&s);
    TSTAThreadObject* Result=NULL;
    long ThreadID = GetCurrentThreadId();
    TThreaIdMap::iterator it = Data.find(ThreadID);
    if(it!=Data.end())
    {
      Result=it->second;
      Result->AddRef();
    }
    else
    {
      Result = new TSTAThreadObject;
      if(Result)
      {
        Data.insert(TThreaIdMap::value_type(ThreadID,Result));
      }
      else
       MessageBox(0,"Cannot create CAsyncData block","CAsyncDataMap",0);
    }
    LeaveCriticalSection(&s);  
    return Result;
  };

  void ReleaseThreadObject(TSTAThreadObject * obj)
  {
    EnterCriticalSection(&s);
    DWORD ThId=obj->GetThreadID();
    int RefCnt=obj->Release();
    if(RefCnt==0)
     Remove(ThId);
    LeaveCriticalSection(&s);  
  };
}; 

extern TSTAThreadObjectFactory  STAThreadFactory;

template <class T, class COMObj>
class CSTAOtherAppartCaller : public CSTABaseAppartCaller
{
protected:
  void* m_Object;
  TSTAThreadObject* Sender;	 
  	SCallerWindowMessage* PrepareCall(int MethodNum, //Method  Number from 0 - QI =0 , AddRef=1 , Release =2
								T& ParametersStruct,void *Object)
	{
	int ParametersSize=sizeof(ParametersStruct);
	SCallerWindowMessage* WindowData =(SCallerWindowMessage*) new char [sizeof(SCallerWindowMessage)-1+ParametersSize];
	WindowData->DataSize=ParametersSize;
	WindowData->MethodNum=MethodNum;
	WindowData->Object=Object;
    WindowData->Caller=this;
	*((T *)WindowData->Data) = ParametersStruct;	
	return WindowData;
	};

public:
	CSTAOtherAppartCaller()
	{
//	MessageBox(0,"","",0);
//	__asm  {int 3};
	Sender=STAThreadFactory.CreateThreadObject();
	_ASSERT(Sender!=NULL);
	m_Object=NULL;
	};

	~CSTAOtherAppartCaller()
	{
	  STAThreadFactory.ReleaseThreadObject(Sender);
	};
		
	void CallerBind(COMObj* Object)//Bind Caller with object
	{
		m_Object=Object;
	};

	HRESULT CallMethodViaSend(int MethodNum, //Method  Number from 0 - QI =0 , AddRef=1 , Release =2
								T& ParametersStruct)
	{
	void *m_cur=m_Object;
	HRESULT res=S_FALSE;
	if (m_cur!=NULL)
	{
		SCallerWindowMessage* WindowData =PrepareCall(MethodNum,ParametersStruct,m_cur);
	//затолкнуть и передать в окно
		HWND SenderWindow=Sender->GetWnd();
		res =SendMessage(SenderWindow,WM_STA_CALLER_MESSAGE,0,(LONG)WindowData);
	};
	return res;
	};
	 

	SCallerWindowMessage* PrepareCallNoParams(int MethodNum)
	{
	void *m_cur=m_Object;
	SCallerWindowMessage* WindowData=NULL;
	if (m_cur!=NULL)
	{
		WindowData=new SCallerWindowMessage;
		WindowData->DataSize=0;
		WindowData->MethodNum=MethodNum;
		WindowData->Object=m_cur;
		WindowData->Caller=this;
	};
	return WindowData;
	};

	HRESULT CallMethodViaSendNoParams(int MethodNum //Method  Number from 0 - QI =0 , AddRef=1 , Release =2							
		)
	{
		HRESULT res=S_FALSE;
		SCallerWindowMessage* WindowData=PrepareCallNoParams(MethodNum);
		if (WindowData!=NULL)
		{
			HWND SenderWindow=Sender->GetWnd();
			res =SendMessage(SenderWindow,WM_STA_CALLER_MESSAGE,0,(LONG)WindowData);
		};
		return res;
	};
	
	HRESULT CallMethodViaPostNoParams(int MethodNum //Method  Number from 0 - QI =0 , AddRef=1 , Release =2							
		)
	{
		HRESULT res=S_FALSE;
		SCallerWindowMessage* WindowData=PrepareCallNoParams(MethodNum);
		if (WindowData!=NULL)
		{
			HWND SenderWindow=Sender->GetWnd();
			res =PostMessage(SenderWindow,WM_STA_CALLER_MESSAGE,0,(LONG)WindowData);
		};
		return res;
	};

	HRESULT CallMethodViaPost(int MethodNum, //Method  Number from 0 - QI =0 , AddRef=1 , Release =2
		T& ParametersStruct)
	{
	void *m_cur=m_Object;
	HRESULT res=S_FALSE;
	if (m_cur!=NULL)
	{
		SCallerWindowMessage* WindowData =PrepareCall(MethodNum,ParametersStruct,m_cur);
		HWND SenderWindow=Sender->GetWnd();
		PostMessage(SenderWindow,WM_STA_CALLER_MESSAGE,0,(LONG)WindowData);
		res=S_OK;
	}
	return res;
	};


	int   Dump(char *Str,int MemSize)
	{	
	char PrintStr[]="m_Object=%p Sender=%p ";
	int NeededSize=sizeof(PrintStr) + 10*2;
	if (MemSize<NeededSize) 
			return  NeededSize;
	sprintf(Str,PrintStr,m_Object,Sender);
	return 0;
	};

	void Clear()
	{
	m_Object=NULL;
	};
};


