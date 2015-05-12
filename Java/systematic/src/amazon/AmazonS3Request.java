package amazon;

import static util.Dates.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;

import util.*;

public class AmazonS3Request extends AmazonRequest {
    
    public static class KeyResult {

        final String name;
        final long size;
        final Date lastModified;

        public KeyResult(Tag contents) {
            name = contents.text("Key");
            size = contents.longg("Size");
            lastModified = contents.date("LastModified");
        }
    }

    private static final List<String> ACTIONS = list("PUT", "GET", "DELETE", "POST", "HEAD", "COPY");
    private static final Map<String, String> NONE = emptyMap();
    private final String action;
    private final String bucket;
    private final String key;
    private final StringRequestEntity data;

    AmazonS3Request(String bucket, String key, String action, Object data) {
        super(url(bucket, key), NONE);
        this.bucket = bucket;
        this.key = key;
        if(data != null) {
            this.data = requestEntity(data);
            this.parameters.put("Content-Length", this.data.getContentLength() + "");
            this.parameters.put("Content-Type", this.data.getContentType());
        } else
            this.data = null;
        bombUnless(ACTIONS.contains(action), "unknown http action specified: " + action);
        this.action = action;
        this.parameters.put("Date", amazonS3Date());
        this.parameters.put("Authorization", "AWS " + accessKeyId + ":" + signatureRaw());
    }

    private static StringRequestEntity requestEntity(Object data) {
        try {
            return new StringRequestEntity(serialize(data), null, null);
        } catch (UnsupportedEncodingException e) {
            throw bomb("unsupported encoding?", e);
        }
    }
    
    public AmazonS3Request(String bucket, String action) {
        this(bucket, "", action, null);
    }

    private static String url(String bucket, String key) {
        return "http://" + (isEmpty(bucket) ? "" : bucket + ".") + "s3.amazonaws.com/" + key;
    }
    
    public static void put(String bucket) {
        new AmazonS3Request(bucket, "PUT").put();
    }
    
    public static void put(String bucket, String key, Object data) {
        new AmazonS3Request(bucket, key, "PUT", data).put();
    }
    


	private String get() {
        GetMethod method = new GetMethod(url);
        setRequestHeaders(method);
        return makeRequest(method, true);
    }

    private void put() {
        PutMethod method = new PutMethod(url);
        setRequestHeaders(method);
        if(data != null) method.setRequestEntity(data);
        makeRequest(method, false);
    }
    
    private void delete() {
        DeleteMethod method = new DeleteMethod(url);
        setRequestHeaders(method);
        makeRequest(method, false);
    }


	private void setRequestHeaders(HttpMethodBase method) {
        for(String parameter : parameters.keySet())
            method.addRequestHeader(parameter, parameters.get(parameter));
    }

    @Override protected String stringToSign() {
        String date = parameters.get("Date");

        String contentMD5 = "";
        String contentType = "";
        String bucketString = bucket.toLowerCase() + "/" + key.replaceAll("\\?.*", "");

        StringBuffer buf = new StringBuffer();
        buf.append(action).append("\n");
        buf.append(contentMD5).append("\n");
        buf.append(contentType).append("\n");
        buf.append(date).append("\n");
        buf.append("/");
        if (hasContent(bucket)) buf.append(bucketString);
        return buf.toString();
    }

    private String amazonS3Date() {
        String fmt = "EEE, dd MMM yyyy HH:mm:ss ";
        SimpleDateFormat df = new SimpleDateFormat(fmt, Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = df.format(now()) + "GMT";
        return date;
    }

    public static Object get(String name, String key) {
        return deserialize(new AmazonS3Request(name, key, "GET", null).get());
    }

    private static Tag listBucketTag(String name, String prefix, String marker, String delimiter) {
        String extra = "?maxkeys=1000";
        if(marker != null)
            extra += "&marker=" + marker;
        if(prefix != null)
            extra += "&prefix=" + prefix;
        if(delimiter != null)
            extra += "&delimiter=" + delimiter;
            
        AmazonS3Request get = new AmazonS3Request(name, extra, "GET", null);
        return Tag.parse(get.get());
    }

    @Deprecated
    public static List<String> bucketNames() {
        AmazonS3Request get = new AmazonS3Request("", "", "GET", null);
        Tag response = Tag.parse(get.get());
        List<String> result = empty();
        for(Tag bucket : response.child("Buckets").children())
            result.add(bucket.text("Name"));
        return result;
    }
    
    private static Tag listBucketTag(String name, String prefix) {
        return listBucketTag(name, prefix, null, null);
    }

    public static void destroy(String name) {
        new AmazonS3Request(name, "", "DELETE", null).delete();
    }

    public static void delete(String name, String key) {
        new AmazonS3Request(name, urlEncode(key), "DELETE", null).delete();
    }

    public static List<KeyResult> keys(String name, String prefix) {
        Tag listBucketResult = listBucketTag(name, prefix);
        List<KeyResult> result = empty();
        while(true) {
            for (Tag contents : listBucketResult.children("Contents")) {
                result.add(new KeyResult(contents));
            }
            if(listBucketResult.text("IsTruncated").equals("false")) break;
            listBucketResult = listBucketTag(name, prefix, last(listBucketResult.children("Contents")).text("Key"), null);
        }
        return result;
    }
    
    public static List<String> bucketNames(String name, String prefix, String delimiter) {
        Tag listBucketResult = listBucketTag(name, prefix, null, delimiter);
        List<String> result = empty();
        for(Tag common : listBucketResult.children("CommonPrefixes")) {
            String mungedName = common.text("Prefix");
            result.add(mungedName.substring(prefix.length(), mungedName.indexOf(delimiter)));
        }
        return result;
    }

}
