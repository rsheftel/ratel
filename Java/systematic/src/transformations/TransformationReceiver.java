package transformations;

import static transformations.Constants.*;
import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import util.*;

public class TransformationReceiver extends LiveReceiver {

	private final Map<SeriesDefinition, List<Transformation>> transformations = emptyMap();
	
	public TransformationReceiver(TransformationUpdater updater) {
		updater.build(this);
	}

	public void add(Transformation t, Collection<SeriesDefinition> collection) {
		for (SeriesDefinition def : collection) {
			if (!transformations.containsKey(def)) 
				transformations.put(def, new ArrayList<Transformation>());
			transformations.get(def).add(t);
		}
	}

	@Override protected void updated(List<SeriesCategory> updated) {
		for (SeriesCategory template : updated) 
			for (Record record : template.records()) 
				for (String key : record.keys()) {
					SeriesDefinition d = new SeriesDefinition(template.name(), record.name(), key);
					Log.lineStart("received " + d + " = ");
					if(transformations.get(d) == null) {
						Log.lineEnd("<BLANK>");
						continue;
					}
					String value = record.string(key);
					Log.lineEnd(value);
					for (Transformation t : transformations.get(d)) 
						t.set(d, value);
				}
	}

	public void subscribe() {
		for (SeriesDefinition def : transformations.keySet()) {
		    List<Transformation> transforms = transformations.get(def);
		    for (Transformation t : transforms)
		        bombUnless(t.transport().equals(TRANSPORT_ACTIVE_MQ), "only ActiveMQ transport is currently supported. " + def);
	        def.subscribe(this);
		}
	}
}
