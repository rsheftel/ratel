#ifndef _SENDER_CALLER_
#define _SENDER_CALLER_

//-----------------------------------------------------------------------
#include "ProxySender.h"
#include "STAOtherAppartCaller.h"
#include "ThrRefs.h"
#include <atlconv.h>

//-----------------------------------------------------------------------

class TSenderCaller : public CSTAOtherAppartCaller<SDataParams, CProxyTSSupplier>,
						public CThreadRefs
{
public:

	TSenderCaller()
	{
	}

	~TSenderCaller()
	{
	}
};
//-----------------------------------------------------------------------


#endif