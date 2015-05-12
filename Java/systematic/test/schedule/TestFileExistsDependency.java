package schedule;

import static schedule.JobTable.*;
import static util.Dates.*;
import static util.Log.*;
import static util.RunCalendar.*;
import static util.Sequence.*;
import static util.Systematic.*;
import file.*;
import schedule.JobTable.*;
import schedule.dependency.*;

public class TestFileExistsDependency extends AbstractJobTest {
    
    private static final QDirectory FAKE_DATA = new QDirectory("./fakeData");

    @Override public void tearDown() throws Exception {
        setFakeDataDirectoryForTest(null);
        FAKE_DATA.destroyIfExists();
        super.tearDown();
    }
    
    public void testFileExists() throws Exception {
        setFakeDataDirectoryForTest(FAKE_DATA);
        Job nyb = JOBS.insert("waits", new RunMe(), THREE_PM, NYB);
        
        String uploadString = "[[DATA_DIR]]/PerformanceDB_upload/Upload/PNL_[[BIZ_YESTERDAY]]_To_[[DATE]].csv";
        Dependency exists = FileExists.create(nyb, uploadString);
        
        QDirectory base = FAKE_DATA.directory("PerformanceDB_upload").create();
        QDirectory upload = base.directory("Upload").create();
        assertIncomplete(exists, "2009/04/03");
        QFile $04_03 = upload.file("PNL_2009-04-02_To_2009-04-03.csv");
        QFile $04_06 = upload.file("PNL_2009-04-03_To_2009-04-06.csv");
        $04_03.create("!");
        assertComplete(exists, "2009/04/03");
        assertIncomplete(exists, "2009/04/06");
        assertIncomplete(exists, "2009/04/07");
        $04_06.create("!");
        assertComplete(exists, "2009/04/03");
        assertIncomplete(exists, "2009/04/04"); // don't check dependencies off calendar!
        assertComplete(exists, "2009/04/06");
        assertIncomplete(exists, "2009/04/07");
    }

    private void assertComplete(Dependency exists, String date) {
        assertFalse(exists.isIncomplete(date(date)));
    }

    private void assertIncomplete(Dependency exists, String date) {
        assertTrue(exists.isIncomplete(date(date)));
    }
    
    public void functestLastMonth() throws Exception {
        
        Job nyb = JOBS.insert("waits", new RunMe(), THREE_PM, NYB);
        String upload = "[[DATA_DIR]]/PerformanceDB_upload/Upload/PNL_[[BIZ_YESTERDAY]]_To_[[DATE]].csv";
        Dependency nybDependency = FileExists.create(nyb, upload);
        for(int i : sequence(0, 30)) {
            info(nybDependency.isIncomplete(daysAgo(i, midnight())) + "");
        }
        
    
    }

}
