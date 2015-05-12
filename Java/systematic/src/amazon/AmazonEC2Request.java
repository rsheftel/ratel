package amazon;

import static util.Dates.hoursAhead;
import static util.Dates.now;
import static util.Dates.xmlTimestamp;
import static util.Log.info;
import static util.Objects.empty;
import static util.Objects.map;
import static util.Objects.urlEncode;
import static util.Strings.isEmpty;
import static util.Strings.join;

import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.methods.PostMethod;

import util.Tag;

public class AmazonEC2Request extends AmazonRequest {


    public AmazonEC2Request(Map<String, String> parameters) {
        this(parameters, ACCESS_KEY_ID, SECRET_KEY);
    }
    
    AmazonEC2Request(Map<String, String> parameters, String accessKey, String secretKey) {
        super("https://ec2.amazonaws.com", parameters, accessKey, secretKey, false);
        this.parameters.put("AWSAccessKeyId", accessKey);
        this.parameters.put("Version", "2008-12-01");
        this.parameters.put("Expires", xmlTimestamp(hoursAhead(12, now())));
        this.parameters.put("SignatureMethod", "HmacSHA1");
        this.parameters.put("SignatureVersion", "2");
        this.parameters.put("Signature", signatureRaw());
    }

    @Override protected String stringToSign() {
        List<String> queryString = empty();
        for (String key : parameters.keySet())
            if(!(key.equals("Signature") || isEmpty(parameters.get(key))))
                queryString.add(key + "=" + urlEncode(parameters.get(key)));
        return "POST\nec2.amazonaws.com\n/\n" + join("&", queryString);
    }

    public static Tag response(String action, Map<String, String> extraParameters) {
        Map<String, String> parameters = map("Action", action);
        parameters.putAll(extraParameters);
        return new AmazonEC2Request(parameters).response();
    }

    public Tag response() {
        PostMethod method = new PostMethod(url);
        for(String key : parameters.keySet())
            method.setParameter(key, parameters.get(key));
        return Tag.parse(makeRequest(method, true));
    }
    
    public static void main(String[] args) {
        Map<String, String> extras = map("Owner.1", "011811492198");
        Tag response = AmazonEC2Request.response("DescribeImages", extras );
        info("" + response);
    }
    
}