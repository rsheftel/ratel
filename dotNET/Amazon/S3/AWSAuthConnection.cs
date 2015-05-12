// This software code is made available "AS IS" without warranties of any        
// kind.  You may copy, display, modify and redistribute the software            
// code either by itself or as incorporated into your code; provided that        
// you do not remove any proprietary notices.  Your use of this software         
// code is at your own risk and you waive any claim against Amazon               
// Digital Services, Inc. or its affiliates with respect to your use of          
// this software code. (c) 2006-2007 Amazon Digital Services, Inc. or its             
// affiliates.          

using System;
using System.Collections;
using System.Net;
using System.Text;
using System.Text.RegularExpressions;

namespace Amazon.S3 {
    /// An interface into the S3 system.  It is initially configured with
    /// authentication and connection parameters and exposes methods to access and
    /// manipulate S3 data.
    public class AWSAuthConnection {
        readonly string awsAccessKeyId;
        readonly string awsSecretAccessKey;
        readonly bool isSecure;
        readonly string server;
        readonly int port;
        readonly CallingFormat callingFormat;

        public AWSAuthConnection(string awsAccessKeyId, string awsSecretAccessKey)
            : this(awsAccessKeyId, awsSecretAccessKey, true, CallingFormat.SUBDOMAIN) {}

        public AWSAuthConnection(string awsAccessKeyId, string awsSecretAccessKey, CallingFormat format)
            : this(awsAccessKeyId, awsSecretAccessKey, true, format) {}

        public AWSAuthConnection(string awsAccessKeyId, string awsSecretAccessKey, bool isSecure)
            : this(awsAccessKeyId, awsSecretAccessKey, isSecure, Utils.Host, CallingFormat.SUBDOMAIN) {}

        public AWSAuthConnection(string awsAccessKeyId, string awsSecretAccessKey, bool isSecure, CallingFormat format)
            : this(awsAccessKeyId, awsSecretAccessKey, isSecure, Utils.Host, format) {}

        public AWSAuthConnection(
            string awsAccessKeyId,
            string awsSecretAccessKey,
            bool isSecure,
            string server,
            CallingFormat format)
            : this(awsAccessKeyId, awsSecretAccessKey, isSecure, server, pickPort(isSecure), format) {}

        public AWSAuthConnection(
            string awsAccessKeyId,
            string awsSecretAccessKey,
            bool isSecure,
            string server)
            : this(awsAccessKeyId, awsSecretAccessKey, isSecure, server, pickPort(isSecure), CallingFormat.SUBDOMAIN) {}

        static int pickPort(bool isSecure) {
            return isSecure ? Utils.SecurePort : Utils.InsecurePort;
        }

        public AWSAuthConnection(
            string awsAccessKeyId,
            string awsSecretAccessKey,
            bool isSecure,
            string server,
            int port)
            : this(awsAccessKeyId, awsSecretAccessKey, isSecure, server, port, CallingFormat.SUBDOMAIN) {}

        public AWSAuthConnection(
            string awsAccessKeyId,
            string awsSecretAccessKey,
            bool isSecure,
            string server,
            int port,
            CallingFormat format
            ) {
            this.awsAccessKeyId = awsAccessKeyId;
            this.awsSecretAccessKey = awsSecretAccessKey;
            this.isSecure = isSecure;
            this.server = server;
            this.port = port;
            callingFormat = format;
        }

        public Response createBucket(string bucket, String location, SortedList headers) {
            if (!validateBucketName(bucket)) throw new ArgumentException("Invalid Bucket Name: " + bucket);

            string body;
            if (location == null) body = "";
            else if (Location.EU.Equals(location))
                body =
                    "<CreateBucketConstraint><LocationConstraint>" +
                        location + "</LocationConstraint></CreateBucketConstraint>";
            else throw new ArgumentException("Invalid Location: " + location);

            return new Response(makeRequest("PUT", bucket, "", null, headers, new S3Object(body, null)));
        }

        public bool checkBucketExists(String bucket) {
            try {
                var response = makeRequest("HEAD", bucket, "", null, null, null);
                response.Close();
                return true;
            } catch (WebException ex) {
                var response = ex.Response as HttpWebResponse;
                if (response != null && response.StatusCode == HttpStatusCode.NotFound) return false;
                throw;
            }
        }

