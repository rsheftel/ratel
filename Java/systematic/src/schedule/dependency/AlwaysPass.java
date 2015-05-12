package schedule.dependency;

import java.util.*;

public class AlwaysPass extends Dependency {

	public AlwaysPass(Integer id, @SuppressWarnings("unused") Map<String, String> parameters) {
		super(id);
	}

	@Override public boolean isIncomplete(Date asOf) {
	    return false;
	}

	@Override public String explain(Date asOf) {
		return "This dependency should always pass.";
	}

}
