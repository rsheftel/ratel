package org.ratel.r.generator;

import static org.ratel.r.Util.*;


public class Not implements RCode {

    private RCode code;
    
    public Not(RCode code) {
        this.code = code;
    }

    @Override public String toR() {
        return "!" + paren(code.toR());
    }
}