        public ListBucketResponse listBucket(
            string bucket, string prefix, string marker, int maxKeys, SortedList headers
            ) {
            return listBucket(bucket, prefix, marker, maxKeys, null, headers);
        }

        public ListBucketResponse listBucket(
            string bucket, string prefix, string marker, int maxKeys, string delimiter, SortedList headers
            ) {
            var query = Utils.queryForListOptions(prefix, marker, maxKeys, delimiter);
            return new ListBucketResponse(makeRequest("GET", bucket, "", query, headers, null));
        }

        public Response deleteBucket(string bucket, SortedList headers) {
            return new Response(makeRequest("DELETE", bucket, "", null, headers, null));
        }

        public Response put(string bucket, string key, S3Object obj, SortedList headers) {
            return new Response(makeRequest("PUT", bucket, key, null, headers, obj));
        }

        public GetResponse get(string bucket, string key, SortedList headers) {
            return new GetResponse(makeRequest("GET", bucket, key, null, headers, null));
        }

        public Response delete(string bucket, string key, SortedList headers) {
            return new Response(makeRequest("DELETE", bucket, key, null, headers, null));
        }

        public LocationResponse getBucketLocation(string bucket) {
            return new LocationResponse(makeRequest("GET", bucket, "", newSingleParam("location"), null, null));
        }

        public GetResponse getBucketLogging(string bucket, SortedList headers) {
            return new GetResponse(makeRequest("GET", bucket, "", newSingleParam("logging"), headers, null));
        }

        public Response putBucketLogging(string bucket, string loggingXMLDoc, SortedList headers) {
            return
                new Response(
                    makeRequest(
                        "PUT", bucket, "", newSingleParam("logging"), headers, new S3Object(loggingXMLDoc, null)));
        }

        public GetResponse getBucketACL(string bucket, SortedList headers) {
            return getACL(bucket, null, headers);
        }

        public GetResponse getACL(string bucket, string key, SortedList headers) {
            if (key == null) key = "";
            return new GetResponse(makeRequest("GET", bucket, key, newSingleParam("acl"), headers, null));
        }

        public Response putBucketACL(string bucket, string aclXMLDoc, SortedList headers) {
            return putACL(bucket, null, aclXMLDoc, headers);
        }

        public Response putACL(string bucket, string key, string aclXMLDoc, SortedList headers) {
            if (key == null) key = "";
            return
                new Response(
                    makeRequest("PUT", bucket, key, newSingleParam("acl"), headers, new S3Object(aclXMLDoc, null)));
        }

        public ListAllMyBucketsResponse listAllMyBuckets(SortedList headers) {
            return new ListAllMyBucketsResponse(makeRequest("GET", "", "", null, headers, null));
        }

        WebResponse makeRequest(
            string method, string bucket, string key, SortedList query, IDictionary headers, S3Object obj
            ) {
            if (!String.IsNullOrEmpty(key)) key = Utils.urlEncode(key);
            var url = buildUrl(bucket, key, query);

            var redirectCount = 0;
            for (;;) {
                // prep request
                var req = (HttpWebRequest) WebRequest.Create(url);
                req.Method = method;
                // we handle redirects manually
                req.AllowAutoRedirect = false;
                // we already buffer everything
                req.AllowWriteStreamBuffering = false;
                // if we are sending >1kb data, ask for 100-Continue
                if (obj != null && obj.Bytes.Length > 1024) req.ServicePoint.Expect100Continue = true;

                addHeaders(req, headers);
                if (obj != null) addMetadataHeaders(req, obj.Metadata);
                addAuthHeader(req, bucket, key, query);

                if (obj != null) {
                    // Work around an HttpWebRequest bug where it will
                    // send the request body even if the server does *not* 
                    // send 100 continue
                    req.KeepAlive = false;
                    // Write actual data
                    var data = obj.Bytes;
                    req.ContentLength = data.Length;
                    var requestStream = req.GetRequestStream();
                    requestStream.Write(data, 0, data.Length);
                    requestStream.Close();
                }

                // execute request
                HttpWebResponse response;
                try {
                    response = (HttpWebResponse) req.GetResponse();
                    if (!isRedirect(response)) return response;
                    // retry against redirected url
                    url = response.Headers["Location"];
                    response.Close();
                    if (string.IsNullOrEmpty(url))
                        throw new WebException(
                            "Redirect without Location header, may need to change calling format.",
                            WebExceptionStatus.ProtocolError);
                } catch (WebException ex) {
                    if (ex.Response == null) throw;
                    var msg = Utils.slurpInputStreamAsString(ex.Response.GetResponseStream());
                    throw new WebException(msg, ex, ex.Status, ex.Response);
                }

                redirectCount++;
                if (redirectCount > 10) throw new WebException("Too many redirects.");
            }
        }

