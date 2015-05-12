package util;
import static util.Dates.now;
import static util.Errors.*;

import java.util.Date;
public class Times {

	public static long millisSince(long start) {
		return nowMillis() - start;
	}
	
	public static long millisSince(Date start) {
		return millisSince(start.getTime());
	}
	
	public static long reallyMillisSince(long start) {
		return System.currentTimeMillis() - start;
	}
	
	public static long reallyMillisSince(Date start) {
		return reallyMillisSince(start.getTime());
	}
	
	public static double reallySecondsSince(long start) {
	    return reallyMillisSince(start) / 1000;
	}
	
	public static void sleepSeconds(long seconds) { 
		sleep(seconds * 1000);
	}
	
	public static long nowMillis() {
		return now().getTime();
	}
	
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw bomb(e);
		}
	}

	public static double perReallyMilliSince(long start, int count) {
	    return count / reallyMillisSince(start);
	}
}
