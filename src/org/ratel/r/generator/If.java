package org.ratel.r.generator;

import static org.ratel.r.Util.*;

public class If implements RCode {

    private RCode condition;
    private Block then;
    private final Block els;
    
    public If(RCode condition, Block then, Block els) {
        this.condition = condition;
        this.then = then;
        this.els = els;
    }

    @Override public String toR() {
        String result = "if" + paren(condition.toR()) + then.toR();
        if(els != null)
            result = result + " else " + els.toR();
        return result;
    }
}
