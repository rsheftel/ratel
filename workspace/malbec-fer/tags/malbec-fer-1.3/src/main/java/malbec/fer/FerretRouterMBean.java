package malbec.fer;

public interface FerretRouterMBean {
    String currentStateString();
    
    boolean setStateToTicket();
    
    boolean setStateToDma();
    
    boolean setStateToStage();
}
