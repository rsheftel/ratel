package amazon;

import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import amazon.MetaBucket.*;

public class SqsDbServer extends Thread {

    private boolean stopped;
    private static Map<Key, Boolean> keysAlreadyProcessed = emptyMap();
    private static S3Cache cache;
    public static void reset() {
        cache = null;
        keysAlreadyProcessed.clear();
    }

    public SqsDbServer(S3Cache cache) {
        if (SqsDbServer.cache != null && SqsDbServer.cache != cache) 
            throw bomb("can't create SqsDbServers against multiple caches yet!");
        SqsDbServer.cache = cache;
    }

    @Override public void run()  {
        while(!stopped) {
            List<Message> messages = cache.messagesBlocking();
            for (Message message : messages) {
            	Object deserialized = message.object();
                S3Cacheable<?> cacheable = (S3Cacheable<?>) deserialized;
                MetaBucket.Key keyToWrite = cache.key(cacheable);
                try {
                    if(!keyProcessed(keyToWrite)) 
                        keyToWrite.write(cacheable.response());
                    message.delete();
                } catch (RuntimeException e) {
                    removeKeyProcessed(keyToWrite);
                    throw e;
                }
            }
        }
    }

    private static synchronized void removeKeyProcessed(Key keyToWrite) {
        keysAlreadyProcessed.remove(keyToWrite);
    }

    private static synchronized boolean keyProcessed(Key keyToWrite) {
        if(keysAlreadyProcessed.containsKey(keyToWrite))
            return true;
        keysAlreadyProcessed.put(keyToWrite, true);
        return false;
    }

    public void stopServer() {
        stopped = true;
    }
    


}