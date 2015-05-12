package bloomberg;

import db.*;

public abstract class BloombergTestCase extends DbTestCase {

    protected static final BloombergSecurity AAPL = new BloombergSecurity("AAPL US Equity");
    protected static final BloombergSecurity IBM = new BloombergSecurity("IBM US Equity");


}