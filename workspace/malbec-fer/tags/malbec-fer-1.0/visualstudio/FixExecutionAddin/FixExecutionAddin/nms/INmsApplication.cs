using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Apache.NMS;

namespace FixExecutionAddin.nms
{
    public interface INmsApplication
    {
        void InboundApp(IMessage message);

        void OutboundApp(IMessage message);
    }
}
