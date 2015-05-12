/******************************************************************************* 
 *  Copyright 2007 Amazon Technologies, Inc.  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  
 *  You may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at: http://aws.amazon.com/apache2.0
 *  This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 *  CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 *  specific language governing permissions and limitations under the License.
 * ******************************************************************************/

using System;
using System.Xml.Serialization;
using System.Collections.Generic;
using System.Text;

namespace Amazon.SQS.Model {
    [XmlTypeAttribute(Namespace = "http://queue.amazonaws.com/doc/2008-01-01/")]
    [XmlRootAttribute(Namespace = "http://queue.amazonaws.com/doc/2008-01-01/", IsNullable = false)]
    public class ListQueuesResult {
        List<string> queueUrlField;

        [XmlElementAttribute(ElementName = "QueueUrl")]
        public List<string> QueueUrl { 
            get {
                if (queueUrlField == null) queueUrlField = new List<string>();
                return queueUrlField;
            }
            set { queueUrlField = value; }
        }

        public ListQueuesResult WithQueueUrl(params string[] list) {
            foreach (var item in list) QueueUrl.Add(item);
            return this;
        }  

        public Boolean IsSetQueueUrl() {
            return QueueUrl.Count > 0;
        }

        protected internal string ToXMLFragment() {
            var xml = new StringBuilder();
            var queueUrlList = QueueUrl;
            foreach (var queueUrl in queueUrlList) { 
                xml.Append("<QueueUrl>");
                xml.Append(XMLHelper.EscapeXML(queueUrl));
                xml.Append("</QueueUrl>");
            }	
            return xml.ToString();
        }
    }
}