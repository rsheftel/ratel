package systemdb.data;

import jms.*;

public abstract class FieldsResponder extends MessageResponder {

	@Override public String reply(String message) throws MessageFailedException {
		try {
			return reply(Fields.parse(message)).messageText();
		} catch(RuntimeException ex) {
			throw new MessageFailedException("failed processing message " + message, ex);
		}
	}
	
	public static FieldsResponder responder(final FieldsResponderListener listener) {
		return new FieldsResponder() {
			@Override public Fields reply(Fields fields) {
				return listener.reply(fields);
			}
		};
	}

	public abstract Fields reply(Fields parse);
}
