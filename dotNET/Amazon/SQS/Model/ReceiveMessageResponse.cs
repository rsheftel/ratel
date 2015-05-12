/******************************************************************************* 
 *  Copyright 2007 Amazon Technologies, Inc.  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  
 *  You may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at: http://aws.amazon.com/apache2.0
 *  This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 *  CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 *  specific language governing permissions and limitations under the License.
 * *****************************************************************************/

using System;
using System.Xml.Serialization;
using System.Text;

namespace Amazon.SQS.Model {
    [XmlTypeAttribute(Namespace = "http://queue.amazonaws.com/doc/2008-01-01/")]
    [XmlRootAttribute(Namespace = "http://queue.amazonaws.com/doc/2008-01-01/", IsNullable = false)]
    public class ReceiveMessageResponse {
    
        ReceiveMessageResult receiveMessageResultField;
        ResponseMetadata responseMetadataField;

        [XmlElementAttribute(ElementName = "ReceiveMessageResult")]
        public ReceiveMessageResult ReceiveMessageResult {
            get { return receiveMessageResultField; }
            set { receiveMessageResultField = value; }
        }

        public ReceiveMessageResponse WithReceiveMessageResult(ReceiveMessageResult receiveMessageResult) {
            receiveMessageResultField = receiveMessageResult;
            return this;
        }

        public Boolean IsSetReceiveMessageResult() {
            return receiveMessageResultField != null;
        }

        [XmlElementAttribute(ElementName = "ResponseMetadata")]
        public ResponseMetadata ResponseMetadata {
            get { return responseMetadataField; }
            set { responseMetadataField = value; }
        }

        public ReceiveMessageResponse WithResponseMetadata(ResponseMetadata responseMetadata) {
            responseMetadataField = responseMetadata;
            return this;
        }

        public Boolean IsSetResponseMetadata() {
            return responseMetadataField != null;
        }

        public String ToXML() {
            var xml = new StringBuilder();
            xml.Append("<ReceiveMessageResponse xmlns=\"http://queue.amazonaws.com/doc/2008-01-01/\">");
            if (IsSetReceiveMessageResult()) {
                var receiveMessageResult = ReceiveMessageResult;
                xml.Append("<ReceiveMessageResult>");
                xml.Append(receiveMessageResult.ToXMLFragment());
                xml.Append("</ReceiveMessageResult>");
            } 
            if (IsSetResponseMetadata()) {
                var responseMetadata = ResponseMetadata;
                xml.Append("<ResponseMetadata>");
                xml.Append(responseMetadata.ToXMLFragment());
                xml.Append("</ResponseMetadata>");
            } 
            xml.Append("</ReceiveMessageResponse>");
            return xml.ToString();
        }
    }
}