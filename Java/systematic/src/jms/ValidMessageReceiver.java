package jms;

import static util.Errors.*;
import static util.Log.*;

public abstract class ValidMessageReceiver implements MessageReceiver {

	@Override public void onError(Envelope envelope) {
		info("(logging at info) received error with text " + envelope.text());
		bomb("received error with text " + envelope.text());
	}

}
