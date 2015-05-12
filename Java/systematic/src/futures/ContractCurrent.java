package futures;

import static futures.FuturesOptionTable.*;
import static futures.BloombergField.*;
import static futures.FuturesTable.*;
import static tsdb.DataSource.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import tsdb.*;

public class ContractCurrent extends Contract implements BloombergLoadable {

	private final String yellowKey;

	public ContractCurrent(String name, String yellowKey) {
		super(name);
		this.yellowKey = yellowKey;
	}
	
	@Override public List<BloombergField> fields() {
		return list(FUTURES_PRICE, VOLUME);
	}
	
	@Override public String toString() {
		return name;
	}
	
	String yellowKey() {
		return yellowKey;
	}

	@Override protected int quarterlies() {
		return FUTURES.quarters(name);
	}

	@Override protected String expiryType() {
		return FUTURES.expiry(name);
	}
	
	public List<BloombergJobEntry> jobEntries(Date asOf, BloombergField field) {
		List<BloombergJobEntry> result = empty();
		List<FuturesTicker> futuresTickers = futuresTickers(asOf);
		createTimeSeries(futuresTickers);
		for (FuturesTicker ticker : futuresTickers) 
			result.add(entry(ticker, field));
		return result;
	}

	private BloombergJobEntry entry(FuturesTicker ticker, BloombergField field) {
		String source = field.source();
		source += isEmpty(source) ? "" : " ";
		return new BloombergJobEntry(
			ticker.bloomberg() + " " + source + yellowKey, 
			field.bloomberg(), BLOOMBERG.with(series(ticker, field.tsdb()))
		);
	}

	public static TimeSeries series(FuturesTicker ticker, String seriesSuffix) {
		return new TimeSeries(ticker.name() + "_" + seriesSuffix);
	}

	public static List<ContractCurrent> contracts() {
		return FUTURES.contracts();
	}

	public BloombergJob job(BloombergField field) {
		return FUTURES.job(name, "bloomberg_futures_autogen", field.bloomberg());
	}
	

	public BloombergJob optionJob(BloombergField field) {
		return FUTURES.job(name, "bloomberg_futures_option_autogen", field.bloomberg());
	}
	
	int id() {
		return FUTURES.id(name);
	}

	public OptionCurrent option(String optionName) {
		bombUnless(OPTIONS.has(id(), optionName), "no option with name " + optionName + " defined for future " + name);
		return new OptionCurrent(this, optionName);
	}

	public List<OptionCurrent> options() {
		List<OptionCurrent> result = empty();
		List<String> options = OPTIONS.options(id());
		for (String option : options)
			result.add(new OptionCurrent(this, option));
		return result;
	}

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((yellowKey == null) ? 0 : yellowKey.hashCode());
		return result;
	}

	@Override public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final ContractCurrent other = (ContractCurrent) obj;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (yellowKey == null) {
			if (other.yellowKey != null) return false;
		} else if (!yellowKey.equals(other.yellowKey)) return false;
		return true;
	}

}
