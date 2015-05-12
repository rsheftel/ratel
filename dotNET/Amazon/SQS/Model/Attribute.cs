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
    public class Attribute { 
        private string nameField;
        private string valueField;

        [XmlElementAttribute(ElementName = "Name")]
        public string Name {
            get { return nameField ; }
            set { nameField= value; }
        }

        public Attribute WithName(string name) {
            nameField = name;
            return this;
        }

        public Boolean IsSetName() {
            return nameField != null;
        }

        [XmlElementAttribute(ElementName = "Value")]
        public string Value {
            get { return valueField ; }
            set { valueField= value; }
        }

        public Attribute WithValue(string value) {
            valueField = value;
            return this;
        }

        public Boolean IsSetValue() {
            return valueField != null;
        }

        protected internal string ToXMLFragment() {
            var xml = new StringBuilder();
            if (IsSetName()) {
                xml.Append("<Name>");
                xml.Append(XMLHelper.EscapeXML(Name));
                xml.Append("</Name>");
            }
            if (IsSetValue()) {
                xml.Append("<Value>");
                xml.Append(XMLHelper.EscapeXML(Value));
                xml.Append("</Value>");
            }
            return xml.ToString();
        }
    }
}