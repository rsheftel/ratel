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
    public class DeleteMessage {
        string queueNameField;
        string receiptHandleField;
        
        [XmlElementAttribute(ElementName = "QueueName")]
        public string QueueName {
            get { return queueNameField ; }
            set { queueNameField = value; }
        }

        public DeleteMessage WithQueueName(string queueName) {
            queueNameField = queueName;
            return this;
        }

        public Boolean IsSetQueueName() {
            return queueNameField != null;
        }

        [XmlElementAttribute(ElementName = "ReceiptHandle")]
        public string ReceiptHandle {
            get { return receiptHandleField; }
            set { receiptHandleField = value; }
        }
        
        public DeleteMessage WithReceiptHandle(string receiptHandle) {
            receiptHandleField = receiptHandle;
            return this;
        }

        public Boolean IsSetReceiptHandle() {
            return receiptHandleField != null;
        }
    }
}