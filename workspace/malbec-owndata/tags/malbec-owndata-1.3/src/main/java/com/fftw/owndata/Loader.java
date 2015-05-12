package com.fftw.owndata;

import com.fftw.metadb.domain.TimeSeriesResults;
import com.fftw.metadb.service.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;

public class Loader {

    private static final Logger staticLogger = LoggerFactory.getLogger(Loader.class);


    private static final Hashtable<Integer, Request> requestList = new Hashtable<Integer, Request>();
    protected static final long ORIGTIME = -2209161600000L; //December 30, Saturday 1899 00:00:00

    public static double[][] getHistoryBar(String[] param) {
        try {
            BarParameter barParam = new BarParameter(param);

            staticLogger.info("BarParam: " + barParam);

            SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, EEEE yyyy kk:mm:ss");
            formatter.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));

            Manager manager = Manager.getInstance(barParam.symbol);

            List<TimeSeriesResults> tsDailies = manager.getHistoryDailies(barParam.resolution,
                    formatter.parse(barParam.start), formatter.parse(barParam.finish));

            double results[][] = new double[tsDailies.size()][14];
            for (int j = 0; j < tsDailies.size(); j++) {
                //logger.log(Level.INFO, "==================");       
                TimeSeriesResults ts = tsDailies.get(j);
                results[j][0] = (ts.getTimestamp().getTime() - ORIGTIME) / 1000d;
                results[j][1] = toDouble(ts.getOpen());
                results[j][2] = toDouble(ts.getHigh());
                results[j][3] = toDouble(ts.getLow());
                results[j][4] = toDouble(ts.getClose());
                results[j][8] = toDouble(ts.getVolume());
                results[j][13] = toDouble(ts.getOpenInterest());
            }

            staticLogger.info("Number of ticks for BarParams:" + results.length);
            return results;
        }
        catch (Throwable e) {
            e.printStackTrace();
            staticLogger.error(e.getMessage(), e);
            return new double[0][14];
        }
    }

    private static double toDouble(Double val) {
        return val == null ? 0 : val.doubleValue();
    }

    /**
     * This is the entry point to the real-time ticks.
     * <p/>
     * Determine which <code>LiveSubscriber</code> to use and then pass the listener to it.
     *
     * @param tranId
     * @param name
     * @return
     */
    public static synchronized boolean subscribe(int tranId, String name) {
        try {
            Request request = new Request(tranId, name);
            requestList.put(tranId, request);

            staticLogger.info("Subscribing: " + tranId + " : " + name);

            return Manager.getInstance(name).getLiveSubscriber().subscribe(name, request);
        }
        catch (Throwable e) {
            staticLogger.error(e.getMessage(), e);
            return false;
        }
    }

    public static synchronized void unsubscribe(int tranId, String name) {
        try {
            Request request = requestList.remove(tranId);
            if (request != null) {
                staticLogger.info("unsubscribing: " + tranId + " : " + name);
                Manager.getInstance(name).getLiveSubscriber().unsubscribe(name, request);
                request = null;
            }
        }
        catch (Throwable e) {
            staticLogger.error(e.getMessage(), e);
        }
    }

    public static double[][] getLiveTicks(int tranId) {
        Request request = requestList.get(tranId);

        try {
            if (request == null) {
                return new double[0][3];
            }

            double[][] result = request.getLiveTicks();
            return result;
        }
        catch (Throwable e) {
            staticLogger.error(e.getMessage(), e);
            return new double[0][3];
        }
    }

    public static double[][] getHistoryTicks(int tranId) {
        Request request = requestList.get(tranId);

        try {
            if (request == null) {
                return new double[0][3];
            }

            double[][] result = request.getHistoryTicks();
            return result;
        }
        catch (Throwable e) {
            staticLogger.error(e.getMessage(), e);
            return new double[0][3];
        }
    }

    private static class BarParameter {
        String category;
        String exchange;
        String symbol;
        String start;
        String finish;
        int resolution;

        public BarParameter(String[] param) {
            if (param.length < 6) {
                throw new IllegalArgumentException("Not enough parameters to process");
            }

            int i = 0;
            category = param[i++];
            exchange = param[i++];
            symbol = param[i++];
            start = param[i++];
            finish = param[i++];
            resolution = Integer.parseInt(param[i++]);
        }

        public String toString() {
            return "category=" + category + ", exchange=" + exchange + ", symbol=" + symbol + ", start=" + start
                    + ", finish=" + finish + ", resolution=" + resolution;
        }
    }
}
