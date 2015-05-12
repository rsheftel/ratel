package amazon;

import static systemdb.metadata.SystemDetailsTable.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Sequence.*;
import static util.Strings.*;
import static util.Times.*;

import java.util.*;

import systemdb.metadata.SystemDetailsTable.*;
import amazon.AmazonS3Request.*;

public class MetaBucket {
    private static final int NUM_THREADS = 10;
    private static final String BUCKET_NAME = "quantys-data";

    private static abstract class RangeLister<T> extends Thread {
        private final int index;
        private final List<List<T>> results;
        private final int rangeSize;

        private RangeLister(int index, List<List<T>> results, int rangeSize) {
            this.index = index;
            this.results = results;
            this.rangeSize = rangeSize;
        }

        @Override public void run() {
            List<T> result = empty();
            results.add(result);
            int start = index * rangeSize;
            int end = start + rangeSize;
            for(int partition = start; partition < end; partition++) {
                String withZeros = leftZeroPad(partition, 3);
                String fullPrefix = withZeros + ".";
                populate(fullPrefix, result);
            }
        }

        protected abstract void populate(String fullPrefix, List<T> result);
    }

    public Thread startedKeyLister(int i, final String prefix, List<List<Key>> results, int rangeSize) {
        Thread thread = new RangeLister<Key>(i, results, rangeSize) {
            @Override protected void populate(String fullPrefix, List<Key> result) {
                String prefixOrEmpty = prefix == null ? "" : prefix;
                String fullFullPrefix = fullPrefix + name + "/" + prefixOrEmpty;
                List<KeyResult> keys = AmazonS3Request.keys(BUCKET_NAME, fullFullPrefix);
                for (KeyResult key : keys)
                    result.add(key(prefix, key.name.substring(fullFullPrefix.length()), key));
            }
        };
        thread.start();
        return thread;
    }
    
    public static Thread startedBucketLister(int i, List<List<MetaBucket>> results, int rangeSize) {
        Thread thread = new RangeLister<MetaBucket>(i, results, rangeSize) {
            @Override protected void populate(String fullPrefix, List<MetaBucket> result) {
                List<String> buckets = AmazonS3Request.bucketNames(BUCKET_NAME, fullPrefix, "/");
                for (String bucket : buckets)
                    result.add(new MetaBucket(bucket));
            }
        };
        thread.start();
        return thread;
    }

    public static class Key {
        private static final int NUM_PARTITIONS = 1000;
        private final String prefix;
        private final String keyName;
        private final MetaBucket bucket;
        private final KeyResult metadata;
        public Key(MetaBucket bucket, String prefix, String keyName, KeyResult metadata) {
            this.bucket = bucket;
            this.prefix = prefix;
            this.keyName = keyName;
            this.metadata = metadata;
        }
        
        public void write(Object data) {
            AmazonS3Request.put(BUCKET_NAME, key(), data);
        }
        
        public void write(byte[] bytes) {
            write((Object) bytes);
        }

        public Object read() {
        	if(verbose()) info("reading " + BUCKET_NAME + ":" + key());
            return AmazonS3Request.get(BUCKET_NAME, key());
        }
        
        public Object read(int timeoutMillis) {
            Date end = millisAhead(timeoutMillis, now());
            while (isBeforeNow(end)) {
                Object result = readOrNull();
                if (result != null) return result;
                sleep(50);
            }
            throw bomb("no value found for " + sQuote(key()));
        }

        public Object readOrNull() {
            try {
                return read();
            } catch (RuntimeException e) {
                if(e.getMessage().matches("(?s).*specified key does not exist.*"))
                    return null;
                throw e;
            }
        }

        public void delete() {
            AmazonS3Request.delete(BUCKET_NAME, key());
        }
        
        private String key() {
            String rawKey = prefix == null ? keyName : prefix + keyName;
            rawKey = bucket.name + "/" + rawKey;
            rawKey = leftZeroPad(Math.abs(rawKey.hashCode()) % NUM_PARTITIONS, 3) + "." + rawKey;
            return rawKey;
        }

