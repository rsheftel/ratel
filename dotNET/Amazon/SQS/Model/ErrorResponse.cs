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
using System.Collections.Generic;
using System.Text;


namespace Amazon.SQS.Model {
    [XmlTypeAttribute(Namespace = "http://queue.amazonaws.com/doc/2008-01-01/")]
    [XmlRootAttribute(Namespace = "http://queue.amazonaws.com/doc/2008-01-01/", IsNullable = false)]
    public class ErrorResponse {
        List<Error> errorField;
        string requestIdField;

        [XmlElementAttribute(ElementName = "Error")]
        public List<Error> Error {
            get {
                if (errorField == null) errorField = new List<Error>();
                return errorField;
            }
            set { errorField = value; }
        }

        public ErrorResponse WithError(params Error[] list) {
            foreach (var item in list) Error.Add(item);
            return this;
        }          
 
        public Boolean IsSetError() {
            return (Error.Count > 0);
        }

        [XmlElementAttribute(ElementName = "RequestId")]
        public string RequestId {
            get { return requestIdField; }
            set { requestIdField = value; }
        }

        public ErrorResponse WithRequestId(string requestId) {
            requestIdField = requestId;
            return this;
        }

        public Boolean IsSetRequestId() {
            return requestIdField != null;
        }

        public string ToXML() {
            var xml = new StringBuilder();
            xml.Append("<ErrorResponse xmlns=\"http://queue.amazonaws.com/doc/2008-01-01/\">");
            var errorList = Error;
            foreach (var error in errorList) {
                xml.Append("<Error>");
                xml.Append(error.ToXMLFragment());
                xml.Append("</Error>");
            }
            if (IsSetRequestId()) {
                xml.Append("<RequestId>");
                xml.Append(XMLHelper.EscapeXML(RequestId));
                xml.Append("</RequestId>");
            }
            xml.Append("</ErrorResponse>");
            return xml.ToString();
        }     
    }
}