package com.fftw.metadb.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fftw.metadb.service.LiveListener;
import com.fftw.metadb.service.LiveSubscriber;
import com.fftw.util.DBTools;
import com.fftw.util.PropertyLoader;
import com.gissing.contex.tcpo.ConnectParams;
import com.gissing.contex.tcpo.Field;
import com.gissing.contex.tcpo.ManagedTcpoConnection;
import com.gissing.contex.tcpo.Record;
import com.gissing.contex.tcpo.TcpoDataCallback;
import com.gissing.contex.tcpo.Template;

public class GissingLiveSubscriber implements LiveSubscriber, TcpoDataCallback, Runnable
{
    private static final Logger logger = Logger.getLogger(GissingLiveSubscriber.class.getName());
    private static GissingLiveSubscriber gls;
    
    private ManagedTcpoConnection conn;
    private Hashtable<String, Hashtable<String, Set<LiveListener>>> templateHt = 
        new Hashtable<String, Hashtable<String, Set<LiveListener>>>();
    
    public synchronized static GissingLiveSubscriber getInstance()  throws Exception
    {
        if (gls == null)
        {
            gls = new GissingLiveSubscriber();
        }
        
        return gls;
    }
    
    private GissingLiveSubscriber() throws Exception
    {
        com.gissing.contex.tcpo.Log.FileDevice fileDevice = new com.gissing.contex.tcpo.Log.FileDevice();
        fileDevice.open( "logs\\mylog.txt" );
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
        
        conn.setUseAsync( false );
        
        String host = PropertyLoader.getProperty("ConteX.host", "nysrv37.fftw.com");
        int port = Integer.parseInt(PropertyLoader.getProperty("ConteX.port", "8500"));
        
        conn.connect(host, port, host, port, cp, null );

        try {
            // Wait forever until we get a callback
            synchronized (onConnectionEvent) {
                onConnectionEvent.wait();
            }
        } catch (InterruptedException e) {
        }

        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();        
       // callbackThread( conn );
    }
    
    public void run() {
        //long lastPing = 0L;
        while( true ) {
            try {
                conn.waitForData(this, 1000L);
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
        Iterator templateIt = records.iterator();
        while( templateIt.hasNext() ) {
            Template t = (Template)templateIt.next();
            Iterator recordIt = t.getRecords().values().iterator();
            while( recordIt.hasNext() ) {
                Record r = (Record)recordIt.next();
                logger.info( "RECORD ADD:  activeServer=" + activeServer + 
                    " templateName=" + t.getName() + " recordName=" + r.getName() );
            }
        }
    }

    public void onRecordDrop( boolean activeServer, List records ) throws Exception {
        Iterator templateIt = records.iterator();
        while( templateIt.hasNext() ) {
            Template t = (Template)templateIt.next();
            Iterator recordIt = t.getRecords().values().iterator();
            while( recordIt.hasNext() ) {
                Record r = (Record)recordIt.next();
                logger.info( "RECORD DROP:  activeServer=" + activeServer + 
                    " templateName=" + t.getName() + " recordName=" + r.getName() );
            }
        }
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
                    logger.info( "DATA UPDATE: activeServer=" + activeServer + 
                        " templateName=" + t.getName() + " recordName=" + r.getName() );                
                    
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
    
    public void subscribe(String template, String record, LiveListener liveListener) 
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
        
        listenerSet.add(liveListener);
        //if (listenerSet.size() == 1)
        {
            conn.subscribe(template, record);
        }
    }
    
    public boolean subscribe(String topic, LiveListener liveListener) throws Exception
    {
        logger.info("Subscription...........");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;        
        try
        {      
            con = DBTools.getConnection("DB.SystemDB");
            ps = con.prepareStatement("select Template, Record from Gissing where Name=?");
            ps.setString(1, topic);            
            rs = ps.executeQuery();
            
            if (rs.next())
            {                       
                subscribe(rs.getString("Template"), rs.getString("Record"), liveListener);                
                return true;
            }
            
            HashMap<String, Object> fieldMap = new HashMap<String, Object>();
            double invaid = -99;//Double.MIN_VALUE;
            fieldMap.put("HighPrice", invaid);
            fieldMap.put("LastPrice", invaid);
            fieldMap.put("LastVolume", invaid);
            fieldMap.put("LowPrice", invaid);
            fieldMap.put("OpenPrice", invaid);
            fieldMap.put("LowPrice", invaid);     
            fieldMap.put("Timestamp", new SimpleDateFormat("yyyy/MM/dd kk:mm:ss").format(new Date()));
            liveListener.onData(fieldMap);
            return true;
        }
        finally
        {
            DBTools.close(rs, ps);
            DBTools.close(con);
        }         
    }
    
    public void unsubscribe(String template, String record, LiveListener liveListener) 
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
    
    public void unsubscribe(String topic, LiveListener liveListener) throws Exception
    {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;        
        try
        {        
            con = DBTools.getConnection("DB.SystemDB");
            ps = con.prepareStatement("select Template, Record from Gissing where Name=?");
            ps.setString(1, topic);            
            rs = ps.executeQuery();
            
            if (rs.next())
            {                           
                unsubscribe(rs.getString("Template"), rs.getString("Record"), liveListener);
            }
        }
        finally
        {
            DBTools.close(rs, ps);
            DBTools.close(con);
        }         
    }
    
    
    public static void main (String[] args) throws Exception
    {
        GissingLiveSubscriber subscriber = new GissingLiveSubscriber();
        subscriber.subscribe("USSWAP10", null);
        //subscriber.unsubscribe("", null);
        Thread.sleep(1000000);
    }    
}
