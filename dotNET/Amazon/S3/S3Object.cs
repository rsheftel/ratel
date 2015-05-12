// This software code is made available "AS IS" without warranties of any        
// kind.  You may copy, display, modify and redistribute the software            
// code either by itself or as incorporated into your code; provided that        
// you do not remove any proprietary notices.  Your use of this software         
// code is at your own risk and you waive any claim against Amazon               
// Digital Services, Inc. or its affiliates with respect to your use of          
// this software code. (c) 2006 Amazon Digital Services, Inc. or its             
// affiliates.          

using System.Collections;
using System.Text;

namespace Amazon.S3 {
    public class S3Object {
        readonly byte[] bytes;
        public byte[] Bytes {
            get { return bytes; }
        }

        public string Data {
            get {
                var encoder = new ASCIIEncoding();
                return encoder.GetString(bytes, 0, bytes.Length);
            }
        }

        readonly SortedList metadata;
        public SortedList Metadata {
            get { return metadata; }
        }

        public S3Object(byte[] bytes, SortedList metadata) {
            this.bytes = bytes;
            this.metadata = metadata;
        }

        public S3Object(string data, SortedList metadata) {
            var encoder = new ASCIIEncoding();
            bytes = encoder.GetBytes(data.ToCharArray());
            this.metadata = metadata;
        }
    }
}