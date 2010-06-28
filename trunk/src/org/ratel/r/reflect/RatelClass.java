package org.ratel.r.reflect;

import java.util.List;

import javax.lang.model.element.*;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.jvm.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.Name;

import static org.ratel.r.Util.*;

public class RatelClass {

    private ClassSymbol symbol;
    private static ClassReader cr;
    private static Context context;

    public <T> RatelClass(Class<T> c) {
        symbol = classSymbol(c.getName());
    }

    public <T> List<RatelMethod> allConstructors() {
        List<RatelMethod> constructors = empty();
        for (Symbol member : symbol.members().getElements())
            if(member.isConstructor())
                constructors.add(new RatelMethod(member));
        return constructors;
    }

    private static ClassSymbol classSymbol(String classname) {
        if(cr == null) {
            context = new Context();
            Options.instance(context).put("save-parameter-names", "1");
            DefaultFileManager.preRegister(context);
            cr = ClassReader.instance(context);
        }
        return cr.loadClass(Name.fromString(Name.Table.instance(context), classname));
    }
    
    public static <T> List<Class<?>> allClassesInPackage(Class<T> c) {
        ClassSymbol cs = classSymbol(c.getName());
        PackageSymbol ps = cs.packge();
        List<Symbol> enclosed = ps.getEnclosedElements();
        List<Class<?>> result = empty();
        for (Symbol symbol : enclosed) {
            String name = symbol.getQualifiedName().toString();
            try {
                result.add(Class.forName(name));
            } catch (ClassNotFoundException e) {
                throw bomb("cannot find class " + name, e);
            }
        }
        return result;
    }

    public List<RatelMethod> allMethods() {
        List<RatelMethod> methods = empty();
        for (Symbol member : symbol.members().getElements())
            if(member.getKind() == ElementKind.METHOD) {
                com.sun.tools.javac.util.List<VarSymbol> params = ((MethodSymbol) member).getParameters();
                if(params.length() > 0 && first(params).getSimpleName() == null) continue;
                methods.add(new RatelMethod(member));
            }
        return methods;
    }

    public List<RatelMethod> methods() {
        return declared(allMethods());
    }

    private List<RatelMethod> declared(List<RatelMethod> in) {
        List<RatelMethod> result = empty();
        for (RatelMethod method : in)
            if(method.isPublic())
                result.add(method);
        return result;
    }

    public List<RatelMethod> constructors() {
        return declared(allConstructors());
    }

    public List<RatelMethod> methods(String name) {
        List<RatelMethod> result = empty();
        for (RatelMethod m : methods())
            if(m.name().equals(name))
                result.add(m);
        return result;
    }

    public RatelMethod method(String name) {
        return the(methods(name));
    }

    public List<RatelField> fields() {
        List<RatelField> fields = empty();
        for (Symbol member : symbol.members().getElements())
            if(member.getKind().isField() && member.getModifiers().contains(Modifier.PUBLIC))
                fields.add(new RatelField(member));
        return fields;
    }

    public RatelField field(String name) {
        for (RatelField f : fields())
            if(f.name().equals(name))
                return f;
        throw bomb("No field " + name + " on " + name());
    }

    private String name() {
        return symbol.getQualifiedName().toString();
    }

}
