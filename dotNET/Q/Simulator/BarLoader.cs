using System;
using System.Collections.Generic;
using file;
using systemdb.data;
using util;
using Bar=Q.Trading.Bar;
using Symbol=Q.Trading.Symbol;

namespace Q.Simulator {
    public interface BarLoader {
        DateTime date(int dateIndex);
        Dictionary<Symbol, Bar> currentBars(DateTime date);
        int numDates();
        Dictionary<Symbol, double> currentSlippages(DateTime date);
    }

    public class Slippage : Util.Objects {
        public static void Main(string[] args) {
            var arguments = Arguments.arguments(args, jStrings("markets", "start", "end", "out", "calculator", "interval"));
            var names = split(",", arguments.@string("markets"));
            var symbols = list(STO.symbols(names));
            if(arguments.containsKey("calculator")) {
                var calculator = Type.GetType(arguments.@string("calculator"));
                each(symbols, symbol => symbol.overrideSlippageCalculator(calculator));
            }
            var start = arguments.get("start", "");
            var end = arguments.get("end", "");
            var outFile = new QFile(arguments.@string("out"));
            var range = new Range(isEmpty(start) ? null : Dates.date(start), isEmpty(end) ? null : Dates.date(end));
            var loader = new SystemDbBarLoader(Interval.lookup(arguments.@string("interval")), symbols, dictionary(symbols, s => range));
            var csv = new Csv();
            var columns = list("date");
            columns.AddRange(convert(symbols, s => s.name));
            csv.addHeader(jList(columns));
            for(var i = 0; i < loader.numDates(); i++) {
                var time = loader.date(i);
                var record = list(ymdHuman(time));
                var slippages = loader.currentSlippages(time);
                each(symbols, s => record.Add(slippages.ContainsKey(s) ? slippages[s].ToString("N12") : "NA"));
                csv.add(jList(record));
            }
            csv.overwrite(outFile);
        }

    }
}