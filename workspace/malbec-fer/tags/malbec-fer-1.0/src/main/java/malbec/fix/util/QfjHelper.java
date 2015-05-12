package malbec.fix.util;

import java.lang.reflect.Method;
import java.util.Properties;

import quickfix.SessionSettings;

/**
 * Help us integrate with QuickFIX/J.
 * 
 * There are things in QFJ that we want to use and set that are either not public or too difficult to do. This
 * 'helper' make it easier for the application to get QFJ to act the way we want.
 */
public class QfjHelper {

    public static SessionSettings createSessionSettings(Properties props) {
        SessionSettings settings = new SessionSettings();
        addSessionSettingSection(settings, "session", props);

        return settings;
    }

    /**
     * Work around the fact that <code>storeSection</code> is private and that most of QFJ requires a
     * <code>SessionSettings</code> object to be configured or used.
     * 
     * @param settings
     * @param sectionId
     * @param sectionSettings
     */
    private static void addSessionSettingSection(SessionSettings settings, String sectionId,
            Properties sectionSettings) {
        try {
            Class<?> partypes[] = new Class[1];
            partypes[0] = SessionSettings.class;

            final Method[] methods = settings.getClass().getDeclaredMethods();
            // loop over the list since finding it directly does not work
            Method meth = null;
            for (int i = 0; i < methods.length; i++) {
                meth = methods[i];
                if (meth.getName().equals("storeSection")) {
                    break;
                }
            }

            meth.setAccessible(true);

            Object arglist[] = new Object[2];
            arglist[0] = sectionId;
            arglist[1] = sectionSettings;

            meth.invoke(settings, arglist);
        } catch (Throwable e) {
            System.err.println(e);
        }
    }
    
    public static void executeVoidNoArgMethod(Object dest, String methodName) {
        try {
            Class<?> partypes[] = new Class[1];
            partypes[0] = SessionSettings.class;

            final Method[] methods = dest.getClass().getDeclaredMethods();
            // loop over the list since finding it directly does not work
            Method meth = null;
            for (int i = 0; i < methods.length; i++) {
                meth = methods[i];
                if (meth.getName().equals(methodName)) {
                    break;
                }
            }

            meth.setAccessible(true);

            Object arglist[] = new Object[0];

            meth.invoke(dest, arglist);
        } catch (Throwable e) {
            System.err.println(e);
        }
    }
    
}
