package systemdb.portfolio;

import static java.util.Collections.*;
import static systemdb.portfolio.GroupLeafs.*;
import static util.Objects.*;

import java.util.*;

import systemdb.metadata.*;
import util.*;

public class TestGroups extends MsivPvTestCase {
	
	@Override public void setUp() throws Exception {
		super.setUp();
		Groups.GROUPS.insert("test");
		Groups.GROUPS.insert("foob");
		Groups.GROUPS.insert("mega");
		Groups.GROUPS.insert("no leaves");
		LEAFS.insert("test", SP_1C_FAST, 2);
		LEAFS.insert("test", SP_1C_SLOW, 0.5);
		LEAFS.insert("test", US_1C_FAST, 3);
		LEAFS.insert("foob", US_1C_SLOW, 7);
		Groups.GROUPS.insert("mega", "test", 2);
		Groups.GROUPS.insert("mega", "foob", 4);
		// mega is 4 * SP FAST + 1 * SP SLOW + 6 * US FAST + 28 * US SLOW 
	}
	
	/** called from R - needed for simple access */ 
	@Override public void tearDown() throws Exception {
		super.tearDown();
	}
	
	/** called from R - needed for simple access */ 
	@Override public void releaseLock() {
	    super.releaseLock();
	}
	
	public void testCanReadAndWriteGroupToXml() throws Exception {
	    Group g = Groups.GROUPS.forName("mega");
	    Tag rep = Tag.parse("<root />");
	    g.addTo(rep);
	    Groups.GROUPS.load(rep.child("group"), "jeff_");
	    Group reloaded = Groups.GROUPS.forName("jeff_mega");
	    Tag reloadedXml = new Tag("root");
	    reloaded.addTo(reloadedXml);
	    List<MpvWeight> reloadedWeights = weightList(reloaded);
	    List<MpvWeight> originalWeights = weightList(reloaded);
	    assertEquals(reloadedWeights, originalWeights);
    }
	
	static class MpvWeight { 
	    String file;
	    double weight;
        public MpvWeight(String file, double weight) {
            this.file = file;
            this.weight = weight;
        }
        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((file == null) ? 0 : file.hashCode());
            long temp;
            temp = Double.doubleToLongBits(weight);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            final MpvWeight other = (MpvWeight) obj;
            if (file == null) {
                if (other.file != null) return false;
            } else if (!file.equals(other.file)) return false;
            if (Double.doubleToLongBits(weight) != Double.doubleToLongBits(other.weight)) return false;
            return true;
        }
	    
	}
	
	private List<MpvWeight> weightList(Group reloaded) {
        List<MpvWeight> result = empty();
        Map<MsivPv, Double> weighting = reloaded.weighting();
        List<MsivPv> mpvs = list(weighting.keySet());
        sort(mpvs);
        for(MsivPv mpv : mpvs) result.add(new MpvWeight(mpv.fileName(), weighting.get(mpv)));
        return result;
    }

    public void testCanGenerateMsivPvWeightings() throws Exception {
		Map<MsivPv, Double> weightings = LEAFS.weighting("test", 2);
		assertSize(3, weightings.keySet());
		assertEquals("key " + SP_1C_FAST + " not found in \n" + weightings + " ", 4.0, weightings.get(SP_1C_FAST));
	}
	
	public void testCanGenerateMegaGroupWeightings() throws Exception {
		Map<MsivPv, Double> weightings = Groups.GROUPS.weighting("mega");
		assertSize(4, weightings.keySet());
		assertEquals(4.0, weightings.get(SP_1C_FAST));
		assertEquals(28.0, weightings.get(US_1C_SLOW));
	}
	
	public void testWeightingLookupWorksAtAnyLevel() throws Exception {
		Map<MsivPv, Double> weightings = Groups.GROUPS.weighting("test");
		assertSize(3, weightings.keySet());
		assertEquals(2.0, weightings.get(SP_1C_FAST));
	}
	
	public void testWeightingLookupWorksWhenNoLeafNodes() throws Exception {
	    LEAFS.delete("foob");
        Map<MsivPv, Double> weightings = Groups.GROUPS.weighting("mega");
        assertSize(3, weightings.keySet());
        assertEquals(4.0, weightings.get(SP_1C_FAST));
        assertNull(weightings.get(US_1C_SLOW));
    }
	
	public void testWeightedMsivPvFiles() throws Exception {
		WeightedMsivPvFiles wmpf = Groups.GROUPS.rWeighting("mega");
		double[] expectedWeights = new double[] { 4.0, 1.0, 6.0, 28.0 };
		String[] expectedFiles = array(
			"TestSystem1_1.0_daily_Fast_TEST.SP.1C", "TestSystem1_1.0_daily_Slow_TEST.SP.1C", 
			"TestSystem1_1.0_daily_Fast_TEST.US.1C", "TestSystem1_1.0_daily_Slow_TEST.US.1C"
		);
		String[] expectedSystems = array("TestSystem1","TestSystem1", "TestSystem1", "TestSystem1");
		String[] expectedIntervals = array("daily","daily", "daily", "daily");
		String[] expectedVersions = array("1.0", "1.0", "1.0", "1.0");
		String[] expectedMarkets = array("TEST.SP.1C", "TEST.SP.1C", "TEST.US.1C", "TEST.US.1C");
		String[] expectedPvs = array("Fast", "Slow", "Fast", "Slow");
		String[] expectedMsivs = array(
            "TEST.SP.1C_TestSystem1_daily_1.0", "TEST.SP.1C_TestSystem1_daily_1.0", 
            "TEST.US.1C_TestSystem1_daily_1.0", "TEST.US.1C_TestSystem1_daily_1.0"
        );
		assertTrue(Arrays.equals(expectedFiles, wmpf.filenames()));
		assertTrue(Arrays.equals(expectedWeights, wmpf.weights()));
		assertTrue(Arrays.equals(expectedSystems, wmpf.systems()));
		assertTrue(Arrays.equals(expectedIntervals, wmpf.intervals()));
		assertTrue(Arrays.equals(expectedVersions, wmpf.versions()));
		assertTrue(Arrays.equals(expectedMarkets, wmpf.markets()));
		assertTrue(Arrays.equals(expectedPvs, wmpf.pvs()));
		assertTrue("" + list(wmpf.msivs()), Arrays.equals(expectedMsivs, wmpf.msivs()));
	}
	
	
}
