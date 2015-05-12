package malbec.util;

import static org.testng.Assert.*;

import malbec.AbstractBaseTest;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

public class EmailAlertManagerTest extends AbstractBaseTest {

    @Test(groups = { "unittest" })
    public void testSendingUsingDefaultSchedule() {

        DateTimeUtil.freezeTime("2009/03/26 10:10:00");

        EmailAlertManager eam = new EmailAlertManager(new EmailSettings());
        long emailTime = new DateTime().getMillis();

        DateTime dt = eam.send("30MinuteError", "mfranz@fftw.com", "Job Failed with 30 minute email delay 1",
            "This would be the very lengthy email body\n" + new DateTime());

        assertNotNull(dt);
        assertTrue(emailTime <= dt.getMillis());

        DateTime dt2 = eam.send("30MinuteError", "mfranz@fftw.com",
            "Job Failed with 30 minute email delay 2", "This would be the very lengthy email body\n"
                + new DateTime());

        assertNotNull(dt2);
        assertEquals(dt2, dt);

        DateTimeUtil.freezeTime("2009/03/26 10:40:00");
        DateTime dt3 = eam.send("30MinuteError", "mfranz@fftw.com",
            "Job Failed with 30 minute email delay 3", "This would be the very lengthy email body\n"
                + new DateTime());

        assertFalse(dt3.equals(dt));
        assertEquals(dt3, new DateTime(2009, 3, 26, 10, 40, 0, 0));

        eam.setDefaultInterval(1000 * 60 * 10);
        
        assertEquals(eam.getDefaultInterval(), 1000 * 60 * 10);
        
        assertEquals(eam.getEventInterval("NEWEVENT"), 1000 * 60 * 10);
        eam.setEventInterval("NEWEVENT", 1000 * 60 * 11);
        
        assertEquals(eam.getEventInterval("NEWEVENT"), 1000 * 60 * 11);
        assertEquals(eam.getEventInterval("NEWEVENT2"), 1000 * 60 * 10);
        
        
    }
}
