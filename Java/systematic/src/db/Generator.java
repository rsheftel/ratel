package db;

import static util.Objects.*;
import static util.Errors.*;
import java.util.*;

import file.*;

public class Generator {

	public static void main(String[] args) {
		Generator generator = new Generator();
		QDirectory dir = new QDirectory("./src/db/tables");
		dir.removeAllNonHiddenFilesRecursive();
		generator.writeFiles("TSDB");
		generator.writeFiles("SystemDB");
		generator.writeFiles("IvyDB");
		generator.writeFiles("BloombergFeedDB");
		generator.writeFiles("ScheduleDB");
		generator.writeFiles("PerformanceDB");
		generator.writeFiles("LiveDB");
		generator.writeFiles("MetricDB");
	}

	private void writeFiles(String dbName) {
		QDirectory dir = new QDirectory("./src/db/tables/" + dbName);
		if(!dir.exists()) dir.create();
		List<SchemaTable> tables = nonEmpty(Schema.tables(dbName));
		for (SchemaTable table : tables) {
			System.out.println("Processing " + table.qualifiedName());
			writeFile(table, "tables");
		}
	}

	public void writeFile(SchemaTable table, String superPackage) {
		bombUnless(new QDirectory(".").directory("src").exists(), "current directory not where expected!");
		QFile file = new QFile("./src/db/" + superPackage + "/" + table.fileName());
		file.create(table.java(superPackage));
	}
}
