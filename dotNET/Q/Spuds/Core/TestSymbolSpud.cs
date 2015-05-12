using NUnit.Framework;
using Q.Trading;
using Q.Util;
using systemdb.data;
using systemdb.metadata;
using util;
using Bar=Q.Trading.Bar;
using O = Q.Util.Objects;
using Symbol=Q.Trading.Symbol;
using JTick=systemdb.data.Tick;

namespace Q.Spuds.Core {
    [TestFixture]
    public class TestSymbolSpud : DbTestCase {
        [Test]
        public void testSymbolBarSpud() {
            var manager = new SpudManager();
            var barSpud = new BarSpud(manager);
            var symbol = new Symbol("RE.TEST.TY.1C");
            var spud = symbol.bars(barSpud);
            barSpud.set(new Bar(1, 3, 1, 2, O.date("2007/01/02")));
            AreEqual(105.04687500, spud[0].close);
            barSpud.lastTickedAt(O.date("2007/01/02 12:34:55"));
            symbol.javaSymbol().jmsLive().publish(new JTick(98, 100, 96, 99, 93, O.jDate("2007/01/02 12:34:56")));
            O.sleep(250);
            AreEqual(105.04687500, spud[0].close);

            manager.goLive();
            symbol.javaSymbol().jmsLive().publish(new JTick(98, 100, 96, 99, 93, O.jDate("2007/01/02 12:34:57")));
            O.wait(() => spud[0].time.Equals(date("2007/01/02 12:34:57")));

            AreEqual(98.0, spud[0].close);
            AreEqual(93.0, spud[0].low);
        }


        [Test]
        public void testSymbolValueSpud() {
            var manager = new SpudManager();
            var barSpud = new BarSpud(manager);
            var spud = new Symbol("RE.TEST.TY.1C").doubles(barSpud);
            barSpud.set(new Bar(1, 3, 1, 2, O.date("2007/01/02")));
            AreEqual(105.04687500, spud[0]);
            
            // test barSPud has date, but spud does not
            manager.newBar();
            barSpud.set(new Bar(1, 3, 1, 2, O.date("2007/01/06")));
            AreEqual(105.32812500, spud[0]);
        }

        [Test]
        public void testSymbolValueLive() {
            Log.setFile(@"C:\logs\jefftest");

            SystemTimeSeriesTable.SYSTEM_TS.insert("testone", "ASCII", "ActiveMQ", "somedangtopic");
            AsciiTable.SYSTEM_ASCII.insert("testone", @"\\nysrv37\share\Tools\RightEdge\TransitionTest\TY1C.full.csv", true, 1);
            var manager = new SpudManager();
            var barSpud = new BarSpud(manager);
            var barSpud2 = new BarSpud(manager);
            var symbol = new Symbol("testone");
            var spud = symbol.doubles(barSpud);
            var spud2 = new Symbol("RE.TEST.TY.1C").doubles(barSpud2);
            manager.newBar();
            barSpud.set(new Bar(1, 3, 1, 2, O.date("2008/08/06")));
            barSpud2.set(new Bar(1, 3, 1, 2, O.date("2008/08/06")));
            
            manager.newTick();
            spud.doSubscribe();
            
            barSpud.lastTickedAt(O.date("2008/08/06 11:22:34"));
            spud2.doSubscribe();
            IsTrue(spud2.isDirty());
            symbol.javaSymbol().jmsLive().topic().send("value=97.1|timestamp=2008/08/06 11:22:33|MSTimestamp=2008/06/05 13:10:08");
            
            Bombs(() => LogC.info("" + spud[0]), "stale");
            symbol.javaSymbol().jmsLive().topic().send("value=97.2|timestamp=2008/08/06 11:22:34|MSTimestamp=2008/06/05 13:10:08");
            O.wait(() => spud[0] == 97.2);
            barSpud.lastTickedAt(O.date("2008/08/06 11:22:35"));
            spud.allowStaleTicks();
            AreEqual(97.2, spud[0]);
            AreEqual(116.140625, spud2[0]);
        }

        [Test]
        public void testThisWorksWithNoOpenInterest() {
            new Symbol("AAPL").bars(new BarSpud(new SpudManager()));
        }
    }
}
