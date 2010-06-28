package org.ratel.r;
import static org.ratel.r.R.*;

import java.util.*;

import static org.ratel.r.Util.*;
import static org.ratel.r.generator.RFactory.*;
import org.ratel.r.generator.*;
import org.ratel.util.*;

public class TestRGeneration extends Asserts {

    static {
        RGenerator generator = new RGenerator();
        List<Class<?>> classes = Arrays.asList(new Class<?>[] {
            List.class,
            Date.class,
            String.class,
            Double.class,
            Boolean.class,
        });
        r(".jinit(squish(Sys.getenv('MAIN'), '/Java/systematic/bin'))");
        for (Class<?> c : classes)
            runGeneratedR(generator.rCode(c));
    }

    private static void runGeneratedR(String code) {
        code = block(rawR(code)).toR();
        r(code);
    }

    public void testSampleQClass() throws Exception {
        Block code = block(rawR(new RGenerator().rCode(SampleRatelClass.class)));
        r(code.toR());
    }

    public void testList() throws Exception {
        r("d <- JDate(.jcast(d$.jobj, 'java/util/Date'))");
        assertEquals("Wed Jul 22 15:00:00 EDT 1987", rString("JString$valueOf_by_Object(d)"));
        r("JDouble$by_double(as.integer(1))");
        r("JBoolean$by_boolean(1)");
    }

    public void testArrayArguments() throws Exception {
        runGeneratedR(new RGenerator().rCode(SampleRatelClass.class));
        r("checkSame('cdsirs', JSampleQClass$squish_by_StringArray(c('cds', 'irs')))");
        r("checkSame(17, JSampleQClass$$sum_by_intArray(c(7, 5, 2, 3)))");
        r("checkSame(17, JSampleQClass$sum_by_doubleArray(c(7, 5, 2, 3)))");
        r("diffqs <- list(JSampleQClass$HELLO(), JSampleQClass$GOODBYE())");
        r("sameqs <- list(JSampleQClass$HELLO(), JSampleQClass$HELLO())");
        r("checkSame(FALSE, JSampleQClass$allSame_by_SampleQClassArray(diffqs))");
        r("checkSame(TRUE, JSampleQClass$allSame_by_SampleQClassArray(sameqs))");
        r("hellos <- JSampleQClass$HELLO()$copies_by_int(5)");
        r("needs(hellos = 'list(JSampleQClass)')");
        r("checkLength(hellos, 5)");
        assertEquals("hello, world!", rString("first(hellos)$world()"));
        assertEquals(49, rInt("sum(JSampleQClass$intCopies_by_int(7))"));
        assertEquals(20.0, rDouble("sum(JSampleQClass$doubleCopies_by_double(4))"));
        String javaVersion = join("", SampleRatelClass.HELLO.stringCopies(4));
        String rVersion = rString("squish(JSampleQClass$HELLO()$stringCopies_by_int(4))");
        assertEquals(javaVersion, rVersion);
    }

}
