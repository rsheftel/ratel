package r;

import static util.Errors.*;
import static util.Log.*;
import static util.Sequence.*;
import static util.Systematic.*;

import java.util.*;

import bloomberg.*;

import jms.*;
import junit.framework.*;
import mail.*;
import mortgage.*;
import r.generator.*;
import sto.*;
import systemdb.*;
import systemdb.data.*;
import systemdb.portfolio.*;
import tsdb.*;
import util.*;
import db.*;
import file.*;
import futures.*;

public class RGeneratorMain {

    public static void main(String[] args) {
        for (int i : oneTo(4))
            for (QFile f : mainDir().directory("R/src/Java" + i + "/R").files("J.*"))
                f.delete();
    	Set<Class<?>> classes = new HashSet<Class<?>>(); 
    	classes.addAll(QClass.allClassesInPackage(DataSource.class));
    	classes.add(List.class);
    	classes.add(Map.class);
    	classes.add(Set.class);
    	classes.add(HashSet.class);
    	classes.add(Iterator.class);
    	classes.add(Double.class);
    	classes.add(Integer.class);
    	classes.add(Long.class);
    	classes.add(String.class);
    	classes.add(DataUpload.class);
    	classes.add(Portfolio.class);
    	classes.add(Csv.class);
    	classes.add(System.class);
    	classes.add(Db.class);
    	classes.add(TestHistDailyLoader.class);
    	classes.add(SelectMultiple.class);
    	classes.add(MonthCode.class);
    	classes.add(Date.class);
    	classes.add(TbaTable.class);
    	classes.add(Bar.class);
    	classes.add(YearMonth.class);
    	classes.add(ContractCurrent.class);
    	classes.add(Contract.class);
    	classes.add(Expiry.class);
    	classes.add(BloombergSecurity.class);

    	classes.addAll(QClass.allClassesInPackage(systemdb.portfolio.Groups.class));
    	classes.addAll(QClass.allClassesInPackage(systemdb.data.Bar.class));
    	classes.addAll(QClass.allClassesInPackage(systemdb.data.bars.Bars.class));
    	classes.addAll(QClass.allClassesInPackage(systemdb.metadata.LiveSystem.class)); 
    	classes.addAll(QClass.allClassesInPackage(systemdb.qworkbench.GenerateMetricCsvs.class)); 
    	
    	
    	classes.addAll(QClass.allClassesInPackage(Objects.class));
    	classes.addAll(QClass.allClassesInPackage(QQueue.class));
    	classes.addAll(QClass.allClassesInPackage(Email.class));
    	classes.addAll(QClass.allClassesInPackage(QFile.class));
    	for(Iterator<Class<?>> i = classes.iterator(); i.hasNext(); ) 
    		if (TestCase.class.isAssignableFrom(i.next())) i.remove();

        classes.add(TestGroups.class); // going to use this one from R
    	classes.add(JMSTestCase.class);
    	
    	Set<String> filesCreated = Objects.emptySet();
    	RGenerator generator = new RGenerator();
    	bombUnless(classes.contains(Symbol.class), "symbol not loaded?");
    	for (Class<?> c : classes) {
    	    int dirNumber = Math.abs(c.getSimpleName().hashCode()) % 4 + 1;
    	    QDirectory dir = new QDirectory("C:/SVN/R/src/Java" + dirNumber + "/R");
    		QFile file = dir.file("J" + c.getSimpleName() + ".R");
    		bombIf(filesCreated.contains(file.name()), "already created a file with name " + file.name());
    		filesCreated.add(file.name());
    		file.create(generator.rCode(c));
    		info("created file " + file.path());
    	}
    }

}
