using System;
using System.Collections.Generic;
using Q.Trading;
using Q.Util;

namespace Q.Systems.MultiPairSystems {
    public class MaxTradesManager<T, KEY> : Objects where KEY : SystemKey where T : SingleSystem<KEY> {
        readonly IEnumerable<T> systems;
        readonly int maxTrades;
        readonly Comparison<T> compareSystems;
        readonly Action<T> placeExits;

        public MaxTradesManager(IEnumerable<T> systems, int maxTrades, Comparison<T> compareSystems, Action<T> placeExits) {
            this.systems = systems;
            this.maxTrades = maxTrades;
            this.compareSystems = compareSystems;
            this.placeExits = placeExits;
        }

        // assumes no pyramiding or partial exits
        public void onClose(Dictionary<Symbol, Bar> current) {
            var systemsWithPositions = accept(systems, system => system.hasPosition());
            var systemsWithEntries = list(reverse(sort(accept(systems, system => hasContent(reject(system.allOrders(), order => order.hasPosition()))), compareSystems)));  // best to worst
            var systemsWithExits = accept(systems, system => hasContent(accept(system.allOrders(), order => order.hasPosition())));
            // assume all exits get filled
            each(systemsWithExits, toRemove => systemsWithPositions.Remove(toRemove));
            var systemsWithPositionsAndData = sort(accept(systemsWithPositions, system => system.key().coveredBy(current)), compareSystems);  // worst to best
            
            // "fill" up capacity with the best orders available
            systemsWithEntries.RemoveRange(0, Math.Min(maxTrades - systemsWithPositions.Count, systemsWithEntries.Count));
            
            //  if there are any systems that have better trades than our worst systems have on, flip
            while(hasContent(systemsWithEntries) && hasContent(systemsWithPositionsAndData) && compareSystems(first(systemsWithEntries), first(systemsWithPositionsAndData)) > 0) {
                placeExits(first(systemsWithPositionsAndData));
                systemsWithPositionsAndData.RemoveAt(0);
                systemsWithEntries.RemoveAt(0);
            }
            // any entry trades left are not good enough and need to be cancelled
            each(systemsWithEntries, system => system.cancelOrders(o => !o.hasPosition()));
        }
    }
}