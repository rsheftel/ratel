using System;
using System.Collections.Generic;
using System.Data;
using System.Runtime.Serialization;
using java.util;
using Q.Research;
using Q.Util;
using Double=java.lang.Double;
using O=Q.Util.Objects;

namespace Q.Trading.Results {
    [Serializable]
    public class MetricResults : Dictionary<string, Dictionary<string, double>> {
        public MetricResults() {}
        public MetricResults(SerializationInfo info, StreamingContext context) : base(info, context) {}

        public MetricResults(Map map) {
            foreach(var symbol in O.list<string>(map.keySet())) {
                var result = new Dictionary<string, double>();
                var symbolMetrics = ((Map) map.get(symbol));
                foreach (var metric in O.list<string>(symbolMetrics.keySet())) 
                    result.Add(metric, ((Double) symbolMetrics.get(metric)).doubleValue());
                Add(symbol, result);
            }
        }

        public object java() {
            var result = new HashMap();
            Objects.each(this, (symbol, metrics) => { 
                                   var jMetrics = new HashMap();
                                   Objects.each(metrics, (name, value) => jMetrics.put(name, new Double(value)));
                                   result.put(symbol, jMetrics);
                               });
            return result;
        }

        public DataTable table() {
            var result = new QDataTable();
            result.addTypedColumn("Metric", typeof (string));
            O.each(O.sort(Keys, (a, b) => {
                if(a.Equals(b)) return 0; 
                if(a.Equals("ALL")) return -1; 
                return b.Equals("ALL") ? 1 : a.CompareTo(b);
            }), symbol => result.addTypedColumn(symbol.Replace(".", "_"), typeof (double)));
            var example = O.first(Values);
            O.each(O.sort(example.Keys), metric => addRow(metric, result));
            return result;
        }

        void addRow(string metric, QDataTable result) {
            var row = result.NewRow();
            row["Metric"] = metric;
            foreach (var column in result.columns()) {
                var columnName = column.ColumnName;
                if(columnName.Equals("Metric")) continue;
                row[column] = this[columnName.Replace("_", ".")][metric];
            }
            result.add(row);
        }

    }
}