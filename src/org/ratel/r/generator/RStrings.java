package org.ratel.r.generator;


import org.ratel.r.*;

import java.util.*;

import static org.ratel.r.Util.*;


public class RStrings {

    public static String commaSep(RCode ... args) {
        return commaSep(list(args));
    }

    public static String commaSep(List<RCode> args) {
        return Util.commaSep(rStrings(args));
    }
    
    public static List<String> rStrings(RCode... args) {
        return rStrings(list(args));
    }

    public static List<String> rStrings(List<RCode> args) {
        List<String> s = empty();
        for (RCode arg : args)
            s.add(arg.toR());
        return s;
    }
    
}
