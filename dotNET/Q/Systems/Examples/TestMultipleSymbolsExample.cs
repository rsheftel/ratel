using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Trading;
using O=Q.Util.Objects;

namespace Q.Systems.Examples {
    [TestFixture]
    public class TestMultipleSymbolsExample : OneSystemTest< MultipleSymbolsExample> {
        List<Symbol> symbols;
        DateTime time = O.now();

        protected override void initializeSymbols() {
            base.initializeSymbols();
            var names = new List<string>();
            O.zeroTo(50, i => names.Add("sym" + (i + 1)));
            symbols = O.list(O.convert(names, name => new Symbol(name)));
            O.each(symbols, symbol => insertMarket(symbol.name, 0.0));
            O.each(symbols, symbol => insertSymbol("RC." + symbol.name));
        }

        protected override SystemArguments arguments() {
            return new SystemArguments(symbols, parameters());
        }

        protected override Parameters parameters() {
            var result = base.parameters();
            result.overwrite("NumPairs", "3");
            result.overwrite("MinimumHoldingPeriod", "2");
            return result;
        }

        protected override int leadBars() {
            return 0;
        }

        [Test]
        public void testSystem() {
            var rc = 0.0;
            O.each(symbols, symbol => system().richCheap[symbol].enterTestMode());
            O.each(symbols, symbol => system().richCheap[symbol].add(time, rc++));
            bridge().processBar(bars(time));
            hasOrders(symbols[0], symbols[0].buy("enter long", market(), 1, oneBar()));
            hasOrders(symbols[1], symbols[1].buy("enter long", market(), 1, oneBar()));
            hasOrders(symbols[2], symbols[2].buy("enter long", market(), 1, oneBar()));
            hasOrders(symbols[49], symbols[49].sell("enter short", market(), 1, oneBar()));
            hasOrders(symbols[48], symbols[48].sell("enter short", market(), 1, oneBar()));
            hasOrders(symbols[47], symbols[47].sell("enter short", market(), 1, oneBar()));
            var i = 1;
            system().richCheap[O.first(symbols)].add(time.AddDays(i++), 100);
            processBar(bars(time.AddDays(1)));
            hasOrders(symbols[3], symbols[3].buy("enter long", market(), 1, oneBar()));
            hasOrders(symbols[1], symbols[1].buy("enter long", market(), 1, oneBar()));
            hasOrders(symbols[2], symbols[2].buy("enter long", market(), 1, oneBar()));
            hasOrders(symbols[49], symbols[49].sell("enter short", market(), 1, oneBar()));
            hasOrders(symbols[48], symbols[48].sell("enter short", market(), 1, oneBar()));
            hasOrders(symbols[0], symbols[0].sell("enter short", market(), 1, oneBar()));
            O.each(orders(), order => fill(order, 0));
            processBar(bars(time.AddDays(i++)));
            noOrders();
            processBar(bars(time.AddDays(i++)));
            noOrders();
            processBar(bars(time.AddDays(i++)));
            noOrders();
            system().richCheap[O.first(symbols)].add(time.AddDays(i), 0);
            processBar(bars(time.AddDays(i++)));
            hasOrders(symbols[3], symbols[3].sell("exit long", market(), 1, oneBar()));
            hasOrders(symbols[47], symbols[47].sell("enter short", market(), 1, oneBar()));
            hasOrders(symbols[0], symbols[0].buy("enter long", market(), 1, oneBar()), symbols[0].buy("exit short", market(), 1, oneBar()));
            O.each(O.copy(orders()), order => fill(order, 0));
            system().richCheap[O.first(symbols)].add(time.AddDays(i), 100);
            processBar(bars(time.AddDays(i)));
            noOrders(); // holding period
        }

        List<Order> orders() {
            return O.collect(symbols, s => orders(s));
        }

        Dictionary<Symbol, Bar> bars(DateTime current) {
            return O.dictionary(symbols, symbol => new Bar(1, 1, 1, 1, current));
        }
    }
}
