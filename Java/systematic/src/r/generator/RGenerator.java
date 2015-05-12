package r.generator;

import static java.util.Collections.*;
import static r.generator.RFactory.*;
import static r.generator.RStrings.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.QParameter.*;
import static util.Strings.*;

import java.util.*;

import util.*;

public class RGenerator {

	static final RCode NULL = rawR("NULL");
	static final RCode DOTS = rawR("...");
	static final RObject THIS = robject("this");
	static final RObject STATIC = robject("static");
	static final List<String> EVIL_NAMES = list("get", "in", "next", "repeat", "TRUE", "FALSE", "Inf", "NULL", "NA",
		"NaN", "arg0", "arg1", "arg2", "arg3", "arg4", "arg5", "arg6", "arg7", "arg8", "arg9");
	static final RawR RFALSE = rawR("FALSE");
	static final RawR RTRUE = rawR("TRUE");

	class RBuilder<T> {
		StringBuilder buf = new StringBuilder();
		private RString rClass;
		private final Class<T> c;
		private QClass q;

		RBuilder(Class<T> c) {
			this.c = c;
			rClass = rstring("J" + c.getSimpleName());
			q = new QClass(c);
		}

		public void addConstructorAndMethodsAndFields() {
			addConstructors();
			List<QMethod> methods = q.methods();
			for (QMethod method : methods)
				addMethod(method);
			addFields();
		}

		private void addFields() {
			List<QField> fields = q.fields();
			for (QField field : fields)
				addMethod(
					list(field.isStatic() ? (RCode) STATIC : THIS), 
					avoidEvilNames(field.name()), 
					block(fieldDeclaration(field)), 
					false
				);
		}

		private RCode fieldDeclaration(QField field) {
			RObject statik = robject(rClass.string());
			RCode jFieldTarget = field.isStatic() ? javaClassInR() : THIS.var(".jobj");
            Call getJavaField = call("jField", jFieldTarget, jniName(field.type()), rstring(field.name()));
			Call asJRObject = wrapInJRObjectIfNeeded(field.type(), getJavaField);
			RCode lazyField = statik.var("..." + field.name());
			return call("lazy", lazyField, asJRObject, param("log", RFALSE));
		}

		private void addMethod(QMethod method) {
			List<RCode> parameters = methodParams(method);
			boolean isStatic = method.isStatic();
			parameters.add(0, isStatic ? STATIC : THIS);
			RCode jobj = THIS.var(".jobj");

			List<RCode> callJavaArgs = empty();
			callJavaArgs.add(isStatic ? rJavaType(c.getName()) : jobj);
			callJavaArgs.add(jniName(method.returnType()));
			callJavaArgs.add(rstring(method.name()));
			for (QParameter p : method.parameters())
				callJavaArgs.add(parameterCode(p));
			
			Call callJavaMethod = call("jCall", callJavaArgs);
			callJavaMethod = wrapInJRObjectIfNeeded(method.returnType(), callJavaMethod);
			boolean enforceRCC = !method.name().matches("^[A-Z].*");
			addMethod(parameters, methodName(method), block(callJavaMethod), enforceRCC);
		}

		private RCode parameterCode(QParameter parameter) {
			RObject pName = robject(avoidEvilNames(parameter.name()));
			String pType = parameter.type().qualifiedName();
			if (parameter.isArray())  
				return parameterCodeArray(parameter, pName);
			if(isObject(pType)) 
				return call(".jcast", pName.var(".jobj"), rstring(pType));
			return convertPrimitive(pName, pType, true);
		}

		private RCode parameterCodeArray(QParameter parameter, RawR pName) {
			String pType = parameter.type().qualifiedName();
			if (!isObject(pType)) 
				return asJArray(parameter, convertPrimitive(pName, pType, false), pType);
			RObject x = robject("x");
			Anonymous unwrapJRobject = anonymous(array(x), block(x.var(".jobj")));
			If handleJArray = rIf(call("inherits", pName, rstring("jarrayRef")), 
				assign(pName, call(".jevalArray", pName))); 
			return block(handleJArray, asJArray(parameter, call("lapply", pName, unwrapJRobject), pType));
		}

		private RCode asJArray(QParameter p, RCode rArray, String pType) {
			RCode contentClass = isObject(pType) ? rstring(dotsToSlashs(pType)) : jniName(p.type());
			return call("jArray", rArray, contentClass);
		}

		private RCode convertPrimitive(RawR name, String type, boolean the) {
			if(type.equals("int")) {
				return call(the ? "theInteger" : "as.integer", name);
			} else if(type.equals("double")) {
				return call(the ? "theNumeric" : "as.numeric", name);
			} else if(type.equals("boolean")) {
				return call(the ? "theLogical" : "as.logical", name);
			} else if(type.equals("long")) {
				return call(the ? "theLong" : ".jlong", name);
			} else if (type.equals("java.lang.String")) {
				return the ? call("the", name) : name;
			}
			return call("fail", rstring(type + " parameters not handled by R/Java convertPrimitive!"));
		}

		private Call wrapInJRObjectIfNeeded(QType type, Call jcall) {
			if (!isObject(type.qualifiedName())) return jcall;
			if (type.isArray()) 
				return call("lapply", jcall, rawR("J" + type.componentType().simpleName()));
			return call("J" + type.simpleName(), param("jobj", jcall));
		}

