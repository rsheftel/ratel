package systemdb;

import db.*;
import file.*;

public abstract class FileGeneratorTestCase extends DbTestCase {

    protected QDirectory toDir = new QDirectory("bin/testtemp");

    @Override protected void setUp() throws Exception {
        super.setUp();
        toDir.destroyIfExists();
    }
    
    @Override protected void tearDown() throws Exception {
    	toDir.destroyIfExists();
    	super.tearDown();
    }

}