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
    public class Error {
        string typeField;
        string codeField;
        string messageField;
        object detailField;

        [XmlElementAttribute(ElementName = "Type")]
        public string Type {
            get { return typeField; }
            set { typeField = value; }
        }

        public Error WithType(string type) {
            typeField = type;
            return this;
        }

        public Boolean IsSetType() {
            return typeField != null;
        }
        
        [XmlElementAttribute(ElementName = "Code")]
        public string Code {
            get { return codeField; }
            set { codeField = value; }
        }

        public Error WithCode(string code) {
            codeField = code;
            return this;
        }

        public Boolean IsSetCode() {
            return codeField != null;
        }

        [XmlElementAttribute(ElementName = "Message")]
        public string Message {
            get { return messageField; }
            set { messageField = value; }
        }

        public Error WithMessage(string message) {
            messageField = message;
            return this;
        }

        public Boolean IsSetMessage() {
            return messageField != null;
        }
        
        [XmlElementAttribute(ElementName = "Detail")]
        public Object Detail {
            get { return detailField; }
            set { detailField = value; }
        }

        public Error WithDetail(Object detail) {
            detailField = detail;
            return this;
        }

        public Boolean IsSetDetail() {
            return detailField != null;
        }

        protected internal string ToXMLFragment() {
            var xml = new StringBuilder();
            if (IsSetType()) {
                xml.Append("<Type>");
                xml.Append(Type);
                xml.Append("</Type>");
            }
            if (IsSetCode()) {
                xml.Append("<Code>");
                xml.Append(XMLHelper.EscapeXML(Code));
                xml.Append("</Code>");
            }
            if (IsSetMessage()) {
                xml.Append("<Message>");
                xml.Append(XMLHelper.EscapeXML(Message));
                xml.Append("</Message>");
            }
            if (IsSetDetail()) {
                xml.Append("<Detail>");
                xml.Append(Detail.ToString());
                xml.Append("</Detail>");
            } 
            return xml.ToString();
        }
    }
}