package jms;

import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import util.*;

public abstract class MessageResponder extends MessageOnlyReceiver {

	public class MessageFailedException extends Exception {
		public MessageFailedException(String error, RuntimeException e) { super(error, e); }

		private static final long serialVersionUID = 1L; 
	}
	
	private static final Map<String, String> NORMAL_REPLY = emptyMap();
	private static final Map<String, String> ERROR = emptyMap();
	private static final Map<String, String> HEARTBEAT = emptyMap();
	private static boolean heartbeatOn = true;
	static { 
		HEARTBEAT.put(Envelope.IS_HEARTBEAT_PROPERTY, "true");
		ERROR.put(Envelope.IS_ERROR_PROPERTY, "true");
	}

	public class HeartbeatThread extends Thread {
		private final Envelope envelope;
		private boolean shutdown = false;

		public HeartbeatThread(Envelope envelope) {
			this.envelope = envelope;
		}
		
		@Override public void run() {
			while(!shutdown) {
				if (heartbeatOn) envelope.sendBack("heartbeat", HEARTBEAT);
				Times.sleep(envelope.heartbeatFrequencyMillis());
			}
		}

		public void shutdown() {
			shutdown = true;
		}
	}


	@Override public void onMessage(final Envelope e) {
		final HeartbeatThread heartbeat = new HeartbeatThread(e);
		if(heartbeatOn)	heartbeat.start();
//		new Thread() {
//			@Override public void run() {
				try {
					String reply;
					reply = reply(e.text());
					e.sendBack(reply, NORMAL_REPLY);
				} catch (MessageFailedException ex) {
					e.sendBack(ex.getMessage() + " " + trace(ex), ERROR);
				} finally { 
					if(heartbeat.isAlive()) heartbeat.shutdown();
				}
//			}
//		}.start();
	}
	
	public abstract String reply(String message) throws MessageFailedException;

	public static void setHeartbeatOn(boolean isOn) {
		heartbeatOn = isOn;
	}
}
