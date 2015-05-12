import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

public class TimeSeriesService
{
    private Rengine rengine;

    private String argsForR[];

    public TimeSeriesService ()
    {
        if (!Rengine.versionCheck())
        {
            System.err.println("** Version mismatch - Java files don't match library version.");
            System.exit(1);
        }
        rengine = new Rengine(argsForR, false, new TSCallBacks());
        if (!rengine.waitForR())
        {
            System.out.println("Cannot load R");
            System.exit(1);
        }
    }

    public void retrieveOneTimeSeriesByName (String timeSeriesName, String dataSourceName) throws ParseException
    {
        REXP rexp;
        Calendar calendar = Calendar.getInstance();
        calendar.set(2007, 2, 22);
        SimpleDateFormat dateToStringFormatter = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat stringToDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String beginDate = dateToStringFormatter.format(calendar.getTime());
        calendar.set(2007, 2, 26);
        String endDate = dateToStringFormatter.format(calendar.getTime());
        rengine.eval("library(GSFCore)", false);
        rengine.eval("tsdb <- TimeSeriesDB()", false);
        rexp = rengine.eval("testData <- tsdb$retrieveOneTimeSeriesByName(\"" + timeSeriesName
            + "\", \"" + dataSourceName + "\", \"" + beginDate + "\", \"" + endDate + "\")");
        String[] dates = rengine.eval("as.character(index(testData))").asStringArray();
        for (String date : dates)
        {
            Date resultDate = stringToDateFormatter.parse(date);
            System.out.println(resultDate);
        }
        double[] values = rengine.eval("testData[,1]").asDoubleArray();
        for (double value : values)
        {
            System.out.println(value);
        }
        /*
         * double[] results = rexp.asDoubleArray(); for (double result: results) {
         * System.out.println(result); }
         */
        rengine.end();

    }

    public static void main (String[] args) throws ParseException
    {
        TimeSeriesService timeSeriesService = new TimeSeriesService();
        timeSeriesService.retrieveOneTimeSeriesByName("aapl close", "yahoo");
    }
}
