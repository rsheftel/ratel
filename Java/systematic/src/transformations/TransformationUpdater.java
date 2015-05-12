package transformations;

import static util.Errors.*;
import static util.Objects.*;
import static util.Times.*;

import java.util.*;

import transformations.Transformation.*;

public class TransformationUpdater {
	
	final List<Transformation> transformations = empty();
    private final String name;

	public TransformationUpdater(String name) {
        this.name = name;
	}

    public void add(Transformation t) {
		transformations.add(t);
	}

	public boolean runOnce() {
		boolean updatedSomething = false;
		boolean allFailed = true;
		boolean allInitializationFailed = true;
		for (Transformation t : transformations) {
			updatedSomething |= runOne(t);
			allFailed &= t.hasFailure();
			allInitializationFailed &= t.initializationFailed();
		}
		bombIf(allFailed, "all transformations failed for server " + name + ".  Server crashing...");
		bombIf(allInitializationFailed, "initialization failed for all transformations for server " + name + ".  Server crashing...");
		return updatedSomething;
	}

	private boolean runOne(Transformation t) {
		OutputValues outputs = t.updateIfNeeded();
		LiveTransformation.publisher().publish(outputs);
		return !outputs.isEmpty();
	}
	
	public void run() {
		while(true)
			if(!runOnce())
				sleep(10);
	}

	public void build(TransformationReceiver receiver) {
		for (Transformation t : transformations)
			receiver.add(t, t.inputDefinitions());
	}
}
