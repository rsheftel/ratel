package com.fftw.owndata;

import java.text.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.*;

import com.fftw.metadb.domain.*;
import com.fftw.metadb.service.*;

public class Loader
{       
    private static final Logger logger = Logger.getLogger(Loader.class.getName());
    private static final Hashtable<Integer, Request> requestList = new Hashtable<Integer, Request>();
    protected static long ORIGTIME = -2209161600000L; //December 30, Saturday 1899 00:00:00
    
    public static double[][] getHistoryBar(String[] param) 
    {
        for (int i = 0; i < param.length; i++)
        {
            System.out.println(param[i]);
            logger.info(param[i]);
            logger.info("\n\n");
        }
        
        try
        {
            int i = 0;
            String category = param[i++];
            String exchange = param[i++];
            String symbol = param[i++];
            String start = param[i++];
            String finish = param[i++];
            int resolution = Integer.parseInt(param[i++]);
            
            SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, EEEE yyyy kk:mm:ss");
            formatter.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));        
    
            Manager manager = new Manager(symbol, resolution, 
                formatter.parse(start), formatter.parse(finish));
            List<TimeSeriesResults> tsDailies = manager.getHistoryDailies();
                        
            double results[][] = new double[tsDailies.size()][14];
            for (int j = 0; j < tsDailies.size(); j++)
            { 
                //logger.log(Level.INFO, "==================");       
                TimeSeriesResults ts = tsDailies.get(j);            
                results[j][0] = (ts.getTimestamp().getTime() - ORIGTIME) / 1000;                
                results[j][1] = toDouble(ts.getOpen());
                results[j][2] = toDouble(ts.getHigh());
                results[j][3] = toDouble(ts.getLow());
                results[j][4] = toDouble(ts.getClose());
                results[j][8] = toDouble(ts.getVolume());
                results[j][13] = toDouble(ts.getOpenInterest());
            }
    
            logger.log(Level.INFO, "Results======" + results.length);
            return results;
        } 
        catch (Throwable e)
        {            
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), e);
            return new double[0][14];
        }        
    }    

    private static double toDouble(Double val)
    {
        return val == null ? 0 : val.doubleValue();
    }
    
    public static synchronized boolean subscribe(int tranId, String name)
    {
        try
        {         
            Request request = new Request(tranId, name);
            requestList.put(tranId, request);        
            return GissingLiveSubscriber.getInstance().subscribe(name, request);
        } 
        catch (Throwable e)
        {            
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }         
    }
    
    public static synchronized void unsubscribe(int tranId, String name)
    {
        try
        {                  
            Request request = requestList.remove(tranId);     
            if (request != null)
            {
                GissingLiveSubscriber.getInstance().unsubscribe(name, request);
                request = null; 
            }
        } 
        catch (Throwable e)
        {            
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), e);
        }           
    }    
    
    public static double[][] getLiveTicks(int tranId)
    {
        Request request = requestList.get(tranId);
     
        try
        {       
            if (request == null)
                return new double[0][3];
            
            double[][] result = request.getLiveTicks();
            return result;
        } 
        catch (Throwable e)
        {            
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), e);
            return new double[0][3];
        } 
    }
  
    public static double[][] getHistoryTicks(int tranId)
    {
        Request request = requestList.get(tranId);
     
        try
        {       
            if (request == null)
                return new double[0][3];
            
            double[][] result = request.getHistoryTicks();
            return result;
        } 
        catch (Throwable e)
        {            
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), e);
            return new double[0][3];
        } 
    }
}
