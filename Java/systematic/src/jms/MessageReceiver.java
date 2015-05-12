package jms;

public interface MessageReceiver {
	void onMessage(Envelope envelope); 
	void onError(Envelope envelope); 
	void onHeartBeat(Envelope envelope); 
}
