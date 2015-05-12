package com.fftw.bloomberg.cmfp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.ConfigError;
import quickfix.FieldConvertError;

/**
 * Session settings for the proprietary Bloomberg CMFP
 * settings.
 * 
 */
public class CmfSessionSettings
{
    private Properties variableValues = System.getProperties();
    
    public boolean isSetting (CmfSessionID sessionID, String key)
    {
        return getOrCreateSessionProperties(sessionID).getProperty(key) != null;
    }

    private Properties getOrCreateSessionProperties(CmfSessionID sessionID) {
        Properties p = cmfSections.get(sessionID);
        if (p == null) {
            p = new Properties(cmfSections.get(DEFAULT_SESSION_ID));
            cmfSections.put(sessionID, p);
        }
        return p;
    }
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private Map<CmfSessionID, Properties> cmfSections = new HashMap<CmfSessionID, Properties>();

    private static final CmfSessionID DEFAULT_SESSION_ID = new CmfSessionID("DEFAULT", "0", "0",
        "0");

    private static final String SESSION_SECTION_NAME = "cmfsession";

    private static final String DEFAULT_SECTION_NAME = "default";

    public static final String PRICING_NUMBER = "PricingNumber";

    public static final String SPEC_VERSION = "SpecVersion";

    public static final String PORT = "SocketConnectPort";

    public static final String HOST = "SocketConnectHost";

    public static final String SESSION_QUALIFIER = "CmfSessionQualifier";

    // This was using the line.separator system property but that caused
    // problems with moving configuration files between *nix and Windows.
    private static final String NEWLINE = "\r\n";

    public CmfSessionSettings ()
    {
        cmfSections.put(DEFAULT_SESSION_ID, new Properties());
    }

    public CmfSessionSettings (InputStream stream) throws ConfigError
    {
        this();
        load(stream);
    }

    /**
     * Loads session settings from a file.
     *
     * @param filename
     *            the path to the file containing the session settings
     */
    public CmfSessionSettings(String filename) throws ConfigError {
        this();
        InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
        if (in == null) {
            try {
                in = new FileInputStream(filename);
            } catch (IOException e) {
                throw new ConfigError(e.getMessage());
            }
        }
        load(in);
    }
    
    private void load (InputStream inputStream) throws ConfigError
    {
        try
        {
            Properties currentSection = null;
            String currentSectionId = null;
            Tokenizer tokenizer = new Tokenizer();
            Reader reader = new InputStreamReader(inputStream);
            Tokenizer.Token token = tokenizer.getToken(reader);
            while (token != null)
            {
                if (token.getType() == Tokenizer.SECTION_TOKEN)
                {
                    storeSection(currentSectionId, currentSection);
                    if (token.getValue().equalsIgnoreCase(DEFAULT_SECTION_NAME))
                    {
                        currentSectionId = DEFAULT_SECTION_NAME;
                        currentSection = getSessionProperties(DEFAULT_SESSION_ID);
                    }
                    else if (token.getValue().equalsIgnoreCase(SESSION_SECTION_NAME))
                    {
                        currentSectionId = SESSION_SECTION_NAME;
                        currentSection = new Properties(getSessionProperties(DEFAULT_SESSION_ID));
                    }
                }
                else if (token.getType() == Tokenizer.ID_TOKEN)
                {
                    Tokenizer.Token valueToken = tokenizer.getToken(reader);
                    if (currentSection != null && token != null)
                    {
                        String value = interpolate(valueToken.getValue());
                        currentSection.put(token.getValue(), value);
                    }
                }
                token = tokenizer.getToken(reader);
            }
            storeSection(currentSectionId, currentSection);
        }
        catch (IOException e)
        {
            ConfigError configError = new ConfigError(e.getMessage());
            configError.fillInStackTrace();
            throw configError;
        }
    }
    
