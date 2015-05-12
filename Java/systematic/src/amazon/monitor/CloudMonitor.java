/**
 * 
 */
package amazon.monitor;

import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import jms.*;
import systemdb.data.*;
import amazon.*;

public class CloudMonitor {

	private final Map<String, InstanceSummary> instances = emptyMap();
	private final QTopic topic;

	public CloudMonitor(int systemId, List<Instance> instances) {
		topic = new QTopic(topic(systemId));
		if(hasContent(instances))
			add(instances);
	}

	public static String topic(int systemId) {
		return "CLOUD_STO.instances." + systemId;
	}
	
	public static String instanceTopic(String instanceId) {
		return "CLOUD_STO." + instanceId;
	}
	
	public static String progressTopic(int systemId) {
	    return "CLOUD_STO.progress." + systemId;
	}

	public void add(List<Instance> extra) {
		for (Instance instance : extra)
			instances.put(instance.id(), new InstanceSummary(instance.id()));
		publishInstanceList();
		
	}

	private void publishInstanceList() {
		Fields fields = new Fields();
		fields.put("Instances", join(",", instances.keySet()));
		topic.send(fields);
	}

	public int instanceCount() {
		return instances.size();
	}

	public void received(STOResponse response) {
		if(response.instanceId().equals("LOCAL")) return;
		summary(response.instanceId()).received(response);
	}

	private InstanceSummary summary(String instance) {
		return instances.get(instance);
	}

	public int runsComplete(String instance) {
		return summary(instance).completed();
	}

	public Date lastRunCompleted(String instance) {
		return summary(instance).latestKnownCompletion();
	}

	public int numRed(String instance) {
		return summary(instance).numRed();
	}

	public int numGreen(String instance) {
		return summary(instance).numGreen();
	}

	public Date redTime(String instance) {
		return summary(instance).redTime();
	}

	
}