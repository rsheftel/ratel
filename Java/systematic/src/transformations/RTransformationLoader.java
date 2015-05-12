package transformations;

import static util.Objects.*;

import java.util.*;

import db.*;
import db.tables.SystemDB.*;

public class RTransformationLoader implements TransformationLoader {

	private final String group;
	
	public RTransformationLoader(List<String> args) {
		this.group = the(args);
	}

	@Override public void load(TransformationUpdater updater) {
		LiveRTransformationBase live = new LiveRTransformationBase("live");
		List<Column<?>> columns = empty();
		columns.add(live.C_CLASS_NAME);
		columns.add(live.C_ARGS);
		columns.add(live.C_MESSAGING_LAYER);
		SelectMultiple distinct = live.selectDistinct(columns, live.C_GROUP_NAME.is(group));
		for (Row row : distinct.rows()) 
			updater.add(new RTransformation(group, row.string(live.C_CLASS_NAME), row.string(live.C_ARGS), row.string(live.C_MESSAGING_LAYER)));
	}

	@Override public String name() {
		return group;
	}

}
