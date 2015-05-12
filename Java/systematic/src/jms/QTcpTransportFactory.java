package jms;

import java.io.*;
import java.net.*;

import javax.net.*;

import org.apache.activemq.transport.tcp.*;
import org.apache.activemq.wireformat.*;

public class QTcpTransportFactory extends TcpTransportFactory {
    @Override protected TcpTransport createTcpTransport(WireFormat wf, SocketFactory socketFactory, URI location, URI localLocation) throws UnknownHostException, IOException {
        return new QTcpTransport(wf, socketFactory, location, localLocation);
    }

}
