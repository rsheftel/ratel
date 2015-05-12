package util;

import util.runcalendar.*;
import db.*;
import static util.RunCalendar.*;
import static util.Dates.*;

public class TestRunCalendar extends DbTestCase {
	public void testRunCalendar() throws Exception {
		assertRunCalendar(RunCalendar.from("nyb"), "nyb", 0);
		assertRunCalendar(RunCalendar.from("nyb+1"), "nyb", 1);
		assertRunCalendar(RunCalendar.from("nyb+10"), "nyb", 10);
		assertRunCalendar(RunCalendar.from("nyb-10"), "nyb", -10);
		try {
			RunCalendar.from("sevendays+1");
			fail();
		} catch (Exception success) {
			assertMatches("offset does not make sense", success);
		}
	}
	
	public void testWithOffsetToString() throws Exception {
	    assertEquals("nyb+2", new WithOffset(NYB, 2).dbName());
    }
    
    private void assertIsValid(RunCalendar cal, String date, String expectedPriorDay) {
        assertTrue(cal.isValid(cal.asOf(date(date))));
        assertEquals(date(expectedPriorDay), cal.priorDay(cal.asOf(date(date))));
    }
    private void assertNotValid(RunCalendar cal, String date, String expectedPriorDay) {
        assertFalse(cal.isValid(cal.asOf(date(date))));
        assertEquals(date(expectedPriorDay), cal.priorDay(cal.asOf(date(date))));
    }
	
	public void testIsValidDay() throws Exception {
        assertIsValid(NYB, "2008/07/03", "2008/07/02");
        assertNotValid(NYB, "2008/07/04", "2008/07/03");
        assertIsValid(NYB, "2008/07/07", "2008/07/03");

        RunCalendar plusOne = RunCalendar.from("nyb+1");
        assertIsValid(plusOne, "2008/07/04", "2008/07/02");
        assertNotValid(plusOne, "2008/07/05", "2008/07/03");
        assertIsValid(plusOne, "2008/07/08", "2008/07/03");
        assertIsValid(plusOne, "2008/07/12", "2008/07/10");
        
        assertIsValid(WEEKDAYS, "2008/07/04", "2008/07/03");
        assertNotValid(WEEKDAYS, "2008/07/05", "2008/07/04");
        
        RunCalendar tuesSat = RunCalendar.from("weekdays+1");
        assertIsValid(tuesSat, "2008/07/05", "2008/07/03");
        assertNotValid(tuesSat, "2008/07/06", "2008/07/04");
        assertIsValid(tuesSat, "2008/07/08", "2008/07/04");
        
        RunCalendar seven = RunCalendar.from("sevendays");
        assertIsValid(seven, "2008/07/05", "2008/07/04");
        assertIsValid(seven, "2008/07/06", "2008/07/05");
		
		RunCalendar lastNyb = RunCalendar.from("lastdayofweek_nyb");
		assertNotValid(lastNyb, "2009/05/28", "2009/05/22");
		assertIsValid(lastNyb, "2009/05/29", "2009/05/22");
		assertNotValid(lastNyb, "2009/05/30", "2009/05/29");
		assertNotValid(lastNyb, "2008/07/02", "2008/06/27");
        assertIsValid(lastNyb, "2008/07/03", "2008/06/27");
        assertNotValid(lastNyb, "2008/07/04", "2008/07/03");
                
        RunCalendar lastNybPlusOne = RunCalendar.from("lastdayofweek_nyb+1");
        assertNotValid(lastNybPlusOne, "2009/05/28", "2009/05/22");
        assertNotValid(lastNybPlusOne, "2009/05/29", "2009/05/22");
        assertIsValid(lastNybPlusOne, "2009/05/30", "2009/05/22");
        assertNotValid(lastNybPlusOne, "2009/05/31", "2009/05/29");
        assertNotValid(lastNybPlusOne, "2008/07/02", "2008/06/27");
        assertNotValid(lastNybPlusOne, "2008/07/03", "2008/06/27");
        assertIsValid(lastNybPlusOne, "2008/07/04", "2008/06/27");
        assertNotValid(lastNybPlusOne, "2008/07/05", "2008/07/03");
        
        
	}
	
	private void assertNotValid(RunCalendar cal, String date) {
	    assertFalse(cal.isValid(cal.asOf(date(date))));
	}

    public void testAsOfDate() throws Exception {
	    assertEquals(date("2008/07/03"), NYB.asOf(date("2008/07/03"))); // thurs
	    assertEquals(date("2008/07/04"), NYB.asOf(date("2008/07/04"))); // thurs
	    assertEquals(date("2008/07/04"), WEEKDAYS.asOf(date("2008/07/04"))); // fri
	    RunCalendar plusOne = RunCalendar.from("nyb+1");
	    assertEquals(date("2008/07/02"), plusOne.asOf(date("2008/07/03"))); // thurs
	    assertEquals(date("2008/07/03"), plusOne.asOf(date("2008/07/04"))); // fri
	    assertEquals(date("2008/07/04"), plusOne.asOf(date("2008/07/05"))); // sat
	    assertEquals(date("2008/07/05"), plusOne.asOf(date("2008/07/06"))); // sun
	    assertEquals(date("2008/07/06"), plusOne.asOf(date("2008/07/07"))); // mon
    }
	
	public void testWeirdBehavior() throws Exception {
	    assertNotValid(RunCalendar.from("nyb+1"), "2008/03/24");
    }

	private void assertRunCalendar(RunCalendar c, String name, int offset) {
		assertEquals(c.name(), name);
		assertEquals(c.offset(), offset);
	}
}
