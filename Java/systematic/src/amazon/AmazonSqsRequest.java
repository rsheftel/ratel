package amazon;

import static util.Dates.hoursAhead;
import static util.Dates.now;
import static util.Dates.xmlTimestamp;
import static util.Objects.map;
import static util.Strings.isEmpty;

import java.util.Map;

import org.apache.commons.httpclient.methods.PostMethod;

import util.Tag;

public class AmazonSqsRequest extends AmazonRequest {


    public AmazonSqsRequest(String url, Map<String, String> parameters) {
        this(url, parameters, ACCESS_KEY_ID, SECRET_KEY);
    }
    
    AmazonSqsRequest(String url, Map<String, String> parameters, String accessKey, String secretKey) {
        super(url, parameters, accessKey, secretKey);
        this.parameters.put("AWSAccessKeyId", accessKey);
        this.parameters.put("Version", "2008-01-01");
        this.parameters.put("Expires", xmlTimestamp(hoursAhead(12, now())));
        this.parameters.put("SignatureVersion", "1");
        this.parameters.put("Signature", signatureRaw());
    }

    @Override protected String stringToSign() {
        StringBuilder buf = new StringBuilder();
        for (String key : parameters.keySet())
            if(!(key.equals("Signature") || isEmpty(parameters.get(key))))
                buf.append(key).append(parameters.get(key));
        return buf.toString();
    }

    public static Tag response(String url, String action, Map<String, String> extraParameters) {
        Map<String, String> parameters = map("Action", action);
        parameters.putAll(extraParameters);
        return new AmazonSqsRequest(url, parameters).response();
    }

    public Tag response() {
        PostMethod method = new PostMethod(url);
        for(String key : parameters.keySet())
            method.setParameter(key, parameters.get(key));
        return Tag.parse(makeRequest(method, true));
    }
}