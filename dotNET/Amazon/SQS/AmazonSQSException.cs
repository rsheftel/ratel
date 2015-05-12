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
using System.Net;

namespace Amazon.SQS {
    public class AmazonSQSException : Exception {
        readonly String message;
        readonly HttpStatusCode statusCode = default(HttpStatusCode);
        readonly String errorCode;
        readonly String errorType;
        readonly String requestId;
        readonly String xml;
    
        public AmazonSQSException(String message) {
            this.message = message;
        }
    
        public AmazonSQSException(String message, HttpStatusCode statusCode) : this (message) {
            this.statusCode = statusCode;
        }
    
        public AmazonSQSException(Exception t) : this (t.Message, t) {}
    
        public AmazonSQSException(String message, Exception t) : base (message, t) {
            this.message = message;
            if (!(t is AmazonSQSException)) return;
            var ex = (AmazonSQSException)t;
            statusCode = ex.StatusCode;
            errorCode = ex.ErrorCode;
            errorType = ex.ErrorType;
            requestId = ex.RequestId;
            xml = ex.XML;
        }

        public AmazonSQSException(String message, HttpStatusCode statusCode, String errorCode, String errorType, String requestId, String xml) : this (message, statusCode) {
            this.errorCode = errorCode;
            this.errorType = errorType;
            this.requestId = requestId;
            this.xml = xml;
        }
    
        public String ErrorCode {
            get { return errorCode; }
        }

        public String ErrorType {
            get { return errorType; }
        }

        public override String Message {
            get { return message; }
        }
    
        public HttpStatusCode StatusCode {
            get { return statusCode; }
        }

        public String XML {
            get { return xml; }
        }
 
        public String RequestId {
            get { return requestId; }
        }
    
    }
}
