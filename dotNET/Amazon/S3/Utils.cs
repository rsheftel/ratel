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
using System.IO;
using System.Net;
using System.Security.Cryptography;
using System.Text;
using System.Web;
using System.Xml;

namespace Amazon.S3 {
    public enum CallingFormat {
        PATH, // http://s3.amazonaws.com/key
        SUBDOMAIN, // http://bucket.s3.amazonaws.com/key
        VANITY // http://mydomain.com/key -- a vanity domain which resolves to s3.amazonaws.com
    }

    public static class Location {
        public const string DEFAULT = null;
        public const string EU = "EU";
    }

    internal class Utils {
        public static readonly string METADATA_PREFIX = "x-amz-meta-";
        public static readonly string AMAZON_HEADER_PREFIX = "x-amz-";
        public static readonly string ALTERNATIVE_DATE_HEADER = "x-amz-date";

        static string host = "s3.amazonaws.com";

        public static string Host {
            get { return host; }
            set { host = value; }
        }

        static int securePort = 443;
        public static int SecurePort {
            get { return securePort; }
            set { securePort = value; }
        }

        static int insecurePort = 80;
        public static int InsecurePort {
            get { return insecurePort; }
            set { insecurePort = value; }
        }

        public static string makeCanonicalString(string bucket, string key, WebRequest request) {
            return makeCanonicalString(bucket, key, new SortedList(), request);
        }

        public static string makeCanonicalString(string bucket, string key, SortedList query, WebRequest request) {
            var headers = new SortedList();
            foreach (string header in request.Headers) headers.Add(header, request.Headers[header]);
            return makeCanonicalString(request.Method, bucket, key, query, headers, null);
        }

        public static string makeCanonicalString(
            string verb,
            string bucketName,
            string key,
            SortedList queryParams,
            SortedList headers,
            string expires) {
            var buf = new StringBuilder();
            buf.Append(verb);
            buf.Append("\n");

            var interestingHeaders = new SortedList();
            if (headers != null)
                foreach (string header in headers.Keys) {
                    var lk = header.ToLower();
                    if (lk.Equals("content-type") ||
                        lk.Equals("content-md5") ||
                            lk.Equals("date") ||
                                lk.StartsWith(AMAZON_HEADER_PREFIX)) interestingHeaders.Add(lk, headers[header]);
                }
            if (interestingHeaders[ALTERNATIVE_DATE_HEADER] != null) interestingHeaders.Add("date", "");

            // if the expires is non-null, use that for the date field.  this
            // trumps the x-amz-date behavior.
            if (expires != null) interestingHeaders.Add("date", expires);

            // these headers require that we still put a new line after them,
            // even if they don't exist.
                {
                    string[] newlineHeaders = {"content-type", "content-md5"};
                    foreach (var header in newlineHeaders) if (interestingHeaders.IndexOfKey(header) == -1) interestingHeaders.Add(header, "");
                }

            // Finally, add all the interesting headers (i.e.: all that startwith x-amz- ;-))
            foreach (string header in interestingHeaders.Keys) {
                if (header.StartsWith(AMAZON_HEADER_PREFIX)) buf.Append(header).Append(":").Append(((string) interestingHeaders[header]).Trim());
                else buf.Append(interestingHeaders[header]);
                buf.Append("\n");
            }

            // Build the path using the bucket and key
            buf.Append("/");
            if (bucketName != null && !bucketName.Equals("")) {
                buf.Append(bucketName);
                buf.Append("/");
            }

            // Append the key (it may be an empty string)
            if (!string.IsNullOrEmpty(key)) buf.Append(key);

            // if there is an acl, logging, or torrent paramter, add them to the string.
            if (queryParams != null)
                if (queryParams.IndexOfKey("acl") != -1) buf.Append("?acl");
                else if (queryParams.IndexOfKey("torrent") != -1) buf.Append("?torrent");
                else if (queryParams.IndexOfKey("logging") != -1) buf.Append("?logging");
                else if (queryParams.IndexOfKey("location") != -1) buf.Append("?location");

            return buf.ToString();
        }

        public static string encode(string awsSecretAccessKey, string canonicalString, bool urlEncode) {
            Encoding ae = new UTF8Encoding();
            var signature = new HMACSHA1(ae.GetBytes(awsSecretAccessKey));
            var b64 = Convert.ToBase64String(
                signature.ComputeHash(
                    ae.GetBytes(
                        canonicalString.ToCharArray()))
                );

            return urlEncode ? HttpUtility.UrlEncode(b64) : b64;
        }

