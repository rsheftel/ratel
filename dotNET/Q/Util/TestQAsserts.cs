using NUnit.Framework;
using System;

namespace Q.Util {
    [TestFixture]
    public class TestQAsserts : DbTestCase {
        [Test]
        public void test() {
            DatesMatch("2000/07/07", "2000/07/07");
            Bombs(() => DatesMatch("2000/07/07", "2000/07/08"), "dates did not match");
        }

        [Test]
        public void testBombs() {
            Bombs(() => DatesMatch("2000/07/07", "2000/07/08"), "dates did not match");
            try {
                Bombs(() => DatesMatch("2000/07/07", "2000/07/07"));
                Fail("success when expecting failure did not cause bombs to fail");           
            } catch (Exception e) {
                Matches("expected failure did not occur", e);
            }
            Bombs(() => DatesMatch("2000/07/07", "2000/07/08"), "dates did not match");
            try {
                Bombs(() => DatesMatch("2000/07/07", "2000/07/08"), "this is the wrong message");
                Fail();
            } catch (Exception e) {
                Matches("this is the wrong message", e);
            }
            Bombs(failsWithNestedException, "caught an exception", "dates did not match");
            try {
                Bombs(failsWithNestedException, "caught an exception", "dates match");
                Fail();
            } catch (Exception e) {
                Matches("expected patterns:", e);
            }
            try {
                Bombs(failsWithNestedException, "no exception", "dates did not match");
                Fail();
            } catch (Exception e) {
                Matches("expected patterns:", e);
            }
            try {
                Bombs(failsWithNestedException, "caught an exception", "dates did not match", "no exception here");
                Fail();
            } catch (Exception e) {
                Matches("expected patterns:", e);
            }

        }

        static void failsWithNestedException() {
            try {
                DatesMatch("2000/07/07", "2000/07/08");
            }
            catch (Exception e) {
                Bomb.toss("caught an exception", e);
            }
        }
    }
}
