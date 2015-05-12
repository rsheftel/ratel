// This software code is made available "AS IS" without warranties of any 
// kind.  You may copy, display, modify and redistribute the software
// code either by itself or as incorporated into your code; provided that
// you do not remove any proprietary notices.  Your use of this software
// code is at your own risk and you waive any claim against Amazon Web 
// Services LLC or its affiliates with respect to your use of this software 
// code. (c) Amazon Web Services LLC or its affiliates.

using System;
using System.Collections.Generic;

namespace Amazon.EC2 {
   public class EC2Operation {
        readonly string _AWSAccessKeyId;
        readonly string _AWSSecretAccessKey;

        const string DEFAULT_VERSION = "2007-01-19";  
        const string DEFAULT_SIGNATURE_VERSION = "1";
        const string DEFAULT_HOST = "ec2.amazonaws.com";
        const string DEFAULT_PROTOCOL = "http";
        
        readonly string _Version;
        readonly string _SignatureVersion;
        readonly string _Host;
        readonly string _Protocol;
        
        public EC2Operation(string accessKeyId, string secretAccessKey)
          : this(accessKeyId, secretAccessKey, DEFAULT_VERSION, DEFAULT_SIGNATURE_VERSION, DEFAULT_HOST, DEFAULT_PROTOCOL) {}

        public EC2Operation(string accessKeyId, string secretAccessKey, string version)
          : this(accessKeyId, secretAccessKey, version, DEFAULT_SIGNATURE_VERSION, DEFAULT_HOST, DEFAULT_PROTOCOL) {}

        public EC2Operation(string accessKeyId, string secretAccessKey, string version, string sigVersion)
          : this(accessKeyId, secretAccessKey, version, sigVersion, DEFAULT_HOST, DEFAULT_PROTOCOL) {}

        public EC2Operation(string accessKeyId, string secretAccessKey, string version, string sigVersion, string host)
          : this(accessKeyId, secretAccessKey, version, sigVersion, host, DEFAULT_PROTOCOL) {}

        public EC2Operation(string accessKeyId, string secretAccessKey, string version, string sigVersion, string host, string protocol) {
            _Version = version;
            _SignatureVersion = sigVersion;
            _Host = host;
            _Protocol = protocol;
            _AWSAccessKeyId = accessKeyId;
            _AWSSecretAccessKey = secretAccessKey;
        }
        
        EC2Request CreateEC2Request() {
            return new EC2Request(_AWSAccessKeyId, _AWSSecretAccessKey, _SignatureVersion, _Version, _Host, _Protocol);
        }
        
        
        #region Images : DescribeImages, RegisterImages, DeregisterImages 
        
        public EC2Response DescribeImages(IList<string> imageIds, IList<string> owners, IList<string> executableBy) {
            var req = CreateEC2Request();
            req.AddParameters("ImageId", imageIds);
            req.AddParameters("Onwer", owners);
            req.AddParameters("ExecutableBy", executableBy);
            return req.Execute("DescribeImages");
        }
        
        public EC2Response RegisterImage(string imageLocation) {
            if (string.IsNullOrEmpty(imageLocation))
                throw new ArgumentNullException("Image Location can't be null or empty"); 
                
            var req = CreateEC2Request();
            req.AddParameter("ImageLocation", imageLocation);
            return req.Execute("RegisterImage");
        }

        public EC2Response DeregisterImage(string imageId) {
            if (string.IsNullOrEmpty(imageId))
                throw new ArgumentNullException("Image Id can't be null or empty");

            var req = CreateEC2Request();
            req.AddParameter("ImageId", imageId);
            return req.Execute("DeregisterImage");
        }
        
        #endregion 
       
        #region Instances : DescribeInstances, RunInstances, TerminateInstances, RebootInstances 

        public EC2Response DescribeInstances(string instanceId) {
            var req = CreateEC2Request();
            req.AddParameter("InstanceId", instanceId);
            return req.Execute("DescribeInstances");
        }
        
