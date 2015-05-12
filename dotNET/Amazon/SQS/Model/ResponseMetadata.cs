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
    public class ResponseMetadata {
    
        String requestIdField;

        [XmlElementAttribute(ElementName = "RequestId")]
        public String RequestId {
            get { return requestIdField; }
            set { requestIdField = value; }
        }

        public ResponseMetadata WithRequestId(String requestId) {
            requestIdField = requestId;
            return this;
        }

        public Boolean IsSetRequestId() {
            return requestIdField != null;
        }

        protected internal String ToXMLFragment() {
            var xml = new StringBuilder();
            if (IsSetRequestId()) {
                xml.Append("<RequestId>");
                xml.Append(XMLHelper.EscapeXML(RequestId));
                xml.Append("</RequestId>");
            }
            return xml.ToString();
        }
    }

}