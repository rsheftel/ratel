package transformations;

import static transformations.Constants.*;
import static transformations.Record.*;
import static transformations.SeriesCategory.*;
import static util.Objects.*;

import java.util.*;

import transformations.Transformation.*;

public class TestRTransformation extends TransformationTestCase {
	
	
	private static final SeriesDefinition TU_IN = new SeriesDefinition("MARKETDATA", "TU.1C", "LastPrice");
	private static final SeriesDefinition US_IN = new SeriesDefinition("MARKETDATA", "US.1C", "LastPrice");
	private static final SeriesDefinition SP_IN = new SeriesDefinition("MARKETDATA", "SP.1C", "LastPrice");
	private static final SeriesDefinition SP_OUT = new SeriesDefinition("TEST", "SP.1C", "BID");

	public void testSimpleTransformation() throws Exception {
		RTransformation t = transformation("SimpleTestTransformation", "'SP.1C'");
		Collection<SeriesDefinition> inputs = t.inputDefinitions();
		assertSize(1, inputs);
		SeriesDefinition inputDef = SP_IN;
		assertEquals(inputDef, the(inputs));
		assertEquals(SP_OUT, the(t.outputDefinitions()));
		OutputValues outputs = t.updateIfNeeded();
		assertEmpty(outputs.keySet());
		t.set(inputDef, "987.65");
		outputs = t.updateIfNeeded();
		assertEquals(SP_OUT, the(outputs.keySet()));
		assertEquals("987.650000000000", the(outputs.values()));
		outputs = t.updateIfNeeded();
		assertEmpty(outputs.keySet());
		t.set(inputDef, "");
		outputs = t.updateIfNeeded();
		assertEmpty(outputs.keySet());
	}
	
	public void testUpdateLoopHaltsAndSendsEmailWhenAllTransformsHaveFailed() throws Exception {
        LiveTransformation.publisher = new MockPublisher();
        TransformationUpdater updater = new TransformationUpdater("TestServer");
        RTransformation sp = transformation("SimpleTestTransformation", "'SP.1C'");
        RTransformation tu = transformation("SimpleTestTransformation", "'TU.1C'");
        updater.add(sp);
        updater.add(tu);
        assertFalse(updater.runOnce());
        sp.set(SP_IN, "foo");
        assertTrue(updater.runOnce());
        tu.set(TU_IN, "foo");
        try {
            updater.runOnce();
            fail("expected all failed");
        } catch(Exception success) {
            assertMatches("transformations failed for server TestServer", success);
        }
    }
	
	public void testUpdateloopHaltsAndEmailsWhenAllTransformsAreDisabled() throws Exception {
        LiveTransformation.publisher = new MockPublisher();
        TransformationUpdater updater = new TransformationUpdater("TestServer");
        RTransformation sp = transformation("TestTransformation", "'INIT_FAILURE'");
        updater.add(sp);
        RTransformation tu = transformation("TestTransformation", "'TU.1C'");
        updater.add(tu);
        SeriesDefinition def = new SeriesDefinition("TEST", "INIT_FAILURE", "Value");
        sp.set(def, "1234");
        updater.runOnce();
        tu.set(new SeriesDefinition("TEST", "TU.1C", "Value"), "1234");
        updater.runOnce();
        updater = new TransformationUpdater("TestServer");
        sp = transformation("TestTransformation", "'INIT_FAILURE'");
        updater.add(sp);
        tu = transformation("TestTransformation", "'INIT_FAILURE', 'foo'");
        updater.add(tu);
        sp.set(def, "1234");
        updater.runOnce();
        tu.set(def, "1234");
        try {
            updater.runOnce();
            fail("expected all disabled");
        } catch(Exception success) {
            assertMatches("initialization failed for all transformations for server TestServer", success);
        }
    }

    public void testUpdateLoop() throws Exception {
		MockPublisher publisher = new MockPublisher();
        LiveTransformation.publisher = publisher;
		TransformationUpdater updater = new TransformationUpdater("TestServer");
		RTransformation sp = transformation("SimpleTestTransformation", "'SP.1C'");
		RTransformation tu = transformation("SimpleTestTransformation", "'TU.1C'");
		RTransformation us = transformation("SimpleTestTransformation", "'US.1C'");
		updater.add(sp);
		updater.add(tu);
		updater.add(us);
		sp.set(SP_IN, "1234.56");
		updater.runOnce();
		List<String> outputs = publisher.published();
		assertEquals("[TEST:||:SP.1C:||:BID:||:1234.560000000000]", outputs.toString());
		publisher.clear();
		sp.set(SP_IN, "4321.56");
		tu.set(TU_IN, "123.56");
		assertTrue(updater.runOnce());
		outputs = publisher.published();
		assertEquals("[TEST:||:SP.1C:||:BID:||:4321.560000000000, TEST:||:TU.1C:||:BID:||:123.560000000000]", outputs.toString());
		publisher.clear();
		us.set(US_IN, "232.56");
		assertTrue(updater.runOnce());
		outputs = publisher.published();
		assertEquals("[TEST:||:US.1C:||:BID:||:232.560000000000]", outputs.toString());
		publisher.clear();
		assertFalse(updater.runOnce());
		assertSize(0, publisher.published());
	}
	
	public void testEmptyResponse() throws Exception {
		Transformation sp = transformation("EmptyTestTransformation", "'SP.1C'");
		sp.set(SP_IN, "1234.56");
		assertEmpty(sp.updateIfNeeded().keySet());
	}
	
	public void testUpdaterLoad() throws Exception {
		MockPublisher publisher = new MockPublisher();
        LiveTransformation.publisher = publisher;
		TransformationLoader loader = new RTransformationLoader(list("TEST"));
		TransformationUpdater updater = new TransformationUpdater(loader.name());
		loader.load(updater);
		assertSize(5, updater.transformations);
		TransformationReceiver receiver = new TransformationReceiver(updater);
		receiver.updated(list(category("MARKETDATA", record("SP.1C", 7))));
		updater.runOnce();
		assertEquals("[TEST:||:SP.1C:||:BID:||:7.000000000000]", publisher.published().toString());
		updater.runOnce();
		publisher.clear();
		assertEquals("[]", publisher.published().toString());
		updater.runOnce();
		receiver.updated(list(category("MARKETDATA", record("SP.DNE", 7))));
		assertEquals("[]", publisher.published().toString());
		publisher.clear();
		updater.runOnce();
		assertEquals("[]", publisher.published().toString());
		
	}
	
	public void functestFunc() throws Exception {
	    TransformationUpdater updater = new TransformationUpdater("TestServer");
        updater.add(new RTransformation("TEST", "SimpleTestTransformation", "'SP.1C'", TRANSPORT_ACTIVE_MQ));
        TransformationReceiver receiver = new TransformationReceiver(updater);
        receiver.subscribe();
        updater.run();
    }
}
