package malbec.hawkeye;

public class HawkEyeConfiguration {

    private String brokerUrl;
    private String orderStatusBase;
    private String ferretStateTopic;

    public HawkEyeConfiguration(String brokerUrl, String orderStatusBase, String ferretStateTopic) {
        this.brokerUrl = brokerUrl;
        this.orderStatusBase = orderStatusBase;
        this.ferretStateTopic = ferretStateTopic;
    }

    /**
     * @return the brokerUrl
     */
    public String getBrokerUrl() {
        return brokerUrl;
    }

    /**
     * @param brokerUrl
     *            the brokerUrl to set
     */
    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    /**
     * @return the orderStatusBase
     */
    public String getOrderStatusBase() {
        return orderStatusBase;
    }

    /**
     * @param orderStatusBase
     *            the orderStatusBase to set
     */
    public void setOrderStatusBase(String orderStatusBase) {
        this.orderStatusBase = orderStatusBase;
    }

    /**
     * @return the ferretStateTopic
     */
    public String getFerretStateTopic() {
        return ferretStateTopic;
    }

    /**
     * @param ferretStateTopic
     *            the ferretStateTopic to set
     */
    public void setFerretStateTopic(String ferretStateTopic) {
        this.ferretStateTopic = ferretStateTopic;
    }

}
