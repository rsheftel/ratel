package malbec.fer.rediplus;

public enum RediExchange {

    UNKNOWN("UNKNOWN"), SIGMA("SIGMA"), SIGMAX("SIGMAX"), GSAT("GSAT"), DEMO("DEMO"), TICKET("*ticket");
    
    private String text;
    
    private RediExchange(String t) {
        text = t;
    }
    
    /**
     * This might be redundant as the default <code>valueOf</code> should work.
     * 
     * @param text
     * @return
     */
    public static RediExchange valueFor(String text) {
        if (SIGMA.text.equals(text)) {
            return SIGMA;
        } else if (SIGMAX.text.equals(text)) {
            return SIGMAX;
        }  else if (GSAT.text.equals(text)) {
            return GSAT;
        } else if (DEMO.text.equals(text)) {
            return DEMO;
        } else if (TICKET.text.equalsIgnoreCase(text) || "TICKET".equalsIgnoreCase(text)) {
            // This is the only lowercase item we have
            return TICKET;
        }
        
        return UNKNOWN;
    }
    
    public String toString() {
        return text;
    }
}