    /**
     * Set properties that will be the source of variable values in the settings. A variable
     * is of the form ${variable} and will be replaced with values from the
     * map when the setting is retrieved.
     * 
     * By default, the System properties are used for variable values. If
     * you use this method, it will override the defaults so use the Properties
     * default value mechanism if you want to chain a custom properties object
     * with System properties as the default.
     * 
     * <code><pre>
     * // Custom properties with System properties as default
     * Properties myprops = new Properties(System.getProperties());
     * myprops.load(getPropertiesInputStream());
     * settings.setVariableValues(myprops);
     * 
     * // Custom properties with System properties as override
     * Properties myprops = new Properties();
     * myprops.load(getPropertiesInputStream());
     * myprops.putAll(System.getProperties());
     * settings.setVariableValues(myprops);
     * </pre></code>
     * 
     * @param variableValues
     * 
     * @see java.util.Properties
     * @see java.lang.System
     */
    public void setVariableValues(Properties variableValues) {
        this.variableValues = variableValues;
    }
    
    private void storeSection (String currentSectionId, Properties currentSection)
    {
        if (currentSectionId != null && currentSectionId.equals(SESSION_SECTION_NAME))
        {
            CmfSessionID sessionId = new CmfSessionID(currentSection.getProperty(PRICING_NUMBER),
                currentSection.getProperty(SPEC_VERSION), currentSection.getProperty(HOST),
                currentSection.getProperty(PORT));
            cmfSections.put(sessionId, currentSection);
            currentSectionId = null;
            currentSection = null;
        }
    }

    private Pattern variablePattern = Pattern.compile("\\$\\{(.+?)}");

    private String interpolate(String value) {
        if (value == null || value.indexOf('$') == -1) {
            return value;
        }
        StringBuffer buffer = new StringBuffer();
        Matcher m = variablePattern.matcher(value);
        while (m.find()) {
            if (m.start() > 0 && value.charAt(m.start() - 1) == '\\') {
                continue;
            }
            String variable = m.group(1);
            String variableValue = variableValues.getProperty(variable);
            if (variableValue != null) {
                m.appendReplacement(buffer, variableValue);
            }
        }
        m.appendTail(buffer);
        return buffer.toString();
    }
    
    /**
     * Adds defaults to the settings. Will not delete existing settings not
     * overlapping with the new defaults, but will overwrite existing settings
     * specified in this call.
     *
     * @param defaults
     */
    public void setDefaultValues(Map<Object,Object> defaults) {
        getOrCreateSessionProperties(DEFAULT_SESSION_ID).putAll(defaults);
    }
    
    /**
     * Sets a string-valued session setting.
     *
     * @param sessionID
     *            the session ID
     * @param key
     *            the setting key
     * @param value
     *            the string value
     */
    public void setString(CmfSessionID sessionID, String key, String value) {
        getOrCreateSessionProperties(sessionID).setProperty(key, value.trim());
    }
    
    /**
     * Return the settings for a session as a Properties object.
     * 
     * @param sessionID
     * @return the Properties object with the session settings
     * @throws ConfigError
     * @see java.util.Properties
     */
    public Properties getSessionProperties (CmfSessionID sessionID) throws ConfigError
    {
        Properties p = cmfSections.get(sessionID);
        if (p == null)
        {
            throw new ConfigError("Session not found");
        }
        return p;
    }

    /* **************************************** */
    private static class Tokenizer
    {
        public static final int NONE_TOKEN = 1;

        public static final int ID_TOKEN = 2;

        public static final int VALUE_TOKEN = 3;

        public static final int SECTION_TOKEN = 4;

        private static class Token
        {
            private int type;

            private String value;

            public Token (int type, String value)
            {
                super();
                this.type = type;
                this.value = value;
            }

            public int getType ()
            {
                return type;
            }

            public String getValue ()
            {
                return value;
            }

            public String toString ()
            {
                return type + ": " + value;
            }
        }

        private char ch = '\0';

        private StringBuffer sb = new StringBuffer();

