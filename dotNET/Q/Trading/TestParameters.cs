using NUnit.Framework;
using Q.Util;
using systemdb.metadata;
using JSystemTable = systemdb.metadata.SystemTable;

namespace Q.Trading {
    [TestFixture]
    public class TestParameters : DbTestCase {
        [Test]
        public void testParameters() {
            var p = new Parameters {
                {"ATRlen", 8}, 
                {"bool", 1}, 
                {"false", 0}
            };
            AreEqual(8, p.get<int>("ATRlen"));
            IsTrue(p.get<bool>("bool"));
            IsFalse(p.get<bool>("false"));
        }

        [Test]
        public void testParametersWithSTO() {
            JSystemTable.SYSTEM.insert("Test", "Q.Systems.ExampleSymbolSystem");
            var systemId = SystemDetailsTable.DETAILS.insert(
                "Test", "1", "daily", "asdf", Env.svn(@"dotNET\Q\testdata"), "testSTO"
            );
            var p = new Parameters{{"systemId", systemId}, {"RunMode", 1}, {"RunNumber", 2}, {"DeleteMe", 1}};
            AreEqual("Q.Systems.ExampleSymbolSystem", p.systemClassName());
            AreEqual(13, p.get<int>("ATRlen"));
            IsFalse(p.has("DeleteMe"));
        }

        [Test]
        public void testParametersLive() {
            JSystemTable.SYSTEM.insert("Test", "Q.Systems.ExampleSymbolSystem");
            var systemId = SystemDetailsTable.DETAILS.insert(
                "Test", "1", "daily", "TestPV", "asdf", "asdf"
            );
            ParameterValuesTable.VALUES.insert("Test", "TestPV", "ATRlen", "23");
            var p = new Parameters{{"systemId", systemId}, {"RunMode", 2}, {"DeleteMe", 1}};
            AreEqual(23, p.get<int>("ATRlen"));
            IsFalse(p.has("DeleteMe"));
        }
    }
}