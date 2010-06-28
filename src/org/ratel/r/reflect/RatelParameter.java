package org.ratel.r.reflect;

import java.util.*;

import com.sun.tools.javac.code.Symbol.*;

import static org.ratel.r.Util.*;

public class RatelParameter {

    private final VarSymbol symbol;
    private final RatelType type;

    public RatelParameter(VarSymbol symbol) {
        this.symbol = symbol;
        this.type = new RatelType(symbol.asType());
    }

    public String name() {
        return bombNull(symbol.getSimpleName(), "no simple name on ").toString();
    }
    
    public boolean isArray() { 
        return type.isArray();
    }
    
    public RatelType type() {
        return type;
    }

    public String simpleType() {
        return type.simpleName();
    }
    
    public static List<String> names(List<RatelParameter> params) {
        List<String> result = empty();
        for (RatelParameter p : params)
            result.add(p.name());
        return result;
    }
    
    public static List<String> simpleTypes(List<RatelParameter> params) {
        List<String> result = empty();
        for (RatelParameter p : params)
            result.add(p.simpleType());
        return result;
    }
    
    @Override public String toString() {
        return type() + " " + name();
    }

}
