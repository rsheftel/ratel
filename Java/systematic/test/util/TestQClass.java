package util;

import static util.Objects.*;

import java.util.*;

public class TestQClass extends Asserts {
	private QClass testClass = new QClass(SampleQClass.class);

	public void testConstructors() throws Exception {
		List<QMethod> constructors = testClass.allConstructors();
		assertSize(3, constructors);
		
		assertSize(2, testClass.constructors());
		
		List<QParameter> parameters = second(constructors).parameters();
		assertSize(1, parameters);
		assertEquals("java.lang.String", the(parameters).type().qualifiedName());
		assertEquals("s", the(parameters).name());
	}
	
	/*
	 list from the last time it was 16.
	    compareTo(QType:java.lang.Object x0) 
        equals(QType:java.lang.Object obj)
        hashCode()
        allSame(QType:util.SampleQClass args)
        doubleCopies(QType:double n)
        intCopies(QType:int n)
        stringCopies(QType:int n)
        copies(QType:int n)
        sum(QType:double args)
        sum(QType:int args)
        squish(QType:java.lang.String args)
        compareTo(QType:util.SampleQClass o)
        doNothing()
        world(QType:int n)
        world()
        toString()
	 */
	
	public void testMethods() throws Exception {
		List<QMethod> methods = testClass.allMethods();
		assertContains(methods.size(), list(15, 16));
		
		List<QMethod> publicMethods = testClass.methods();
		assertContains(publicMethods.size(), list(14, 15));
		List<QMethod> world = testClass.methods("world");
		QMethod method = first(world);
		assertEquals("java.lang.String", method.returnTypeName());
		
		assertTrue(testClass.method("doNothing").isStatic());
		assertFalse(first(world).isStatic());
	}
	
	public void testGetAllClassesInPackage() throws Exception {
		List<Class<?>> classes = QClass.allClassesInPackage(SampleQClass.class);
		assertContains(TestQClass.class, classes);
	}
	
	public void testFields() throws Exception {
		List<QField> fields = testClass.fields();
		assertSize(2, fields);
		
		QField hello = testClass.field("HELLO");
		assertEquals("HELLO", hello.name());
		assertEquals("util.SampleQClass", hello.type().qualifiedName());
		assertEquals("SampleQClass", hello.type().simpleName());
	}
	
	public void testGenericReturnTypes() throws Exception {
		QClass c = new QClass(List.class);
		QMethod get = c.method("get");
		assertEquals("java.lang.Object", get.returnTypeName());
	}
}
