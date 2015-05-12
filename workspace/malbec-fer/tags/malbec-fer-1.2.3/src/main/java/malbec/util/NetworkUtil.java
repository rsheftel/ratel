package malbec.util;

import static malbec.util.StringUtils.upperCaseOrNull;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkUtil {

    private static String localHostName;

    private NetworkUtil() {}

    public static String getHostName() {
        try {
            if (localHostName == null) {
                localHostName = upperCaseOrNull(InetAddress.getLocalHost().getHostName());
            }
            return localHostName;
        } catch (UnknownHostException e) {
            return "UnknownHost";
        }
    }
}
