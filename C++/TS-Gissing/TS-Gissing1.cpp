//#import "c:\Program Files\TradeStation\Program\tskit.dll" no_namespace
#include "stdafx.h"
#include <jni.h>
#include <string>
#include <iostream>
#include <fstream>
#include <stdlib.h>
#include <tchar.h> 
#include <strsafe.h>
#include <direct.h>
#include "LogFile.h"

#ifdef _MANAGED
#pragma managed(push, off)
#endif

#define BUFFER_SIZE 10000

//#define DllExport __declspec( dllexport )

CLogFile fff("TS-Gissing");
using namespace std;

BOOL APIENTRY DllMain( HMODULE hModule,
                       DWORD  ul_reason_for_call,
                       LPVOID lpReserved
					 )
{
    return TRUE ;
}

JavaVM *jvm = NULL;
JNIEnv *env = NULL;
jclass subCls = NULL, pubCls = NULL;
/*
static int JNICALL vfprintfHook(FILE* f, const char *fmt, va_list args) 
{
	fff<<"ERROR:" <<endl;
	char buf[4096];
	int len = vsprintf(buf, fmt, args);
	fff<<buf<<endl;
	return len;
}*/



//int __stdcall gissingInitialize(IEasyLanguageObject * pEL)
int __stdcall gissingInitialize()
{
	//freopen("TS-Gissing - orchart.log", "a+", stdout);
	//freopen("TS-Gissing - orchart.log", "a+", stderr);

	if (subCls != NULL && pubCls != NULL)
		return true;

	char CurrentPath[BUFFER_SIZE]; 
	getcwd(CurrentPath, BUFFER_SIZE);
	
	TCHAR classpath1[BUFFER_SIZE]; 
	string classpath2;
	ifstream pathFile ("classpath.conf");
	if (pathFile.is_open())
	{
//			while (!pathFile.eof() )
		{
			getline(pathFile, classpath2);
		}
		pathFile.close();
	}
	else 
	{
		fff << "Unable to open file"<<endl; 
		return false;
	}

		// Add the systematic integration
	char * basePath = getenv("MAIN");
	if (basePath) {
		TCHAR path[] = "\\Java\\systematic\\lib\\";
		TCHAR systematic[BUFFER_SIZE];
		TCHAR jarBase[BUFFER_SIZE];
		StringCchCopy(jarBase, BUFFER_SIZE, basePath);
		StringCchCat(jarBase, BUFFER_SIZE, path);
		StringCchCopy(systematic, BUFFER_SIZE, jarBase);

		StringCchCat(systematic, BUFFER_SIZE, "\\*.jar");

		WIN32_FIND_DATA ffd;
		HANDLE hFind = FindFirstFile(systematic, &ffd);
		StringCchCopy(classpath1, BUFFER_SIZE, jarBase);
		StringCchCat(classpath1, BUFFER_SIZE, TEXT(ffd.cFileName));
		StringCchCat(classpath1, BUFFER_SIZE, ";");

		do
		{
			StringCchCat(classpath1, BUFFER_SIZE, jarBase);
			StringCchCat(classpath1, BUFFER_SIZE, TEXT(ffd.cFileName));
			StringCchCat(classpath1, BUFFER_SIZE, ";");
			//fff << classpath1 <<" \r\n"; 
		}
		while (FindNextFile(hFind, &ffd) != 0);
	}

	const int optionsLen = 3;
	JavaVMOption options[optionsLen];
	JavaVMInitArgs vm_args;
	char buf[BUFFER_SIZE];
	StringCchCopy(buf, BUFFER_SIZE, "-Djava.class.path=");
	StringCchCat(buf, BUFFER_SIZE, classpath1);
	StringCchCat(buf, BUFFER_SIZE, ";");
	strcat(buf, classpath2.c_str());

	fff << buf <<" \r\n"; 

	int i = 0;
	options[i++].optionString = buf;
	options[i++].optionString = "-Djava.compiler=NONE";
	options[i++].optionString = "-Djava.util.logging.config.file=logging.properties";
	//options[i++].optionString = "-verbose:jni";    
	//options[i++].optionString = "vfprintf";  
	//options[i].extraInfo = vfprintfHook;

	vm_args.version = JNI_VERSION_1_6;
	vm_args.options = options;
	vm_args.nOptions = optionsLen;
	vm_args.ignoreUnrecognized = JNI_FALSE;

	jint cr = JNI_CreateJavaVM(&jvm, (void **)&env, &vm_args);
	if (cr != JNI_OK) 
	{
		fff << "Error creating the JVM: " << "\r\n";

		cr = JNI_GetCreatedJavaVMs(&jvm, 1, NULL);
        if (cr != JNI_OK)
		{
			fff << "Error finding existing JVM: " << "\r\n";
			return false;
		}

		jvm->AttachCurrentThread((void**) &env, &vm_args);
		
		fff<<"Attached thread to existing JVM "<<cr << ", PID=" << GetCurrentProcessId() <<"\r\n";
	}

	subCls = env->FindClass("com/fftw/gissing/Subscriber");
	if( subCls == NULL ) 
	{
		fff<<"can't find Subscriber class"<< "\r\n";
		return false;
	}

	pubCls = env->FindClass("com/fftw/gissing/Publisher");
	if( pubCls == NULL ) 
	{
		fff<<"can't find Publisher class"<< "\r\n";
		return false;
	}

	fff<<"Loaded Gissing for EasyLanguage"<< "\r\n";
	return true; 
}
/*
bool __stdcall gissingFinalize()
{
	//env->ReleaseStringChars
	env = NULL;
	pubCls = NULL;
	subCls = NULL;

	if (jvm == NULL)
	{
		fff << "jvm is null"<<endl; 
		return true;
	}

	//jvm->DetachCurrentThread();	
	bool ret = jvm->DestroyJavaVM() == 0;	
	jvm = NULL;
	fff << "jvm is destroyed"<<endl; 
	return ret;
}*/

