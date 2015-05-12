package tsdb;

import tsdb.alvinmetadbtest.*;
import db.*;

public class TestAlvinServiceMetaDbManager extends DbTestCase {

    public void testAlvinsUseCase() throws Exception {
        new AlvinServiceMetaDbManagerHiddenTestCode().doTest();
    }
}
