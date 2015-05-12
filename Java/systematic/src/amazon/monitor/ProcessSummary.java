/**
 * 
 */
package amazon.monitor;

import static util.Dates.*;
import static util.Times.*;

import java.util.Date;

import amazon.STOResponse;

class ProcessSummary {
	int completed;
	Date latestKnownCompletion;
	long lastRuntimeMillis;
	
	public void received(STOResponse response) {
		completed++;
		latestKnownCompletion = laterOf(latestKnownCompletion, response.completedAt());
		lastRuntimeMillis = response.runTimeMillis();
	}
	
	public boolean isRed() {
		return millisSince(latestKnownCompletion) > 2 * lastRuntimeMillis + 1000;
	}

	public boolean isGreen() {
		return !isRed();
	}

	public Date redTime() {
		return millisAhead(2 * lastRuntimeMillis + 2000, latestKnownCompletion);
	}
}