/*
bool __stdcall gissingSubscribe(const char* templateName, const char* record) 
{
	env->ExceptionClear();
	jmethodID mid = env->GetStaticMethodID(subCls, "subscribe",	"(Ljava/lang/String;Ljava/lang/String;)V");
	env->CallStaticVoidMethod(subCls, mid, env->NewStringUTF(templateName), env->NewStringUTF(record)); 
	
	env->ExceptionDescribe();
	return !env->ExceptionCheck();
}
*/

bool __stdcall gissingUnsubscribe(const char* templateName, const char* record) 
{
	env->ExceptionClear();
	jmethodID mid = env->GetStaticMethodID(subCls, "unsubscribe", "(Ljava/lang/String;Ljava/lang/String;)V");
	env->CallStaticVoidMethod(subCls, mid, env->NewStringUTF(templateName), env->NewStringUTF(record)); 

	return !env->ExceptionCheck();
}

const char* __stdcall gissingGetString(const char* templateName, const char* record, const char* field) 
{	
	env->ExceptionClear();
	jmethodID mid = env->GetStaticMethodID(subCls, "get", 
		"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");

	jstring temp = (jstring)env->CallStaticObjectMethod(subCls, mid, 
		env->NewStringUTF(templateName), env->NewStringUTF(record), env->NewStringUTF(field)); 

    jboolean iscopy;
    const char *result = env->GetStringUTFChars(temp, &iscopy);
	//env->ReleaseStringUTFChars(temp, result);	
		
	//env->ExceptionDescribe();
	return result;
}

//double& ret
double __stdcall gissingGetDouble(const char* templateName, const char* record, const char* field) 
{
	const char * str = gissingGetString(templateName, record, field);
	return atof(str);
}


bool __stdcall gissingSetString(const char* templateName, const char* record, const char* field, const char* value)
{
	env->ExceptionClear();
	jmethodID mid = env->GetStaticMethodID(pubCls, "publish", 
		"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
	env->CallStaticVoidMethod(pubCls, mid, 
		env->NewStringUTF(templateName), env->NewStringUTF(record), env->NewStringUTF(field), env->NewStringUTF(value)); 
		
	//env->ExceptionDescribe();
	return !env->ExceptionCheck();
}

bool __stdcall gissingSetDouble(const char* templateName, const char* record, const char* field, double value)
{
	env->ExceptionClear();
	jmethodID mid = env->GetStaticMethodID(pubCls, "publish", 
		"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;D)V");

	env->CallStaticVoidMethod(pubCls, mid, 
		env->NewStringUTF(templateName), env->NewStringUTF(record), env->NewStringUTF(field), value); 
		
	//env->ExceptionDescribe();
	return !env->ExceptionCheck();
}

#ifdef _MANAGED 
#pragma managed(pop)
#endif

