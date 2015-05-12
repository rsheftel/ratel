package amazon;

import util.web.*;
import jms.*;

public abstract class S3CacheableTestCase extends JMSTestCase {

    protected SqsDbServer server;

    public S3CacheableTestCase() {
        super();
    }

    @Override public void setUp() throws Exception {
        super.setUp();
        S3Cache cache = new S3Cache("test4");
        server = cache.startDbServer();
        S3Cache.setS3Cache(cache);
        S3Cache.beInSqsDbMode(true);
    }

    @Override public void tearDown() throws Exception {
        server.stopServer();
        S3Cache.s3cache().bucket().clear(null);
        S3Cache.beInSqsDbMode(false);
        S3Cache.setS3Cache(null);
        SqsDbServer.reset();
        QHttpClient.turnOnProxy();
        super.tearDown();
    }

}