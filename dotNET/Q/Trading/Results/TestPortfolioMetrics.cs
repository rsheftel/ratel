using System;
using System.Collections.Generic;
using file;
using NUnit.Framework;
using Q.Systems;
using systemdb.metadata;
using O=Q.Util.Objects;

namespace Q.Trading.Results {
    [TestFixture]
    public class TestPortfolioMetrics : OneSystemTest<IndependentSymbolSystems<BuySellAndHold>> {
        static readonly Symbol A = new Symbol("TEST1");
        static readonly Symbol B = new Symbol("TEST2");
        static readonly Symbol C = new Symbol("TEST3");
        static readonly Symbol D = new Symbol("TEST4");

        static readonly Portfolio P1 = new Portfolio("P1", new Siv("s", "i", "v")) {{A, 1.0}, {B, 2.0}};
        static readonly Portfolio P2 = new Portfolio("P2", new Siv("s", "i", "v")) {{C, 3.0}, {B, 0.5}};
        DateTime current = O.now();

        public override void tearDown() {
            new QDirectory(@".\NA").destroyIfExists();
            base.tearDown();
        }


        [Test]
        public void testMetrics() {
            bar(1,2,10,4);
            fill(A, 0, 2);
            fill(B, 0, 2);
            fill(C, 0, 2);
            fill(D, 0, 2);
            bar(2,4,6,8);
            bar(0,0,0,0);
            bar(3,6,9,2);
            bar(2,3,4,5);
            O.each(O.copy(system().allPositions()), p => system().systems_[p.symbol].placeOrder(p.exit("f-in a", market(), fillOrKill())));
            fill(A, 0, 0);
            fill(B, 0, 0);
            fill(C, 0, 0);
            fill(D, 0, 0);
            bar(5,7,9,11);
            bridge().statistics().writeSTOFiles(true);
            checkMetrics(A.name, "QNetProfit", -2);
            checkMetrics("ALL", "QNetProfit", -8);
            checkMetrics(A.name, "QMaxDrawdown", -3);
            checkMetrics("ALL", "QMaxDrawdown", -20);
            checkMetrics("P1", "QMaxDrawdown", -15);
            checkMetrics("P2", "QMaxDrawdown", -30);

        }

        [Test]
        public void testCollectorsHaveProperDates() {
            bar(1, 1, 1, 1);
            bar(1, 1, 1, 1);
            processBar(O.dictionaryOne(A, new Bar(2, 2, 2, 2, current)));
            current = current.AddDays(1);
            HasCount(3, collector(A).dates());
            HasCount(2, collector(B).dates());
            HasCount(3, bridge().statistics().portfolioCollector(P1).dates());
            HasCount(2, bridge().statistics().portfolioCollector(P2).dates());
            bar(3,3,3,3);
            HasCount(4, collector(A).dates());
            HasCount(3, collector(B).dates());
            HasCount(4, bridge().statistics().portfolioCollector(P1).dates());
            HasCount(3, bridge().statistics().portfolioCollector(P2).dates());
            processBar(O.dictionaryOne(C, new Bar(2, 2, 2, 2, current)));
            current = current.AddDays(1);
            HasCount(4, collector(A).dates());
            HasCount(3, collector(B).dates());
            HasCount(4, bridge().statistics().portfolioCollector(P1).dates());
            HasCount(4, bridge().statistics().portfolioCollector(P2).dates());
        }

        StatisticsCollector collector(Symbol s) {
            return bridge().statistics().collector(system().systems_[s]);
        }

        void checkMetrics(string symbolName, string name, int expected) {
            var metricsTable = sto.MetricResultsTable.METRICS;
            var metric = metricsTable.value(systemId, symbolName, 1, name);
            AreEqual(expected, metric);
        }

        void bar(int a, int b, int c, int d) {
            var closes = new Dictionary<Symbol, int> {{A, a}, {B, b}, {C, c}, {D, d}};
            var bars = O.dictionary(closes.Keys, s => new Bar(closes[s], closes[s], closes[s], closes[s], current));
            current = current.AddDays(1);
            processBar(bars);
        }

        protected override SystemArguments arguments() {
            var parameters = new Parameters {
                { "buyOrSell", 1.0 },
                { "RunMode", (double)RunMode.STO },
                { "RunNumber", 1.0 },
                { "systemId", systemId},
                { "TradeSize", 1.0},
                { "LeadBars", leadBars()}
            };
            parameters.bePreloaded();
            return new SystemArguments(O.list(A, B, C, D), O.list(P1, P2), parameters);
        }

        protected override int leadBars() {
            return 0;
        }
    }
}
