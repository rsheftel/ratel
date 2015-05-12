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
using System.Text;

namespace Amazon.S3 {
    /// This class mimics the behavior of AWSAuthConnection, except instead of actually performing
    /// the operation, QueryStringAuthGenerator will return URLs with query string parameters that
    /// can be used to do the same thing.  These parameters include an expiration date, so that
    /// if you hand them off to someone else, they will only work for a limited amount of time.
    public class QueryStringAuthGenerator {
        readonly string awsAccessKeyId;
        readonly string awsSecretAccessKey;
        readonly bool isSecure;
        readonly string server;
        readonly int port;
        CallingFormat callingFormat;

        long expiresIn = NOT_SET;
        long expires = NOT_SET;

        // by default, expire in 1 minute
        public static readonly long DEFAULT_EXPIRES_IN = 60 * 1000;

        // Sentinel to indicate when a date is not set
        const long NOT_SET = -1;

        public QueryStringAuthGenerator(string awsAccessKeyId, string awsSecretAccessKey)
            : this(awsAccessKeyId, awsSecretAccessKey, true, CallingFormat.SUBDOMAIN) {}

        public QueryStringAuthGenerator(
            string awsAccessKeyId,
            string awsSecretAccessKey,
            bool isSecure,
            CallingFormat format)
            : this(awsAccessKeyId, awsSecretAccessKey, isSecure, Utils.Host, format) {}

        public QueryStringAuthGenerator(
            string awsAccessKeyId,
            string awsSecretAccessKey,
            bool isSecure)
            : this(awsAccessKeyId, awsSecretAccessKey, isSecure, Utils.Host, CallingFormat.SUBDOMAIN) {}

        public QueryStringAuthGenerator(
            string awsAccessKeyId,
            string awsSecretAccessKey,
            bool isSecure,
            string server,
            CallingFormat format)
            : this(awsAccessKeyId,
                awsSecretAccessKey,
                isSecure,
                server,
                isSecure ? Utils.SecurePort : Utils.InsecurePort,
                format) {}

        public QueryStringAuthGenerator(
            string awsAccessKeyId,
            string awsSecretAccessKey,
            bool isSecure,
            string server)
            : this(awsAccessKeyId,
                awsSecretAccessKey,
                isSecure,
                server,
                isSecure ? Utils.SecurePort : Utils.InsecurePort,
                CallingFormat.SUBDOMAIN) {}

        public QueryStringAuthGenerator(
            string awsAccessKeyId,
            string awsSecretAccessKey,
            bool isSecure,
            string server,
            int port)
            : this(awsAccessKeyId,
                awsSecretAccessKey,
                isSecure,
                server,
                port,
                CallingFormat.SUBDOMAIN) {}

        public QueryStringAuthGenerator(
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
            expiresIn = DEFAULT_EXPIRES_IN;
            expires = NOT_SET;
            callingFormat = format;
        }
      
        public CallingFormat CallingFormat {
            get { return callingFormat; }
            set { callingFormat = value; }
        }

        public long Expires {
            get { return expires; }
            set {
                expires = value;
                expiresIn = NOT_SET;
            }
        }

        public long ExpiresIn {
            get { return expiresIn; }
            set {
                expiresIn = value;
                expires = NOT_SET;
            }
        }

        public string createBucket(string bucket, SortedList headers, SortedList metadata) {
            return generateURL("PUT", bucket, "", mergeMeta(headers, metadata));
        }

        public string listBucket(
            string bucket,
            string prefix,
            string marker,
            int maxKeys,
            SortedList headers
        ) {
            return listBucket(bucket, prefix, marker, maxKeys, null, headers);
        }

        public string listBucket(
            string bucket,
            string prefix,
            string marker,
            int maxKeys,
            string delimiter,
            SortedList headers
        ) {
            var query = Utils.queryForListOptions(prefix, marker, maxKeys, delimiter);
            return generateURL("GET", bucket, "", query, headers);
        }

