package r.generator;

import static util.Objects.*;

import java.util.*;

public class RFactory {
	public static And and(RCode lhs, RCode rhs) {
		return new And(lhs, rhs);
	}
	
	public static Anonymous anonymous(RCode[] args, Block body) {
		return new Anonymous(args, body);
	}

	public static Anonymous anonymous(List<RCode> args, Block body) {
		return new Anonymous(args, body);
	}
	
	public static Assignment assign(RCode lhs, RCode rhs) {
		return new Assignment(lhs, rhs);
	}
	
	public static Block block(RCode ... statements) {
		return new Block(list(statements));
	}
	
	public static Block block(List<RCode> statements) {
		return new Block(statements);
	}
	
	public static Call call(String name, RCode ... parameters) {
		return new Call(name, list(parameters));
	}
	
	public static Call call(String name, List<RCode> parameters) {
		return new Call(name, parameters);
	}

	public static RCode constructor(RCode name, Anonymous function) {
		return new Constructor(name, function);
	}
	
	public static If rIf(RCode condition, Block then) {
		return new If(condition, then, null);
	}

	public static If rIf(RCode condition, Block then, Block els) {
		return new If(condition, then, els);
	}

	public static If rIf(RCode condition, RCode then) {
		return new If(condition, block(then), null);
	}

	public static Not not(RCode code) {
		return new Not(code);
	}

	public static Parameter param(String name) {
		return new Parameter(name);
	}

	public static Parameter param(String name, RCode value) {
		return new Parameter(name, value);
	}

	public static RawR rawR(String code) {
		return new RawR(code);
	}

	public static RObject robject(String name) {
		return new RObject(name);
	}
	
	public static RString rstring(String name) {
		return new RString(name);
	}

}
