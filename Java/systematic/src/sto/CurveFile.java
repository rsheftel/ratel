package sto;

import static java.lang.Double.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.Sequence.*;

import java.io.*;
import java.util.*;

import util.*;
import file.*;
public class CurveFile {

	private static final long R_NA = 0x7ff80000000007a2L;
	private final QFile file;
	private double[][] data;
	private long rowCount;
	private final byte[] performanceBuffer = new byte[8]; // reuse buffer for perf

	public CurveFile(QFile file) {
		this.file = file;
	}
	
	public int size() {
        return (int) rowCount;
    }

    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(int row = 0; row < data[0].length; row++) 
			builder.append(data[0][row] + ", " + data[1][row] + ", " + data[2][row] + "\n");
		return builder.toString();
	}

	public void load() {
	    if (data != null) return;
	    if (file.basename().endsWith(".bin")) loadFromBinFile();
	    else loadFromCsvFile();
	}
	
	private void loadFromCsvFile() {
	    Csv csv = file.csv(true);
        rowCount = csv.count();
        data = new double[3][(int)rowCount];

	    List<List<String>> records = csv.records();
        for(int row : along(records)) {
            List<String> record = records.get(row);
            String dateHeader = first(csv.columns());
	        data[0][row] = Dates.date(csv.value(dateHeader, record)).getTime()/1000;
	        data[1][row] = parseDouble(csv.value("pnl", record)); 
	        data[2][row] = parseDouble(csv.value("position", record)); 
	    }
	}

    private void loadFromBinFile() {
        rowCount = file.size()/24;
		data = new double[3][(int)rowCount];
		InputStream in = file.in();
		try {
			for(int row = 0; row < rowCount; row++) 
				for(int col = 0; col < 3; col++) 
					data[col][row] = readDouble(in);
		} catch (IOException e) {
			throw bomb("IOException while loading data", e);
		} finally {
			try {
				in.close();
			} catch (IOException uncatchable) {
				uncatchable.printStackTrace();
			}
		}
    }

	private double readDouble(InputStream in) throws IOException {
		in.read(performanceBuffer, 0, 8);
		return Double.longBitsToDouble(
			(performanceBuffer[0] & 0xff) |
			((performanceBuffer[1] & 0xff) << 8) |
			((performanceBuffer[2] & 0xff) << 16) |
			(((long) (performanceBuffer[3] & 0xff)) << 24) |
			(((long) (performanceBuffer[4] & 0xff)) << 32) |
			(((long) (performanceBuffer[5] & 0xff)) << 40) |
			(((long) (performanceBuffer[6] & 0xff)) << 48) |
			(((long) (performanceBuffer[7] & 0xff)) << 56)
		);
	}

	public void save() {
		DataOutputStream dataOut = file.dataOut();
		try {
			for(int row = 0; row < rowCount; row++) {
				for(int col = 0; col < 3; col++) {
					Long protoValue = Double.isNaN(data[col][row]) 
						? R_NA 
						: Double.doubleToLongBits(data[col][row]);
					for(int i = 0; i < 8; i++) {
						performanceBuffer[i] = (byte) (protoValue & 0xff);
						protoValue >>= 8;
					}
					dataOut.write(performanceBuffer);
				}
			}
		} catch (IOException e) {
			throw bomb("IOException while loading data", e);
		} finally {
			try {
				dataOut.close();
			} catch (IOException uncatchable) {
				uncatchable.printStackTrace();
			}
		}
	}

	public void data(CurveFile curve) {
		data = curve.data.clone();
		rowCount = curve.rowCount;
	}

	public boolean dataMatches(CurveFile that) {
		return Arrays.deepEquals(data(), that.data());
	}

	private double[][] data() {
		return bombNull(data, "data has not been loaded yet");
	}

	public void add(CurveFile curveFile, double weight) {
		try {
			int currentRow = 0;
			double[] dates = curveFile.dates();
			for(int curveRow = 0; curveRow < dates.length; curveRow++) {
				while (date(currentRow) != dates[curveRow]) currentRow++; // scan forward to appropriate row
				data[1][currentRow] += curveFile.pnl(curveRow) * weight;
				currentRow++;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw bomb(
				"failed adding \n" + 
				curveFile.fileName() + "\nto\n" + 
				fileName() + "\nmost likely cause is mismatched dates.", e);
		}
	}

	private String fileName() {
		return file.path();
	}

	public double pnl(int row) {
		if (rowCount <= row) // unrolled bombUnless for perf
			bomb("only " + rowCount + " rows, asked for " + row);
		return data[1][row];
	}

    public double position(int row) {
        return data[2][row];
    }

    public double date(int row) {
        return data[0][row];
    }

	public boolean exists() {
		return file.exists();
	}
	
	

	public void init(double[] dates) {
		bombNotNull(data, "data already loaded in this curve file " + fileName());
		data = new double[3][dates.length];
		data[0] = dates;
		rowCount = dates.length;
		for(int row = 0; row < rowCount; row++) 
			data[2][row] = Double.NaN; 
	}

	public double[] dates() {
		return data[0];
	}

	public void ensurePath() {
		file.ensurePath();
	}

    public long count() {
        return rowCount;
    }

    public Date jDate(int i) {
        double dateValue = date(i);
        return new Date((long)dateValue * 1000);
    } 

}
