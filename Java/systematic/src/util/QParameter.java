package util;

import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import com.sun.tools.javac.code.Symbol.*;

public class QParameter {

	private final VarSymbol symbol;
	private final QType type;

	public QParameter(VarSymbol symbol) {
		this.symbol = symbol;
		this.type = new QType(symbol.asType());
	}

	public String name() {
		return bombNull(symbol.getSimpleName(), "no simple name on ").toString();
	}
	
	public boolean isArray() { 
		return type.isArray();
	}
	
	public QType type() {
		return type;
	}

	public String simpleType() {
		return type.simpleName();
	}
	
	public static List<String> names(List<QParameter> params) {
		List<String> result = empty();
		for (QParameter p : params)
			result.add(p.name());
		return result;
	}
	
	public static List<String> simpleTypes(List<QParameter> params) {
		List<String> result = empty();
		for (QParameter p : params)
			result.add(p.simpleType());
		return result;
	}
	
	@Override public String toString() {
		return type() + " " + name();
	}

}
