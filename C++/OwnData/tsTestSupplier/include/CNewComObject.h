#ifndef C_NEW_COMOBJECT
#define C_NEW_COMOBJECT

template<class T>
class CNewComObject: public CComObject<T>
{
public:
	CNewComObject(void* = NULL) : CComObject<T>(){
	}
	STDMETHOD_(ULONG, AddRef)() {

		//MessageBox(0, "Native Addref", "", 0);
#ifdef NEED_TO_PRINT_REF
		char h[255];	
		sprintf(h, "BEFORE	AddRef l= %d  this =%p  \n", m_dwRef , this );
		DumpToF2444(h);
#endif
		long res = InternalAddRef();
			//CComObject<T>::AddRef();
#ifdef NEED_TO_PRINT_REF
		sprintf(h, "AddRef l= %d  this =%p   ThrId =%d \n", res , this ,GetCurrentThreadId());
		DumpToF2444(h);
#endif 
		return res; 

	}
	STDMETHOD_(ULONG, Release)(){

		//MessageBox(0, "Native Release", "", 0);

		ULONG l = InternalRelease();
#ifdef NEED_TO_PRINT_REF
		char h[255];
		sprintf(h, "Release l= %d  this = %p  ThrId =%d \n", l ,this , GetCurrentThreadId());		
		DumpToF2444(h);
#endif
		if (l == 0)
		{
			//temp
			delete this;

		}
		return l;

	}
};


template<class T>
class CCollectionComObject: public CComObject<T>
{
public:
	CCollectionComObject(void* = NULL) : CComObject<T>()
	{
	}

	STDMETHOD_(ULONG, AddRef)() {

		//MessageBox(0, "Native Addref", "", 0);

		return 1 ;
	}
	STDMETHOD_(ULONG, Release)(){

		//MessageBox(0, "Native Release", "", 0);
		
		return l;
		//return CComObject<T>::Release();
	}
};




#endif