        string buildUrl(string bucket, string key, SortedList query) {
            var url = new StringBuilder();
            url.Append(isSecure ? "https://" : "http://");
            url.Append(Utils.buildUrlBase(server, port, bucket, callingFormat));
            if (!string.IsNullOrEmpty(key)) url.Append(key);
            if (query != null && query.Count != 0) url.Append(Utils.convertQueryListToQueryString(query));
            return url.ToString();
        }

        static Boolean isRedirect(WebResponse response) {
            var httpResp = response as HttpWebResponse;
            if (httpResp != null) {
                var status = (int) httpResp.StatusCode;
                return (status >= 300 && status < 400);
            }
            return false;
        }

        static void addHeaders(WebRequest req, IDictionary headers) {
            addHeaders(req, headers, "");
        }

        static void addMetadataHeaders(WebRequest req, IDictionary metadata) {
            addHeaders(req, metadata, Utils.METADATA_PREFIX);
        }

        static void addHeaders(WebRequest req, IDictionary headers, string prefix) {
            if (headers == null) return;
            foreach (string key in headers.Keys)
                if (prefix.Length == 0 && key.Equals("Content-Type")) req.ContentType = headers[key] as string;
                else req.Headers.Add(prefix + key, headers[key] as string);
        }

        void addAuthHeader(WebRequest request, string bucket, string key, SortedList query) {
            var headers = request.Headers;
            if (headers[Utils.ALTERNATIVE_DATE_HEADER] == null) headers.Add(Utils.ALTERNATIVE_DATE_HEADER, Utils.getHttpDate());

            var canonicalString = Utils.makeCanonicalString(bucket, key, query, request);
            var encodedCanonical = Utils.encode(awsSecretAccessKey, canonicalString, false);
            headers.Add("Authorization", "AWS " + awsAccessKeyId + ":" + encodedCanonical);
        }

        static readonly Regex PATH
            = new Regex("^[0-9A-Za-z\\.\\-_]*$", TOLERANT);
        static readonly Regex SUBDOMAIN
            = new Regex("^[a-z0-9]([a-z0-9\\-]*[a-z0-9])?(\\.[a-z0-9]([a-z0-9\\-]*[a-z0-9])?)*$", TOLERANT);
        static readonly Regex SUBDOMAIN_BAD
            = new Regex("^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$", TOLERANT);
        const RegexOptions TOLERANT = RegexOptions.CultureInvariant;

        bool validateBucketName(string bucketName) {
            bool valid;
            if (callingFormat == CallingFormat.PATH)
                valid = null != bucketName && bucketName.Length >= 3 &&
                    bucketName.Length <= 255 && PATH.IsMatch(bucketName);
            else
                // If there wasn't a location-constraint, then the current actual 
                // restriction is just that no 'part' of the name (i.e. sequence
                // of characters between any 2 '.'s has to be 63) but the recommendation
                // is to keep the entire bucket name under 63.
                valid = null != bucketName && bucketName.Length >= 3 && bucketName.Length <= 63 &&
                    !SUBDOMAIN_BAD.IsMatch(bucketName) && SUBDOMAIN.IsMatch(bucketName);
            return valid;
        }

        static SortedList newSingleParam(string name) {
            var query = new SortedList(1) {{name, null}};
            return query;
        }
    }
}