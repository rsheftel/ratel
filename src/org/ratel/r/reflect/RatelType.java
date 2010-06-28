package org.ratel.r.reflect;

import javax.lang.model.type.*;

import com.sun.tools.javac.code.*;

import static org.ratel.r.Util.*;

public class RatelType {

    private final Type type;

    public RatelType(Type type) {
        this.type = type;
    }

    public boolean isArray() {
        return type.getKind() == TypeKind.ARRAY;
    }
    
    public String qualifiedName() {
        if (isGeneric()) return "java.lang.Object";
        if(isArray()) return componentType().qualifiedName();
        return type.asElement().getQualifiedName().toString();
    }
    
    @Override public String toString() {
        return "RatelType:" + qualifiedName();
    }
    
    public RatelType componentType() {
        bombUnless(isArray(), "non-array types don't have components, type: " + type);
        ArrayType asArray = (ArrayType)type;
        return new RatelType((Type) asArray.getComponentType());
    }

    public String simpleName() {
        if (isGeneric()) return "Object";
        if (isArray()) return componentType().simpleName() + "Array";
        return type.asElement().getSimpleName().toString();
    }

    public boolean isGeneric() {
        return type.asElement().getQualifiedName().toString().length() == 1; // don't call qualified name without refactoring more - causes inf loop
    }

}
