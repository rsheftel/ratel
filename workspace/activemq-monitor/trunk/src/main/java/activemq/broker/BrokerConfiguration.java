package activemq.broker;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Contain all of the values that are configurable.
 * 
 * 
 */
@SuppressWarnings("serial")
public class BrokerConfiguration implements Serializable {
//*************
    // All ports must be between 32000 and 65535
    // EM Broker nyws751:63636 - 1099
    // Market data  amqmktdata:63636 - 9393 (nysrv61)
    // Positions amqpositions:63636 - 9393 (nysrv31)
    // FERET amqfersrv:61600 - 50100 (nysrv31)
    
//*************    
    
    // Default is to use Malbec market data
    private long id = System.currentTimeMillis();  // incase we change the name
    private String serverName; // = "amqmktdata";
    private String serviceName;
    private int jmsPort; // = 63636;
    private int jmxPort; // = 9393;
    
    private Map<String,String> jmsUrlParameters = new HashMap<String, String>();

    public BrokerConfiguration(BrokerConfiguration other) {
        setServiceName(other.getServiceName());
        setServerName(other.getServerName());
        setJmsPort(other.getJmsPort());
        setJmxPort(other.getJmxPort());
    }

    public BrokerConfiguration() {
    }

    public BrokerConfiguration(String serverName, int jmsPort, int jmxPort) {
        this.serverName = serverName;
        this.jmsPort = jmsPort;
        this.jmxPort = jmxPort;
    }

    public long getID() {
        return id;
    }
    
    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getJmsPort() {
        return jmsPort;
    }

    public void setJmsPort(int jmsPort) {
        this.jmsPort = jmsPort;
    }

    public int getJmxPort() {
        return jmxPort;
    }

    public void setJmxPort(int jmxPort) {
        this.jmxPort = jmxPort;
    }

    public void addJmsProperty(String propertyName, String propertyValue) {
        jmsUrlParameters.put(propertyName, propertyValue);
    }
    
    /**
     * Return a JMX url following this pattern:
     * <tt>
     * service:jmx:rmi:///jndi/rmi://<host name>:<port>/jmxrmi
     * </tt>
     * 
     * @return
     */
    public String getJmxUrl() {
        StringBuilder sb = new StringBuilder(256);
        
        sb.append("service:jmx:rmi:///jndi/rmi://");
        sb.append(getServerName()).append(":");
        sb.append(getJmxPort()).append("/jmxrmi");
        
        return sb.toString();
    }

    /**
     * Return the JMS url following this pattern:
     * <tt>
     * tcp://<host name>:<jms port>
     * </tt>
     * 
     * @return
     */
    public String getJmsUrl() {
        StringBuilder sb = new StringBuilder(256);
        
        sb.append("tcp://").append(getServerName()).append(":").append(getJmsPort());
        
        if (jmsUrlParameters.size() > 0) {
            sb.append("?");
            int count = jmsUrlParameters.size();
            int index = 0;
            for (Map.Entry<String, String> entry : jmsUrlParameters.entrySet()) {
                sb.append(entry.getKey()).append(entry.getValue());
                index++;
                if (index< count) {
                    sb.append("&");
                }
            }
        }
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (this == obj) {
            return true;
        }
        
        if (! (obj instanceof BrokerConfiguration)) {
            return false;
        }
        
        BrokerConfiguration other = (BrokerConfiguration) obj;
        
        return compareStrings(serverName, other.serverName) && compareStrings(serviceName, other.serviceName)
                && jmsPort == other.jmsPort && jmxPort == other.jmxPort;
    }

    private boolean compareStrings(String str1, String str2) {
        if (str1 != null) {
            return str1.equals(str2);
        } else if (str2 != null) {
            return false;
        } else {
            // both are null
            return true;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return hashCode(serverName) + hashCode(serviceName) + jmsPort * (jmxPort + 17);
    }

    private int hashCode(String string) {
        if (string == null) {
            return 0;
        } else {
            return string.hashCode();
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        
        sb.append("ServerName=").append(serverName);
        sb.append(", JmsPort=").append(jmsPort);
        sb.append(", JmxPort=").append(jmxPort);
        
        return sb.toString();
    }
   
}
