// This software code is made available "AS IS" without warranties of any        
// kind.  You may copy, display, modify and redistribute the software            
// code either by itself or as incorporated into your code; provided that        
// you do not remove any proprietary notices.  Your use of this software         
// code is at your own risk and you waive any claim against Amazon               
// Digital Services, Inc. or its affiliates with respect to your use of          
// this software code. (c) 2006-2007 Amazon Digital Services, Inc. or its             
// affiliates.          

using System.Net;
using System.Xml;

namespace Amazon.S3 {
    public class LocationResponse : Response {
        readonly string location;

        public string Location {
            get { return location; }
        }

        static void noOp() {}
        public LocationResponse(WebResponse response) : base(response) {
            try {
                var r = new XmlTextReader(response.GetResponseStream());
                while (r.Read() && !r.IsStartElement()) 
                    noOp();
                location = r.ReadElementString("LocationConstraint") ?? "";
            } catch (XmlException) {}
        }

    }
}