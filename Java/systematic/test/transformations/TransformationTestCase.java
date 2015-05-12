package transformations;

import db.*;

public abstract class TransformationTestCase extends DbTestCase {

	public TransformationTestCase() {
		super();
	}

    protected RTransformation transformation(String className, String record) {
        return new RTransformation("TEST", className, record, "unused");
    }
	

}