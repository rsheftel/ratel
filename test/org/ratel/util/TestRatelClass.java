package org.ratel.util;

import org.ratel.r.reflect.*;

import static org.ratel.r.Util.*;

import java.util.*;

public class TestRatelClass extends Asserts {
    private RatelClass testClass = new RatelClass(SampleRatelClass.class);

    public void testConstructors() throws Exception {
        List<RatelMethod> constructors = testClass.allConstructors();
        assertSize(3, constructors);

        assertSize(2, testClass.constructors());

        List<RatelParameter> parameters = second(constructors).parameters();
        assertSize(1, parameters);
        assertEquals("java.lang.String", the(parameters).type().qualifiedName());
        assertEquals("s", the(parameters).name());
    }

    /*
     list from the last time it was 16.
        compareTo(RatelType:java.lang.Object x0)
        equals(RatelType:java.lang.Object obj)
        hashCode()
        allSame(RatelType:util.SampleRatelClass args)
        doubleCopies(RatelType:double n)
        intCopies(RatelType:int n)
        stringCopies(RatelType:int n)
        copies(RatelType:int n)
        sum(RatelType:double args)
        sum(RatelType:int args)
        squish(RatelType:java.lang.String args)
        compareTo(RatelType:util.SampleRatelClass o)
        doNothing()
        world(RatelType:int n)
        world()
        toString()
     */

    public void testMethods() throws Exception {
        List<RatelMethod> methods = testClass.allMethods();
        assertContains(methods.size(), list(15, 16));

        List<RatelMethod> publicMethods = testClass.methods();
        assertContains(publicMethods.size(), list(14, 15));
        List<RatelMethod> world = testClass.methods("world");
        RatelMethod method = first(world);
        assertEquals("java.lang.String", method.returnTypeName());

        assertTrue(testClass.method("doNothing").isStatic());
        assertFalse(first(world).isStatic());
    }

    public void testGetAllClassesInPackage() throws Exception {
        List<Class<?>> classes = RatelClass.allClassesInPackage(SampleRatelClass.class);
        assertContains(TestRatelClass.class, classes);
    }

    public void testFields() throws Exception {
        List<RatelField> fields = testClass.fields();
        assertSize(2, fields);

        RatelField hello = testClass.field("HELLO");
        assertEquals("HELLO", hello.name());
        assertEquals("org.ratel.util.SampleRatelClass", hello.type().qualifiedName());
        assertEquals("SampleRatelClass", hello.type().simpleName());
    }

    public void testGenericReturnTypes() throws Exception {
        RatelClass c = new RatelClass(List.class);
        RatelMethod get = c.method("get");
        assertEquals("java.lang.Object", get.returnTypeName());
    }
}
