package com.fftw.owndata;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fftw.metadb.domain.Tick;
import com.fftw.metadb.service.LiveListener;
import com.fftw.metadb.service.LiveSubscriber;

public class Request implements LiveListener
{
    private static final Logger logger = Logger.getLogger(Request.class.getName());
    
    private int tranId;
    private String name; 
    private LiveSubscriber subscriber;
    private ArrayList<Tick> tickLiveList = new ArrayList<Tick>();
    private ArrayList<Tick> tickHistoryList = new ArrayList<Tick>();
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
    
    public Request(int tranId, String name)
    {
        this.tranId = tranId;
        this.name = name;
    }

    public synchronized double[][] getLiveTicks()
    {       
        double[][] result = getTicks(tickLiveList);
        /*
        if (tickLiveList.size() > 1)
        {
            Tick lastTick = tickLiveList.get(tickLiveList.size() - 1);        
            tickLiveList.clear();
            tickLiveList.add(lastTick);
        }*/
        tickLiveList.clear();
        return result;
    }
    
    public synchronized double[][] getHistoryTicks() throws Exception
    {
        return getTicks(tickHistoryList);
    }    
    
    private static double[][] getTicks(ArrayList<Tick> tickList)
    {
        int len = tickList.size();
        double[][] result = new double[len][3];
        for (int i = 0; i < len; i++)
        {
            Tick tick = tickList.get(i);
            result[i][0] = (tick.getTimestamp().getTime() - Loader.ORIGTIME) / 1000; 
            result[i][1] = tick.getPrice();
            result[i][2] = tick.getVolume();
        }
        
        //logger.info("----------ticks " + len);
        return result;
    }
    
    public synchronized void onData(Map dataMap) 
    {                     
        if (tickHistoryList.isEmpty())
        {         
            Date date = new Date();
            tickHistoryList.add(new Tick(date, 
                Double.valueOf(dataMap.get("OpenPrice").toString()), 
                100D));
            tickHistoryList.add(new Tick(date, 
                Double.valueOf(dataMap.get("HighPrice").toString()), 
                100D));
            tickHistoryList.add(new Tick(date, 
                Double.valueOf(dataMap.get("LowPrice").toString()), 
                100D));            
        }
        
        try
        {
            Tick tick = new Tick(formatter.parse(dataMap.get("Timestamp").toString()), 
                Double.valueOf(dataMap.get("LastPrice").toString()), 
                Double.valueOf(dataMap.get("LastVolume").toString()));        
            tickLiveList.add(tick);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), e);          
        }
    }

    public LiveSubscriber getSubscriber ()
    {
        return subscriber;
    }

    public void setSubscriber (LiveSubscriber subscriber)
    {
        this.subscriber = subscriber;
    }
}
