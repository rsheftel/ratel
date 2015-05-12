package jms;

import java.io.*;
import java.net.*;

import javax.net.*;

import org.apache.activemq.transport.tcp.*;
import org.apache.activemq.wireformat.*;

public class QTcpTransport extends TcpTransport {

    public QTcpTransport(WireFormat arg0, SocketFactory arg1, URI arg2, URI arg3) throws UnknownHostException, IOException {
        super(arg0, arg1, arg2, arg3);
        setDaemon(true);
    }

}
