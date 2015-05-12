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
using System.Web;
using System.Net;
using System.Text;
using System.IO;
using System.Text.RegularExpressions;
using System.Security.Cryptography;
using System.Globalization;
using System.Xml.Serialization;
using System.Collections.Generic;
using Amazon.SQS.Model;

namespace Amazon.SQS {
    public class AmazonSQSClient : AmazonSQS {
        readonly string awsAccessKeyId;
        readonly string awsSecretAccessKey;
        readonly AmazonSQSConfig config;

        public AmazonSQSClient(string awsAccessKeyId, string awsSecretAccessKey)
            : this(awsAccessKeyId, awsSecretAccessKey, new AmazonSQSConfig()) {}

        public AmazonSQSClient(string awsAccessKeyId, string awsSecretAccessKey, AmazonSQSConfig config) {
            this.awsAccessKeyId = awsAccessKeyId;
            this.awsSecretAccessKey = awsSecretAccessKey;
            this.config = config;
        }

        public CreateQueueResponse CreateQueue(CreateQueue request) {
            return Invoke<CreateQueueResponse>(ConvertCreateQueue(request));
        }

        public ListQueuesResponse ListQueues(ListQueues request) {
            return Invoke<ListQueuesResponse>(ConvertListQueues(request));
        }

        public DeleteMessageResponse DeleteMessage(DeleteMessage request) {
            return Invoke<DeleteMessageResponse>(ConvertDeleteMessage(request));
        }
        
        public DeleteQueueResponse DeleteQueue(DeleteQueue request) {
            return Invoke<DeleteQueueResponse>(ConvertDeleteQueue(request));
        }

        public GetQueueAttributesResponse GetQueueAttributes(GetQueueAttributes request) {
            return Invoke<GetQueueAttributesResponse>(ConvertGetQueueAttributes(request));
        }

        public ReceiveMessageResponse ReceiveMessage(ReceiveMessage request) {
            return Invoke<ReceiveMessageResponse>(ConvertReceiveMessage(request));
        }

        public SendMessageResponse SendMessage(SendMessage request) {
            return Invoke<SendMessageResponse>(ConvertSendMessage(request));
        }

        public SetQueueAttributesResponse SetQueueAttributes(SetQueueAttributes request) {
            return Invoke<SetQueueAttributesResponse>(ConvertSetQueueAttributes(request));
        }

        private HttpWebRequest ConfigureWebRequest(int contentLength, string path) {
            var request = (HttpWebRequest)WebRequest.Create(config.ServiceURL + path);
            if (config.IsSetProxyHost()) request.Proxy = new WebProxy(config.ProxyHost, config.ProxyPort);
            request.UserAgent = config.UserAgent;
            request.Method = "POST";
            request.Timeout = 50000;
            request.ContentType = "application/x-www-form-urlencoded; charset=utf-8";
            request.ContentLength = contentLength;
            return request;
        }

        private T Invoke<T>(IDictionary<string, string> parameters) {
            var actionName = parameters["Action"];
            var response = default(T);
            var queueName = parameters.ContainsKey("QueueName") ? parameters["QueueName"] : null;
            var removeQueueNameFromParameters = queueName != null && !"CreateQueue".Equals(actionName);
            var queuepath = removeQueueNameFromParameters ? "/" + queueName : "";
            if (removeQueueNameFromParameters) parameters.Remove("QueueName");

            AddRequiredParameters(parameters);
            var queryString = GetParametersAsString(parameters);
            var requestData = new UTF8Encoding().GetBytes(queryString);
            bool shouldRetry;
            var retries = 0;
            do {
                var request = ConfigureWebRequest(requestData.Length, queuepath);
                var responseBody = "response unread";
                try {
                    using (var requestStream = request.GetRequestStream()) 
                        requestStream.Write(requestData, 0, requestData.Length);
                    using (var httpResponse = (HttpWebResponse)request.GetResponse()) {
                        var reader = new StreamReader(httpResponse.GetResponseStream(), Encoding.UTF8);
                        responseBody = reader.ReadToEnd();
                    }
                    Console.WriteLine(responseBody);
                    response = (T)new XmlSerializer(typeof(T)).Deserialize(new StringReader(responseBody));
                    shouldRetry = false;
                } catch (WebException we) {
                    HttpStatusCode statusCode;
                    using (var error = we.Response as HttpWebResponse) {
                        if (error == null) throw new AmazonSQSException("unknown response type\n" + responseBody, we);
                        statusCode = error.StatusCode;
                        var reader = new StreamReader(error.GetResponseStream(), Encoding.UTF8);
                        responseBody = reader.ReadToEnd();
                    }

                    if (statusCode == HttpStatusCode.InternalServerError || statusCode == HttpStatusCode.ServiceUnavailable) {
                        shouldRetry = true;
                        PauseOnRetry(++retries, statusCode);
                    } else {
                        try {
                            var serializer = new XmlSerializer(typeof(ErrorResponse));
                            var reader = new StringReader(responseBody);
                            var errorResponse = (ErrorResponse) serializer.Deserialize(reader);
                            var error = errorResponse.Error[0];
                            throw new AmazonSQSException(
                                error.Message,
                                statusCode,
                                error.Code,
                                error.Type,
                                errorResponse.RequestId,
                                errorResponse.ToXML());
                        } catch (AmazonSQSException) {
                            throw;
                        } catch (Exception e) {
                            throw ReportAnyErrors(responseBody, statusCode, e);
                        }
                    }
                } catch (Exception e) {
                    throw new AmazonSQSException("failed on server response:\n" + responseBody, e);
                }
            } while (shouldRetry);

            return response;
        }

