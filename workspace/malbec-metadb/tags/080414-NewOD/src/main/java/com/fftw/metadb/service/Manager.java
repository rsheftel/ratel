package com.fftw.metadb.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.fftw.metadb.domain.GissingLiveSubscriber;
import com.fftw.metadb.domain.TimeSeriesResults;
import tsdb.*;
import util.*;
import com.fftw.util.DBTools;

public class Manager
{
    //private List<TimeSeriesResults> historyDailies;
    private List<TimeSeriesResults> historyTicks;
    private List<TimeSeriesResults> todayTicks;
    
    private String name;
    private int resolution;
    private Date startDate;
    private Date finishDate;
    
    public Manager(String name, int resolution, Date startDate, Date finishDate)
    {
        this.name = name;
        this.resolution = resolution;
        this.startDate = startDate;
        this.finishDate = finishDate;
    }

    public List<TimeSeriesResults> getHistoryDailies() throws Exception
    {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;   
        
        try
        {           
            con = DBTools.getConnection("DB.SystemDB");
            
            ps = con.prepareStatement("select HistDaily from Time_series_data where Name=?");
            ps.setString(1, name);            
            rs = ps.executeQuery();
            if (!rs.next())
            {
                throw new Exception("Cannot find " + name + " in Time_series_data table.");
            }
            
            String histDailyTable = rs.getString("HistDaily");
            DBTools.close(rs, ps);
            
            ps = con.prepareStatement("select * from " + histDailyTable + " where Name=?");
            ps.setString(1, name);            
            rs = ps.executeQuery();
            
            List<TimeSeriesResults> historyDailies = new ArrayList<TimeSeriesResults>();
         
            if (rs.next())
            {
                if ("TSDB".equals(histDailyTable))
                {
                    String source = rs.getString("Data_source");
                    DataSource ds = new DataSource(source); 
                    
                    String calMethod = rs.getString("Calculate_method");
                    boolean isOpenIsPriorClose = "OpenIsPriorClose".equalsIgnoreCase(calMethod);
                    
                    Range range = new Range(startDate, finishDate);

long start = System.currentTimeMillis();
                    Observations open = getObservations(ds, rs.getString("Name_open"), range);
                    Observations high = getObservations(ds, rs.getString("Name_high"), range);
                    Observations low = getObservations(ds, rs.getString("Name_low"), range);
                    Observations close = getObservations(ds, rs.getString("Name_close"), range);
                    Observations volume = getObservations(ds, rs.getString("Name_volume"), range);
                    Observations openInterest = getObservations(ds, rs.getString("Name_open_interest"), range); 
System.out.println(System.currentTimeMillis() - start);

                    List<Date> times = close.times();
                    for (int i = 0; i < times.size(); i++)
                    {
                        Date t = times.get(i);                        
                        double openDouble = isOpenIsPriorClose && i != 0 ? 
                                getDouble(close, times.get(i - 1)) :
                                getDouble(open, t);                        

                        TimeSeriesResults tsResults = new TimeSeriesResults(
                            t,                                                        
                            openDouble, 
                            openDouble >= getDouble(high, t) ? openDouble : getDouble(high, t),
                            openDouble <= getDouble(low, t) ? openDouble : getDouble(low, t),
                            getDouble(close, t),
                            getDouble(volume, t),
                            getDouble(openInterest, t));
                        historyDailies.add(tsResults); 
                    }
                }
                else if ("ASCII".equals(histDailyTable))
                {
                    String table = rs.getString("Filename").replaceAll("\\\\", "\\\\\\\\\\\\\\\\");
                    String columnDate = rs.getString("columnDate");
                    String columnOpen = rs.getString("columnOpen");
                    String columnHigh = rs.getString("columnHigh");
                    String columnLow = rs.getString("columnLow");
                    String columnClose = rs.getString("columnClose");
                    String columnVolume = rs.getString("columnVolume");
                    String columnOpenInterest = rs.getString("columnOpenInterest");
                    
                    DBTools.close(rs, ps);
                    DBTools.close(con);
                    
                    
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                    //formatter.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));        
                             
                    con = DBTools.getConnection("DB.ASCII");                    
                    ps = con.prepareStatement("select * from \"" + table + 
                        "\" WHERE \"" + columnDate + "\">=? and \"" + columnDate + "\"<=?");
                    ps.setString(1, formatter.format(startDate));
                    ps.setString(2, formatter.format(finishDate));
                                    
                    rs = ps.executeQuery();
                    while (rs.next())
                    {
                        TimeSeriesResults tsResults = new TimeSeriesResults(
                            formatter.parse(rs.getString(columnDate)),
                            rs.getDouble(columnOpen),
                            rs.getDouble(columnHigh),
                            rs.getDouble(columnLow),
                            rs.getDouble(columnClose),
                            rs.getDouble(columnVolume),
                            rs.getDouble(columnOpenInterest));
                        historyDailies.add(tsResults);                     
                    }                                       
                } else 
                    throw new Exception("Cannot find " + histDailyTable + " table.");
                
                return historyDailies;    
            }
            throw new Exception("Cannot find " + name + " in " + histDailyTable + " table.");
        } 
        finally
        {
            DBTools.close(rs, ps);
            DBTools.close(con);         
        }                
    }
    /*
    private static Double getDouble(ResultSet rs, ResultSet rs1, String column) throws Exception
    {
        return rs1.getDouble(rs.getString(column));

        String result = rs1.getString(rs.getString("columnOpen"));
        if (result == null)
            return null;
        
        return Double.parseDouble(result);    
    }*/
    
    private static Observations getObservations(DataSource ds, String timeSeriesName, Range range)
    {
        try
        {
            return ds.observations(TimeSeries.series(timeSeriesName), range);
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    private static Double getDouble(Observations ob, Date t)
    {
        try
        {
            return Double.valueOf(ob.value(t));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public List<TimeSeriesResults> getHistoryTicks ()
    {
        return historyTicks;
    }


    public List<TimeSeriesResults> getTodayTicks ()
    {
        return todayTicks;
    }


    public LiveSubscriber getLiveSubscriber () throws Exception
    {
        return GissingLiveSubscriber.getInstance();
    }
    
    public static LiveSubscriber getLiveSubscriber (String name)
    {
        return null;
    }

    public Date getFinishDate ()
    {
        return finishDate;
    }

    public String getName ()
    {
        return name;
    }

    public int getResolution ()
    {
        return resolution;
    }

    public Date getStartDate ()
    {
        return startDate;
    }   
}
