package org.ratel.r.generator;

import static org.ratel.r.Util.*;
import static org.ratel.r.generator.RStrings.*;

import java.util.*;

public class Anonymous implements RCode {

    private List<RCode> args;
    private Block body;

    public Anonymous(List<RCode> args, Block body) {
        this.args = args;
        this.body = body;
    }
    public Anonymous(RCode[] args, Block body) {
        this(list(args), body);
    }

    @Override public String toR() {
        return "function" + paren(commaSep(args)) + body.toR();
    }
    
}
