package db;

import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import db.clause.*;

import util.*;
import file.*;
public class DataUpload {

	private final Uploads data = new Uploads();
	private String csvPath;

	class Uploads extends HashMap<Table, List<Row>> {
		private static final long serialVersionUID = 1L;

		List<ConcreteColumn<?>> columnOrder = empty();
		Map<Table, List<Column<?>>> keys = emptyMap();

		private boolean allSuccess;
		
		public void addColumn(boolean isPrimaryKey, ConcreteColumn<?> c) {
			if (!containsKey(c.owner())) put(c.owner(), new ArrayList<Row>());
			if (isPrimaryKey) {
			    if (!keys.containsKey(c.owner())) keys.put(c.owner(), new ArrayList<Column<?>>());
			    keys.get(c.owner()).add(c);
			}
			columnOrder.add(c);
		}

		public <T> void add(List<Cell<?>> datums) {
			Map<Table, Row> newRows = emptyMap();
			for (Cell<?> cell : datums) {
				ConcreteColumn<?> column = (ConcreteColumn<?>) cell.column;
				Table t = column.owner();
				if (!newRows.containsKey(t)) newRows.put(t, new Row());
				newRows.get(t).put(cell);
			}
			for (Table t : newRows.keySet()) 
				addRow(t, newRows.get(t));
		}

		private void addRow(Table table, Row row) {
			bombNull(get(table), "no table " + table + " in header data").add(row);
		}

		public void writeCsv(QFile file) {
			List<String> tables = empty();
			List<String> columns = empty();
			for(ConcreteColumn<?> c : columnOrder) {
				tables.add(c.owner().schemaTable().qualifiedName());
				columns.add(c.name());
			}
			Csv csv = new Csv(true);
			csv.add(tables);
			csv.add(columns);

			for(int i = 0; i < first(data.values()).size(); i++)
				writeCsvLine(csv, i);
			csv.write(file);
		}

		private void writeCsvLine(Csv file, int i) {
			List<String> record = empty();
			for(ConcreteColumn<?> c : columnOrder)
				record.add(tableRow(c, i).string(c));
			file.add(record);
		}

		private Row tableRow(ConcreteColumn<?> c, int i) {
			return nth(get(c.owner()), i + 1);
		}

		public boolean uploadFrom(QFile file) {
			bombUnless(columnOrder.isEmpty(), "can't use uploadFrom on populated Uploads with \n" + columnOrder);
			
			List<List<String>> records = new Csv(file).records();
			allSuccess = true;
			processHeaders(records);
			loadData(records);
			insertOrUpdateData();
			return allSuccess;
		}

		private void insertOrUpdateData() {
			List<Table> tables = empty();
			for (ConcreteColumn<?> c : columnOrder) 
				if (!tables.contains(c.owner())) tables.add(c.owner());
			for (Table table : tables) {
				for (Row row : get(table)) {
					Clause allMatch = row.allMatch(keys.get(table));
                    if (!table.rowExists(allMatch)) {
						try {
							table.insert(row);
							Log.dot();
						} catch (RuntimeException e) {
							Log.info("skipping failed row " + table + " " + row, e);
							allSuccess = false;
						}
					} else {
					    Log.info("updating existing row " + table + " " + row);
					    table.updateOne(row, allMatch);
					}
				}
			}
		}

		private boolean loadData(List<List<String>> records) {
			for (List<String> record : records) {
				if (record.isEmpty()) continue;
				if (record.equals(list(""))) continue;
				requireSameSize(record, columnOrder);
				
				List<Cell<?>> cells = empty();
				for(int col = 0; col < columnOrder.size(); col++) {
					Column<?> column = columnOrder.get(col);
					String value = record.get(col);
					if (Strings.isEmpty(value)) continue;
					try { 
						cells.add(column.withString(value));
					} catch (RuntimeException ryanWantsToLogOnlyOnBadData) {
						Log.info("warning: parse value failed: " + ryanWantsToLogOnlyOnBadData.getMessage());
						allSuccess = false;
					}
				}
				add(cells);
			}
			return allSuccess;
		}

		private void processHeaders(List<List<String>> records) {
			List<String> tables = first(records);
			bombIf(tables.isEmpty(), "No header line in csv file");
			if (first(tables).contains(":")) {
				processOneLineHeaders(tables);
				records.remove(0);
				return;
			}
			List<String> columns = second(records);
			requireSameSize(tables, columns);
			for(int i = 0; i < tables.size(); i++) {
				Table table = Tables.table(tables.get(i).trim());
				String columnName = columns.get(i).trim();
				boolean isPrimaryKey = columnName.matches(".*\\*");
				columnName = columnName.replaceAll("\\*$", "");
                addColumn(isPrimaryKey, table.column(columnName));
			}
			records.remove(0);
			records.remove(0);
		}

        private void processOneLineHeaders(List<String> headers) {
			for(int i = 0; i < headers.size(); i++) {
				String header = headers.get(i);
				String table = header.replaceFirst(":.*", "");
				String column = header.replaceFirst(".*:", "");
				bombIf(table.contains(":") || column.contains(":"), header + " invalid format, contains extra :");
				bombEmpty(table, "incorrect format for " + header + " use table:column");
				bombEmpty(column, "incorrect format for " + header + " use table:column");
                boolean isPrimaryKey = column.matches(".*\\*");
                column = column.replaceAll("\\*$", "");
				addColumn(isPrimaryKey, Tables.table(table).column(column));
			}
		}
		
		private void requireSameSize(List<?> list1, List<?> list2) {
			if (list1.size() == list2.size()) return;
			bomb(
				"list sizes do not match:\n" + list1 + "\n" + list2
			);
		}
	}
	
	public DataUpload(String csvPath) {
		this.csvPath = csvPath;
	}

	public DataUpload(QFile file) {
		this(file.path());
	}

	public void addColumn(ConcreteColumn<?> c) {
		data.addColumn(false, c);
	}

	public void add(List<Cell<?>> datums) {
		data.add(datums);
	}

	public void writeCsv() {
		data.writeCsv(new QFile(csvPath));
	}

	public boolean upload() {
		return data.uploadFrom(new QFile(csvPath));
	}

	public static void main(String[] args) {
		upload(args);
	}

	public static boolean upload(String[] args) {
		bombUnless(args.length > 0, "usage: DataUpload <filename>");
		String path = args[0];
		QFile file = new QFile(path);
		bombUnless(file.exists(), "file " + file + " does not exist.");
		try {
			turnOffSqlDebugging();
			boolean upload = new DataUpload(file).upload();
			Db.commit();
			return upload;
		} catch (RuntimeException e) {
			Db.rollback();
			throw bomb("failed before commit - all changes rolled back.", e);
		} finally {
			Db.rollback();
		}
	}

	@SuppressWarnings("deprecation") 
	private static void turnOffSqlDebugging() {
		Log.info("uploading to server " + Systematic.dbServer());
		Log.debugSql(false);
	}
}
