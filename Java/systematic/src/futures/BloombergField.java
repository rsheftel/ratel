package futures;

public enum BloombergField {

	FUTURES_PRICE("PX_SETTLE", "price_mid", "PIT"), 
	VOLUME("VOLUME", "volume", ""),
	TBA_PRICE("PX_BID", "price", "BBT3"),
	SETTLE_DATE("SETTLE_DT", "settle_date", "BBT3");
	
	private final String bloomberg;
	private final String tsdb;
	private final String source;

	private BloombergField(String bloomberg, String tsdb, String source) {
		this.bloomberg = bloomberg;
		this.tsdb = tsdb; 
		this.source = source;
	}
	
	public String bloomberg() {
		return bloomberg;
	}

	public String tsdb() {
		return tsdb;
	}

	public String source() {
		return source;
	}

}
