package org.ratel.r.generator;

import static org.ratel.r.Util.*;

public class RString implements RCode {

    private String s;

    public RString(String s) {
        this.s = s;
    }

    @Override public String toR() {
        return dQuote(s);
    }
    
    public String string() {
        return s;
    }

}
