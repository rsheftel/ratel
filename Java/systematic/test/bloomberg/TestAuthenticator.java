package bloomberg;


public class TestAuthenticator extends BloombergTestCase {

    /* only passes on eric's machine when eric is logged into bloomberg */
    public void functestAuthentication() throws Exception {
        assertTrue(BloombergAuthenticator.hasLoggedIn(4768604, "192.168.20.81").hasLoggedIn());
        assertFalse(BloombergAuthenticator.hasLoggedIn(4768603, "192.168.20.81").hasLoggedIn());
        assertFalse(BloombergAuthenticator.hasLoggedIn(4768604, "192.168.12.227").hasLoggedIn());
    }
    
    public void testFunctionalOnly() throws Exception {
		
	}
}
