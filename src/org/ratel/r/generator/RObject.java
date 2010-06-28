package org.ratel.r.generator;

import static org.ratel.r.generator.RFactory.*;

public class RObject extends RawR {

    public RObject(String name) {
        super(name);
    }

    public RCode var(String string) {
        return rawR(toR() + "$" + string);
    }
}
