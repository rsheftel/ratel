package transformations;

import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;
import static util.Times.*;
import static java.lang.Math.*;

import java.util.*;

import util.*;

public abstract class Transformation {

	public static class InputValues extends HashMap<SeriesDefinition, LiveObservation> {
		public InputValues(InputValues inputs) { super(inputs); }
		public InputValues() { }
		private static final long serialVersionUID = 1L;
		public InputValues deepCopy() {
			InputValues copy = new InputValues();
			for (SeriesDefinition def : keySet())
				copy.put(def, get(def).deepCopy());
			return copy;
		} 
	}
	public static InputValues emptyInputs() { return new InputValues(); }
	
	static class OutputValues extends HashMap<SeriesDefinition, String> {
		public OutputValues(OutputValues inputs) { super(inputs); }
		public OutputValues() { }
		private static final long serialVersionUID = 1L; 
	}
	public static OutputValues emptyOutputs() { return new OutputValues(); }
	

	private final InputValues inputs = emptyInputs();
	private List<SeriesDefinition> outputs;
	private long lastUpdate = System.currentTimeMillis();
    private boolean hasFailed;
	private static final long NO_UPDATE_LOG_TIMEOUT_MILLIS = 60 * 1000;

	public Transformation() {
		super();
	}
	
	protected OutputValues updateSafely(InputValues frozenInputs) {
		try {
			startLogger();
			hasFailed = false;
			return update(frozenInputs);
		} catch(Exception e) {
			OutputValues result = emptyOutputs();
			String message = e.getMessage();
			for (SeriesDefinition output : outputDefinitions())
				result.put(output, "ERROR:" + message.substring(0, min(14, message.length())));
			Log.info("ERROR updating transformation: ", e);
			hasFailed = true;
			return result;
		} finally {
			clearLogger();
		}
	}
	
	public OutputValues updateIfNeeded() {
		for (SeriesDefinition d : inputDefinitions()) 
			if (inputs.get(d) == null) 
				return noUpdate("transformation " + this + " waiting for bootstrap on " + d);
		Collection<LiveObservation> observations = inputs.values();
		for (LiveObservation go : observations) 
			if(go.changed()) {
				lastUpdate = System.currentTimeMillis();
				return updateSafely(freezeInputs());
			}
		return emptyOutputs();
	}
	
	protected OutputValues noUpdate(String message) {
		if (reallyMillisSince(lastUpdate) > NO_UPDATE_LOG_TIMEOUT_MILLIS) {
			Log.info(message);
			noUpdateEvent(message);
			lastUpdate = System.currentTimeMillis();
		}
		return emptyOutputs();
	}

	protected void noUpdateEvent(@SuppressWarnings("unused") String message) {}
	protected void startLogger() {}
	protected void clearLogger() {}

	private InputValues freezeInputs() {
		synchronized (inputs) {
			InputValues copy = inputs.deepCopy();
			for (SeriesDefinition d : inputs.keySet())
				inputs.get(d).clearChanged();
			return copy;
		}
	}

	public final void set(SeriesDefinition inputDef, String s) {
		synchronized (inputs) {
			if (!inputDefinitions().contains(inputDef)) 
				bomb(inputDef + "\nnot in transformation\n" + this + "\n\t" + join("\n\t", strings(inputs.keySet())));
			if (hasContent(s)) 
				inputs.put(inputDef, new LiveObservation(s, true));
		}
	}
	
	protected abstract OutputValues update(InputValues frozenInputs);
	protected abstract List<SeriesDefinition> buildInputs();
	protected List<SeriesDefinition> buildOutputs() { return empty(); }

	public Collection<SeriesDefinition> inputDefinitions() {
		synchronized (inputs) {
			if(inputs.isEmpty())
				for (SeriesDefinition def : buildInputs())
					inputs.put(def, null);
			return inputs.keySet();
		}
	}

	public Collection<SeriesDefinition> outputDefinitions() {
		if(outputs == null)
			outputs = buildOutputs();
		return outputs;
	}

    public abstract String transport();

    public boolean hasFailure() {
        return hasFailed;
    }
    
    public boolean initializationFailed() {
        return false;
    }
}