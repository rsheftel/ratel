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
    public class Message {
        string messageIdField;
        string receiptHandleField;
        string MD5OfBodyField;
        string bodyField;

        [XmlElementAttribute(ElementName = "MessageId")]
        public string MessageId {
            get { return messageIdField; }
            set { messageIdField = value; }
        }

        public Message WithMessageId(string messageId) {
            messageIdField = messageId;
            return this;
        }

        public Boolean IsSetMessageId() {
            return messageIdField != null;
        }

        [XmlElementAttribute(ElementName = "ReceiptHandle")]
        public string ReceiptHandle {
            get { return receiptHandleField; }
            set { receiptHandleField = value; }
        }

        public Message WithReceiptHandle(string receiptHandle) {
            receiptHandleField = receiptHandle;
            return this;
        }

        public Boolean IsSetReceiptHandle() {
            return receiptHandleField != null;
        }

        [XmlElementAttribute(ElementName = "MD5OfBody")]
        public string MD5OfBody {
            get { return MD5OfBodyField; }
            set { MD5OfBodyField = value; }
        }

        public Message WithMD5OfBody(string md5OfBody) {
            MD5OfBodyField = md5OfBody;
            return this;
        }

        public Boolean IsSetMD5OfBody() {
            return MD5OfBodyField != null;
        }

        [XmlElementAttribute(ElementName = "Body")]
        public string Body {
            get { return bodyField; }
            set { bodyField = value; }
        }

        public Message WithBody(string body) {
            bodyField = body;
            return this;
        }

        public Boolean IsSetBody() {
            return bodyField != null;
        }

        protected internal string ToXMLFragment() {
            var xml = new StringBuilder();
            if (IsSetMessageId()) {
                xml.Append("<MessageId>");
                xml.Append(XMLHelper.EscapeXML(MessageId));
                xml.Append("</MessageId>");
            }
            if (IsSetReceiptHandle()) {
                xml.Append("<ReceiptHandle>");
                xml.Append(XMLHelper.EscapeXML(ReceiptHandle));
                xml.Append("</ReceiptHandle>");
            }
            if (IsSetMD5OfBody()) {
                xml.Append("<MD5OfBody>");
                xml.Append(XMLHelper.EscapeXML(MD5OfBody));
                xml.Append("</MD5OfBody>");
            }
            if (IsSetBody()) {
                xml.Append("<Body>");
                xml.Append(XMLHelper.EscapeXML(Body));
                xml.Append("</Body>");
            }
            return xml.ToString();
        }
    }
}