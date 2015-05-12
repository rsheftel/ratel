package futures;

import util.*;

public class ExpiryNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ExpiryNotFoundException(int optionId, YearMonth ym) {
		super("expiry not found for " + optionId + " in " + ym);
	} 
}
