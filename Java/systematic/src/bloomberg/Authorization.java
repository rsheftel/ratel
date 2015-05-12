package bloomberg;

import static util.Errors.*;

import com.bloomberglp.blpapi.*;

public class Authorization {

    private final Message message;

    public Authorization(Message message) {
        this.message = bombNull(message, "can't create authorization with null message!");
    }
    
    public boolean hasLoggedIn() {
        return message.asElement().name().equals(new Name("AuthorizationSuccess"));
    }

    public String status() {
        return hasLoggedIn() ? "SUCCESS" : "NOT LOGGED IN";
    }

    public String message() {
        return message.toString();
    }
    
}