        static AmazonSQSException ReportAnyErrors(string body, HttpStatusCode status, Exception e) {
            if (body != null && body.StartsWith("<")) {
                var errorMatcherOne = Regex.Match(body, "<RequestId>(.*)</RequestId>.*<Error>" +
                        "<Code>(.*)</Code><Message>(.*)</Message></Error>.*(<Error>)?", RegexOptions.Multiline);
                var errorMatcherTwo = Regex.Match(body, "<Error><Code>(.*)</Code><Message>(.*)" +
                        "</Message></Error>.*(<Error>)?.*<RequestID>(.*)</RequestID>", RegexOptions.Multiline);
                if (errorMatcherOne.Success) {
                    var requestId = errorMatcherOne.Groups[1].Value;
                    var code = errorMatcherOne.Groups[2].Value;
                    var message = errorMatcherOne.Groups[3].Value;
                    return new AmazonSQSException(message, status, code, "Unknown", requestId, body);
                }
                if (errorMatcherTwo.Success) {
                    var code = errorMatcherTwo.Groups[1].Value;
                    var message = errorMatcherTwo.Groups[2].Value;
                    var requestId = errorMatcherTwo.Groups[4].Value;
                    return new AmazonSQSException(message, status, code, "Unknown", requestId, body);
                }
                return new AmazonSQSException("Internal Error no match error from response " + body, e);
            }
            return new AmazonSQSException("Internal Error", e);
        }

        void PauseOnRetry(int retries, HttpStatusCode status) {
            if (retries > config.MaxErrorRetry) 
                throw new AmazonSQSException("Maximum number of retry attempts reached : " + (retries - 1), status);
            System.Threading.Thread.Sleep((int) Math.Pow(4, retries) * 100);
        }

        void AddRequiredParameters(IDictionary<string, string> parameters) {
            parameters.Add("AWSAccessKeyId", awsAccessKeyId);
            parameters.Add("Timestamp", GetFormattedTimestamp());
            parameters.Add("Version", config.ServiceVersion);
            parameters.Add("SignatureVersion", config.SignatureVersion);
            parameters.Add("Signature", SignParameters(parameters, awsSecretAccessKey));
        }

        static string GetParametersAsString(IDictionary<string, string> parameters) {
            var data = new StringBuilder();
            foreach (var key in parameters.Keys) {
                var value = parameters[key];
                if (string.IsNullOrEmpty(value)) continue;
                data.Append(key);
                data.Append('=');
                data.Append(HttpUtility.UrlEncode(value, Encoding.UTF8));
                data.Append('&');
            }
            var stringData = data.ToString();
            if (stringData.EndsWith("&")) stringData = stringData.Remove(stringData.Length - 1, 1);
            return stringData;
        }

        static string SignParameters(IDictionary<string, string> parameters, string key)
        {
            var signatureVersion = parameters["SignatureVersion"];
            var data = new StringBuilder();
            if ("0".Equals(signatureVersion)) 
                data.Append(parameters["Action"]).Append(parameters["Timestamp"]);
            else if ("1".Equals(signatureVersion)) {
                var ignoreCase = StringComparer.InvariantCultureIgnoreCase;
                var sorted = new SortedDictionary<string, string>(parameters, ignoreCase);
                parameters.Remove("Signature");
                foreach (var pair in sorted) {
                    if (string.IsNullOrEmpty(pair.Value)) continue;
                    data.Append(pair.Key);
                    data.Append(pair.Value);
                }
            } else {
                throw new Exception("Invalid Signature Version specified");
            }
            return Sign(data.ToString(), key);
        }

