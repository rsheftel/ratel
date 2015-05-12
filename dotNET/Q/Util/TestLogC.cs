using System;
using NUnit.Framework;
using O = Q.Util.Objects;

namespace Q.Util {
    [TestFixture] public class TestLogC : DbTestCase {

        [Test]
        public void testLogStackTrace() {
            try {
                throw new Exception("outer", new NullReferenceException("inner"));
            } catch (Exception e) {
                LogC.err("logc err", e);
            }
        }

        [Test] public void testLogPrologIsCallable() {
           // info(util.Log.prolog() + "some string");
            O.info("some string"); 
        }

        [TearDown]
        public override void tearDown() {
            base.tearDown();
            LogC.setOut("Test", null, false);
        }

        [Test]
        public void testLogSetOutMultipleTimes() {
            LogC.setOut("Test", "foo", false);
            LogC.setOut("Test", "foo", false);
            LogC.setOut("Test", "bar", false);
            LogC.setOut("Test", "foo", false);
            LogC.setOut("Test", null, false);
            LogC.setOut("Test", null, false);
            LogC.setOut("Test", null, false);
        }              
    }
}