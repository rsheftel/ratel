package com.fftw.gissing;

import java.util.*;
import java.util.logging.*;
//import com.gissing.contex.tcpo.*;

import com.fftw.util.PropertyLoader;
import com.gissing.contex.tc2i.ConnectParams;
import com.gissing.contex.tc2i.Tc2iConnection;
import com.gissing.contex.tc2i.Tc2iRecordSpecification;

public class Publisher
{
    private static final Logger logger = Logger.getLogger(Subscriber.class.getName());
    
    private static Publisher publisher;    
    private Tc2iConnection conn;
    
    public synchronized static Publisher getInstance() throws Exception
    {
        if (publisher == null)
        {
            publisher = new Publisher();
        }
        
        return publisher;
    }   
    
    private Publisher() throws Exception
    {
        conn = new Tc2iConnection();
        ConnectParams cp = new ConnectParams();
        cp.setClientIdleLimit(0);
        cp.setServerHeartbeatInterval(0);
        
        // Set the handler for async callbacks from TC2I
        //conn.setStatusHandler( m_callbackStatus );
        
        // Connect to the TC2I ConteX handler
        String host = PropertyLoader.getProperty("ConteX.host", "nysrv37.fftw.com");
        int port = Integer.parseInt(PropertyLoader.getProperty("ConteX.pubPort", "7788"));        
        conn.connect(host, port, cp, null);     
    }
    
    public static void publish(String template, String record, String field, String value) 
        throws Exception
    {        
        Tc2iRecordSpecification updflds = new Tc2iRecordSpecification();
        updflds.add(template, record, field, value);
        getInstance().conn.sync.fieldUpdate(updflds);           
    }
    
    public static void publish(String template, String record, String field, double value) 
        throws Exception
    {
        publish(template, record, field, "" + value);
        //Thread.sleep(10);
    }           
}