        private Token getToken (Reader reader) throws IOException
        {
            if (ch == '\0')
            {
                ch = nextCharacter(reader);
            }
            skipWhitespace(reader);
            if (isLabelCharacter(ch))
            {
                sb.setLength(0);
                do
                {
                    sb.append(ch);
                    ch = nextCharacter(reader);
                } while (isLabelCharacter(ch));
                return new Token(ID_TOKEN, sb.toString());
            }
            else if (ch == '=')
            {
                ch = nextCharacter(reader);
                sb.setLength(0);
                if (isValueCharacter(ch))
                {
                    do
                    {
                        sb.append(ch);
                        ch = nextCharacter(reader);
                    } while (isValueCharacter(ch));
                }
                return new Token(VALUE_TOKEN, sb.toString().trim());
            }
            else if (ch == '[')
            {
                ch = nextCharacter(reader);
                Token id = getToken(reader);
                // check ]
                ch = nextCharacter(reader); // skip ]
                return new Token(SECTION_TOKEN, id.getValue());
            }
            else if (ch == '#')
            {
                do
                {
                    ch = nextCharacter(reader);
                } while (isValueCharacter(ch));
                return getToken(reader);
            }
            return null;
        }

        private boolean isNewLineCharacter (char ch)
        {
            return NEWLINE.indexOf(ch) != -1;
        }

        private boolean isLabelCharacter (char ch)
        {
            return !isEndOfStream(ch) && "[]=#".indexOf(ch) == -1;
        }

        private boolean isValueCharacter (char ch)
        {
            return !isEndOfStream(ch) && !isNewLineCharacter(ch);
        }

        private boolean isEndOfStream (char ch)
        {
            return (byte)ch == -1;
        }

        private char nextCharacter (Reader reader) throws IOException
        {
            return (char)reader.read();
        }

        private void skipWhitespace (Reader reader) throws IOException
        {
            if (Character.isWhitespace(ch))
            {
                do
                {
                    ch = nextCharacter(reader);
                } while (Character.isWhitespace(ch));
            }
        }
    }

    public Iterator<CmfSessionID> cmfSectionIterator ()
    {
        HashSet<CmfSessionID> nondefaultSessions = new HashSet<CmfSessionID>(cmfSections.keySet());
        nondefaultSessions.remove(DEFAULT_SESSION_ID);
        return nondefaultSessions.iterator();
    }

    /**
     * Gets a string from the default section of the settings.
     *
     * @param key
     * @return the default string value
     * @throws ConfigError
     * @throws FieldConvertError
     */
    public String getString(String key) throws ConfigError, FieldConvertError {
        return getString(DEFAULT_SESSION_ID, key);
    }
    
    public String getString (CmfSessionID sessionID, String key) throws ConfigError, FieldConvertError
    {
        String value = interpolate(getSessionProperties(sessionID).getProperty(key));
        if (value == null) {
            throw new ConfigError(key + " not defined");
        }
        return value;
    }

    public long getLong (CmfSessionID sessionID, String key) throws ConfigError, FieldConvertError
    {
        try {
            return Long.parseLong(getString(sessionID, key));
        } catch (NumberFormatException e) {
            throw new FieldConvertError(e.getMessage());
        }
    }

    
    private void writeSection(String sectionName, PrintWriter writer, Properties properties) {
        writer.println(sectionName);
        Iterator<Object> p = properties.keySet().iterator();
        while (p.hasNext()) {
            String key = (String) p.next();
            writer.print(key);
            writer.print("=");
            writer.println(properties.getProperty(key));
        }
    }
    
    public double getDouble(CmfSessionID sessionID, String key) throws ConfigError, FieldConvertError {
        try {
            return Double.parseDouble(getString(sessionID, key));
        } catch (NumberFormatException e) {
            throw new FieldConvertError(e.getMessage());
        }
    }
    
    public void toString(PrintWriter writer) {
        try {
            Iterator<CmfSessionID> s = cmfSectionIterator();
            while (s.hasNext()) {
                try {
                    writeSection("[CMFSESSION]", writer, getSessionProperties(s.next()));
                } catch (ConfigError e) {
                    log.error("Invalid session", e);
                }
            }
        } finally {
            writer.flush();
        }
    }
    
    public String toString() {
        StringWriter writer = new StringWriter();
        toString(new PrintWriter(writer));
        return writer.toString();
    }
}
