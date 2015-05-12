package util;

import static util.Errors.*;
import static util.Objects.*;

import java.util.List;

import javax.lang.model.element.*;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.jvm.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.Name;

public class QClass {

	private ClassSymbol symbol;
	private static ClassReader cr;
	private static Context context;

	public <T> QClass(Class<T> c) {
		symbol = classSymbol(c.getName());
	}
	
	public <T> List<QMethod> allConstructors() {
		List<QMethod> constructors = empty();
		for (Symbol member : symbol.members().getElements())
			if(member.isConstructor())
				constructors.add(new QMethod(member));
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

	public List<QMethod> allMethods() {
		List<QMethod> methods = empty();
		for (Symbol member : symbol.members().getElements())
			if(member.getKind() == ElementKind.METHOD) {
				com.sun.tools.javac.util.List<VarSymbol> params = ((MethodSymbol) member).getParameters();
				if(params.length() > 0 && first(params).getSimpleName() == null) continue;
				methods.add(new QMethod(member));
			}
		return methods;
	}

	public List<QMethod> methods() {
		return declared(allMethods());
	}

	private List<QMethod> declared(List<QMethod> in) {
		List<QMethod> result = empty();
		for (QMethod method : in)
			if(method.isPublic())
				result.add(method);
		return result;
	}

	public List<QMethod> constructors() {
		return declared(allConstructors());
	}

	public List<QMethod> methods(String name) {
		List<QMethod> result = empty();
		for (QMethod m : methods())
			if(m.name().equals(name))
				result.add(m);
		return result;
	}

	public QMethod method(String name) {
		return the(methods(name));
	}

	public List<QField> fields() {
		List<QField> fields = empty();
		for (Symbol member : symbol.members().getElements())
			if(member.getKind().isField() && member.getModifiers().contains(Modifier.PUBLIC))
				fields.add(new QField(member));
		return fields;
	}

	public QField field(String name) {
		for (QField f : fields())
			if(f.name().equals(name))
				return f;
		throw bomb("No field " + name + " on " + name());
	}

	private String name() {
		return symbol.getQualifiedName().toString();
	}

}
