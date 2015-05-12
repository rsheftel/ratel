package amazon;

import static util.Errors.bombNotNull;
import static util.Errors.bombNull;
import static util.Times.sleep;

import java.util.List;

import amazon.MetaBucket.Key;
import db.Db;

public class S3Cache {
    private final MetaBucket bucket;
    private final SqsQ dbQ;
    
    private static boolean[] defaultSqsDbMode = { false };
    private final static ThreadLocal<Boolean> sqsDbMode = new ThreadLocal<Boolean>() {
        @Override protected Boolean initialValue() {
            return defaultSqsDbMode[0];
        }
    };
    public static boolean saveAllQueryResultsToS3;
    private static S3Cache s3cache;
    
    public S3Cache(String name) {
        name = "quantys-" + name;
        bucket = new MetaBucket(name);
        dbQ = new SqsQ(name.replaceAll("\\.", "_"));
    }

    
    public void createBucket() {
        bucket.create();
    }

    
    public void drainRequestQueue() {
        dbQ.drain();
    }


    public SqsDbServer startDbServer() {
        SqsDbServer server = new SqsDbServer(this);
        server.start();
        return server;
    }


    public List<Message> messagesBlocking() {
        return dbQ.messagesBlocking();
    }

    public MetaBucket bucket() {
        return bucket;
    }

    @SuppressWarnings("unchecked") public <T> T retrieve(S3Cacheable<T> file) {
        Key keyToRead = file.key(bucket());
        T deserialized = (T) keyToRead.readOrNull();
        if(deserialized != null)
            return deserialized;
        return response(file, keyToRead);
    }

    @SuppressWarnings("unchecked") private <T> T response(S3Cacheable<T> file, Key keyToRead) {
        T deserialized = null;
        dbQ.send(file);
        int attempts = 0;
        while(deserialized == null && attempts++  < 300) {
            deserialized = (T) keyToRead.readOrNull();
            if(deserialized == null)
                sleep(100);
        }
        bombNull(deserialized, "no response within 30 seconds from sqsdb server\n" + file);
        return deserialized;
    }



    public Key key(S3Cacheable<?> key) {
        return key.key(bucket());
    }

    public static void beInSqsDbMode(boolean b) {
        Db.beInReadOnlyMode(b);
        sqsDbMode.set(b);
    }
    
    public static void setDefaultSqsDbMode(boolean b) {
        defaultSqsDbMode[0] = b;
    }
    
    public static void saveAllQueryResultsToS3(boolean b) {
        saveAllQueryResultsToS3 = b;
    }

    public static S3Cache s3cache() {
        return bombNull(s3cache, "no s3 cache set");
    }
    
    public static void setS3Cache(S3Cache newCache) {
        if (newCache == null) s3cache = null;
        bombNotNull(s3cache, "did not expect cache to be populated when setting S3 cache");
        s3cache = newCache;
    }
    
    public static boolean sqsDbMode() {
        return sqsDbMode.get();
    }
    
    public static <T> T saveResultsIfNeeded(S3Cacheable<T> key, T results) {
        if(S3Cache.saveAllQueryResultsToS3) s3cache().key(key).write(results);
        return results;
    }

}
