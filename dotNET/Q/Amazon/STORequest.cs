using System;
using System.Collections.Generic;
using amazon;
using java.util;
using Q.Trading;
using Q.Trading.Results;
using Q.Util;

namespace Q.Amazon {
    public class STORequest : Objects {
        public readonly List<Symbol> symbols;
        public readonly List<Portfolio> portfolios;
        public readonly Parameters parameters;

        public STORequest(List<Symbol> symbols, List<Portfolio> portfolios, Parameters parameters) {
            this.symbols = symbols;
            this.portfolios = portfolios;
            this.parameters = parameters;
        }

        public STORequest(object java) {
            var javaMap = (Map) java;
            symbols = list<systemdb.data.Symbol, Symbol>(javaMap.get("symbols"), j => new Symbol((systemdb.data.Symbol) j));
            var jParameters = (Map) javaMap.get("parameters");
            var keys = list<String>(jParameters.keySet());
            var parameterData = new Dictionary<string, double>();
            each(keys, key => parameterData[key] = ((java.lang.Double) jParameters.get(key)).doubleValue() );
            parameters = new Parameters(parameterData);
            portfolios = list<sto.Portfolio, Portfolio>(javaMap.get("portfolios"), j => new Portfolio(j));
        }

        public java.lang.Object java() {
            var result = new HashMap();
            result.put("symbols", jList(symbols, symbol => symbol.javaSymbol()));
            var parameterMap = new HashMap();
            each(parameters, name => parameterMap.put(name, new java.lang.Double(parameters.get<double>(name))));
            result.put("parameters", parameterMap);
            result.put("portfolios", jList(portfolios, port => port.java()));
            return result;
        }

        public bool Equals(STORequest obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return listEquals(obj.symbols, symbols) && Equals(obj.parameters, parameters);
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.GetType() == typeof (STORequest) && Equals((STORequest) obj);
        }

        public override int GetHashCode() {
            unchecked {
                return (symbols.GetHashCode() * 397)^parameters.GetHashCode();
            }
        }

        public override string ToString() {
            return "symbols: " + toShortString(symbols) + "\nparameters:\n" + parameters;
        }

        public void requireMatch(IEnumerable<Symbol> received) {
            Bomb.unless(listEquals(symbols, received), 
                () => "must have same symbol list on every request.  Expecting: " + toShortString(symbols) + "\nActual: " + toShortString(received));
        }

        public STOResponse response(string instanceId, DateTime start, DateTime end, int processId) {
            return new STOResponse(parameters.get<int>("RunNumber"), instanceId, jDate(start), jDate(end), processId);
            ;
        }
    }
}