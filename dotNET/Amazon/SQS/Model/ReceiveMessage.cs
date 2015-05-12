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
using System.Xml.Serialization;
namespace Amazon.SQS.Model {
    [XmlTypeAttribute(Namespace = "http://queue.amazonaws.com/doc/2008-01-01/")]
    [XmlRootAttribute(Namespace = "http://queue.amazonaws.com/doc/2008-01-01/", IsNullable = false)]
    public class ReceiveMessage {
        private String queueNameField;
        private Decimal? maxNumberOfMessagesField;
        private Decimal? visibilityTimeoutField;

        [XmlElementAttribute(ElementName = "QueueName")]
        public String QueueName {
            get { return queueNameField; }
            set { queueNameField = value; }
        }

        public ReceiveMessage WithQueueName(String queueName) {
            queueNameField = queueName;
            return this;
        }

        public Boolean IsSetQueueName() {
            return queueNameField != null;
        }

        [XmlElementAttribute(ElementName = "MaxNumberOfMessages")]
        public Decimal MaxNumberOfMessages {
            get { return maxNumberOfMessagesField.GetValueOrDefault(); }
            set { maxNumberOfMessagesField = value; }
        }

        public ReceiveMessage WithMaxNumberOfMessages(Decimal maxNumberOfMessages) {
            maxNumberOfMessagesField = maxNumberOfMessages;
            return this;
        }

        public Boolean IsSetMaxNumberOfMessages() {
            return maxNumberOfMessagesField.HasValue;
        }

        [XmlElementAttribute(ElementName = "VisibilityTimeout")]
        public Decimal VisibilityTimeout {
            get { return visibilityTimeoutField.GetValueOrDefault(); }
            set { visibilityTimeoutField = value; }
        }

        public ReceiveMessage WithVisibilityTimeout(Decimal visibilityTimeout) {
            visibilityTimeoutField = visibilityTimeout;
            return this;
        }

        public Boolean IsSetVisibilityTimeout() {
            return visibilityTimeoutField.HasValue;
        }
    }
}