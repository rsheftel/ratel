package org.ratel.r.generator;

import static org.ratel.r.generator.RFactory.*;
public class Constructor implements RCode {
    private final RCode name;
    private final Anonymous function;

    public Constructor(RCode name, Anonymous function) {
        this.name = name;
        this.function = function;
    }
    
    public String toR() {
        return call("constructor", name, function).toR();
    }
}
