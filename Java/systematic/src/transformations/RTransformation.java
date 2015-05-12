package transformations;
import static r.R.*;
import static transformations.Constants.*;
import static util.Dates.*;
import static util.Objects.*;
import static util.Strings.*;
import static util.Errors.*;
import file.*;

import java.util.*;

import r.*;
import util.*;

public class RTransformation extends Transformation {

	
	private final String rClass;
	final String rInstance;

	
	private final QFile logFile;
    private final String transport;
    private boolean firstUpdate = true;
    private boolean initializationFailed;



	public RTransformation(String group, String name, String args, String transport) {
		this.rClass = name;
        this.transport = transport;
		this.rInstance = rString("make.names('" + rQuote(name + args, QUOTE)+ "')");
		logFile = LOG_HOME.file("LiveTransformation", group, name, rInstance + ".log");
		logFile.ensurePath();
		startLogger();
		r(rInstance + " <- " + rClass + paren(args));
		clearLogger();
		Log.info("started " + rInstance);
	}
	
	@Override public String toString() {
		return rInstance;
	}

	@Override public List<SeriesDefinition> buildInputs() {
		String[] inputStrings = rStrings("strings(" + rInstance + "$inputs())");
		List<SeriesDefinition> result = empty();
		for (String input : inputStrings)
			result.add(SeriesDefinition.from(input));
		return result;
	}
	
	@Override public List<SeriesDefinition> buildOutputs() {
		String[] outputStrings = rStrings("strings(" + rInstance + "$outputs())");
		List<SeriesDefinition> result = empty();
		for (String output : outputStrings)
			result.add(SeriesDefinition.from(output));
		return result;
	}

	@Override protected void noUpdateEvent(String message) {
		startLogger();
		rLog(message);
		R.setLogger(null);
	}



	@Override protected OutputValues update(InputValues inputs) {
		List<String> strings = empty();
		for (SeriesDefinition d : inputs.keySet())
			strings.add(rQuote(d.rString(inputs.get(d)), QUOTE));
		try {
			String[] outputValues = rStrings("strings(" + rInstance + "$update(c('" + join("', '", strings) + "')))");
			if(firstUpdate && rString("as.character(" + rInstance + "$isDisabled())").equals("TRUE"))
			    initializationFailed = true;
			firstUpdate  = false;
			return SeriesDefinition.values(outputValues);
		} catch (RuntimeException e) {
			throw bomb("R Failed", e);
		}
	}
	
	@Override public boolean initializationFailed() {
	    return initializationFailed;
	}

	@Override protected void clearLogger() {
		r("cat('\n===== DONE\n')");
		R.clearLogger();
	}

	@Override protected void startLogger() {
		LogWriter logger = new LogWriter(logFile.appender());
		logger.info("---------" + yyyyMmDdHhMmSs(now()) + "----------------------------------------------------------------\n");
		R.setLogger(logger);
	}

    @Override public String transport() {
        return transport;
    }
	

}
