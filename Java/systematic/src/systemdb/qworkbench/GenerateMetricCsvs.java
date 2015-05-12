package systemdb.qworkbench;

import static util.Errors.*;
import db.*;
import sto.*;

public class GenerateMetricCsvs {

    public static void main(String[] args) {
        if(args.length != 1) bomb("Usage: jrun systemdb.qworkbench.GenerateMetricCsvs <systemId> [-dir C:\\logs\\");
        int systemId = Integer.parseInt(args[0]);
        STO sto = STO.fromId(systemId);
        try {
            Db.setQueryTimeout(30000);
            sto.generateMetricCsvs();
        } catch (OutOfMemoryError e) {
            throw bomb("try using jrunBig ", e);
        }
    }

}
