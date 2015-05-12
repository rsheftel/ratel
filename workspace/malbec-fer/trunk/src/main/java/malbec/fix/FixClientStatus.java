package malbec.fix;

/**
 * The possible status/state of the connection. <tt>
 * DISCONNECTED -> CONNECTED -> LOGGED_ON 
 *    |    ^           | ^         |      
 *    |    |-----------------------|
 *    |-> LOGGOUT_OUT ---|
 *     </tt>
 */
public enum FixClientStatus {
    DISCONNECTED, CONNECTED, LOGGED_ON, LOGGED_OUT;
}
