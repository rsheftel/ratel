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

namespace Amazon.SQS {
    public class AmazonSQSConfig {
        const string serviceVersion = "2008-01-01";
        string serviceURL = "http://queue.amazonaws.com";
        string userAgent = "Amazon SQS CSharp Library #Q78";
        string signatureVersion = "1";
        string proxyHost;
        int proxyPort = -1;
        int maxErrorRetry = 3;

        public string ServiceVersion {
            get { return serviceVersion; }
        }

        public string SignatureVersion {
            get { return signatureVersion; }
            set { signatureVersion = value; }
        }

        public AmazonSQSConfig WithSignatureVersion(string newSignatureVersion) {
            signatureVersion = newSignatureVersion;
            return this;
        }

        public Boolean IsSetSignatureVersion() {
            return signatureVersion != null;
        }
    
        public string UserAgent {
            get { return userAgent; }
            set { userAgent = value; }
        }

        public AmazonSQSConfig WithUserAgent(string newUserAgent) {
            userAgent = newUserAgent;
            return this;
        }

        public Boolean IsSetUserAgent() {
            return userAgent != null;
        }

        public string ServiceURL {
            get { return serviceURL; }
            set { serviceURL = value; }
        }

        public AmazonSQSConfig WithServiceURL(string newServiceURL) {
            serviceURL = newServiceURL;
            return this;
        }

        public Boolean IsSetServiceURL() {
            return serviceURL != null;
        }

        public string ProxyHost {
            get { return proxyHost; }
            set { proxyHost = value; }
        }

        public AmazonSQSConfig WithProxyHost(string newProxyHost) {
            proxyHost = newProxyHost;
            return this;
        }

        public Boolean IsSetProxyHost() {
            return proxyHost != null;
        }

        public int ProxyPort {
            get { return proxyPort; }
            set { proxyPort = value; }
        }

        public AmazonSQSConfig WithProxyPort(int newProxyPort) {
            proxyPort = newProxyPort;
            return this;
        }

        public Boolean IsSetProxyPort() {
            return proxyPort != -1;
        }

        public int MaxErrorRetry {
            get { return maxErrorRetry; }
            set { maxErrorRetry = value; }
        }

        public AmazonSQSConfig WithMaxErrorRetry(int newMaxErrorRetry) {
            maxErrorRetry = newMaxErrorRetry;
            return this;
        }

        public Boolean IsSetMaxErrorRetry() {
            return maxErrorRetry != -1;
        }
    }
}
