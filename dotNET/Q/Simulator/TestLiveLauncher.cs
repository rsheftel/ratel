using NUnit.Framework;
using Q.Recon;
using Q.Util;
using systemdb.metadata;

namespace Q.Simulator {
    [TestFixture]
    public class TestLiveLauncher : DbTestCase {
        LiveSystem liveSystem;
        LiveLauncher launcher;

        [Test]
        public void testStart() {
            liveSystem.populateTagIfNeeded("QF.Example", false);
            var system = liveSystem.siv().system();
            launcher.start(system);
            launcher.requireStarted(system);
        }

        public override void  setUp() {
            base.setUp();
            liveSystem = new LiveSystem(new Siv("TestSystem1", "daily", "1.0"), new Pv("Slow"));
            liveSystem.populateDetailsIfNeeded(false);

            launcher = new LiveLauncher();
            launcher.beInTestMode();
        }

        [Test]
        public void testCannotStartInWrongMode() {
            liveSystem.populateTagIfNeeded("QF.Example", true);
            FerretControl.setStatus("Reject");
            emailer.allowMessages();
            launcher.start(liveSystem.siv().system());
            launcher.noneStarted();
            emailer.requireSent(1);
        }

        public static void Main(string[] args) {
            OrderTable.prefix = args[0];
            new LiveLauncher().run();
            Objects.sleep(int.MaxValue);
        }
    }
}
