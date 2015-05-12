/**
 * 
 */
package jms;

import static org.apache.activemq.ActiveMQConnection.*;
import static util.Errors.*;
import static util.Log.*;
import systemdb.data.*;
import util.*;

class QQueuePong {
	public static void main(String[] args) {
		info("broker: " + DEFAULT_BROKER_URL);
		// Log.setVerboseLogging(true);
		QQueue receiverSide = new QQueue("jefftest");
		MessageReceiver receiver = new FieldsResponder() {
			@Override public Fields reply(Fields parse) {
				throw bomb("do you see me?");
			}
		};
		receiverSide.register(receiver );
		Times.sleep(10000);
	}

}