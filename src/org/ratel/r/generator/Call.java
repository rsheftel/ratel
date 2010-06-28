package org.ratel.r.generator;

import static org.ratel.r.Util.*;
import static org.ratel.r.generator.RStrings.*;

import java.util.*;

public class Call implements RCode {

    private final String name;
    private final List<RCode> parameters;

    public Call(String name, List<RCode> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    @Override public String toR() {
        return name + paren(commaSep(parameters));
    }
    

}