        public EC2Response RunInstances(string imageId, int minCount, int maxCount, string keyName, 
            string userData, string securityGroup) {
            var req = CreateEC2Request();
            req.AddParameter("ImageId", imageId);
            req.AddParameter("MinCount", minCount.ToString());
            req.AddParameter("MaxCount", maxCount.ToString());
            req.AddParameter("KeyName", keyName);
            req.AddParameter("UserData", userData);
            req.AddParameter("SecurityGroup", securityGroup);
            return req.Execute("RunInstances");
        }
    
        public EC2Response TerminateInstances(string instanceId) {
            if (string.IsNullOrEmpty(instanceId)) 
                throw new ArgumentNullException("Instance ID can't be null or empty");

            var req = CreateEC2Request();
            req.AddParameter("InstanceId", instanceId);
            return req.Execute("TerminateInstances");
        }

        public EC2Response RebootInstances(string instanceId) {
            if (string.IsNullOrEmpty(instanceId))
                throw new ArgumentNullException("InstanceId can't be null or empty"); 
            
            var req = CreateEC2Request();
            req.AddParameter("InstanceId", instanceId);
            return req.Execute("RebootInstances");                        
        }

        #endregion 


        

        #region Keypairs : CreateKeyPair, DescribeKeyPairs, DeleteKeypair 

        public EC2Response CreateKeyPair(string keyName) {
            if (string.IsNullOrEmpty(keyName))
                throw new ArgumentNullException("KeyName can't be null or empty");
            
            var req = CreateEC2Request();
            req.AddParameter("KeyName", keyName);
            return req.Execute("CreateKeyPair");                  
        }

        public EC2Response DeleteKeyPair(string keyName) {
            if (string.IsNullOrEmpty(keyName))
                throw new ArgumentNullException("KeyName can't be null or empty");

            var req = CreateEC2Request();
            req.AddParameter("KeyName", keyName);
            return req.Execute("DeleteKeyPair");    
        }

        public EC2Response DescribeKeyPairs(string keyName) {
            var req = CreateEC2Request();
            req.AddParameter("KeyName", keyName);
            return req.Execute("DescribeKeyPairs");                       
        }

        #endregion 
                
        #region Image Attributes : ModifyImageAttribute, DescribeImageAttribute, ResetImageAttribute 

        public EC2Response ModifyImageAttribute(
            string imageId, string attribute, string operationType, string userId, string userGroup
        ) {
            if (string.IsNullOrEmpty(imageId))
                throw new ArgumentNullException("ImageId can't be null or empty");
            if (string.IsNullOrEmpty(attribute))
                throw new ArgumentNullException("Attribute can't be null or empty");
            if (string.IsNullOrEmpty(operationType))
                throw new ArgumentNullException("OperationType can't be null or empty"); 

            var req = CreateEC2Request();
            req.AddParameter("ImageId", imageId);
            req.AddParameter("Attribute", attribute);
            req.AddParameter("OperationType", operationType);
            req.AddParameter("UserId", userId);
            req.AddParameter("UserGroup", userGroup);
            return req.Execute("ModifyImageAttribute");                       
        }

        public EC2Response DescribeImageAttribute(string imageId, string attribute) {
            if (string.IsNullOrEmpty(imageId))
                throw new ArgumentNullException("ImageId can't be null or empty");
            if (string.IsNullOrEmpty(attribute))
                throw new ArgumentNullException("Attribute can't be null or empty");                       
            
            var req = CreateEC2Request();
            req.AddParameter("ImageId", imageId);
            req.AddParameter("Attribute", attribute);
            return req.Execute("DescribeImageAttribute");                
        }

