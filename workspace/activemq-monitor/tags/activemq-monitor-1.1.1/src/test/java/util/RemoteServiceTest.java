package util;

import java.io.IOException;

import org.testng.annotations.Test;

public class RemoteServiceTest {

    private static final String SERVER = "nyws802";
    
    @Test(groups = { "remote" })
    public void testRestartService() throws IOException {
        RemoteService remoteService = new RemoteService(SERVER, "ActiveMQ 5.1.0");
        boolean stopped = remoteService.stop();

        assert stopped : "Service was not stopped";

        Monitoring.sleep(2000);
        
        boolean started = remoteService.start();
        assert started : "Service was not started";

        Monitoring.sleep(3000);
        boolean restarted = remoteService.restart();
        assert restarted : "Service was not restarted";
    }

}
