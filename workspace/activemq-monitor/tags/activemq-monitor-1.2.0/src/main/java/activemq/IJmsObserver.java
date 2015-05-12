package activemq;

import javax.jms.Message;

public interface IJmsObserver<M extends Message> {

    void onUpdate(M message);
}
