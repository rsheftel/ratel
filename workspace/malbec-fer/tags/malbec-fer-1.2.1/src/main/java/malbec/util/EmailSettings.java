package malbec.util;

import java.util.Properties;

public class EmailSettings {

    private static final String MAIL_TO = "mail.to";

    private static final String MAIL_FROM = "mail.from";

    private static final String MAIL_HOST = "mail.host";

    /**
     * There is only one server - hardcode it
     */
    private String hostname = "mail.fftw.com";

    // Over-ride these from the configuration
    private String fromAddress = "Test Email <DevTest@fftw.com>";

    private String errorToList = "MFranz@fftw.com";

    // we are probably not going to be using these....
//    private String warnToList = "MFranz@fftw.com";
//    private String infoToList = "MFranz@fftw.com";

    public EmailSettings() {
    }
    
    public EmailSettings(Properties props) {
        if (props.containsKey(MAIL_HOST)) {
            hostname = props.getProperty(MAIL_HOST);
        }

        if (props.containsKey(MAIL_FROM)) {
            fromAddress = props.getProperty(MAIL_FROM);
        }

        if (props.containsKey(MAIL_TO)) {
            errorToList = props.getProperty(MAIL_TO);
        }
    }

    public void setErrorToAddress(String errorToList) {
        this.errorToList = errorToList;
    }

    public Properties getAsProperties() {
        Properties props = new Properties();
        props.setProperty(MAIL_HOST, hostname);
        props.setProperty(MAIL_FROM, fromAddress);
        props.setProperty(MAIL_TO, errorToList);

        return props;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getErrorToList() {
        return errorToList;
    }

}
