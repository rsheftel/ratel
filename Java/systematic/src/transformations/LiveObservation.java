package transformations;

public class LiveObservation {

	private final String value;
	private boolean changed;

	public LiveObservation(String value, boolean changed) {
		this.value = value;
		this.changed = changed;
	}

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (changed ? 1231 : 1237);
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final LiveObservation other = (LiveObservation) obj;
		if (changed != other.changed) return false;
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equals(other.value)) return false;
		return true;
	}
	
	public boolean changed() {
		return changed;
	}

	public String value() {
		return value;
	}

	public String changedString() {
		return changed() ? "TRUE" : "FALSE";
	}

	public void clearChanged() {
		changed = false;
	}
	
	@Override public String toString() {
		return "GO:" + value() + "," + changedString();
	}

	public LiveObservation deepCopy() {
		return new LiveObservation(value, changed);
	}

}
