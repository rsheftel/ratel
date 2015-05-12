package amazon;

import static util.Errors.*;
import static util.Log.*;
import static util.Strings.*;
import static util.Times.*;

import java.io.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.apache.commons.codec.binary.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.params.*;

import util.*;
import util.web.*;
import file.*;

public abstract class AmazonRequest {


    public class NoRetry extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public NoRetry(String message) {
            super(message);
        }

    }


    protected static final String SECRET_KEY = "gD0NUGToPDv7MzTEi3G4EbYJ1gH1tEq+NBwLxxXE";
    protected static final String ACCESS_KEY_ID = "1VA799Q6986T9ZDX8DG2";
    protected static final String HMACSHA1 = "HmacSHA1";
    protected final Map<String, String> parameters;
    protected final String secretKey;
    protected final String url;
    protected final String accessKeyId;



    public AmazonRequest(String url, Map<String, String> parameters) {
        this(url, parameters, ACCESS_KEY_ID, SECRET_KEY);
    }
    
    public AmazonRequest(String url, Map<String, String> parameters, String accessKeyId, String secretKey) {
        this(url, parameters, accessKeyId, secretKey, true);
    }

    public AmazonRequest(String url, Map<String, String> parameters, String accessKeyId, String secretKey, boolean useCaseInsensitiveComparator) {
        this.parameters = useCaseInsensitiveComparator 
            ? new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) 
            : new TreeMap<String, String>();
        this.url = url;
        this.accessKeyId = accessKeyId;
        this.secretKey = secretKey;
        this.parameters.putAll(parameters);
    }
    
    protected String signatureRaw() {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(), HMACSHA1);
            Mac mac = Mac.getInstance(HMACSHA1);
            mac.init(signingKey);
            String stringToSign = stringToSign();
            byte[] rawHmac = mac.doFinal(stringToSign.getBytes());
            return new String(Base64.encodeBase64(rawHmac));
        } catch (Exception e) {
            throw bomb("exception thrown in HMAC string encoding", e);
        }
    }

    protected abstract String stringToSign();

    protected String bodyConsumed(HttpMethodBase method) throws IOException {
        InputStream in = method.getResponseBodyAsStream();
        return in == null ? "<no body in response>": QFile.text(in);
    }

    public static void setRetry(HttpMethodBase method) {
        method.getParams().setParameter(
            HttpMethodParams.RETRY_HANDLER, 
            new DefaultHttpMethodRetryHandler(10, false) {
                @Override public boolean retryMethod(HttpMethod retryMethod, IOException exception, int executionCount) {
                    sleep(500 * (long) Math.pow(2, executionCount));
                    return super.retryMethod(retryMethod, exception, executionCount);
                }
            }
        );
    }
    

    private String makeRequest(HttpMethodBase method, boolean hasBody, int retriesLeft, long pauseMillis) {
		try {
	        int statusCode = QHttpClient.client.executeMethod(method);
	        boolean isSuccess = statusCode / 100 == 2;
	        boolean isRetry = !isSuccess && (statusCode / 100 != 4);
			if (isRetry && retriesLeft > 0) {
	        	return retry(method, hasBody, retriesLeft, pauseMillis, "non 2** status code " + paren("" + statusCode));
	        }
	        String body = bodyConsumed(method);
	        if(isSuccess) return body;
	        String xml = convertToLongXmlIfPossible(body);
			throw new NoRetry("non-200 reponse code: " + statusCode + "\n" + (isEmpty(xml) ? body : xml));
	    } catch(NoRetry e) {
	        throw e;
	    }catch (Exception e) {
	        if(retriesLeft > 0)
	            return retry(method, hasBody, retriesLeft, pauseMillis, "'Fatal' transport error (retrying): " + e.getMessage());
            throw bomb("Fatal transport error: " + e.getMessage(), e);
	    } finally {
	        method.releaseConnection();
	    }  
	}

    private String retry(HttpMethodBase method, boolean hasBody, int retriesLeft, long pauseMillis, String message) {
        long sleepTime = pauseMillis + new Random().nextInt((int) pauseMillis);
        info(message + " - sleeping " + sleepTime);
        sleep(sleepTime);
        return makeRequest(method, hasBody, retriesLeft - 1, pauseMillis * 2);
    }
    
    protected String makeRequest(HttpMethodBase method, boolean hasBody) {
    	setRetry(method);
    	return makeRequest(method, hasBody, 10, 50);
    }
    

    private String convertToLongXmlIfPossible(String body) {
	    String xml = "";
	    try { xml = Tag.parse(body).longXml(); } 
	    catch (Throwable t) {}
	    return xml;
	}

}
