package util;

import javax.lang.model.type.*;

import static util.Errors.*;
import com.sun.tools.javac.code.*;

public class QType {

	private final Type type;

	public QType(Type type) {
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
	    return "QType:" + qualifiedName();
	}
	
	public QType componentType() {
		bombUnless(isArray(), "non-array types don't have components, type: " + type);
		ArrayType asArray = (ArrayType)type;
		return new QType((Type) asArray.getComponentType());
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
