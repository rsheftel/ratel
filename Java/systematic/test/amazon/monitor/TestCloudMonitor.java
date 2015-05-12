package amazon.monitor;

import static util.Dates.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import jms.*;
import amazon.*;

public class TestCloudMonitor extends JMSTestCase {

	public void testStuff() throws Exception {
		String INSTANCE = "17";
		CloudMonitor monitor = new CloudMonitor(2468, list(new Instance(INSTANCE)));
		assertEquals(1, monitor.instanceCount());
		Date completedAt = date("2001/02/03 04:05:06");
		monitor.received(new STOResponse(1, INSTANCE, date("2001/02/03 03:35:06"), completedAt, 1234));
		assertEquals(1, monitor.runsComplete(INSTANCE));
		assertEquals(completedAt, monitor.lastRunCompleted(INSTANCE));
		monitor.received(new STOResponse(2, INSTANCE, date("2001/02/03 05:04:05"), date("2001/02/03 05:05:05"), 5678));
		freezeNow("2001/02/03 05:05:09");
		assertEquals(1, monitor.numRed(INSTANCE));
		assertEquals(1, monitor.numGreen(INSTANCE));
		monitor.received(new STOResponse(3, INSTANCE, completedAt, date("2001/02/03 04:25:07"), 1234));
		assertEquals(0, monitor.numRed(INSTANCE));
		assertEquals(2, monitor.numGreen(INSTANCE));
		assertEquals(date("2001/02/03 05:05:11"), monitor.redTime(INSTANCE));
		freezeNow("2001/02/03 05:05:11");
		assertEquals(1, monitor.numRed(INSTANCE));
		assertEquals(1, monitor.numGreen(INSTANCE));
	}
	
	public void testMQPublish() throws Exception {
		String INSTANCE = "17";
		int systemId = 2468;
		QTopic instanceList = new QTopic("CLOUD_STO.instances." + systemId);
		RecentFieldsKeeper listReceiver = instanceList.register(new RecentFieldsKeeper());
		QTopic instance1 = new QTopic("CLOUD_STO." + INSTANCE);
		RecentFieldsKeeper instanceReceiver = instance1.register(new RecentFieldsKeeper());
		CloudMonitor monitor = new CloudMonitor(systemId, list(new Instance(INSTANCE), new Instance("i-98765")));
		listReceiver.waitForMessage(1000);
		assertSetEquals(list("17","i-98765"), split(",", listReceiver.latest.get("Instances")));
		freezeNow("2001/02/03 05:05:09");
		monitor.received(new STOResponse(2, INSTANCE, date("2001/02/03 05:04:05"), date("2001/02/03 05:05:05"), 5678));
		instanceReceiver.waitForMessage(1000);
		monitor.received(new STOResponse(3, INSTANCE, date("2001/02/03 04:05:06"), date("2001/02/03 04:25:07"), 1234));
		instanceReceiver.waitForMessage(1000);
		assertEquals("0", instanceReceiver.latest.get("NumRed"));
		assertEquals("2", instanceReceiver.latest.get("NumGreen"));
		assertEquals("2001/02/03 05:05:11", instanceReceiver.latest.get("RedTime"));
	}
	
	
}
