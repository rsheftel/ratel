package transformations;

import static transformations.Record.*;
import static transformations.SeriesCategory.*;
import static transformations.Transformation.*;
import static util.Objects.*;
import static util.Strings.*;


import java.util.*;

import jms.*;
import systemdb.data.*;

public class SeriesDefinition {

	private static final String TANK_RE = ":\\|\\|:";
	private static final String TANK = ":||:";
	private final String template;
	private final String record;
	private final String field;

	public SeriesDefinition(String template, String record, String field) {
		this.template = template;
		this.record = record;
		this.field = field;
	}

	public static SeriesDefinition from(String input) {
		String[] parts = input.split(TANK_RE);
		return new SeriesDefinition(first(parts), second(parts), third(parts));
	}
	
	public static OutputValues values(String[] outputs) {
		OutputValues result = emptyOutputs();
		for (String output : outputs) {
			String[] parts = output.split(TANK_RE);
			result.put(
				new SeriesDefinition(first(parts), second(parts), third(parts)), 
				fourth(parts)
			);
		}
		return result;
	}

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((record == null) ? 0 : record.hashCode());
		result = prime * result + ((template == null) ? 0 : template.hashCode());
		return result;
	}

	@Override public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final SeriesDefinition other = (SeriesDefinition) obj;
		if (field == null) {
			if (other.field != null) return false;
		} else if (!field.equals(other.field)) return false;
		if (record == null) {
			if (other.record != null) return false;
		} else if (!record.equals(other.record)) return false;
		if (template == null) {
			if (other.template != null) return false;
		} else if (!template.equals(other.template)) return false;
		return true;
	}

	public String rString(LiveObservation go) {
		return join(TANK, template, record, field, go.value(), go.changedString());
	}

	public String stringWithValue(double value) {
		return stringWithString(String.valueOf(value));
	}
	
	@Override public String toString() {
		return join(TANK, template, record, field);
	}

	public String stringWithString(String s) {
		return join(TANK, template, record, field, s);
	}

    public void subscribe(final TransformationReceiver receiver) {
        MessageReceiver messageReceiver = new FieldsReceiver() {
            @Override public void onMessage(Fields fields) {
                List<SeriesCategory> updated = list(category(template, record(record, field, fields.get(field))));
                receiver.updated(updated);
            }
        };
        topic().register(messageReceiver);
    }

    public QTopic topic() {
        return new QTopic(template + "." + record);
    }

    public void addTo(Fields fields, String value) {
        fields.put(field, value);
    }

	
}
