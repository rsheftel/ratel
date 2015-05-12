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
    public class CreateQueueResult {
        string queueUrlField;
        
        [XmlElementAttribute(ElementName = "QueueUrl")]
        public string QueueUrl {
            get { return queueUrlField; }
            set { queueUrlField = value; }
        }

        public CreateQueueResult WithQueueUrl(string queueUrl) {
            queueUrlField = queueUrl;
            return this;
        }

        public Boolean IsSetQueueUrl() {
            return queueUrlField != null;
        }

        protected internal string ToXMLFragment() {
            var xml = new StringBuilder();
            if (IsSetQueueUrl()) {
                xml.Append("<QueueUrl>");
                xml.Append(XMLHelper.EscapeXML(QueueUrl));
                xml.Append("</QueueUrl>");
            }
            return xml.ToString();
        }
    }
}