        public static byte[] slurpInputStream(Stream stream) {
            using (var ms = new MemoryStream()) {
                var buffer = new byte[32 * 1024];
                while (true) {
                    var nRead = stream.Read(buffer, 0, buffer.Length);
                    if (nRead <= 0) return ms.ToArray();
                    ms.Write(buffer, 0, nRead);
                }
            }
        }

        public static string slurpInputStreamAsString(Stream stream) {
            var encoding = new ASCIIEncoding();
            return encoding.GetString(slurpInputStream(stream));
        }

        public static string getXmlChildText(XmlNode data) {
            var builder = new StringBuilder();
            foreach (XmlNode node in data.ChildNodes)
                if (node.NodeType == XmlNodeType.Text ||
                    node.NodeType == XmlNodeType.CDATA) builder.Append(node.Value);
            return builder.ToString();
        }

        public static DateTime parseDate(string dateStr) {
            return DateTime.Parse(dateStr);
        }

        public static string getHttpDate() {
            // Setting the Culture will ensure we get a proper HTTP Date.
            var date =
                DateTime.UtcNow.ToString(
                    "ddd, dd MMM yyyy HH:mm:ss ", System.Globalization.CultureInfo.InvariantCulture) + "GMT";
            return date;
        }

        public static long currentTimeMillis() {
            return (long) (DateTime.UtcNow - new DateTime(1970, 1, 1)).TotalMilliseconds;
        }

        /// <summary>
        /// Calculates the endpoint based on the calling format.
        /// </summary>
        public static string buildUrlBase(string server, int port, string bucket, CallingFormat format) {
            var endpoint = new StringBuilder();

            if (format.Equals(CallingFormat.PATH)) {
                endpoint.Append(server);
                endpoint.Append(":");
                endpoint.Append(port);
                if (bucket != null && ! bucket.Equals("")) {
                    endpoint.Append("/");
                    endpoint.Append(bucket);
                }
            } else if (Equals(format, CallingFormat.SUBDOMAIN)) {
                if (bucket.Length != 0) {
                    endpoint.Append(bucket);
                    endpoint.Append(".");
                }
                endpoint.Append(server);
                endpoint.Append(":");
                endpoint.Append(port);
            } else if (format == CallingFormat.VANITY) {
                endpoint.Append(bucket);
                endpoint.Append(":");
                endpoint.Append(port);
            }
            endpoint.Append("/");
            return endpoint.ToString();
        }

        /// <summary>
        /// Escape a string that is destined to be part of a URL path.
        /// The Uri class tries to be smart about cleaning up the URL
        /// but we need to know exactly what the URL will look like 
        /// on the wire.  This is the result of trial-and-error
        /// analysis of what the Uri escapes and what it doesn't.
        /// </summary>
        public static string urlEncode(string path) {
            var extra = "?something";
            var uri = new Uri("http://nowhere/a" + path + extra);
            var result = uri.PathAndQuery;
            // trim leading '/a' and trailing '?something'
            if (result.StartsWith("/a")) result = result.Substring(2);
            if (result.EndsWith(extra)) result = result.Substring(0, result.Length - extra.Length);
            // special case '+', as S3 interprets unescaped + as space...
            result = result.Replace("+", "%2B");
            // if Uri parser lost some of the text, fall back to just UrlEncode
            if (path != HttpUtility.UrlDecode(result)) {
                result = HttpUtility.UrlEncode(path);
                if (result != null) result = result.Replace("%2f", "/");
            }
            return result;
        }

        public static SortedList queryForListOptions(string prefix, string marker, int maxKeys) {
            return queryForListOptions(prefix, marker, maxKeys, null);
        }

        public static SortedList queryForListOptions(string prefix, string marker, int maxKeys, string delimiter) {
            var queryStrings = new SortedList();
            if (prefix != null) queryStrings.Add("prefix", HttpUtility.UrlEncode(prefix));
            if (marker != null) queryStrings.Add("marker", HttpUtility.UrlEncode(marker));
            if (maxKeys != 0) queryStrings.Add("max-keys", "" + maxKeys);
            if (delimiter != null) queryStrings.Add("delimiter", HttpUtility.UrlEncode(delimiter));
            return queryStrings;
        }

        public static string convertQueryListToQueryString(SortedList query) {
            var queryString = new StringBuilder();
            var firstParameter = true;
            if (query != null)
                foreach (string key in query.Keys) {
                    var value = (string) query[key];
                    queryString.Append(firstParameter ? "?" : "&");
                    queryString.Append(key);
                    if (!string.IsNullOrEmpty(value)) queryString.Append("=").Append(value);
                    firstParameter = false;
                }
            return queryString.ToString();
        }
    }
}