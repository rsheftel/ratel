package amazon.monitor;

import static util.Errors.*;
import systemdb.data.*;

public class RecentFieldsKeeper extends FieldsReceiver {
	public Fields latest;

	@Override
	public void onMessage(Fields fields) {
		latest = fields;
	}

    public String string(String name) {
        bombNull(latest, "no message received");
        return latest.get(name);
    }
	



}