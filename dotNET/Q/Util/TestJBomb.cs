using System;
using NUnit.Framework;
using JDouble = java.lang.Double;

namespace Q.Util {
    [TestFixture]
    public class TestJBomb : QAsserts {
        [Test]
        public void testBomb() {
            try {
                util.Errors.bomb("boom!");
                Fail();
            } catch(Exception success) {
                Matches("boom", success);
            }
        }

        [Test]
        public void testParseDouble() {
            try {
                JDouble.parseDouble("TRUE");
                Fail();
            } catch (Exception success) {
                Matches("For input string", success);
            }
        }
    }


}
