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

namespace Amazon.S3 {
    public class GetResponse : Response {
        readonly S3Object obj;
        public S3Object Object {
            get { return obj; }
        }

        public GetResponse(WebResponse response)
            : base(response) {
            var metadata = extractMetadata(response);
            var data = Utils.slurpInputStream(response.GetResponseStream());
            obj = new S3Object(data, metadata);
        }

        static SortedList extractMetadata(WebResponse response) {
            var metadata = new SortedList();
            foreach (string key in response.Headers.Keys) {
                if (key == null) continue;
                if (key.StartsWith(Utils.METADATA_PREFIX)) metadata.Add(key.Substring(Utils.METADATA_PREFIX.Length), response.Headers[key]);
            }
            return metadata;
        }
    }
}