        public String keyName() {
            return keyName;
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((bucket == null) ? 0 : bucket.hashCode());
            result = prime * result + ((keyName == null) ? 0 : keyName.hashCode());
            result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            final Key other = (Key) obj;
            if (bucket == null) {
                if (other.bucket != null) return false;
            } else if (!bucket.equals(other.bucket)) return false;
            if (keyName == null) {
                if (other.keyName != null) return false;
            } else if (!keyName.equals(other.keyName)) return false;
            if (prefix == null) {
                if (other.prefix != null) return false;
            } else if (!prefix.equals(other.prefix)) return false;
            return true;
        }
        
        @Override public String toString() {
            return "key: " + paren("" + hashCode()) + " " + key();
        }

        public long size() {
            return bombNull(metadata, "no metadata").size;
        }

        public Date lastModified() {
            return bombNull(metadata, "no metadata").lastModified;
        }
    }

    private final String name;
    private static final int RANGE_SIZE = Key.NUM_PARTITIONS / NUM_THREADS;

    public MetaBucket(String name) {
        this.name = name;
    }

    public void create() {
        AmazonS3Request.put(BUCKET_NAME);
    }

    public Key key(String prefix, String keyName) {
        return key(prefix, keyName, null);
    }
    public Key key(String prefix, String keyName, KeyResult metadata) {
        return new Key(this, prefix, keyName, metadata);
    }

    public void destroy() {
        AmazonS3Request.destroy(name);
    }
    
    public List<Key> keys() {
        return keys(null);
    }

    public List<Key> keys(final String prefix) {
        List<Thread> threads = empty();
        final List<List<Key>> results = emptySynchronized();
        for(int i : zeroTo(NUM_THREADS))
            threads.add(startedKeyLister(i, prefix, results, RANGE_SIZE));
        List<Key> result = empty();
        for(int i : zeroTo(NUM_THREADS)) {
            join(threads.get(i));
            result.addAll(results.get(i));
        }
        Collections.reverse(result);
        return result;
    }
    

    public static List<MetaBucket> all() {
        List<Thread> threads = empty();
        final List<List<MetaBucket>> results = emptySynchronized();
        for(int i : zeroTo(NUM_THREADS))
            threads.add(startedBucketLister(i, results, RANGE_SIZE));
        List<MetaBucket> result = empty();
        for(int i : zeroTo(NUM_THREADS)) {
            join(threads.get(i));
            result.addAll(results.get(i));
        }
        return result;
    }
 
    public static String reportAll(String prefix) {
        StringBuilder report = new StringBuilder();
        for(MetaBucket bucket : all())
            report.append(bucket.report(prefix));
        return report.toString();
    }

    String report(String prefix) {
        StringBuilder report = new StringBuilder();
        int count = 0;
        long totalSize = 0;
        Date lastModified = date("1900/01/01");
        Map<String, Integer> groupCount = emptyMap();
        List<Key> keys = keys(prefix);
        for(Key key : keys) {
            List<String> parts = split(".", key.keyName());
            String groupPrefix = first(parts);
            if(parts.size() < 2 || groupPrefix.length() > 100)
                groupPrefix = "NO_PREFIX";
            if (!groupCount.containsKey(groupPrefix)) groupCount.put(groupPrefix, 0);
            groupCount.put(groupPrefix, groupCount.get(groupPrefix) + 1);
            count++;
            totalSize += key.size();
            Date modified = key.lastModified();
            if(modified.after(lastModified)) lastModified = modified;
        }
        report.append(sprintf(
            "%35s - Keys=%9d Size=%15d LastModified=%s STO=%s",
            name, count, totalSize, ymdHuman(lastModified), systemStoId(name)
        ));
        report.append(" " + groupCount + "\n");
        return report.toString();
    }
    
    private static String systemStoId(String bucketName) {
        if(!bucketName.matches("quantys-\\d+")) return "";
        try {
            int systemId = Integer.parseInt(bucketName.substring(8));
            SystemDetails details = DETAILS.details(systemId);
            return details.siv().system() + "/" + details.stoId();
        } catch(Exception e) {
            return "";
        }
    }

    public void clear(String prefix) {
        List<Key> keys = keys(prefix);
        int count = 0;
        for (Key key : keys) {
            key.delete();
            if(++count % 100 == 0)
                info("deleted " + count + " / " + keys.size());
        }
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final MetaBucket other = (MetaBucket) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }

    public String name() {
        return name;
    }

}
