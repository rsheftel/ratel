// This software code is made available "AS IS" without warranties of any        
// kind.  You may copy, display, modify and redistribute the software            
// code either by itself or as incorporated into your code; provided that        
// you do not remove any proprietary notices.  Your use of this software         
// code is at your own risk and you waive any claim against Amazon               
// Digital Services, Inc. or its affiliates with respect to your use of          
// this software code. (c) 2006 Amazon Digital Services, Inc. or its             
// affiliates.          

using System.Xml;

namespace Amazon.S3 {
    public class CommonPrefixEntry {
        public string Prefix { set; get; }

        public CommonPrefixEntry(string prefix) {
            Prefix = prefix;
        }

        public CommonPrefixEntry(XmlNode node) {
            foreach (XmlNode child in node.ChildNodes) if (child.Name.Equals("Prefix")) Prefix = Utils.getXmlChildText(child);
        }
    }
}