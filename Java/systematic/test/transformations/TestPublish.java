package transformations;

import static java.util.Collections.*;
import static util.Strings.*;
import junit.framework.*;

public class TestPublish extends TestCase {
	public void functestCanPublish() throws Exception {
		SeriesDefinition def = new SeriesDefinition("TEST", "SP.1C", "BID");
		LivePublisher publisher = new LivePublisher();
		publisher.publish(def, 345.68);
	}
	
	public void functestPublishError() throws Exception {
		SeriesDefinition def = new SeriesDefinition("TEST", "SP.1C", "BID");
		new LivePublisher().publish(def, join("", nCopies(100, "1234567890")));
	}
	
	public void testNothing() throws Exception {
	
	}
}
