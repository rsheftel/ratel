#ifndef __COMCALLER__
#define __COMCALLER__

#pragma once 

#include <wtypes.h>
// позволяет вызавать methods COM обектов

inline __declspec(naked)
HRESULT ComCall(int MethodNum,void* Object, void *args,int ArgSize)
{
	_asm{
		push ebp
		mov	ebp, esp
		push ecx
		push edx

		mov ecx, ArgSize
		test ecx, ecx	// Если размер аргументов 0, то 
		jz IsVoid		// не копируем

		mov edx, args
	
		sub esp, ArgSize	// Резервируем место для аргументов
											
m1:
		mov al, byte ptr [edx + ecx - 1]	// Копируем аргументы
		mov byte ptr [esp + ecx - 1], al	// 
		loop m1
IsVoid:		
		mov eax, MethodNum
		mov	ecx, Object						// Адрес объекта
		mov edx, dword ptr [ ecx ]			// Адрес вирт. таблицы

		
		
		push ecx							// Запоминаем this
		call [edx + eax * 4]				// Адрес метода


		pop edx
		pop ecx
		pop ebp
		
		ret
	}
}

#endif