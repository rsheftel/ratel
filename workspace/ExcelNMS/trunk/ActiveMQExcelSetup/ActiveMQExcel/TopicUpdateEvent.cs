using System;
using System.Collections.Generic;

namespace ActiveMQExcel
{
   

    public class TopicUpdateEvent : EventArgs
    {
        readonly IDictionary<string, string> _eventMessage;

        public TopicUpdateEvent(IDictionary<string, string> eventMessage) {
            _eventMessage = eventMessage;
        }

        public IDictionary<string, string> EventMessage {
            get { return _eventMessage; }
        }
    }
}
