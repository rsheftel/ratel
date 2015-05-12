// This software code is made available "AS IS" without warranties of any 
// kind.  You may copy, display, modify and redistribute the software
// code either by itself or as incorporated into your code; provided that
// you do not remove any proprietary notices.  Your use of this software
// code is at your own risk and you waive any claim against Amazon Web 
// Services LLC or its affiliates with respect to your use of this software 
// code. (c) Amazon Web Services LLC or its affiliates.

using System;
using System.Net;
using System.IO;

namespace Amazon.EC2
{
	
	public class EC2Response
	{
	    readonly HttpStatusCode _StatusCode;
	    readonly String _Text;
	    readonly String _ResponseUri;
	    readonly String _RequestUri;
	    
		public EC2Response(String requestUri, HttpWebResponse response) {
		    _StatusCode = response.StatusCode;
		    _Text = new StreamReader(response.GetResponseStream()).ReadToEnd();
		    _ResponseUri = response.ResponseUri.ToString();
		    _RequestUri = requestUri;
		}
		
		public String RequestUri {
		    get { return _RequestUri; }
		}
		
		public String ResponseUri {
		    get { return _ResponseUri; }
		}
		
		public bool Success {
		    get { return _StatusCode == HttpStatusCode.OK; }
		}
		
		public bool Failed {
		    get { return _StatusCode != HttpStatusCode.OK; }
		}
		
		public HttpStatusCode StatusCode {
		    get { return _StatusCode; }
		}
		
		public String Text {
		    get { return _Text; }
		}
	}
}
