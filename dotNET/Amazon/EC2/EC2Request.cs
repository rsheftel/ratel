// This software code is made available "AS IS" without warranties of any 
// kind.  You may copy, display, modify and redistribute the software
// code either by itself or as incorporated into your code; provided that
// you do not remove any proprietary notices.  Your use of this software
// code is at your own risk and you waive any claim against Amazon Web 
// Services LLC or its affiliates with respect to your use of this software 
// code. (c) Amazon Web Services LLC or its affiliates.

using System.Collections.Generic;
using System.Text;
using System.Net;
using System.Web;

namespace Amazon.EC2 {
	public class EC2Request {
	    readonly IDictionary<string, string> _Parameters;
	    readonly string _SecretAccessKey;
	    readonly string _Host;
	    readonly string _Protocol;
	    
	    public EC2Request(
            string accessKeyId, string secretAccessKey, string signatureVersion, string version,
	        string host, string protocol
        ) {
		    _Parameters = new Dictionary<string,string> {
		        {"AWSAccessKeyId", accessKeyId},
		        {"SignatureVersion", signatureVersion},
		        {"Version", version}
		    };
	        _Host = host;
		    _Protocol = protocol;
		    _SecretAccessKey = secretAccessKey;
		}
		
		public void AddParameter(string key, string value) {
		    if (value != null) _Parameters.Add(key, value);
		}
		
		public void AddParameters(string key, IList<string> values) {
		    if (values == null || values.Count == 0) return;
		    for (var i = 0; i < values.Count; i++) 
                _Parameters.Add(key + "." + i, values[i]);
		}
		
		public EC2Response Execute(string action) {
		    _Parameters.Add("Action", action);
		    _Parameters.Add("Timestamp", EC2Helper.GetTimestamp());
		    _Parameters.Add("Signature", EC2Helper.GetSignature(_Parameters, _SecretAccessKey));
		    return DoRequest(BuildUri());
		}
		
        static EC2Response DoRequest(string uri) {
            var request = (HttpWebRequest) WebRequest.Create(uri);
            using (var response = (HttpWebResponse) request.GetResponse()) 
                return new EC2Response(uri, response);
        }
        
		public string BuildUri() {
		    var sb = new StringBuilder();
		    sb.Append(_Protocol);
		    sb.Append("://");
		    sb.Append(_Host);
		    sb.Append("/");
		    if (_Parameters.Count > 0) {
		        sb.Append("?");
		        var first = true;
    		    foreach (var pair in _Parameters) {
    		        if (first) first = false;
    		        else sb.Append("&");

    		        sb.Append(HttpUtility.UrlEncode(pair.Key));
    		        sb.Append("=");
    		        sb.Append(HttpUtility.UrlEncode(pair.Value));
                }
		    }
		    return sb.ToString();
		}
	}
}
