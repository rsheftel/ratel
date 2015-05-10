#ifndef _ONE_OBJECT_IN_PROCESS_H
#define _ONE_OBJECT_IN_PROCESS_H

#include <wtypes.h>
#pragma warning (disable :4786)

template<class T>
class COneObjectCriticalSection
{
  protected:

  CRITICAL_SECTION s;

  public:

  COneObjectCriticalSection()
  {
    InitializeCriticalSection(&s);
  }

  ~COneObjectCriticalSection()
  {
    DeleteCriticalSection(&s);
  }

  void Lock()
  {
    EnterCriticalSection(&s);
  }

  void Unlock()
  {
    LeaveCriticalSection(&s);  
  }
};

template<class T>
class COneObjectAccess
{
  protected:

  static COneObjectCriticalSection<T> crt;

  public:

  void AnythingLock()
  {
    crt.Lock();
  }

  void AnythingUnlock()
  {
    crt.Unlock();
  }
};

template<class T>
class COneObjectData
{
  protected:

  T*    m_Object;
  DWORD m_Ref;

  public:

  COneObjectData()
  {
    m_Ref=1; m_Object = new T;
  }

  ~COneObjectData()
  {
    if(m_Object)
     delete m_Object;
  }

  DWORD AddRef()
  {
    return ++m_Ref;
  }

  DWORD Release()
  {
    DWORD Result = --m_Ref;
    if(Result==0)
     delete this;
    return Result;
  }

  T* GetObject()
  {
    return m_Object;
  }
};

// ”ничтожает объект COneObjectInProcess при завершении приложени€
// при использовании флага NeverErase
template<class T>
class COneObjectDataTerminator
{
  public:

  ~COneObjectDataTerminator()
  {
    if(COneObjectInProcess<T>::m_Object!=NULL)
    {
      delete COneObjectInProcess<T>::m_Object;
      COneObjectInProcess<T>::m_Object=NULL;
    }
  }
};

template<class T>
class COneObjectInProcess : public COneObjectAccess<T>
{
  protected:

  static COneObjectData<T>* m_Object;
  static COneObjectDataTerminator<T> m_Terminator;
  bool m_NeverErase;

  public:

  COneObjectInProcess(bool NeverErase=false)
  {  
    AnythingLock();
     m_NeverErase =NeverErase;
    if(m_Object)
    {
      m_Object->AddRef();
    }
    else
     m_Object = new COneObjectData<T>;
    AnythingUnlock();
  }

  ~COneObjectInProcess()
  {
	  AnythingLock();

	  if(!m_NeverErase)
      {
        int RelRes=m_Object->Release();
   	    if(RelRes==0)
 	     m_Object=NULL;
      }

	  AnythingUnlock();
  }

  T* GetObject()
  {
    T* Value = NULL;
    if(m_Object)
     Value = m_Object->GetObject();
    return Value;
  }

  friend class COneObjectDataTerminator<T>;

};

#endif
