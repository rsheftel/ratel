using systemdb.data;
using systemdb.data.bars;
using util;
using Bar=Q.Trading.Bar;

namespace Q.Spuds.Core {
    public class IntervalSpud : RootSpud<Bar> {
        ProtoBar current;
        Range currentRange;

        // USE WITH CARE!  This tremendously useful spud has one drawback.  An IntervalSpud creates a new "time" definition, based on the BarSpud it is wrapped around.
        // So, 2 IntervalSpuds wrapped around different BarSpuds are not necessarily time-synchronized 
        // (if one of the spuds has gotten a data point in the new interval, and the other hasn't, spud[0] will be different for each of them).
        // If you don't understand, go through your use case with Jeric and make sure it works. or pray. or sacrifice a chicken. 
        public IntervalSpud(Spud<Bar> values, Interval interval) : base(new SpudManager()) {
            values.valueSet += newBar => {
                var newRange = interval.range(jDate(newBar.time));
                if(currentRange == null || !currentRange.equals(newRange)) {
                    manager.newBar();
                    currentRange = newRange;
                    current = new ProtoBar(newBar.java());
                } else {
                    current.update(newBar.java());
                }
                set(new Bar(current.asBar()));
            };
        }

    }
}