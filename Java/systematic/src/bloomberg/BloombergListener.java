package bloomberg;

import com.bloomberglp.blpapi.*;

public interface BloombergListener {
    void onMessage(Message m);
}
