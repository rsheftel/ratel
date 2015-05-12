package systemdb.data;

import static util.Dates.*;

import java.util.*;

public class Tick  {

	public final double last;
	public final long volume;
	public final double open;
	public final double high;
	public final double low;
	public final Date time;

	public Tick(double lastPrice, long lastVolume, double open, double high, double low, Date time) {
		this.last = lastPrice;
		this.volume = lastVolume;
		this.open = open;
		this.high = high;
		this.low = low;
		this.time = time;
	}

	public Fields fields() {
		Fields fields = new Fields();
		fields.put("LastPrice", last);
		fields.put("LastVolume", volume);
		fields.put("OpenPrice", open);
		fields.put("HighPrice", high);
		fields.put("LowPrice", low);
		fields.put("Timestamp", yyyyMmDdHhMmSsMmm(time));
		return fields;
	}
	
	@Override public String toString() {
	    return fields().messageText();
	}
	
}