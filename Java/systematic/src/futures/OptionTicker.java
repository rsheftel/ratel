package futures;

import java.text.*;

public class OptionTicker extends FuturesTicker {

	private final double strike;

	public OptionTicker(FuturesTicker ticker, double strike) {
		super(ticker.name());
		this.strike = strike;
	}
	
	@Override public String toString() {
		return name() + "_" + prettyStrike();
	}

	String prettyStrike() {
		NumberFormat format = NumberFormat.getInstance();
		return format.format(strike);
	}
	
	@Override protected String sortableName() {
		NumberFormat format = NumberFormat.getInstance();
		format.setMinimumIntegerDigits(12);
		return super.sortableName() + "_" + format.format(strike);
	}

	public FuturesTicker future() {
		return new FuturesTicker(name());
	}

	public double strike() {
		return strike;
	}

	public String bloomberg(String optionType) {
		return bloomberg() + optionType.toUpperCase().charAt(0) + " " + prettyStrike();
	}

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(strike);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final OptionTicker other = (OptionTicker) obj;
		if (Double.doubleToLongBits(strike) != Double.doubleToLongBits(other.strike)) return false;
		return true;
	}

}
