using System;
using System.Collections.Generic;
using Q.Spuds.Core;
using Q.Util;

namespace Q.Spuds.Indicators {
    public class AggregatorSpud<T> : Spud<T> {
        readonly Spud<T> values;
        readonly Converter<IEnumerable<T>, T> aggregate;
        readonly int windowSize;
        
        public AggregatorSpud(Spud<T> values, Converter<IEnumerable<T>, T> aggregate, int windowSize) : base(values.manager) {
            this.values = dependsOn(values);
            this.aggregate = aggregate;
            this.windowSize = windowSize;
        }


        protected override T calculate() {
            Bomb.unless(values.hasContent(), () => "use of uninitialized spud in AggregatorSpud");
            var array = windowSize == Window.INFINITE ? values.toArray() : values.toArray(windowSize);
            return aggregate(array);
        }
    }
}