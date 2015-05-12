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
    public class GetQueueAttributesResult {
        List<Attribute> attributeField;

        [XmlElementAttribute(ElementName = "Attribute")]
        public List<Attribute> Attribute {
            get {
                if (attributeField == null) attributeField = new List<Attribute>();
                return attributeField;
            }
            set { attributeField =  value; }
        }

        public GetQueueAttributesResult WithAttribute(params Attribute[] list) {
            foreach (var item in list) Attribute.Add(item);
            return this;
        }          
 
        public Boolean IsSetAttribute() {
            return (Attribute.Count > 0);
        }

        protected internal String ToXMLFragment() {
            var xml = new StringBuilder();
            var attributeList = Attribute;
            foreach (var attribute in attributeList) {
                xml.Append("<Attribute>");
                xml.Append(attribute.ToXMLFragment());
                xml.Append("</Attribute>");
            }
            return xml.ToString();
        }
    }
}