package activemq.ui;

import activemq.broker.BrokerConfiguration;

/**
 * For classes that can be configured.
 * 
 */
public interface IConfigurable {

    void setConfiguration(BrokerConfiguration config);

    BrokerConfiguration getConfiguration();

    /**
     * Notify that the configuration has changed.
     * 
     * This is different from the {@code setConfiguration} as this may be called multiple 
     * times before we are ready to use the new configuration.
     * 
     */
    void configurationUpdated();
}
