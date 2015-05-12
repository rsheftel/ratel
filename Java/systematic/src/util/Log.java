package util;
import static util.Dates.*;
import static util.Objects.*;
import static util.Strings.*;

import java.io.*;
import java.util.*;

import file.*;

public class Log {

	private static PrintStream ERR_STREAM = System.err;
	private static PrintStream LOG_STREAM = System.out;
	private static boolean isDebuggingSql = true;
	private static boolean progressDots = true;
	private static ThreadLocal<Boolean> lineStarted = local(false);
	private static ThreadLocal<String> context = new ThreadLocal<String>();
    private static boolean priorIsDebuggingSql = true;
	
	public static String setContext(String context) {
	    String old = Log.context.get();
		Log.context.set(context);
		return old;
	}
	
	public static void info(String string) {
		if (lineStarted()) lineEnd("");
		logStream().println(prolog() + string);
	}

	private static boolean lineStarted() {
		if(lineStarted.get() == null) lineStarted.set(false);
		return lineStarted.get();
	}

	public static PrintStream errStream() {
		return ERR_STREAM;
	}

	public static PrintStream logStream() {
		return LOG_STREAM;
	}
	
	public static String prolog() {
		String nowString = nowFrozen() ? " (" + ymdHuman(now()) + ") " : "";
        return paren("j" + Thread.currentThread().getId()) + " " + yyyyMmDdHhMmSs(new Date()) + nowString  + context() + ": ";
	}

	private static String context() {
		return isEmpty(context.get()) ? "" : " " + context.get();
	}

	@Deprecated public static void debug(String string) {
		__Debug.debug(prolog() + string);
	}

    @Deprecated public static void debug(Object o) {
        debug("" + o);
    }

    @Deprecated public static <T> void debug(Collection<T> o) {
        if(isEmpty(o)) debug("EMPTY");
        int i = 0;
        for(T t : o) debug(i++ + " " + t);
    }
	
	public static void lineStart(String string) {
	    lineStart(string, true);
	}

    public static void lineStart(String string, boolean includeContext) {
        if(includeContext && !lineStarted()) logStream().print(prolog());
        logStream().print(string);
        lineStarted.set(true);
    }

	public static void linePart(String string) {
		logStream().print(string);
	}
	
	public static void lineEnd(String string) {
		lineStarted.set(false);
		logStream().println(string);
	}
	
	public static boolean debugSql() {
		return isDebuggingSql;
	}

	public static void doNotDebugSqlForever() {
	    setDebugSqlStateForever(false);
	}

    public static void debugSqlForever() {
        setDebugSqlStateForever(true);
    }
    
    @SuppressWarnings("deprecation") 
    public static void setDebugSqlStateForever(boolean debugState) {
        debugSql(debugState);
    }

    public static void restoreSqlDebugging() {
        debugSql(priorIsDebuggingSql);
    }

	@Deprecated
	public static void debugSql(boolean b) {
	    priorIsDebuggingSql = isDebuggingSql;
		isDebuggingSql = b;
	}

	public static void progressDots(boolean b) {
		progressDots = b;
	}
	
	public static void dot() {
		dot(".");
	}

	public static void dot(String type) {
		if(!lineStarted()) lineStart("");
		if (progressDots) linePart(type);
	}

	public static void err(String string) {
		errStream().println(prolog() + string);
	}

	public static void info(String string, Exception e) {
		info(string);
		e.printStackTrace();
	}
	
	public static void err(String string, Throwable e) {
	    err(string);
	    e.printStackTrace(ERR_STREAM);
	}
	
	public static String errMessage(Throwable t) {
	    StringWriter w = new StringWriter();
        t.printStackTrace(new PrintWriter(w));
        return w.toString();
	}

	@Deprecated
	public static void stack(String message) {
		lineStart(message + "\n");
		new Exception().printStackTrace(logStream());
		lineEnd("");
	}

	public static void setToSystem() {
		LOG_STREAM = System.out;
		ERR_STREAM = System.err;
	}

	public static void setFile(QFile file) {
		setBothStreams(file.printAppender());
	}

	public static void setFile(String file) {
	    setFile(new QFile(file));
	}
	
    public static void setBothStreams(PrintStream logStream) {
        LOG_STREAM = logStream;
        ERR_STREAM = logStream;
    }

    private static boolean logVerbose = toBoolean(System.getenv("LOG_VERBOSE"), false);
    static { info("log verbose = " + logVerbose); }
    
    @Deprecated
    public static boolean setVerboseLogging(boolean beVerbose) {
        return setVerboseLoggingForever(beVerbose);
    }

    public static boolean setVerboseLoggingForever(boolean beVerbose) {
        info("log verbose = " + beVerbose + " was " + logVerbose);
        boolean oldLogVerbose = logVerbose;
        logVerbose = beVerbose;
        return oldLogVerbose;
    }
    
    public static boolean verbose() {
    	return logVerbose;
    }
	
	
}
