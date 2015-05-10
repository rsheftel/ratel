#ifndef _SYNCH_CONT_
#define _SYNCH_CONT_

#include <map>
#include <vector>
#include <list>
#include <windows.h>

inline void CONST_ENTER_SECTION(LPCRITICAL_SECTION s)
{
EnterCriticalSection(s);
};

inline void LEAVE_SECTION(LPCRITICAL_SECTION s)
{
LeaveCriticalSection(s);
};

inline void LEAVE_CONST_SECTION(LPCRITICAL_SECTION s)
{
LeaveCriticalSection(s);
};

inline void ENTER_SECTION(LPCRITICAL_SECTION s)
{
EnterCriticalSection(s);
};

namespace std {

template<class _Ty ,class _A = allocator<_Ty> >
class TMTVector:protected vector< _Ty,_A >
{
protected:
 mutable CRITICAL_SECTION InsideOperation;
public:

typedef TMTVector<_Ty, _A> _Myt;
typedef vector<_Ty, _A> _BaseType;
typedef typename _BaseType::iterator iterator;

TMTVector()
{
 InitializeCriticalSection(&InsideOperation);
};

~TMTVector()
{
 DeleteCriticalSection(&InsideOperation);
};

_Myt& operator=(const _Myt& v)
{
	_BaseType::operator=(v);
	return *this;
}

size_type capacity() const
{
	 ENTER_SECTION(&InsideOperation);
	size_type result=vector<typename _Ty,_A>::capacity();
	LEAVE_SECTION(&InsideOperation);
	return result;
};

 void push_back( const _Ty& _X )
 {
  ENTER_SECTION(&InsideOperation);
   vector<_Ty,_A>::push_back(_X);
  LEAVE_SECTION(&InsideOperation);
 };

iterator insert(iterator _P, const _Ty& _X = _Ty())
{
 ENTER_SECTION(&InsideOperation);
typename vector< _Ty,_A>::insert(iterator _P, const _Ty& _X = _Ty());
 LEAVE_SECTION(&InsideOperation);
};

void clear()
{
 ENTER_SECTION(&InsideOperation);
 vector< _Ty,_A>::clear();
 LEAVE_SECTION(&InsideOperation);
};

const_reference operator[](size_type _P) const
{
 ENTER_SECTION(&InsideOperation);
typename vector< _Ty,_A>::operator[]( _P);
 LEAVE_SECTION(&InsideOperation);
};

reference operator[](size_type _P)
{
	return vector< _Ty,_A>::operator[]( _P);
};

iterator begin()
{
	return vector< _Ty,_A>::begin();
}
iterator end()
{
	return vector< _Ty,_A>::end();
}
reverse_iterator rbegin()
{
	return vector< _Ty,_A>::rbegin();
}
reverse_iterator rend()
{
	return vector< _Ty,_A>::rend();
}
void reserve(size_type _N)
{
	ENTER_SECTION(&InsideOperation);
	vector< _Ty,_A>::reserve(_N);
	LEAVE_SECTION(&InsideOperation);
}
bool empty() const
{
	bool res;
	ENTER_SECTION(&InsideOperation);
	res = vector< _Ty,_A>::empty();
	LEAVE_SECTION(&InsideOperation);
	return res;
}

void enter_section()
{
	ENTER_SECTION(&InsideOperation);
};

void leave_section()
{
	LEAVE_SECTION(&InsideOperation);
};

size_t raw_size()const
{
return  vector< _Ty,_A>::size();
};

const _Ty& raw_at(size_type I)const 
{
return  vector< _Ty,_A>::at(I);
}; 

};//end TMTVector

template<class _Ty ,class _A = allocator<_Ty> >
class synch_list : protected list< _Ty,_A >
{
protected:
 mutable CRITICAL_SECTION InsideOperation;
public:

typedef synch_list<_Ty, _A> _Myt;
typedef list<_Ty, _A> _BaseType;
typedef typename _BaseType::iterator iterator;

synch_list()
{
 InitializeCriticalSection(&InsideOperation);
};

~synch_list()
{
 DeleteCriticalSection(&InsideOperation);
};

_Myt& operator=(const _Myt& v)
{
	_BaseType::operator=(v);
	return *this;
}

