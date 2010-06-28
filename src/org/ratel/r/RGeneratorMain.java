package org.ratel.r;

import org.ratel.r.file.*;
import org.ratel.r.generator.*;

import java.util.*;

import static org.ratel.r.Util.*;

public class RGeneratorMain {

    public static void main(String[] args) {
        // TODO: replace path
        RatelDirectory rDir = new RatelDirectory("C:/SVN/R/scr/Java/R");
        for (RatelFile f : rDir.files("J.*"))
            f.delete();
        Set<Class<?>> classes = new HashSet<Class<?>>(); 
        classes.add(List.class);
        classes.add(Map.class);
        classes.add(Set.class);
        classes.add(HashSet.class);
        classes.add(Iterator.class);
        classes.add(Double.class);
        classes.add(Integer.class);
        classes.add(Long.class);
        classes.add(String.class);
        classes.add(Date.class);

        Set<String> filesCreated = emptySet();
        RGenerator generator = new RGenerator();
        for (Class<?> c : classes) {
            RatelFile file = rDir.file("J" + c.getSimpleName() + ".R");
            bombIf(filesCreated.contains(file.name()), "already created a file with name " + file.name());
            filesCreated.add(file.name());
            file.create(generator.rCode(c));
            System.out.println("created file " + file.path());
        }
    }

}
