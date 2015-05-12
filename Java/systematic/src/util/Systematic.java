package util;

import static util.Errors.*;
import static transformations.Constants.*;
import static util.Strings.*;

import java.io.*;
import java.net.*;
import java.util.*;

import transformations.*;

import mail.*;

import db.*;
import file.*;

public class Systematic {
    public static String[] JAVA_LIB_PARTS = {"Java", "systematic", "lib"};
    public static String[] QRUN_PARTS = {"dotNET", "QRun", "bin", "Release"};
    
	private static final Properties PROPS = new Properties();
    static { 
		try {
			PROPS.load(Db.class.getClassLoader().getResourceAsStream("systematic.properties"));
		} catch (IOException e1) {
			throw bomb("could not load systematic.properties as a resource", e1);
		}
	}
	
	public static QDirectory mainDir() { 
	    return new QDirectory(System.getenv("MAIN"));
	}
	
	public static String dbServer() {
		return property("SERVER");
	}
	
	public static String dbUser() {
		return property("USER");
	}

	public static String dbPassword() {
		return property("PASSWORD");
	}


	private static String property(String key) {
		return bombNull(
			PROPS.getProperty(key), 
			"no property for " + key + " defined in systematic.properties on classpath"
		);
	}

	public static boolean isDevDb() {
		return dbServer().equals("sqldevts");
	}
	
	public static String hostname() {
	    try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw bomb("no localhost?!?!?!?", e);
        }
	}
	
	public static String username() {
	    return System.getProperty("user.name");
	}

	public static QDirectory logsDir() {
        return new QDirectory(isWindows() ? "C:\\logs" : "/logs");
    }

    public static EmailAddress failureAddress() {      
        String address = System.getenv("FAILURE_ADDRESS");
        address = isEmpty(address) ? "us" : address;
        return new EmailAddress(address);
    }

    public static boolean isLoggingTicks() {      
        String address = System.getenv("LOG_TICKS");
        return isEmpty(address) ? true : address.equals("TRUE");
    }
    
    static QDirectory fakeData;
    public static void setFakeDataDirectoryForTest(QDirectory fake) { 
        fakeData = fake;
    }
    
    public static QDirectory dataDirectory() {
        return fakeData == null ? new QDirectory(Constants.dataDirectory()) : fakeData;
    }
	

}
