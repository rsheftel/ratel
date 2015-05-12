package transformations;

import static util.Dates.*;
import static util.Objects.*;
import junit.framework.*;

public class TestServerHeartbeat extends TestCase {
	public void testHeartbeat() throws Exception {
		MockPublisher pub = new MockPublisher();
		LiveTransformation.publisher = pub;
		ServerHeartBeat beat = new ServerHeartBeat(60, "LiveTransformation.TEST.HEARTBEAT");
		freezeNow();
		beat.runOnce();
		assertEquals(
			"CONTROL:||:LiveTransformation.TEST.HEARTBEAT:||:TimeStamp:||:" + yyyyMmDdHhMmSs(now()), 
			the(pub.published())
		);
	}
}
