package r;

import static transformations.Constants.*;
import static util.Objects.*;
import db.*;
import file.*;

public class TestTimeSeriesUploader extends DbTestCase {
    
    private QDirectory upload = new QDirectory("./upload");

    @Override protected void setUp() throws Exception {
        super.setUp();
        upload.destroyIfExists();
        upload.create();
        QDirectory today = upload.directory("Today");
        today.create();
        QFile file = today.file("upload_me.csv");
        Csv csv = new Csv(true);
        csv.add(list("garbage"));
        csv.write(file);
        upload.directory("Archive").create();
        upload.directory("Failures").create();
    }
    
    @Override protected void tearDown() throws Exception {
        upload.destroyIfExists();
        super.tearDown();
    }
    
    public void testUploaderWorks() throws Exception {
        emailer.allowMessages();
        new TimeSeriesUploader(upload, FAILURE_ADDRESS).run();
        emailer.sent().hasContent("upload_me.csv");
    }
    
    public void testConvertToWindows() throws Exception {
        assertEquals("V:\\this\\is\\a\\test.csv", TimeSeriesUploader.convertToWindows("/data/this/is/a/test.csv"));
    }
}
