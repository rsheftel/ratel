package com.fftw.gissing;

import java.util.*;
import java.util.logging.*;
//import com.gissing.contex.tcpo.*;

import com.fftw.util.PropertyLoader;
import com.gissing.contex.tcpo.ConnectParams;
import com.gissing.contex.tcpo.Field;
import com.gissing.contex.tcpo.ManagedTcpoConnection;
import com.gissing.contex.tcpo.Record;
import com.gissing.contex.tcpo.TcpoDataCallback;
import com.gissing.contex.tcpo.Template;

public class Subscriber implements TcpoDataCallback, Runnable
{
    private static final Logger logger = Logger.getLogger(Subscriber.class.getName());
    
    private static Subscriber gls;
    
    private ManagedTcpoConnection conn;
    private Hashtable<String, Hashtable<String, Set<LiveListener>>> templateHt = 
        new Hashtable<String, Hashtable<String, Set<LiveListener>>>();
    
    public synchronized static Subscriber getInstance()  throws Exception
    {
        if (gls == null)
        {
            gls = new Subscriber();
        }
        
        return gls;
    }
    
    private Subscriber() throws Exception
    {
        com.gissing.contex.tcpo.Log.FileDevice fileDevice = new com.gissing.contex.tcpo.Log.FileDevice();
        fileDevice.open( "logs\\ConteXLog.txt" );
        com.gissing.contex.tcpo.Log.setLogDevice( fileDevice );
        com.gissing.contex.tcpo.Log.setLogLevel(com.gissing.contex.tcpo.Log.LOGLEVEL_Proto);            
        
        conn = new ManagedTcpoConnection();
        ConnectParams cp = new ConnectParams();
        cp.setMinimumMessageInterval(100);
        //cp.setServerHeartbeatInterval(15);
        cp.setConflate(false);
        cp.setChangedFieldsOnly(false);
        
        final Object onConnectionEvent = new Object();        
        conn.setConnectionStatusSink( new ManagedTcpoConnection.ConnectionStatusSink() {
                public void down() {
                    com.gissing.contex.tcpo.Log.info("Lost connection to ConteX." );
                }
    
                public void up() {
                    com.gissing.contex.tcpo.Log.info( "Connected to ConteX." );
                    try
                    {
                        Thread.sleep(10);
                    }
                    catch (Exception e)
                    {                        
                    }
                    
                    synchronized (onConnectionEvent) {
                        onConnectionEvent.notifyAll();
                    }
                }
            }
        );
        
        conn.setUseAsync(false);
        
        String host = PropertyLoader.getProperty("ConteX.host", "nysrv37.fftw.com");
        int port = Integer.parseInt(PropertyLoader.getProperty("ConteX.subPort", "8500"));
        conn.connect(host, port, host, port, cp, null);
        
        long start = System.currentTimeMillis();      
        try {
            // Wait forever until we get a callback
            synchronized (onConnectionEvent) {
                onConnectionEvent.wait();
            }
        } catch (InterruptedException e) {
        }
        System.out.println(System.currentTimeMillis() - start);    

        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();  
       
       // conn.disconnect();
       // callbackThread( conn );
    }
    
