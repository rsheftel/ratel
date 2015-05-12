package amazon;

import static util.Errors.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Strings.*;
import static util.Systematic.*;

import java.util.*;

import org.apache.commons.codec.binary.*;
import org.apache.commons.httpclient.methods.*;

import util.*;
import util.web.*;
import amazon.MetaBucket.*;
import amazon.monitor.*;
import file.*;

public class EC2Runner {

    private static final String AMI_ID = "ami-fbf31292";
    static final String SUPPORTING_FILES = "supportingFiles.";
    private final SqsQ requestQueue;
    private final List<Instance> instances = empty();
    private final S3Cache s3Cache;
    private final SqsQ responseQueue;
    private List<Message> messages = empty();
	private final CloudMonitor monitor;

    public EC2Runner(String requestQueueName) {
        requestQueue = new SqsQ(requestQueueName);
        responseQueue = new SqsQ("response-" + requestQueue.name());
        String systemId = requestQueueName.replaceAll("-.*", "");
		s3Cache = new S3Cache(systemId);
		List<Instance> empty = empty();
        monitor = new CloudMonitor(Integer.parseInt(systemId), empty);
    }
    
    public static EC2Runner fromUserData() {
        return new EC2Runner(userData().get("requestQueue"));
    }

    public void stopInstances() {
        stopInstances(0);
    }
    
    public void stopInstances(int numToLeaveUp) {
        for (Instance instance : instances) {
            if (numToLeaveUp-- > 0) info("left instance up: " + instance);
            else instance.shutdown();
        }
    }
    
    public String requestQueueName() {
        return requestQueue.name();
    }

    public void request(Object request) {
        requestQueue.send(request);
    }
    
    public Message nextMessage(int visibilityTimeout) {
        if(isEmpty(messages))
            messages.addAll(requestQueue.messagesBlocking(1, visibilityTimeout));
        return messages.remove(0);
    }
    

    public void startInstances(int numInstances, String type, String commandLine, int numProcs) {
        uploadJarsAndQRunToS3();
        info("jars and dll uploaded.  Starting instances - " + requestQueue.name());
        for(int i = 0; i < numInstances; i += 25) {
            try {
                Map<String, String> params = map("command", commandLine, "numProcs", "" + numProcs, "requestQueue", requestQueue.name());
                List<Instance> current = startInstances(Math.min(25, numInstances - i), type, AMI_ID, params);
                instances.addAll(current);
            } catch (RuntimeException e) {
                if (i == 0 || !Log.errMessage(e).contains("InsufficientInstanceCapacity")) throw e;
                break;
            }
        }
        monitor.add(instances);
        info("instances started.");
    }
    
    public void uploadJarsAndQRunToS3() {
        MetaBucket bucket = bucket();
        bucket.create();
        bucket.clear(SUPPORTING_FILES);
        writeFiles(bucket, mainDir().directory(JAVA_LIB_PARTS));
        QDirectory targetDirectory = mainDir().directory(QRUN_PARTS);
		QDirectory sourceDirectory = new QDirectory(targetDirectory.path().replaceAll("Release", "Debug"));
		writeFiles(bucket, sourceDirectory, targetDirectory);
    }
    
    private void writeFiles(MetaBucket bucket, QDirectory dir) {
    	writeFiles(bucket, dir, dir);
    }

    private void writeFiles(MetaBucket bucket, QDirectory from, QDirectory to) {
        for (QFile file : from.files())
            key(bucket, to.file(file.name())).write(file.bytes());
    }

    private Key key(MetaBucket bucket, QFile file) {
        String repositoryPath = file.path().substring(mainDir().path().length()+1);
        return bucket.key(SUPPORTING_FILES, repositoryPath.replaceAll("\\\\", "/"));
    }

    public MetaBucket bucket() {
        return s3Cache().bucket();
    }
    
    public S3Cache s3Cache() {
        return s3Cache;
    }
    
    public SqsQ responseQueue() {
        return responseQueue;
    }
    
    public void received(STOResponse response) {
    	monitor.received(response);
    }
    
    public void downloadJarsAndQRunFromS3(QDirectory target) {
        target.clear();
        MetaBucket bucket = bucket();
        List<MetaBucket.Key> keys = bucket.keys(SUPPORTING_FILES);
        for (MetaBucket.Key key : keys) {
            String fileName = key.keyName();
            QFile file = target.file(fileName);
            file.parent().createIfMissing();
            file.create((byte[]) key.read());
        }
    }

    private static List<Instance> startInstances(int numInstances, String type, String imageId, Map<String, String> userData) {
        Tag response = AmazonEC2Request.response("RunInstances", map(
            "ImageId", imageId,
            "MinCount", "" + numInstances,
            "MaxCount", "" + numInstances,
            "SecurityGroup.1", "default",
            "InstanceType", type, 
            "UserData", new String(Base64.encodeBase64(serialize(userData).getBytes()))
        ));
        List<Instance> result = empty();
        for (Tag item : response.child("instancesSet").children("item"))
            result.add(new Instance(item.text("instanceId")));
        return result;
    }
    
    @SuppressWarnings("unchecked") public static Map<String, String> userData() {
        GetMethod method = new GetMethod("http://169.254.169.254/latest/user-data");
        int statusCode = -1;
        try {
            statusCode = QHttpClient.client.executeMethod(method);
            String userDataString = QFile.text(method.getResponseBodyAsStream());
            return (Map<String, String>) deserialize(userDataString);
        } catch (Exception e) {
            throw bomb("exception while processing response from user data query\nstatus code: " + statusCode, e);
        }
    }
    
    public static String instanceId() {
    	GetMethod method = new GetMethod("http://169.254.169.254/latest/meta-data/instance-id");
        int statusCode = -1;
        try {
            statusCode = QHttpClient.client.executeMethod(method);
            return QFile.text(method.getResponseBodyAsStream());
        } catch (Exception e) {
            throw bomb("exception while processing response from instance id query\nstatus code: " + statusCode, e);
        }
    }

    
}