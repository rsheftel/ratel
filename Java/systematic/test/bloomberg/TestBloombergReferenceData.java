package bloomberg;


import static bloomberg.BloombergSecurity.*;

public class TestBloombergReferenceData extends BloombergTestCase {
   
    public void testContractSize() throws Exception {
        assertEquals("100000.0", security("TYA Comdty").string("FUT_CONT_SIZE"));
        assertEquals(100000.0, security("TYA Comdty").numeric("FUT_CONT_SIZE"));
        assertEquals("IBM", security("/cusip/459200101").string("TICKER"));
    }


}
