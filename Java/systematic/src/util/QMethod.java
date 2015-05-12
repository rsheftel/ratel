package util;

import static util.Objects.*;
import static util.Strings.*;

import static util.Errors.*;
import java.util.*;

import javax.lang.model.element.*;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;

public class QMethod {

	private final MethodSymbol symbol;

	QMethod(Symbol symbol) {
		bombNull(symbol, "no symbol provided!");
		this.symbol = (MethodSymbol) symbol;
	}

	public List<QParameter> parameters() {
		List<QParameter> result = empty();
		for (VarSymbol varSymbol : symbol.getParameters())
			result.add(new QParameter(bombNull(varSymbol, "empty varSymbol?")));
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

	public QType returnType() { 
		return new QType(symbol.getReturnType());
	}
	
	public String returnTypeName() {
		QType type = returnType();
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
