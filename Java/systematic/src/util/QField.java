package util;

import javax.lang.model.element.*;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;

public class QField {

	private final VarSymbol symbol;

	public QField(Symbol symbol) {
		this.symbol = (VarSymbol) symbol;
		
	}

	public String name() {
		return symbol.getSimpleName().toString();
	}

	public QType type() {
		return new QType(symbol.asType());
	}

    public boolean isStatic() {
        return symbol.getModifiers().contains(Modifier.STATIC);
    }
}
