package transformations;

import java.util.*;

public abstract class LiveReceiver {

	protected void error(String reason) {
		util.Log.info("received error:" + reason);
	}
	protected void updated(@SuppressWarnings("unused") List<SeriesCategory> updated) { }
	protected void added(@SuppressWarnings("unused") List<SeriesCategory> added) { }
	protected void dropped(@SuppressWarnings("unused") List<SeriesCategory> dropped) { }
	
	
}
