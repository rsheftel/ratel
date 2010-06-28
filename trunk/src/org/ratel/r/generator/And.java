package org.ratel.r.generator;

public class And implements RCode {

    private RCode lhs;
    private RCode rhs;

    public And(RCode lhs, RCode rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override public String toR() {
        return lhs.toR() + " && " + rhs.toR();
    }
    
}