        static string Sign(string data, string key) {
            Encoding encoding = new UTF8Encoding();
            var signature = new HMACSHA1(encoding.GetBytes(key));
            return Convert.ToBase64String(signature.ComputeHash(encoding.GetBytes(data.ToCharArray())));
        }

        static string GetFormattedTimestamp() {
            var now = DateTime.Now;
            return new DateTime(
                now.Year, now.Month, now.Day, now.Hour, now.Minute, now.Second, now.Millisecond, 
                DateTimeKind.Local
            ).ToUniversalTime().ToString("yyyy-MM-dd\\THH:mm:ss.fff\\Z", CultureInfo.InvariantCulture);
        }

        static IDictionary<string, string> ConvertCreateQueue(CreateQueue request) {
            var parameters = new Dictionary<string, string> {{"Action", "CreateQueue"}};
            if (request.IsSetQueueName()) 
                parameters.Add("QueueName", request.QueueName);
            if (request.IsSetDefaultVisibilityTimeout()) 
                parameters.Add("DefaultVisibilityTimeout", request.DefaultVisibilityTimeout + "");
            return parameters;
        }

        static IDictionary<string, string> ConvertListQueues(ListQueues request) {
            var parameters = new Dictionary<string, string> {{"Action", "ListQueues"}};
            if (request.IsSetQueueNamePrefix()) 
                parameters.Add("QueueNamePrefix", request.QueueNamePrefix);
            return parameters;
        }

        static IDictionary<string, string> ConvertDeleteMessage(DeleteMessage request) {
            var parameters = new Dictionary<string, string> {{"Action", "DeleteMessage"}};
            if (request.IsSetQueueName()) parameters.Add("QueueName", request.QueueName);
            if (request.IsSetReceiptHandle()) parameters.Add("ReceiptHandle", request.ReceiptHandle);
            return parameters;
        }

        static IDictionary<string, string> ConvertDeleteQueue(DeleteQueue request) {
            var parameters = new Dictionary<string, string> {{"Action", "DeleteQueue"}};
            if (request.IsSetQueueName()) parameters.Add("QueueName", request.QueueName);
            return parameters;
        }
        
        static IDictionary<string, string> ConvertGetQueueAttributes(GetQueueAttributes request) {
            var parameters = new Dictionary<string, string> {{"Action", "GetQueueAttributes"}};
            if (request.IsSetQueueName()) parameters.Add("QueueName", request.QueueName);
            var attributeNameList  =  request.AttributeName;
            foreach (var attributeName in attributeNameList) {
                var key = "AttributeName" + "." + (attributeNameList.IndexOf(attributeName) + 1);
                parameters.Add(key, attributeName);
            }
            return parameters;
        }
        
        static IDictionary<string, string> ConvertReceiveMessage(ReceiveMessage request) {
            var parameters = new Dictionary<string, string> {{"Action", "ReceiveMessage"}};
            if (request.IsSetQueueName()) parameters.Add("QueueName", request.QueueName);
            if (request.IsSetMaxNumberOfMessages()) 
                parameters.Add("MaxNumberOfMessages", request.MaxNumberOfMessages + "");
            if (request.IsSetVisibilityTimeout()) 
                parameters.Add("VisibilityTimeout", request.VisibilityTimeout + "");
            return parameters;
        }

        static IDictionary<string, string> ConvertSendMessage(SendMessage request) {
            var parameters = new Dictionary<string, string> {{"Action", "SendMessage"}};
            if (request.IsSetQueueName()) parameters.Add("QueueName", request.QueueName);
            if (request.IsSetMessageBody()) parameters.Add("MessageBody", request.MessageBody);
            return parameters;
        }
        
        static IDictionary<string, string> ConvertSetQueueAttributes(SetQueueAttributes request) {
            var parameters = new Dictionary<string, string> {{"Action", "SetQueueAttributes"}};
            if (request.IsSetQueueName()) parameters.Add("QueueName", request.QueueName);
            var attributeList = request.Attribute;
            foreach (var attribute in attributeList) {
                if (attribute.IsSetName()) {
                    var key = "Attribute" + "." + (attributeList.IndexOf(attribute) + 1) + "." + "Name";
                    parameters.Add(key, attribute.Name);
                }
                if (!attribute.IsSetValue()) continue;
                var value = "Attribute" + "."  +(attributeList.IndexOf(attribute) + 1) + "." + "Value";
                parameters.Add(value, attribute.Value);
            }

            return parameters;
        }
        
                                                                                                
    }
}