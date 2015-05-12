using Apache.NMS;

namespace FixExecutionAddin.Nms
{
    public interface INmsApplication
    {
        void InboundApp(IMessage message);

        void OutboundApp(IMessage message);
    }
}
