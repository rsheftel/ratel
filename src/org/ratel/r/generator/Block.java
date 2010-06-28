package org.ratel.r.generator;

import java.util.*;


public class Block implements RCode {

    private List<RCode> statements;

    public Block(List<RCode> statements) {
        this.statements = statements;
    }

    @Override public String toR() {
        return RGenerator.brace(statements);
    }
    

}
