// This software code is made available "AS IS" without warranties of any 
// kind.  You may copy, display, modify and redistribute the software
// code either by itself or as incorporated into your code; provided that
// you do not remove any proprietary notices.  Your use of this software
// code is at your own risk and you waive any claim against Amazon Web 
// Services LLC or its affiliates with respect to your use of this software 
// code. (c) Amazon Web Services LLC or its affiliates.

using System;
using System.Collections.Generic;
using System.Text;
using System.Security.Cryptography;

namespace Amazon.EC2 {
    static class EC2Helper {
        public static string GetTimestamp() {
            return DateTime.UtcNow.ToString("yyyy-MM-ddTHH:mm:ss.fffZ", System.Globalization.CultureInfo.InvariantCulture);
        }

        /// Computes RFC 2104-compliant HMAC signature for request parameters 
        /// Implements AWS Signature, as per following spec:
        /// 
        /// Sort all query parameters (including SignatureVersion and excluding Signature,
        /// the value of which is being created), ignoring case. Optional parameters not
        /// included in the request should not be canonicalized as "empty" parameters.
        /// That is, if no value for a ParameterA is specified in the request, there
        /// should not be a ParameterA entry in the canonicalized string.
        ///      
        /// Iterate over the sorted list and append the parameter name (in original case)
        /// and then its value. Do not URL-encode the parameter values before constructing 
        /// this string. There are no separators.
        public static string GetSignature(IDictionary<String, String> parameters, string secretKey) {
            var sorted = new SortedDictionary<string, string>();
            foreach (var pair in parameters) sorted.Add(pair.Key, pair.Value);

            var sb = new StringBuilder();
            foreach (var kvp in sorted) {
                sb.Append (kvp.Key); 
                sb.Append (kvp.Value);
            }

            var data = sb.ToString(); 
            Encoding ae = new UTF8Encoding();
            var signature = new HMACSHA1(ae.GetBytes(secretKey));
            return Convert.ToBase64String(signature.ComputeHash(ae.GetBytes(data.ToCharArray())));
        }

    }
}
