/******************************************************************************* 
 *  Copyright 2007 Amazon Technologies, Inc.  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  
 *  You may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at: http://aws.amazon.com/apache2.0
 *  This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 *  CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 *  specific language governing permissions and limitations under the License.
 * ***************************************************************************** 
 */

using System;
using System.Xml.Serialization;
using System.Text;


namespace Amazon.SQS.Model {
    [XmlTypeAttribute(Namespace = "http://queue.amazonaws.com/doc/2008-01-01/")]
    [XmlRootAttribute(Namespace = "http://queue.amazonaws.com/doc/2008-01-01/", IsNullable = false)]
    public class SendMessageResult {
    
        private String messageIdField;
        private String MD5OfMessageBodyField;

        [XmlElementAttribute(ElementName = "MessageId")]
        public String MessageId {
            get { return messageIdField; }
            set { messageIdField = value; }
        }

        public SendMessageResult WithMessageId(String messageId) {
            messageIdField = messageId;
            return this;
        }

        public Boolean IsSetMessageId() {
            return messageIdField != null;
        }

        [XmlElementAttribute(ElementName = "MD5OfMessageBody")]
        public String MD5OfMessageBody {
            get { return MD5OfMessageBodyField; }
            set { MD5OfMessageBodyField = value; }
        }

        public SendMessageResult WithMD5OfMessageBody(String md5OfMessageBody) {
            MD5OfMessageBodyField = md5OfMessageBody;
            return this;
        }

        public Boolean IsSetMD5OfMessageBody() {
            return MD5OfMessageBodyField != null;
        }

        protected internal String ToXMLFragment() {
            var xml = new StringBuilder();
            if (IsSetMessageId()) {
                xml.Append("<MessageId>");
                xml.Append(XMLHelper.EscapeXML(MessageId));
                xml.Append("</MessageId>");
            }
            if (IsSetMD5OfMessageBody()) {
                xml.Append("<MD5OfMessageBody>");
                xml.Append(XMLHelper.EscapeXML(MD5OfMessageBody));
                xml.Append("</MD5OfMessageBody>");
            }
            return xml.ToString();
        }
    }
}