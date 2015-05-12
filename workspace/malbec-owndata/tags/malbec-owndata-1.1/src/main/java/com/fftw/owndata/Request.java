package com.fftw.owndata;

import com.fftw.metadb.domain.Tick;
import com.fftw.metadb.service.LiveListener;
import com.fftw.metadb.service.LiveSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Request implements LiveListener {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    //static final Logger staticLogger = LoggerFactory.getLogger(Request.class);

    /**
     * Determine how much to adjust the timestamp so that it shows correctly in TradeStation.
     * This could probably be handled better in the C++ code, but I don't know where or
     * how to change it at this time.  I think the C++ code should be changed to C#
     * as it would make the whole process easier.
     */
//    private static final long UTC_OFFSET = Calendar.getInstance().getTimeZone().getOffset(System.currentTimeMillis());
//    private static final Calendar GMT_CALENDAR = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    private int tranId;
    private String name;

    private LiveSubscriber subscriber;
    private List<Tick> tickLiveList = new ArrayList<Tick>();
    private List<Tick> tickHistoryList = new ArrayList<Tick>();
    // 1-24 hour
    //private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
    // 0-23 hour
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public Request(int tranId, String name) {
        this.tranId = tranId;
        this.name = name;
    }

    public int getTranId() {
        return tranId;
    }

    public String getName() {
        return name;
    }

    List<Tick> getTickHistory() {
        return tickHistoryList;
    }

    List<Tick> getTickLiveList() {
        return tickLiveList;
    }

    public synchronized double[][] getLiveTicks() {
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

    public double[][] getHistoryTicks() throws Exception {
        return getTicks(tickHistoryList);
    }

    private static double[][] getTicks(List<Tick> tickList) {
        int len = tickList.size();
        double[][] result = new double[len][3];

        for (int i = 0; i < len; i++) {
            Tick tick = tickList.get(i);
            result[i][0] = (tick.getTimestamp().getTime() - Loader.ORIGTIME) / 1000;
            result[i][1] = tick.getPrice();
            result[i][2] = tick.getVolume();
        }

//        staticLogger.info("----------ticks " + len);
        return result;
    }

    public synchronized void onData(Map<String, String> dataMap) {
        if (tickHistoryList.isEmpty()) {
            Date date = new Date();
            tickHistoryList.add(new Tick(date, Double.valueOf(dataMap.get("OpenPrice")), 100D));
            tickHistoryList.add(new Tick(date, Double.valueOf(dataMap.get("HighPrice")), 100D));
            tickHistoryList.add(new Tick(date, Double.valueOf(dataMap.get("LowPrice")), 100D));
        }

        try {
            // The DateTime is assumed to be GMT, however Java uses local time
            Date tickTimestamp = formatter.parse(dataMap.get("Timestamp"));

            Tick tick = new Tick(tickTimestamp,
                    Double.valueOf(dataMap.get("LastPrice")),
                    Double.valueOf(dataMap.get("LastVolume")));
            tickLiveList.add(tick);
        } catch (NumberFormatException e) {
            logger.warn("No data for tick. " + name + " " + dataMap.get("LastPrice") + ", " + dataMap.get("LastVolume"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public LiveSubscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(LiveSubscriber subscriber) {
        this.subscriber = subscriber;
    }
}
