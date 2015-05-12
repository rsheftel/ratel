package bloomberg;

import static db.clause.Clause.*;
import static util.Errors.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Strings.*;

import java.net.*;
import java.util.*;

import db.*;
import db.clause.*;
import db.tables.BloombergFeedDB.*;

public class UsersTable extends UsersBase {
    private static final long serialVersionUID = 1L;
    public static final UsersTable USERS = new UsersTable();
    
    public UsersTable() {
        super("users");
    }

    public void insert(String name, int uuid) {
        insert(
            C_USERNAME.with(name),
            C_UUID.with(uuid),
            C_IPADDRESS.with(null)
        );
    }
    
    public boolean isLoggedIn(final String username) {
    	String status = Db.doInSidestepTransaction(new SidestepThreadResult<String>(false) {
    	    @Override public String result() {
    	        UserRow user = new UserRow(row(userMatches(username)));
                user.updateLoginStatus();
                return status(username);
    	    }
    	});
    	return status.equals("SUCCESS");
    }

    public String status(String username) {
        return C_STATUS.value(userMatches(username));
    }

    private Clause userMatches(String username) {
        return C_USERNAME.isWithoutCase(username);
    }

    public Object lastSuccessTime(String username) {
        return C_LASTSUCCESS.value(userMatches(username));
    }

    public class UserRow extends Row {
        private static final long serialVersionUID = 1L;
        UserRow(Row data) { super(data); }

        public void updateLoginStatus() {
            String ipAddress;
            try {
                ipAddress = java.net.InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                throw bomb("caught exception looking up IP address", e);
            }
            C_IPADDRESS.updateOne(userMatches(), ipAddress);
            Authorization auth = BloombergAuthenticator.hasLoggedIn(value(C_UUID), ipAddress);
            C_STATUS.updateOne(userMatches(), auth.status());
            if (auth.hasLoggedIn())
                C_LASTSUCCESS.updateOne(userMatches());
            else 
                info(
                    "User " + username() + " has not logged into the Bloomberg service from ip address " + 
                        paren(ip()) + " in the past 24 hours: \n" + auth.message()
                );
        }

        private String ip() {
            return value(C_IPADDRESS);
        }

        private String username() {
            return value(C_USERNAME);
        }

        private Clause userMatches() {
            return UsersTable.this.userMatches(username());
        }
        
    }
    
    public List<UserRow> users() {
        List<UserRow> result = empty();
        for(Row r : rows(TRUE))
            result.add(new UserRow(r));
        return result;
    }

}
