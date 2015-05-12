package malbec.fer.mapping;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

public class PlatformSecurityTest {

    @Test(groups = { "unittest" })
    public void testMapping() {

        // construct without initializing
        PlatformSecurity ps = new PlatformSecurity();
        
        int resultCount = ps.initialize();
        
        assertTrue(resultCount > 0, "Failed to read mappings");
        
        assertTrue(ps.canSendOrder("nyws802", "test"));
        
        assertFalse(ps.canSendOrder("DoesNotExist", "DoesNotExistEither"));
        ps.addClient("TestClient");
        
        assertFalse(ps.canSendOrder("TestClient", "DoesNotExistEither"));
        
        ps.addPlatformToClient("TestClientTwo", "TestPlatform");
        
        assertTrue(ps.canSendOrder("TestClientTwo", "TestPlatform"));
        
        int count = ps.reload();
        assertTrue(count > 0, "Failed to reload Platform security");
    }
}
