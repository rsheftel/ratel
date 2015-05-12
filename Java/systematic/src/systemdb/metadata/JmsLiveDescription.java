package systemdb.metadata;

import static util.Log.*;
import static util.Strings.*;
import static util.Systematic.*;

import java.util.*;

import jms.*;
import systemdb.data.*;

public class JmsLiveDescription implements LiveDataSource {
    private transient QTopic topicDONOTREFERENCE;
    public final String topicName;
    public final String source;
    public final String template;
    public final String dateField;
    public final String valueField;
    
    public JmsLiveDescription(String topicName, String source, String template) {
        this(topicName, source, template, null, null);
    }
    
    public JmsLiveDescription(String topic, String source, String dateField, String valueField) {
        this(topic, source, null, dateField, valueField);
    }
    
    private JmsLiveDescription(String topicName, String source, String template, String dateField, String valueField) {
        this.topicName = topicName;
        this.source = source;
        this.template = template;
        this.dateField = dateField;
        this.valueField = valueField;
    }

    public QTopic topic() {
        if(topicDONOTREFERENCE == null)  
            topicDONOTREFERENCE = new QTopic(topicName);
        return topicDONOTREFERENCE;
    }

    public void publish(Tick tick) {
        topic().send(tick.fields());
    }
    
    public void subscribe(final ObservationListener listener) {
        final String dateFieldName = dateField;
        final String valueFieldName = valueField;
        MessageReceiver receiver = new FieldsReceiver() {
            @Override public void onMessage(Fields fields) {
                try {
                    listener.onUpdate(fields.time(dateFieldName), fields.numeric(valueFieldName));
                } catch(RuntimeException e) {
                    err("exception caught processing message: " + fields, e);
                }
            }
        };
        topic().register(receiver);
    }

    public void subscribe(final TickListener listener) {
        MessageReceiver receiver = new FieldsReceiver() {
            @Override public void onMessage(Fields fields) { 
                Tick t = null;
                try {
                    t = new Tick(
                        fields.numeric("LastPrice"), 
                        fields.longMaybe("LastVolume"), 
                        fields.numeric("OpenPrice"), 
                        fields.numeric("HighPrice"), 
                        fields.numeric("LowPrice"), 
                        fields.time("Timestamp")
                    );
                    if(!fields.isLong("LastVolume"))
                        if (isLoggingTicks()) err("volume does not parse - set to 0: " + fields);
                } catch(RuntimeException e) {
                    err("error parsing tick: " + fields, e);                    
                }
                try {
                    if(t != null)
                        listener.onTick(t);
                } catch(RuntimeException e) {
                    err("exception caught processing tick: " + fields, e);
                }
            }
        };
        topic().register(receiver);
    }

    @Override public String toString() {
        return paren(commaSep(topicName, source, template, dateField, valueField));
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dateField == null) ? 0 : dateField.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((template == null) ? 0 : template.hashCode());
        result = prime * result + ((topicName == null) ? 0 : topicName.hashCode());
        result = prime * result + ((valueField == null) ? 0 : valueField.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final JmsLiveDescription other = (JmsLiveDescription) obj;
        if (dateField == null) {
            if (other.dateField != null) return false;
        } else if (!dateField.equals(other.dateField)) return false;
        if (source == null) {
            if (other.source != null) return false;
        } else if (!source.equals(other.source)) return false;
        if (template == null) {
            if (other.template != null) return false;
        } else if (!template.equals(other.template)) return false;
        if (topicName == null) {
            if (other.topicName != null) return false;
        } else if (!topicName.equals(other.topicName)) return false;
        if (valueField == null) {
            if (other.valueField != null) return false;
        } else if (!valueField.equals(other.valueField)) return false;
        return true;
    }

    public void publish(double value, Date date) {
        QTopic topic = topic();
        Fields fields = new Fields();
        fields.put(valueField, value);
        fields.put(dateField, date);
        topic.send(fields);
    }
    

    
    
}