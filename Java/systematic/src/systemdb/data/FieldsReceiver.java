package systemdb.data;

import static util.Dates.*;
import static util.Errors.*;
import static util.Times.*;
import jms.*;

public abstract class FieldsReceiver extends ValidMessageReceiver {

    private boolean gotMessage;

	@Override public void onHeartBeat(Envelope envelope) {}

    @Override public void onMessage(Envelope envelope) {
    	onMessage(fields(envelope));
    	gotMessage = true;
    	
    }

    public abstract void onMessage(Fields fields);
    
    public static FieldsReceiver receiver(final FieldsListener listener) {
        return new FieldsReceiver() {
            @Override public void onMessage(Fields fields) {
                listener.onMessage(fields);
            }
        };
    }

    private Fields fields(Envelope envelope) {
    	return Fields.parse(envelope.text());
    }
    
	
	public void clearMessageFlag() {
		gotMessage = false;
	}
	
	public void waitForMessage(long timeoutMillis) {
		long start = reallyNow().getTime();
		while (!gotMessage && reallyMillisSince(start) < timeoutMillis) { sleep(20); }
		bombUnless(gotMessage, "message not received within " + timeoutMillis + " millis.");
		clearMessageFlag();
	}
}
