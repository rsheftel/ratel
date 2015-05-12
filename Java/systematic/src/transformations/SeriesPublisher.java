package transformations;

import transformations.Transformation.*;

public interface SeriesPublisher {

	void publish(SeriesDefinition d, double value);

	void publish(SeriesDefinition d, String s);

    void publish(OutputValues outputs);

}