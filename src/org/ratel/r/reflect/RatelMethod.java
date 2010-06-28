package org.ratel.r.reflect;

import java.util.*;

import javax.lang.model.element.*;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;

import static org.ratel.r.Util.*;

public class RatelMethod {

    private final MethodSymbol symbol;

    RatelMethod(Symbol symbol) {
        bombNull(symbol, "no symbol provided!");
        this.symbol = (MethodSymbol) symbol;
    }

    public List<RatelParameter> parameters() {
        List<RatelParameter> result = empty();
        for (VarSymbol varSymbol : symbol.getParameters())
            result.add(new RatelParameter(bombNull(varSymbol, "empty varSymbol?")));
        return result;
    }
    
    public String name() {
        if(symbol.isConstructor())
            return symbol.enclClass().className();
        return symbol.getSimpleName().toString();
    }
    
    @Override public String toString() {
        return name() + paren(commaSep(strings(parameters())));
    }

    public boolean isPublic() {
        return symbol.getModifiers().contains(Modifier.PUBLIC);
    }

    public RatelType returnType() {
        return new RatelType(symbol.getReturnType());
    }
    
    public String returnTypeName() {
        RatelType type = returnType();
        if(type.isGeneric()) return "java.lang.Object";
        return type.qualifiedName();
    }
    
    public String simpleReturnType() {
        if(returnType().isGeneric()) return "Object";
        return returnType().simpleName();
    }

    public boolean isStatic() {
        return symbol.getModifiers().contains(Modifier.STATIC);
    }

}
