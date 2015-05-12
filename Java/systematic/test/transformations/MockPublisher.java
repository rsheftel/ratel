package transformations;


import static util.Objects.*;

import java.util.*;

import transformations.Transformation.*;

public class MockPublisher implements SeriesPublisher {

	private List<String> published = empty();

	public List<String> published() {
		return published;
	}

	public void clear() {
		published.clear();
	}
	
	public void publish(SeriesDefinition d, double value) {
		published.add(d.stringWithValue(value));
	}

	@Override public void publish(SeriesDefinition d, String s) {
		published.add(d.stringWithString(s));
	}

    @Override public void publish(OutputValues outputs) {
        for (SeriesDefinition def : outputs.keySet())
            publish(def, outputs.get(def));
    }

}
