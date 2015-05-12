package schedule;

import static util.Objects.*;

import java.util.*;

import file.*;

import schedule.JobTable.*;
import schedule.dependency.*;

public class FileExists extends Dependency {

    private String file;

    public FileExists(Integer id, Map<String, String> parameters) {
        super(id);
        file = parameters.get("file");
    }

    @Override public String explain(Date asOf) {
        QFile qFile = file(file, asOf);
        return qFile.path() + "misssing";
    }

    @Override public boolean isIncomplete(Date asOf) {
        QFile qFile = file(file, asOf);
        util.Log.info("file: " + file + " " + qFile + " " + qFile.missing());
        return qFile.missing();
    }

    private QFile file(String path, Date asOf) {
        String normalized = path.replace('/', '\\');
        String expanded = JobTable.expanded(normalized, asOf, job().calendar());
        expanded = expanded.replace('/', '-');
        expanded = expanded.replace('\\', '/');
        return new QFile(expanded);
    }

    public static Dependency create(Job parent, String file) {
        return parent.insertDependency(FileExists.class, map("file", file));
    }
    
}