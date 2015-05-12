package transformations;

import util.*;
import file.*;

public class Constants {
	public static final QDirectory LOG_HOME;
	public static final String FAILURE_ADDRESS = Systematic.failureAddress().address();
	public static final String TRANSPORT_ACTIVE_MQ = "ActiveMQ";

	static {
		String path = (isWindows() ? "V:" : "") + "/logs";
		LOG_HOME = new QDirectory(path);
		
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").matches(".*Windows.*");
	}
	
	public static String dataDirectory() {
		return isWindows() ? "V:/" : "/data/";
	}
}
