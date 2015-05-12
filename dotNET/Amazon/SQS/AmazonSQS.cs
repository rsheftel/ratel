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

using Amazon.SQS.Model;

namespace Amazon.SQS {
    public interface AmazonSQS {
        CreateQueueResponse CreateQueue(CreateQueue request);
        ListQueuesResponse ListQueues(ListQueues request);
        DeleteMessageResponse DeleteMessage(DeleteMessage request);
        DeleteQueueResponse DeleteQueue(DeleteQueue request);
        GetQueueAttributesResponse GetQueueAttributes(GetQueueAttributes request);
        ReceiveMessageResponse ReceiveMessage(ReceiveMessage request);
        SendMessageResponse SendMessage(SendMessage request);
        SetQueueAttributesResponse SetQueueAttributes(SetQueueAttributes request);
    }
}