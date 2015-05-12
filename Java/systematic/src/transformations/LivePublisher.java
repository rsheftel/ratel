package transformations;

import static util.Objects.*;

import java.util.*;

import jms.*;
import systemdb.data.*;
import transformations.Transformation.*;

public class LivePublisher implements SeriesPublisher {

	@Override public void publish(SeriesDefinition d, double value) {
		publish(d, String.valueOf(value));
	}

	@Override public void publish(OutputValues outputs) {
	    publishToActiveMq(outputs);
	}
	
	@Override public void publish(SeriesDefinition d, String s) {
		OutputValues outputs = new OutputValues();
		outputs.put(d, s);
        publishToActiveMq(outputs);
	}

    private void publishToActiveMq(OutputValues outputs) {
        Map<QTopic, Fields> topics = emptyMap();
        for (SeriesDefinition def : outputs.keySet()) {
            QTopic topic = def.topic();
            if(!topics.containsKey(topic))
                topics.put(topic, new Fields());
            def.addTo(topics.get(topic), outputs.get(def));
        }
        for (QTopic topic : topics.keySet()) {
            Fields fields = topics.get(topic);
            topic.send(fields);
        }
    }
}
