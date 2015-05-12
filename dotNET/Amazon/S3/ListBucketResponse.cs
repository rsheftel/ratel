// This software code is made available "AS IS" without warranties of any        
// kind.  You may copy, display, modify and redistribute the software            
// code either by itself or as incorporated into your code; provided that        
// you do not remove any proprietary notices.  Your use of this software         
// code is at your own risk and you waive any claim against Amazon               
// Digital Services, Inc. or its affiliates with respect to your use of          
// this software code. (c) 2006-2007 Amazon Digital Services, Inc. or its             
// affiliates.          

using System.Collections;
using System.Net;
using System.Xml;

namespace Amazon.S3 {
    public class ListBucketResponse : Response {
        readonly string name;
        public string Name {
            get { return name; }
        }

        readonly string prefix;
        public string Prefix {
            get { return prefix; }
        }

        readonly string marker;
        public string Marker {
            get { return marker; }
        }

        readonly string delimiter;
        public string Delimiter {
            get { return delimiter; }
        }

        readonly int maxKeys;
        public int MaxKeys {
            get { return maxKeys; }
        }

        readonly bool isTruncated;
        public bool IsTruncated {
            get { return isTruncated; }
        }

        readonly string nextMarker;
        public string NextMarker {
            get { return nextMarker; }
        }

        readonly ArrayList entries;
        public ArrayList Entries {
            get { return entries; }
        }

        readonly ArrayList commonPrefixEntries;
        public ArrayList CommonPrefixEntries {
            get { return commonPrefixEntries; }
        }

        public ListBucketResponse(WebResponse response) : base(response) {
            entries = new ArrayList();
            commonPrefixEntries = new ArrayList();
            var rawBucketXML = Utils.slurpInputStreamAsString(response.GetResponseStream());

            var doc = new XmlDocument();
            doc.LoadXml(rawBucketXML);
            foreach (XmlNode node in doc.ChildNodes)
                if (node.Name.Equals("ListBucketResult"))
                    foreach (XmlNode child in node.ChildNodes)
                        if (child.Name.Equals("Contents")) entries.Add(new ListEntry(child));
                        else if (child.Name.Equals("CommonPrefixes")) commonPrefixEntries.Add(new CommonPrefixEntry(child));
                        else if (child.Name.Equals("Name")) name = Utils.getXmlChildText(child);
                        else if (child.Name.Equals("Prefix")) prefix = Utils.getXmlChildText(child);
                        else if (child.Name.Equals("Marker")) marker = Utils.getXmlChildText(child);
                        else if (child.Name.Equals("Delimiter")) delimiter = Utils.getXmlChildText(child);
                        else if (child.Name.Equals("MaxKeys")) maxKeys = int.Parse(Utils.getXmlChildText(child));
                        else if (child.Name.Equals("IsTruncated")) isTruncated = bool.Parse(Utils.getXmlChildText(child));
                        else if (child.Name.Equals("NextMarker")) nextMarker = Utils.getXmlChildText(child);
        }
    }
}