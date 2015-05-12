package util.web;

import static util.Systematic.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.params.*;

public class QHttpClient {

    public static final HttpClient client;
    static {
        MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setMaxTotalConnections(1000);
        params.setDefaultMaxConnectionsPerHost(1000);
        params.setTcpNoDelay(true);
        params.setLinger(0);
        manager.setParams(params );
        client = new HttpClient(manager);
        if(hostname().toUpperCase().matches("NY.*"))
            turnOnProxy();
    }
    
    public static void turnOnProxy() {
        client.getHostConfiguration().setProxy("nyproxy2.fftw.com", 8080);
    }
    
    public static void turnOffProxy() {
        client.setHostConfiguration(new HostConfiguration());
    }

}
