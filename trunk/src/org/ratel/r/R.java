package org.ratel.r;

import org.rosuda.JRI.*;

import static org.ratel.r.Util.*;

public class R {
    
    private static final class Callbacks implements RMainLoopCallbacks {
        private final LogWrapper logger;
        private boolean loggingOff = false;

        public Callbacks(LogWrapper logWrapper) {
            logger = logWrapper;
        }

        @Override public void rBusy(Rengine re, int which) {}

        @Override public String rChooseFile(Rengine re, int newFile) {
            return null;
        }

        @Override public void rFlushConsole(Rengine re) {}

        @Override public void rLoadHistory(Rengine re, String filename) {}

        @Override public String rReadConsole(Rengine re, String prompt, int addToHistory) {
            return null;
        }

        @Override public void rSaveHistory(Rengine re, String filename) {}

        @Override public void rShowMessage(Rengine re, String message) {
            if (isEmpty(message)) return;
            rWriteConsole(re, message);
        }

        @Override public void rWriteConsole(Rengine re, String text) {
            if (loggingOff) return;
            if (isEmpty(text)) return;
            if (logger.isActive()) logger.info(text);
            else bomb("no logger set - replace this line with your logger or set a LogWriter");  // TODO: add logging: log(text);
        }
    }

    private static final Rengine ENGINE;
    public static final char QUOTE = '\'';
    public static final char DQUOTE = '"';
    static LogWrapper logWrapper = new LogWrapper();
    
    static class LogWrapper { // used to side-step synchronize issue with static methods 
        LogWriter log;

        public void info(String message) {
            if (log != null) log.info(message);
        }

        public boolean isActive() {
            return log != null;
        }

        public void set(LogWriter newLog) {
            if (isActive()) log.close();
            log = newLog;
        }
    }
    static {
        Callbacks callbacks = new Callbacks(logWrapper);
        // TODO: add logging: Log.lineStart("starting R ...");
        callbacks.loggingOff = true;

        ENGINE = new Rengine(array("--vanilla"), false, callbacks);
        bombUnless(ENGINE.waitForR(), "Cannot load R!");
        // TODO: load R libraries here
        //r("library(Live)");
        callbacks.loggingOff = false;
        // TODO: add logging: Log.lineEnd("done");
    }

    public static String rQuote(String string, char enclosing) {
        bombUnless(list(QUOTE, DQUOTE).contains(enclosing), "R only supports quoting with ' and \"");
        string = string.replaceAll("\\\\", "\\\\\\\\");
        return string.replaceAll(""+ enclosing, "\\\\" + enclosing);
    }

    public static String rString(String rExpression) {
        String[] result = rStrings(rExpression);
        bombNull(result, "null result in result of \n\t" + rExpression);
        bombIf(result.length != 1, "not exactly 1 element in result of \n" + rExpression);
        return result[0];    
    }

    public static String[] rStrings(String rExpression) {
        REXP result = r(rExpression);
        RVector vector = result.asVector();
        if (vector != null && vector.isEmpty()) return new String[0];
        
        String[] stringArray = result.asStringArray();
        if(stringArray != null) return stringArray;
        int type = result.getType();
        bombIf(type == REXP.XT_NULL, "problem running \n" + rExpression + "\nMaybe there is a syntax error.  Maybe you returned NULL.  Maybe R hates you.");
        throw bomb("result of " + rExpression + " was not string.  Was " + REXP.xtName(type));
    }

    public static double rDouble(String rExpression) {
        double[] result = rDoubles(rExpression);
        bombNull(result, "null result in result of \n\t" + rExpression);
        bombIf(result.length != 1, "not exactly 1 element in result of " + rExpression);
        return result[0];    
    }
    
    public static double[] rDoubles(String rExpression) {
        return rTyped(rExpression, REXP.XT_ARRAY_DOUBLE).asDoubleArray();
    }
    
    public static int rInt(String rExpression) {
        int[] result = rInts(rExpression);
        bombNull(result, "null result in result of \n\t" + rExpression);
        bombIf(result.length != 1, "not exactly 1 element in result of " + rExpression);
        return result[0];
    }
    
    public static int[] rInts(String rExpression) {
        return rTyped(rExpression, REXP.XT_ARRAY_INT).asIntArray();
    }

    private static REXP rTyped(String rExpression, int expectedType) {
        REXP result = r(rExpression);
        int type = result.getType();
        bombUnless(type == expectedType, 
            "result of " + rExpression + " was not " + REXP.xtName(expectedType) + ".  Was " + REXP.xtName(type)
        );
        return result;
    }
    
    public static synchronized REXP r(String rExpression) {
        REXP result = ENGINE.eval(rExpression);
        if(result == null)
            bomb("Error running r code:\n" + rExpression + "\n" + ENGINE.eval("geterrmessage()").asString());
        return result;
    }
    
    public static void rExecute(String rExpression) {
        r(rExpression);
    }

    public static void setLogger(LogWriter log) {
        logWrapper.set(log);
    }

    public static void rLog(String message) {
        logWrapper.info(message);
    }
    
    public static void clearLogger() {
        logWrapper.set(null);
    }

    public static void main(String[] args) {
        rString("ts.array <- TimeSeriesFile$readTimeSeries(file); length(ts.array)");
        
    }
    
}
