/******************************************************************************* 
 *  Copyright 2007 Amazon Technologies, Inc.  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  
 *  You may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at: http://aws.amazon.com/apache2.0
 *  This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 *  CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 *  specific language governing permissions and limitations under the License.
 * ***************************************************************************** */

using System;
using System.Xml.Serialization;
using System.Collections.Generic;
using System.Text;

namespace Amazon.SQS.Model {
    [XmlTypeAttribute(Namespace = "http://queue.amazonaws.com/doc/2008-01-01/")]
    [XmlRootAttribute(Namespace = "http://queue.amazonaws.com/doc/2008-01-01/", IsNullable = false)]
    public class ReceiveMessageResult {
    
        List<Message> messageField;

        [XmlElementAttribute(ElementName = "Message")]
        public List<Message> Message {
            get {
                if (messageField == null) messageField = new List<Message>();
                return messageField;
            }
            set { messageField = value; }
        }

        public ReceiveMessageResult WithMessage(params Message[] list) {
            foreach (var item in list) Message.Add(item);
            return this;
        } 

        public Boolean IsSetMessage() {
            return (Message.Count > 0);
        }

        protected internal string ToXMLFragment() {
            var xml = new StringBuilder();
            var messageList = Message;
            foreach (var message in messageList) {
                xml.Append("<Message>");
                xml.Append(message.ToXMLFragment());
                xml.Append("</Message>");
            }
            return xml.ToString();
        }
    }
}