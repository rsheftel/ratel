package transformations;

import static transformations.Record.*;
import static transformations.SeriesCategory.*;
import static util.Objects.*;

import java.util.*;

public class TestTransformationReceiver extends TransformationTestCase {
	public void testTransformationReceiver() throws Exception {
		MockPublisher publisher = new MockPublisher();
        LiveTransformation.publisher = publisher;
		TransformationUpdater updater = new TransformationUpdater("TestServer");
		updater.add(transformation("SimpleTestTransformation", "'SP.1C'"));
		updater.add(transformation("SimpleTestTransformation", "'TU.1C'"));
		updater.add(transformation("SimpleTestTransformation", "'US.1C'"));
		TransformationReceiver receiver = new TransformationReceiver(updater);
		List<SeriesCategory> input = list(category("MARKETDATA", record("SP.1C", 193.54), record("TU.1C", 99.0), record("FV.1C", 22.33), record("SP.1C", "BADTICK")));
		receiver.updated(input);
		updater.runOnce();
		List<String> outputs = publisher.published();
		assertEquals("[TEST:||:SP.1C:||:BID:||:ERROR:R Failed, TEST:||:TU.1C:||:BID:||:99.000000000000]", outputs.toString());
	}
}