        public string deleteBucket(string bucket, SortedList headers) {
            return generateURL("DELETE", bucket, "", headers);
        }

        public string put(string bucket, string key, S3Object obj, SortedList headers) {
            SortedList metadata = null;
            if (obj != null) metadata = obj.Metadata;

            return generateURL("PUT", bucket, key, mergeMeta(headers, metadata));
        }

        public string get(string bucket, string key, SortedList headers) {
            return generateURL("GET", bucket, key, headers);
        }

        public string delete(string bucket, string key, SortedList headers) {
            return generateURL("DELETE", bucket, key, headers);
        }

        public string getBucketLogging(string bucket, SortedList headers) {
            var query = new SortedList {{"logging", ""}};
            return generateURL("GET", bucket, "", query, headers);
        }

        public string putBucketLogging(string bucket, SortedList headers) {
            var query = new SortedList {{"logging", ""}};
            return generateURL("PUT", bucket, "", query, headers);
        }

        public string getBucketACL(string bucket, SortedList headers) {
            var query = new SortedList {{"acl", ""}};
            return generateURL("GET", bucket, "", query, headers);
        }

        public string getACL(string bucket, string key, SortedList headers) {
            var query = new SortedList {{"acl", ""}};
            return generateURL("GET", bucket, key, query, headers);
        }

        public string putBucketACL(string bucket, SortedList headers) {
            var query = new SortedList {{"acl", ""}};
            return generateURL("PUT", bucket, "", query, headers);
        }

        public string putACL(string bucket, string key, SortedList headers) {
            var query = new SortedList {{"acl", ""}};
            return generateURL("PUT", bucket, key, query, headers);
        }

        public string listAllMyBuckets(SortedList headers) {
            return generateURL("GET", "", "", headers);
        }

        public string makeBaseURL(string bucket, string key) {
            key = Utils.urlEncode(key);
            return makeBaseURL(new StringBuilder(), bucket, key).ToString();
        }

        StringBuilder makeBaseURL(StringBuilder builder, string bucket, string key) {
            builder.Append(isSecure ? "https://" : "http://");
            builder.Append(Utils.buildUrlBase(server, port, bucket, callingFormat));
            if (!string.IsNullOrEmpty(bucket) && !string.IsNullOrEmpty(key)) builder.Append(key);
            return builder;
        }

        string generateURL(string method, string bucket, string key, SortedList headers) {
            return generateURL(method, bucket, key, new SortedList(), headers);
        }

        string generateURL(
            string method, string bucket, string key,
            SortedList queryParameters, SortedList headers
        ) {
            long expireAt;
            if (expiresIn != NOT_SET) expireAt = Utils.currentTimeMillis() + expiresIn;
            else if (expires != NOT_SET) expireAt = expires;
            else throw new Exception("Illegal expire state!");

            // convert to seconds
            expireAt /= 1000;

            key = Utils.urlEncode(key);

            var canonicalString = Utils.makeCanonicalString(
                method, bucket, key, queryParameters, headers, "" + expireAt);
            var encodedCanonical = Utils.encode(awsSecretAccessKey, canonicalString, true);

            queryParameters.Add("Signature", encodedCanonical);
            queryParameters.Add("Expires", "" + expireAt);
            queryParameters.Add("AWSAccessKeyId", awsAccessKeyId);

            var builder = new StringBuilder();
            makeBaseURL(builder, bucket, key);
            builder.Append(Utils.convertQueryListToQueryString(queryParameters));
            return builder.ToString();
        }

        static SortedList mergeMeta(IDictionary headers, IDictionary metadata) {
            var merged = new SortedList();
            if (headers != null) 
                foreach (string key in headers.Keys) 
                    merged.Add(key, headers[key]);

            if (metadata != null)
                foreach (string key in metadata.Keys) {
                    var existing = merged[key] as string;
                    if (existing != null) existing += "," + metadata[key];
                    else existing = metadata[key] as string;
                    merged.Add(key, existing);
                }

            return merged;
        }
    }
}