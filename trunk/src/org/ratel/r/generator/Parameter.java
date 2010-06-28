package org.ratel.r.generator;

public class Parameter implements RCode {

    private String name;
    private RCode value;
    
    public Parameter(String name, RCode code) {
        this.name = name;
        this.value = code;
    }

    public Parameter(String name) {
        this(name, RGenerator.NULL);
    }
    
    @Override public String toR() {
        return name + " = " + value.toR();
    }
}
