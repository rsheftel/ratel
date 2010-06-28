package org.ratel.r.reflect;

import javax.lang.model.element.*;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;

public class RatelField {

    private final VarSymbol symbol;

    public RatelField(Symbol symbol) {
        this.symbol = (VarSymbol) symbol;
        
    }

    public String name() {
        return symbol.getSimpleName().toString();
    }

    public RatelType type() {
        return new RatelType(symbol.asType());
    }

    public boolean isStatic() {
        return symbol.getModifiers().contains(Modifier.STATIC);
    }
}
