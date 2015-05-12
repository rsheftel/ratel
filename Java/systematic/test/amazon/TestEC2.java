package amazon;

import static util.Log.*;

import java.io.*;
import java.util.*;

import amazon.MetaBucket.*;
import db.*;
import file.*;

public class TestEC2 extends DbTestCase {
    public void testNothing() throws Exception {}

    
    public void testS3Upload() throws Exception {
        EC2Runner runner = new EC2Runner("0");
        runner.uploadJarsAndQRunToS3();
        info("upload complete");
        List<Key> keys = runner.bucket().keys(EC2Runner.SUPPORTING_FILES);
        Key systematic = runner.bucket().key(EC2Runner.SUPPORTING_FILES, "Java/systematic/lib/systematic.jar");
        Key qrun = runner.bucket().key(EC2Runner.SUPPORTING_FILES, "dotNET/QRun/bin/Release/QRun.exe");
        assertContains(systematic, keys);
        assertContains(qrun, keys);
    }
    
    public void slowtestS3Download() throws Exception {
        EC2Runner runner = new EC2Runner("test");
        runner.downloadJarsAndQRunFromS3(new QDirectory("C:\\testS3"));
    }
    
    public static class TestRequest implements Serializable {

        private static final long serialVersionUID = 1L;
        private final String responseQueue;

        public TestRequest(String responseQueue) {
            this.responseQueue = responseQueue;
        }
        
        public void sendResponse() {
            new SqsQ(responseQueue).send("new4");
        }
        
    }
}
