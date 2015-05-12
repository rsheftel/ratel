package sto;

import static util.Objects.*;
import static util.Range.*;

import java.util.*;

import util.*;

import junit.framework.*;
import file.*;

public class TestCombineCurves extends TestCase {
	private QDirectory stoDir = new QDirectory("test/sto/SimpleSTOTemplate");
	private QDirectory raggedStoDir = new QDirectory("test/sto/SimpleSTORaggedDates");

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		stoDir.file("CurvesBin", "ABC_1_daily_mkt1", "run_test.bin").deleteIfExists();
		stoDir.directory("CurvesBin", "ABC_1_daily_test").destroyIfExists();
		stoDir.directory("CurvesBin", "ABC_1_daily_combined").destroyIfExists();
		raggedStoDir.directory("CurvesBin", "ABC_1_daily_combined").destroyIfExists();
	}
	
	public void testCanReadWriteBinaryFile() throws Exception {
		CurveFile curve = new CurveFile(stoDir.file("CurvesBin/ABC_1_daily_mkt1/run_1.bin"));
		CurveFile outputCurve = new CurveFile(stoDir.file("CurvesBin/ABC_1_daily_mkt1/run_test.bin"));
		readWriteMatch(curve, outputCurve);
	}
	
	public void testCurvesCanLoadOneBinFile() throws Exception {
		Curves curves = new Curves(stoDir);
		CurveFile mkt1 = curves.curve("ABC_1_daily_mkt1", 1);
		CurveFile test = curves.curve("ABC_1_daily_test", 1);
		readWriteMatch(mkt1, test);
		Curve mkt1Curve = new Curve(mkt1);
		Range expected = range("2005-11-10", "2005-11-15");
        assertEquals("\n" + expected + "\n" + mkt1Curve.dateRange() + "\n", expected, mkt1Curve.dateRange());
	}

	private void readWriteMatch(CurveFile read, CurveFile write) {
		read.load();
		write.ensurePath();
		write.data(read);
		write.save();
		write.load();
		assertCurvesMatch(read, write, "read and written don't match ");		
		assertTrue("input curve and output curve do not match", read.dataMatches(write));
	}
	
	public void testLoadPortfolio() throws Exception {
		Portfolio port = new Portfolio(stoDir.file("Portfolios/combined"));
		List<WeightedMsiv> msivs = port.msivs();
		assertEquals("ABC_1_daily_mkt1", first(msivs).msiv());
		assertEquals(2.0, second(msivs).weight());
	}
	
	public void testCombine() throws Exception {
		checkCombo(stoDir);
	}
	
	public void checkCombo(QDirectory sto) throws Exception {
		Portfolio port = new Portfolio(sto.file("Portfolios/combined"));
		Curves curves = new Curves(sto);
		port.combineCurves(curves, 20);
		String combined = "ABC_1_daily_combined";
		String expected = "ABC_1_daily_expected";
		checkCurves(curves, expected, combined, curves.runCount(port.msivs()));
		CurveFile expected1 = curves.curve(expected, 1);
		expected1.load();
		CurveFile combined1 = curves.curve(combined, 1);
		combined1.load();
		combined1.add(combined1, 1.0);
		combined1.save();
		port.combineCurves(curves, 1);
		combined1.load();
		assertFalse(combined1.dataMatches(expected1));
	}

	private void checkCurves(Curves curves, String expectedName, String actualName, int runCount) {
		for(int run = 1; run <= runCount; run++) {
			CurveFile expected = curves.curve(expectedName, run);
			CurveFile actual = curves.curve(actualName, run);
			expected.load();
			actual.load();
			String message = "data in run " + run + " did not match\n";
			assertCurvesMatch(expected, actual, message);
		}
	}

	private void assertCurvesMatch(CurveFile expected, CurveFile actual,
			String message) {
		assertTrue(message + "expected:\n" + expected + "actual:\n" + actual, expected.dataMatches(actual));
	}
	
	public void testCombineRaggedDates() throws Exception {
		checkCombo(raggedStoDir);
	}

}
