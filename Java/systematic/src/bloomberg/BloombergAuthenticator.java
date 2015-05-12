package bloomberg;

import static bloomberg.UsersTable.*;
import bloomberg.UsersTable.*;

import com.bloomberglp.blpapi.*;

import db.*;

public class BloombergAuthenticator {

    public static Authorization hasLoggedIn(int uuid, String ipAddress) {
        BloombergSession session = BloombergSession.session();
        Service authService = session.authService();

        Request authRequest = authService.createAuthorizationRequest();
        authRequest.set("uuid", uuid);
        authRequest.set("ipAddress", ipAddress);
        authRequest.set("requireAsidEquivalence", true);
        return session.requestAuthorization(authRequest);
    }

    public void checkAll() {
        for (UserRow user : USERS.users())
            user.updateLoginStatus();
    }
    
    public static void main(String[] args) {
        new BloombergAuthenticator().checkAll();
        Db.commit();
    }

}
