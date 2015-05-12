using System.Collections.Generic;
using db;
using NUnit.Framework;
using systemdb.metadata;
using O=Q.Util.Objects;
using Q.Trading;
using Q.Util;
using DbTestCase=Q.Util.DbTestCase;
using Market=systemdb.metadata.Market;

namespace Q.Research {
    [TestFixture]
    public class TestResearch : DbTestCase {
        [Test]
        public void testCanRunSystem() {
            Db.reallyRollback(); // open transaction system details insert (on unrelated  row, jeff hates sqlserver) from DbTestCase causes test failure.
            var gui = initializeWorkingGui();
            O.wait(() => gui.runButtonEnabled == false);
            O.wait(100, 100, gui.runComplete);
            IsTrue(gui.runButtonEnabled);
            var simulator = gui.researcher.simulator;
            HasCount(1, O.list(gui.researcher.positions));
            var info = gui.researcher.positionInfo(O.the(gui.researcher.positions));
            AlmostEqual(4640.63, info.pnl(0), 0.01);
            AlmostEqual(1546.88 - 49500, info.pnl(5), 0.01);
            AlmostEqual(-121684.1969, simulator.pnl(), 0.001);
            gui.noMessage();
        }

        static FakeResearchGUI initializeWorkingGui() {
            var gui = new FakeResearchGUI();
            gui.setMarkets(O.list("RE.TEST.TY.1C"));
            var parameters = new Parameters {
                {"ATRLen", 10},
                {"ATRlong", 100},
                {"BreakDays", 30},
                {"FirstDayATR", 1},
                {"FixEquity", 1},
                {"InitEquity", 6000000},
                {"LeadBars", 50},
                {"MaxPyramid", 1},
                {"Risk", 0.02},
                {"nATR", 2},
                {"upATR", 2},
                {"systemId", 39} // ok?
            };
            gui.setParameters(parameters);
            gui.setStartDate(date("2001/01/01"));
            gui.setEndDate(date("2001/04/01"));
            gui.setRunInNativeCurrency(true);
            gui.runSystem();
            return gui;
        }

        [Test]
        public void testCanFailToConstructSystemGracefully() {
            Db.reallyRollback(); // open transaction system details insert (on unrelated  row, jeff hates sqlserver) from DbTestCase causes test failure.
            var gui = new FakeResearchGUI();
            gui.setMarkets(O.list("RE.TEST.TY.1C"));
            gui.setParameters(new Parameters {
                {"systemId", 39} 
            });
            gui.setStartDate(date("2001/01/01"));
            gui.setEndDate(date("2001/04/01"));
            gui.runSystem();
            O.wait(() => gui.runButtonWasDisabled);
            O.wait(100, 100, gui.runComplete);
            IsTrue(gui.runButtonEnabled);
            IsNull(gui.researcher.simulator);
            gui.hasMessage("failed");
        }

        [Test]
        public void testCanFailToRunCompleteSimulation() {
            Db.reallyRollback(); // open transaction system details insert (on unrelated  row, jeff hates sqlserver) from DbTestCase causes test failure.
            var gui = new FakeResearchGUI();
            gui.setMarkets(O.list("RE.TEST.TY.1C"));
            var parameters = new Parameters {
                {"ATRLen", 10},
                {"ATRlong", 100},
                {"BreakDays", 30},
                {"FixEquity", 1},
                {"InitEquity", 6000000},
                {"LeadBars", 50},
                {"MaxPyramid", 1},
                {"Risk", 0.02},
                {"nATR", 2},
                {"upATR", 2},
                {"systemId", 39} // ok?
            };
            gui.setParameters(parameters);
            gui.setStartDate(date("2001/01/01"));
            gui.setEndDate(date("2001/04/01"));
            gui.runSystem();
            O.wait(() => gui.runButtonEnabled == false);
            O.wait(100, 100, gui.runComplete);
            IsTrue(gui.runButtonEnabled);
            gui.hasMessage("failed");
            LogC.info("done");
        }

        [Test]
        public void testSaveLoadRunInfo() {
            var gui = initializeWorkingGui();
            gui.wait(gui.runComplete);
            const string name = "imarun";
            gui.setName(name);
            gui.saveSettings();
            var gui2 = new FakeResearchGUI();
            gui2.setName(name);
            gui2.loadSettings();
            AreEqual(gui, gui2);
            gui2.loadSettings();
            AreEqual(gui, gui2);
        }

        [Test]
        public void testCanPopulateStuffFromSystemIdLive() {
            O.freezeNow("2009/04/28");
            var gui = new FakeResearchGUI();
            gui.setSystemId("39");
            gui.loadSystem();
            gui.doAllWork();
            var parameters = gui.parameters();
            AreEqual(39, parameters.get<int>("systemId"));
            AreEqual(50, parameters.get<int>("LeadBars"));
            AreEqual(30, parameters.get<int>("BreakDays"));
            IsTrue(gui.runNumberEnabled());
            IsFalse(gui.runInNativeCurrency());
            var markets = O.convert(O.list<Market>(SystemDetailsTable.DETAILS.details(39).liveSystem().markets()), m => m.name());
            AreEqual(O.list(markets), gui.markets());
        }

        [Test]
        public void testCanPopulateParametersFromSystemIdNoPvNoStoId() {
            var gui = new FakeResearchGUI();
            gui.setSystemId("133486");
            gui.loadSystem();
            gui.doAllWork();
            var parameters = gui.parameters();
            AreEqual(133486, parameters.get<int>("systemId"));
            AreEqual("0", parameters.get<string>("LeadBars"));
            AreEqual("0", parameters.get<string>("closeBetter"));
            IsFalse(gui.runNumberEnabled());
            AreEqual(new List<string>(), gui.markets());
        }

        [Test]
        public void testCanPopulateParametersFromSystemIdWithStoId() {
            var gui = new FakeResearchGUI();
            gui.setSystemId("178114");
            gui.loadSystem();
            gui.doAllWork();
            var details = SystemDetailsTable.DETAILS.details(178114);
            var markets = O.list<string>(MsivBacktestTable.BACKTEST.markets(details.siv(), details.stoId()));
            AreEqual(O.sort(markets), O.sort(gui.markets()));
            var parameters = gui.parameters();
            AreEqual(178114, parameters.get<int>("systemId"));
            AreEqual("0", parameters.get<string>("LeadBars"));
            AreEqual("0", parameters.get<string>("NumDevsDn"));
            IsTrue(gui.runNumberEnabled());
            IsTrue(gui.runInNativeCurrency());
            gui.setRunNumber("4");
            gui.loadSystem();
            gui.doAllWork();
            parameters = gui.parameters();
            AreEqual(178114, parameters.get<int>("systemId"));
            AreEqual("0", parameters.get<string>("LeadBars"));
            AreEqual("4", parameters.get<string>("LengthDn"));
        }

        [Test]
        public void testLoadSystemFailsRight() {
            var gui = new FakeResearchGUI();
            gui.setSystemId("asdf");
            gui.loadSystem();
            gui.doAllWork();
            gui.hasMessage("failed");
        }
    }
}
