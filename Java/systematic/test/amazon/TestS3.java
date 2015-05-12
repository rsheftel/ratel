package amazon;

import static util.Log.*;
import static util.Objects.*;

import java.util.*;

import amazon.MetaBucket.*;
import db.*;

public class TestS3 extends DbTestCase {
    public void testS3() throws Exception {
        MetaBucket bucket = new MetaBucket("quantys-test");
        Key sample = bucket.key("noprefix", "imaobject");
        sample.write("contents");
        assertEquals("contents", sample.read());
    }
    
    public void testKeys() throws Exception {
        MetaBucket one = new MetaBucket("quantys-one");
        MetaBucket two = new MetaBucket("quantys-two");
        one.create();
        addStuffs(one, two);
        List<Key> keys = one.keys("prefix1");
        assertSize(3, keys);
        List<String> stuffs = copy(list("stuff1", "stuff2", "stuff3"));
        for (Key key : keys) {
            String contents = (String) key.read();
            assertContains(contents, stuffs);
            stuffs.remove(contents);
        }
        assertSize(6, two.keys());
    }
    
    public void testAllBuckets() throws Exception {
        MetaBucket one = new MetaBucket("quantys-one");
        MetaBucket two = new MetaBucket("quantys-two");
        one.create();
        addStuffs(one, two);
        List<MetaBucket> buckets = MetaBucket.all();
        assertContains(one, buckets);
        assertContains(two, buckets);
    }

    private void addStuffs(MetaBucket one, MetaBucket two) {
        one.key("prefix1", "name1").write("stuff1");
        one.key("prefix1", "name2").write("stuff2");
        one.key("prefix1", "name3").write("stuff3");
        one.key("prefix2", "name1").write("stuff4");
        one.key("prefix2", "name2").write("stuff5");
        one.key("prefix2", "name3").write("stuff6");
        two.key("prefix3", "name1").write("stuff7");
        two.key("prefix3", "name2").write("stuff8");
        two.key("prefix3", "name3").write("stuff9");
        two.key("prefix2", "name1").write("stuff10");
        two.key("prefix2", "name2").write("stuff11");
        two.key("prefix2", "name3").write("stuff12");
    }
    
    public void testRequestNonExistentKey() throws Exception {
        MetaBucket bucket = new MetaBucket("quantys-test");
        assertNull(bucket.key("noprefix", "nonexistent").readOrNull());
    }
    
    public void functestBucketReport() throws Exception {
        info(new MetaBucket("quantys-356293").report(""));
    }
    
    public void functestKeys() throws Exception {
        info("" + new MetaBucket("quantys-179024").keys(EC2Runner.SUPPORTING_FILES));
    }
    
    public void functestFiles() throws Exception {
        List<Key> keys = new MetaBucket("quantys-356293").keys("parameters.");
        for (Key key : keys) {
			info("" + key.keyName());
		}
    }
    
    public void slowTestBucketReport() throws Exception {
        doNotDebugSqlForever();
        info(MetaBucket.reportAll(null));
    }
}
