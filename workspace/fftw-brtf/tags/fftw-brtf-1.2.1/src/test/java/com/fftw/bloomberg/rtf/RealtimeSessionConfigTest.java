package com.fftw.bloomberg.rtf;

import org.testng.annotations.*;

import java.util.Properties;
import java.net.SocketAddress;

/**
 * RealtimeSessionConfig Tester.
 */
public class RealtimeSessionConfigTest {


    @Test(groups =
            {
                    "unittest"
                    })
    public void testSetConnectionTimeout() {
        Properties props = new Properties();
        props.setProperty("connection.timeout", "45");
        props.setProperty("session.host", "nyws801");
        props.setProperty("session.port", "10101");

        RealtimeSessionConfig sessionConfig = new RealtimeSessionConfig(props);

        assert 45L == sessionConfig.getConnectionTimeout() : "Connection timeout not set";
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testSetReconnectInterval() {
        Properties props = new Properties();
        props.setProperty("connection.reconnect.interval", "45000");
        props.setProperty("session.host", "nyws801");
        props.setProperty("session.port", "10101");

        RealtimeSessionConfig sessionConfig = new RealtimeSessionConfig(props);

        assert 45000L == sessionConfig.getReconnectInterval() : "Reconnect intervaluet not set";
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testGetSocketAddresses() {
        Properties props = new Properties();
        props.setProperty("session.host.1", "nyws801");
        props.setProperty("session.port.1", "10101");

        props.setProperty("session.host.2", "nyws802");
        props.setProperty("session.port.2", "10101");

        RealtimeSessionConfig sessionConfig = new RealtimeSessionConfig(props);
        SocketAddress[] addresses = sessionConfig.getSocketAddresses();


        assert addresses != null : "failed to configure socket addresses - nothing found";
        assert addresses.length == 2 : "failed to configure socket addresses - found " + addresses.length;
    }

}
