package futures;

import static util.Errors.*;

public class ContractHistorical extends Contract {

	private final int quarterlies;
	private final String expiry;

	public ContractHistorical(String name, int quarterlies, int monthlies, String expiry) {
		super(name);
		this.quarterlies = quarterlies;
		this.expiry = expiry;
		bombIf(monthlies > 0, "can't specify serial contracts at this time.");
	}

	@Override protected int quarterlies() {
		return quarterlies;
	}

	@Override protected String expiryType() {
		return expiry;
	}
}
