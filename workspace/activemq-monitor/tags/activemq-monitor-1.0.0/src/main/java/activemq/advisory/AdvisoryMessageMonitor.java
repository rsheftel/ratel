package activemq.advisory;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Topic;

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.DataStructure;
import org.apache.activemq.command.ProducerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import activemq.AbstractJmsMonitor;
import activemq.ConnectionTask;
import activemq.IJmsObserver;
import activemq.JmsException;

/**
 * Main interface to the ActiveMQ Monitoring
 * 
 * 
 */
public class AdvisoryMessageMonitor extends AbstractJmsMonitor<IJmsObserver<ActiveMQMessage>, ActiveMQMessage> implements MessageListener {

    public final Logger log = LoggerFactory.getLogger(getClass().getName());

    private List<ProducerInfo> producerList = new ArrayList<ProducerInfo>();
    private List<ConsumerInfo> consumerList = new ArrayList<ConsumerInfo>();

    private List<ConsumerInfo> noConsumerTopicList = new ArrayList<ConsumerInfo>();
    private List<Object> destinationList = new ArrayList<Object>();

    public AdvisoryMessageMonitor(String brokerUrl) {
        connectionTask = new ConnectionTask(brokerUrl);
    }

    public void startup() {
        super.startup();

        // subscribe to all topic advisors
        ActiveMQDestination destination = new ActiveMQTopic(">");

        Destination producerAdvisoryDest = AdvisorySupport.getProducerAdvisoryTopic(destination);
        Destination consumerAdvisoryDest = AdvisorySupport.getConsumerAdvisoryTopic(destination);
        // I have not been able to figure out what this returns
        Destination ntcad = AdvisorySupport.getNoTopicConsumersAdvisoryTopic(destination);
        Destination dat = AdvisorySupport.getDestinationAdvisoryTopic(destination);

        // AdvisorySupport.getExpiredTopicMessageAdvisoryTopic()

        try {
            MessageConsumer pc = connectionTask.getSession().createConsumer(producerAdvisoryDest);
            pc.setMessageListener(this);

            MessageConsumer cc = connectionTask.getSession().createConsumer(consumerAdvisoryDest);
            cc.setMessageListener(this);

            MessageConsumer ntc = connectionTask.getSession().createConsumer(ntcad);
            ntc.setMessageListener(this);

            MessageConsumer datc = connectionTask.getSession().createConsumer(dat);
            datc.setMessageListener(this);

        } catch (JMSException e) {
            log.error("Unable to create Advisory consumer", e);
            throw new JmsException("Unable to create Advisory consumer", e);
        }
    }

    public List<ProducerInfo> listProducers() {
        List<ProducerInfo> listCopy = new ArrayList<ProducerInfo>(producerList.size());
        listCopy.addAll(producerList);
        return listCopy;
    }

    public List<ConsumerInfo> listConsumers() {
        List<ConsumerInfo> listCopy = new ArrayList<ConsumerInfo>(consumerList.size());
        listCopy.addAll(consumerList);
        return listCopy;
    }

    public List<ConsumerInfo> listNoConsumerTopics() {
        List<ConsumerInfo> listCopy = new ArrayList<ConsumerInfo>(noConsumerTopicList.size());
        listCopy.addAll(noConsumerTopicList);
        return listCopy;
    }

    public void createTempTopicProducer() {
        try {
            Topic topic = createTempTopic();
            connectionTask.getSession().createProducer(topic);
        } catch (JMSException e) {
            log.error("Generated log", e);
            throw new JmsException("Unable to create TemporaryTopic", e);
        }
    }

    public void createTempTopicConsumer() {
        try {
            Topic topic = createTempTopic();
            connectionTask.getSession().createConsumer(topic);
        } catch (JMSException e) {
            log.error("Generated log", e);
            throw new JmsException("Unable to create TemporaryTopic", e);
        }
    }

    public Topic createTempTopic() {
        try {
            Topic topic = connectionTask.getSession().createTemporaryTopic();
            return topic;
        } catch (JMSException e) {
            log.error("Cannot create temporary topic", e);
            throw new JmsException("Cannot create temporary topic", e);
        }
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof ActiveMQMessage) {
            ActiveMQMessage aMsg = (ActiveMQMessage) message;
            onMessage(aMsg);
        } else {
            log.warn("Ignoring message type: " + message.getClass());
        }
    }

    protected void onMessage(ActiveMQMessage message) {
        DataStructure dataStruct = message.getDataStructure();

        if (dataStruct instanceof ProducerInfo) {
            producerList.add((ProducerInfo) message.getDataStructure());
            setUpdated(true);
        } else if (dataStruct instanceof ConsumerInfo) {
            consumerList.add((ConsumerInfo) message.getDataStructure());
            setUpdated(true);
        } else {
            log.warn("Ignoring data structure: " + dataStruct.getClass());
            setUpdated(true);
        }
        notifyObservers(message);
    }

    public List<Object> listDestinations() {
        List<Object> listCopy = new ArrayList<Object>(destinationList.size());
        listCopy.addAll(destinationList);
        return listCopy;
    }


}
