package malbec.fix.util;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Properties;

import quickfix.FileStoreFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;

/**
 * Help us integrate with QuickFIX/J.
 * 
 * There are things in QFJ that we want to use and set that are either not
 * public or too difficult to do. This 'helper' make it easier for the
 * application to get QFJ to act the way we want.
 */
public class QfjHelper
{

    public static SessionSettings createSessionSettings (Properties props)
    {
        SessionSettings settings = new SessionSettings();

        // modify the file path to have the target comp ID at the end
        if (props.containsKey(FileStoreFactory.SETTING_FILE_STORE_PATH))
        {
            String pathBase = props.getProperty(FileStoreFactory.SETTING_FILE_STORE_PATH);
            String targetCompId = props.getProperty("TargetCompID");

            File tmpPath = new File(pathBase, targetCompId);
            // make a copy so we don't have any side effects
            Properties newProps = new Properties();
            newProps.putAll(props);
            newProps.setProperty(FileStoreFactory.SETTING_FILE_STORE_PATH, tmpPath
                .getAbsolutePath());
            props = newProps;
        }
        addSessionSettingSection(settings, "session", props);

        return settings;
    }

    /**
     * Work around the fact that <code>storeSection</code> is private and that
     * most of QFJ requires a <code>SessionSettings</code> object to be
     * configured or used.
     * 
     * @param settings
     * @param sectionId
     * @param sectionSettings
     */
    private static void addSessionSettingSection (SessionSettings settings, String sectionId,
        Properties sectionSettings)
    {
        try
        {
            Class<?> partypes[] = new Class[1];
            partypes[0] = SessionSettings.class;

            final Method[] methods = settings.getClass().getDeclaredMethods();
            // loop over the list since finding it directly does not work
            Method meth = null;
            for (int i = 0; i < methods.length; i++)
            {
                meth = methods[i];
                if (meth.getName().equals("storeSection"))
                {
                    break;
                }
            }

            meth.setAccessible(true);

            Object arglist[] = new Object[2];
            arglist[0] = sectionId;
            arglist[1] = sectionSettings;

            meth.invoke(settings, arglist);
        }
        catch (Throwable e)
        {
            System.err.println(e);
        }
    }

    /**
     * Execute a non-public void no-argument method.
     * 
     * Created to be used during testing.
     * 
     * @param dest
     * @param methodName
     */
    public static void executeVoidNoArgMethod (Object dest, String methodName)
    {
        try
        {
            Class<?> partypes[] = new Class[1];
            partypes[0] = SessionSettings.class;

            final Method[] methods = dest.getClass().getDeclaredMethods();
            // loop over the list since finding it directly does not work
            Method meth = null;
            for (int i = 0; i < methods.length; i++)
            {
                meth = methods[i];
                if (meth.getName().equals(methodName))
                {
                    break;
                }
            }

            meth.setAccessible(true);

            Object arglist[] = new Object[0];

            meth.invoke(dest, arglist);
        }
        catch (Throwable e)
        {
            System.err.println(e);
        }
    }

    /**
     * Create an instance of a SessionID to be used when looking up session
     * setting properties
     * 
     * @param config
     * @return
     */
    public static SessionID createSessionId (Properties config)
    {
        String beginString = config.getProperty("BeginString");
        String targetCompId = config.getProperty("TargetCompID");
        String senderCompId = config.getProperty("SenderCompID");
        return new SessionID(beginString, senderCompId, targetCompId);
    }

}
