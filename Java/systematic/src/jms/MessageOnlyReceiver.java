package jms;

import static util.Errors.*;

public abstract class MessageOnlyReceiver extends ValidMessageReceiver {

	@Override public void onHeartBeat(Envelope envelope) {
		bomb("received heartbeat on message responder thread! text: " + envelope.text());
	}

}