 void push_back( const _Ty& _X )
 {
  ENTER_SECTION(&InsideOperation);
   list<_Ty,_A>::push_back(_X);
  LEAVE_SECTION(&InsideOperation);
 };

iterator insert(iterator _P, const _Ty& _X = _Ty())
{
	list< _Ty,_A>::iterator it;
 ENTER_SECTION(&InsideOperation);
	it = list< _Ty,_A>::insert(_P, _X);
 LEAVE_SECTION(&InsideOperation);
 return it;
};

void clear()
{
 ENTER_SECTION(&InsideOperation);
 list< _Ty,_A>::clear();
 LEAVE_SECTION(&InsideOperation);
};

iterator begin()
{
	return list< _Ty,_A>::begin();
}
iterator end()
{
	return list< _Ty,_A>::end();
}
reverse_iterator rbegin()
{
	return list< _Ty,_A>::rbegin();
}
reverse_iterator rend()
{
	return list< _Ty,_A>::rend();
}
bool empty() const
{
	bool res;
	ENTER_SECTION(&InsideOperation);
	res = list< _Ty,_A>::empty();
	LEAVE_SECTION(&InsideOperation);
	return res;
}

void enter_section()
{
	ENTER_SECTION(&InsideOperation);
};

void leave_section()
{
	LEAVE_SECTION(&InsideOperation);
};

};//end synch_list

template<class Key, class T, class Pred = less<Key>, class A = allocator<T> >
class synch_map : protected map<Key, T, Pred, A> 
{
 protected:

 mutable CRITICAL_SECTION InsideOperation;

 public:

	typedef map<Key, T, Pred, A> _BaseType;
    typedef typename _BaseType::value_type value_type;
    typedef typename _BaseType::iterator iterator;
	typedef synch_map<Key, T, Pred, A> _Myt;


	explicit synch_map(const Pred& comp = Pred(), const A& al = A()):_BaseType(comp, al)
	{

	 InitializeCriticalSection(&InsideOperation);
	};

	_Myt& operator=(const _Myt& v)
	{
		_BaseType::operator=(v);
		return *this;
	}

    bool empty() const
	{
	CONST_ENTER_SECTION(&InsideOperation);
	bool ret=_BaseType::empty();
	LEAVE_SECTION(&InsideOperation);
	return ret;
	};
	
	void clear()
	{
	CONST_ENTER_SECTION(&InsideOperation);
	_BaseType::clear();
	LEAVE_SECTION(&InsideOperation);
	}

    size_type size() const
	{
		size_type ret;
		CONST_ENTER_SECTION(&InsideOperation);
		ret=_BaseType::size();
		LEAVE_SECTION(&InsideOperation);
		return  ret;
	};

	iterator blocking_find(const Key& key)
	{
		iterator it;
		CONST_ENTER_SECTION(&InsideOperation);
		it=_BaseType::find(key);
		//LEAVE_SECTION(&InsideOperation);
		return  it;
	}

	pair<iterator, bool> insert(const value_type& x)
	{
		pair<iterator, bool> p;
		CONST_ENTER_SECTION(&InsideOperation);
		p=_BaseType::insert(x);
		LEAVE_SECTION(&InsideOperation);
		return p;
	}
    

	/* 
	iterator erase(iterator it)
	{
		iterator i;
		CONST_ENTER_SECTION(&InsideOperation);
		i=_BaseType::erase(it);
		LEAVE_SECTION(&InsideOperation);
		return  i;
	}
*/
	size_t erase(const Key& x)
	{
		size_t t;
		CONST_ENTER_SECTION(&InsideOperation);
		t=_BaseType::erase(x);
		LEAVE_SECTION(&InsideOperation);
		return  t;
	}


    iterator begin()
	{
		iterator it;
		CONST_ENTER_SECTION(&InsideOperation);
		it=_BaseType::begin();
		LEAVE_SECTION(&InsideOperation);
		return  it;
	}

    iterator end()
	{
		iterator it;
		CONST_ENTER_SECTION(&InsideOperation);
		it=_BaseType::end();
		LEAVE_SECTION(&InsideOperation);
		return  it;
	}
    
	void enter_section()
	{
		ENTER_SECTION(&InsideOperation);
	};

	void leave_section()
	{
		LEAVE_SECTION(&InsideOperation);
	};

	~synch_map()
	{
		DeleteCriticalSection(&InsideOperation);
	};


public:
	
};




}


#endif