		private String methodName(QMethod method) {
			String name = method.name();
			if (method.parameters().size() > 0) name = name + "_" + methodSuffix(method);
			name = avoidEvilNames(name);
			return name;
		}

		private String avoidEvilNames(String name) {
			if (EVIL_NAMES.contains(name)) name = "j_" + name;
			return name;
		}

		private void addMethod(List<RCode> parameters, String methodName, Block body) {
			addMethod(parameters, methodName, body, true);
		}

		private void addMethod(List<RCode> parameters, String methodName, Block body, boolean enforceRcc) {
			List<RCode> p = copy(parameters);
			p.add(DOTS);
			Parameter enforce = param("enforceRCC", enforceRcc ? RTRUE : RFALSE);
			Call fun = call("method", rstring(methodName), rClass, enforce, anonymous(p, body));
			addCode(fun);
		}

		private boolean isObject(String returnType) {
			return returnType.matches(".*\\..*") && !returnType.equals("java.lang.String");
		}

		private void addConstructors() {
			addGenericConstructor();
			for (QMethod constructor : q.constructors())
				addConstructor(constructor);
		}

		private void addConstructor(QMethod constructor) {
			String name = methodSuffix(constructor);
			
			List<RCode> parameters = params(constructor);
			parameters.add(0, STATIC);
			
			List<RCode> paramsForNew = empty();
			paramsForNew.add(javaClassInR());
			for (QParameter p : constructor.parameters())
				paramsForNew.add(parameterCode(p));
			
			Call jnew = call("jNew", paramsForNew);
			Call callConstructor = call(rClass.string(), jnew);
			addMethod(parameters, name, block(callConstructor));
		}

		private String methodSuffix(QMethod method) {
			List<String> parts = QParameter.simpleTypes(method.parameters());
			parts.add(0, "by");
			return join("_", parts);
		}

		private void addGenericConstructor() {
			Anonymous function = anonymous(array(param("jobj")), block(extend()));
			addCode(constructor(rClass, function));
		}

		private void addCode(RCode code) {
			buf.append(code.toR() + "\n\n");
		}

		private RCode javaClassInR() {
			return rJavaType(c.getName());
		}

		private RCode jniName(QType type) {
			// Stolen from
			// http://jsourcery.com/api/sun/hotspot/7-ea-b02-08-nov-2006/index.html?sun/jvm/hotspot/jdi/JNITypeParser.source.html
			String signature = type.qualifiedName();
			StringBuffer buffer = new StringBuffer();
			int firstIndex = signature.indexOf('[');
			int index = firstIndex;
			while (index != -1) {
				buffer.append('[');
				index = signature.indexOf('[', index + 1);
			}
			if (type.isArray()) buffer.append('[');
			if (firstIndex != -1) {
				signature = signature.substring(0, firstIndex);
			}
			if (signature.equals("boolean")) {
				buffer.append('Z');
			} else if (signature.equals("byte")) {
				buffer.append('B');
			} else if (signature.equals("char")) {
				buffer.append('C');
			} else if (signature.equals("short")) {
				// rJava is stupid. It re-maps this from 'S' to 'T'
				buffer.append('T');
			} else if (signature.equals("int")) {
				buffer.append('I');
			} else if (signature.equals("long")) {
				buffer.append('J');
			} else if (signature.equals("float")) {
				buffer.append('F');
			} else if (signature.equals("double")) {
				buffer.append('D');
			} else if (signature.equals("void")) {
				buffer.append('V');
			} else {
				buffer.append('L');
				buffer.append(dotsToSlashs(signature));
				buffer.append(';');
			}
			return rstring(buffer.toString());
		}

		private String dotsToSlashs(String signature) {
			return signature.replace('.', '/');
		}

		private RString rJavaType(String javaType) {
			return rstring(dotsToSlashs(javaType));
		}

		private List<RCode> methodParams(QMethod constructor) {
			return params(constructor);
		}

		private List<RCode> params(QMethod method) {
			List<RCode> result = empty();
			List<String> parameters = names(method.parameters());
			for (String string : parameters)
				result.add(param(avoidEvilNames(string)));
			return result;
		}

		private Call extend() {
			return call("extend", call("JObject"), rClass, rawR(".jobj = jobj"));
		}

		@Override public String toString() {
			return buf.toString();
		}
	}

	private static int indentLevel = 0;

	public <T> String rCode(Class<T> c) {
		RBuilder<T> b;
		try {
			b = new RBuilder<T>(c);
			b.addConstructorAndMethodsAndFields();
		} catch (RuntimeException e) {
			throw bomb("failed generating code for " + c.getName(), e);
		}
		return b.toString();
	}

	public static String brace(List<RCode> statements) {
		indent();
		String body = indented(statements);
		outdent();
		return " {" + body + indented(list((RCode) rawR("}")));
	}

	private static String indented(List<RCode> statements) {
		StringBuilder buf = new StringBuilder();
		for (String s : rStrings(statements))
			buf.append("\n" + join("", nCopies(indentLevel, "    ")) + s);
		return buf.toString();
	}

	private static void outdent() {
		bombIf(indentLevel-- == 0, "cannot set indent level below 0");
	}

	private static void indent() {
		indentLevel++;
	}

	public <T> List<String> allRCode(Class<T> c) {
		List<Class<?>> classes = QClass.allClassesInPackage(c);
		List<String> result = empty();
		for (Class<?> klass : classes)
			result.add(rCode(klass));
		return result;
	}

}
