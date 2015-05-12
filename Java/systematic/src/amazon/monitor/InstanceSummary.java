package amazon.monitor;

import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import jms.*;
import systemdb.data.*;
import amazon.*;

class InstanceSummary {
	private Map<String, ProcessSummary> processes = emptyMap();
	Date latestKnownCompletion;
	private QTopic topic;
    private int totalComplete;
    private List<Date> completionTimes = empty();
	
	public InstanceSummary(String id) {
		topic = new QTopic(CloudMonitor.instanceTopic(id));
	}
	
	public void received(STOResponse response) {
		processInfo(response.processId()).received(response);
		latestKnownCompletion = laterOf(latestKnownCompletion, response.completedAt());
		totalComplete++;
		completionTimes.add(response.completedAt());
		purgeCompletionTimesBefore(millisAgo(3 * response.runTimeMillis(), latestKnownCompletion));
		double minutes = (last(completionTimes).getTime() - first(completionTimes).getTime()) / 60000.0;
        double runsPerMinute = minutes == 0 ? 0 : (completionTimes.size() / minutes);
		Fields toPublish = new Fields();
		toPublish.put("NumRed", numRed());
		toPublish.put("NumGreen", numGreen());
		toPublish.put("RedTime", ymdHuman(redTime()));
		toPublish.put("Completed", totalComplete);
		toPublish.put("RunsPerMinute", runsPerMinute);
		toPublish.put("LastCompleted", ymdHuman(latestKnownCompletion));
		topic.send(toPublish);
	}

    private void purgeCompletionTimesBefore(Date start) {
        Collections.sort(completionTimes);
        while(first(completionTimes).before(start)) completionTimes.remove(0);
    }

    private ProcessSummary processInfo(String processId) {
		if (!processes.containsKey(processId)) 
			processes.put(processId, new ProcessSummary());
		return processes.get(processId);
	}

	public int numRed() {
		int total = 0;
		for (ProcessSummary summary : processes.values())
			total += summary.isRed() ? 1 : 0;
		return total;
	}

	public int completed() {
		int total = 0;
		for (ProcessSummary summary : processes.values())
			total += summary.completed;
		return total;
	}

	public Date latestKnownCompletion() {
		return latestKnownCompletion;
	}

	public int numGreen() {
		int total = 0;
		for (ProcessSummary summary : processes.values())
			total += summary.isGreen() ? 1 : 0;
		return total;
	}

	public Date redTime() {
		Date result = date("9999/12/31");
		for (ProcessSummary summary : processes.values()) {
			Date redTime = summary.redTime();
			if(redTime.before(result))
				result = redTime;
		}
		return result;
	}
	
}