    public void run() {
        //long lastPing = 0L;
        while( true ) {
            try {
                conn.waitForData(this, 0L);
    /*            
                if( lastPing + 15000L < System.currentTimeMillis() ) {
                    conn.async.ping();
                    lastPing = System.currentTimeMillis();
                }*/
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }  
    
    public void onBadMessage( String reason ) {
        logger.info( "BAD MESSAGE FROM SERVER: " + reason );
    }

    public void onRecordAdd( boolean activeServer, List records ) throws Exception {
    }

    public void onRecordDrop( boolean activeServer, List records ) throws Exception {
    }

    public void onData( boolean activeServer, List fieldUpdates ) {
        try
        {
            Iterator templateIt = fieldUpdates.iterator();
            while( templateIt.hasNext() ) {
                Template t = (Template)templateIt.next();
                
                Hashtable<String, Set<LiveListener>> recordHt = templateHt.get(t.getName());           
                if (recordHt == null)
                {
                    continue;
                }
                
                Iterator recordIt = t.getRecords().values().iterator();
                while( recordIt.hasNext() ) {
                    Record r = (Record)recordIt.next();             
                    
                    Set<LiveListener> listenerSet = recordHt.get(r.getName());
                    if (listenerSet == null)
                    {
                        continue;
                    }
                    
                    //logger.info(r.getField("BID") + "");
                    //logger.info(r.getField("ASK") + "");
                    HashMap fieldMap = new HashMap();
                    Iterator fieldIt = r.getFields().values().iterator();
                    while( fieldIt.hasNext() ) 
                    {
                        Field f =(Field)fieldIt.next();
                        fieldMap.put(f.getName(), f.getValueAsString());
                    }
                  
                    for (LiveListener listener : listenerSet)
                    {
                        listener.onData(fieldMap);
                    }
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), e);       
        }
    }
    
    public synchronized void subscribe(String template, String record, LiveListener liveListener) 
        throws Exception
    {
        Hashtable<String, Set<LiveListener>> recordHt = templateHt.get(template);
        
        if (recordHt == null)
        {
            recordHt = new Hashtable<String, Set<LiveListener>>();
            templateHt.put(template, recordHt);
        }
        
        Set<LiveListener> listenerSet = recordHt.get(record);
        if (listenerSet == null)
        {          
            listenerSet = new HashSet<LiveListener>();
            recordHt.put(record, listenerSet);
        }
     
        if (listenerSet.size() == 0)
        {
           // conn.unsubscribe(template, record);
            conn.subscribe(template, record);
        }        
        listenerSet.add(liveListener);
    }    
    
    public static synchronized void subscribe(String template, String record) 
        throws Exception
    {
        Subscriber subscriber = getInstance();
        Hashtable<String, Set<LiveListener>> recordHt = subscriber.templateHt.get(template);
        if (recordHt != null)
        {
            Set<LiveListener> listenerSet = recordHt.get(record);
            if (listenerSet != null && !listenerSet.isEmpty())            
                return;            
        }         
        
        subscriber.subscribe(template, record, new Listener());
    }    
    
    public synchronized void unsubscribe(String template, String record, LiveListener liveListener) 
        throws Exception
    {
        Hashtable<String, Set<LiveListener>> recordHt = templateHt.get(template);
        if (recordHt != null)
        {
            Set<LiveListener> listenerSet = recordHt.get(record);
            if (listenerSet != null && listenerSet.remove(liveListener) && 
                listenerSet.isEmpty())
            {
                conn.unsubscribe(template, record);
            }
        }         
    }        
    
    public synchronized void unsubscribeAll(String template, String record) 
        throws Exception
    {
        Hashtable<String, Set<LiveListener>> recordHt = templateHt.get(template);
        if (recordHt != null)
        {
            Set<LiveListener> listenerSet = recordHt.remove(record);
            if (listenerSet != null)
            {
                conn.unsubscribe(template, record);
            }            
        }         
    }    
    
    public static synchronized void unsubscribe(String template, String record) 
        throws Exception
    {
        getInstance().unsubscribeAll(template, record);
    }

    public static synchronized String get(final String template, final String record, 
        final String field) throws Exception
    {
        Subscriber subscriber = getInstance();
        Hashtable<String, Set<LiveListener>> recordHt = subscriber.templateHt.get(template);
        if (recordHt == null)
        {
            subscriber.subscribe(template, record, new Listener());
            recordHt = subscriber.templateHt.get(template);
           // throw new Exception("Template " + template + " Record " + record + 
             //   "has not been subscribed");
        }
        
        Set<LiveListener> listenerSet = recordHt.get(record);        
        if (listenerSet == null || listenerSet.isEmpty())
        {
            subscriber.subscribe(template, record, new Listener());
            listenerSet = recordHt.get(record);        
            //throw new Exception("Template " + template + " Record " + record + 
              //  "has not been subscribed");
        }

        LiveListener listener = listenerSet.iterator().next();
        Map dataMap = ((Listener)listener).dataMap;
        if (dataMap.isEmpty())
        {
            synchronized (listener) 
            {      
                listener.wait();           
            }
        }
        
        return (String)dataMap.get(field);
    }    
    
    private static class Listener implements LiveListener 
    {
        public Map dataMap = (new Hashtable());
        public void onData(Map dataMap)
        {          
            System.out.println("Got " + dataMap.get("BID"));
            
            if (this.dataMap.isEmpty())
            {
                synchronized (this) 
                { 
                    this.dataMap.putAll(dataMap);
                    notifyAll();
                }
                return;              
            }
            
            this.dataMap.putAll(dataMap);
        }       
    }
}