        public EC2Response ResetImageAttribute(string imageId, string attribute) {
            if (string.IsNullOrEmpty(imageId))
                throw new ArgumentNullException("ImageId can't be null or empty");
            if (string.IsNullOrEmpty(attribute))
                throw new ArgumentNullException("Attribute can't be null or empty");

            var req = CreateEC2Request();
            req.AddParameter("ImageId", imageId);
            req.AddParameter("Attribute", attribute);
            return req.Execute("ResetImageAttribute");              
        }

        #endregion
        #region Security Groups : AuthorizeSecurityGroupIngress, RevokeSecurityGroupIngress, CreateSecurityGroup, DescribeSecurityGroups, DeleteSecurityGroup 
       
        public EC2Response AuthorizeSecurityGroupIngress(
            string groupName, string sourceSecurityGroupName, string sourceSecurityGroupOwnerId,
            string ipProtocol, string fromPort, string toPort, string cidrIp
        ) {
            if (string.IsNullOrEmpty(groupName))
                throw new ArgumentNullException("GroupName can't be null or empty");
            
            var req = CreateEC2Request();
            req.AddParameter("GroupName", groupName);
            req.AddParameter("SourceSecurityGroupName", sourceSecurityGroupName);
            req.AddParameter("SourceSecurityGroupOwnerId", sourceSecurityGroupOwnerId);
            req.AddParameter("IpProtocol", ipProtocol);
            req.AddParameter("FromPort", fromPort);
            req.AddParameter("ToPort", toPort);
            req.AddParameter("CidrIp", cidrIp);
            return req.Execute("AuthorizeSecurityGroupIngress");        
        }

        public EC2Response RevokeSecurityGroupIngress(
            string groupName, string sourceSecurityGroupName, string sourceSecurityGroupOwnerId, 
            string ipProtocol, string fromPort, string toPort, string cidrIp
        ) {
            if (string.IsNullOrEmpty(groupName))
                throw new ArgumentNullException("GroupName can't be null or empty");

            var req = CreateEC2Request();
            req.AddParameter("GroupName", groupName);
            req.AddParameter("SourceSecurityGroupName", sourceSecurityGroupName);
            req.AddParameter("SourceSecurityGroupOwnerId", sourceSecurityGroupOwnerId);
            req.AddParameter("IpProtocol", ipProtocol);
            req.AddParameter("FromPort", fromPort);
            req.AddParameter("ToPort", toPort);
            req.AddParameter("CidrIp", cidrIp);
            return req.Execute("RevokeSecurityGroupIngress");            
        }

        public EC2Response CreateSecurityGroup(string groupName, string groupDescription) {
            if (string.IsNullOrEmpty(groupName))
                throw new ArgumentNullException("GroupName can't be null or empty");
            if (string.IsNullOrEmpty(groupDescription))
                throw new ArgumentNullException("GroupDescription can't be null or empty");
            
            var req = CreateEC2Request();
            req.AddParameter("GroupName", groupName);
            req.AddParameter("GroupDescription", groupDescription);           
            return req.Execute("CreateSecurityGroup");     
        }

        public EC2Response DescribeSecurityGroups(string groupName) {
            var req = CreateEC2Request();
            req.AddParameter("GroupName", groupName);
            return req.Execute("DescribeSecurityGroups");     
        }
       
        public EC2Response DeleteSecurityGroup(string groupName) {
            if (string.IsNullOrEmpty(groupName))
                throw new ArgumentNullException("GroupName can't be null or empty");

            var req = CreateEC2Request();
            req.AddParameter("GroupName", groupName);
            return req.Execute("DeleteSecurityGroup");    
        }

        #endregion
        
        #region GetConsoleOutput 
       
        public EC2Response GetConsoleOutput(string instanceId) {
            if (string.IsNullOrEmpty(instanceId))
                throw new ArgumentNullException("InstanceId can't be null or empty");
            
            var req = CreateEC2Request();
            req.AddParameter("InstanceId", instanceId);
            return req.Execute("GetConsoleOutput");
        }

        #endregion
    }
}
