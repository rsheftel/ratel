using NUnit.Framework;
using Q.Spuds.Core;
using Q.Util;

namespace Q.Spuds.Indicators {
    public class SpudTestCase<SOURCE, TARGET> : QAsserts {
        protected SpudManager manager;
        protected RootSpud<SOURCE> values;
        protected Spud<TARGET> indicator;

        [SetUp]
        public void setUp() {
            manager = new SpudManager();
            values = new RootSpud<SOURCE>(manager);
        }

        protected virtual void addPoint(SOURCE newInput, TARGET newOutput) {
            addPoint(newInput);
            AreEqual(newOutput, indicator[0]);
        }

        protected void addPoint(SOURCE newInput) {
            manager.newBar();
            values.set(newInput);
        }

    }
}