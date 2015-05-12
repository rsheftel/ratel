using System;
using System.Collections.Generic;
using Q.Util;

namespace Q.Trading.Results {
    public class TradeTracker : Objects {
        readonly Predicate<WeightedPosition, double> includes;
        readonly List<WeightedPosition> positions = new List<WeightedPosition>();
        double maxPnl_;
        double minPnl_;
        double sumPnl_;
        int consecutive;
        int maxConsecutive_;
        int totalBarsHeld_;

        public TradeTracker(Predicate<WeightedPosition, double> includes) {
            this.includes = includes;
        }

        public double sumPnl() {
            return sumPnl_;
        }

        public double maxPnl() {
            return maxPnl_;
        }

        public double minPnl() {
            return minPnl_;
        }

        public double averagePnl() {
            return safeDivide(0, sumPnl_, count());
        }

        public int count() {
            return positions.Count;
        }

        public int maxConsecutive() {
            return maxConsecutive_;
        }

        public void addMaybe(WeightedPosition position, double pnl) {
            if(!includes(position, pnl)) {
                consecutive = 0;
                return;
            }
            positions.Add(position);
            maxPnl_ = Math.Max(pnl, maxPnl_);
            minPnl_ = Math.Min(pnl, minPnl_);
            sumPnl_ += pnl;
            consecutive++;
            maxConsecutive_ = Math.Max(consecutive, maxConsecutive_);
            totalBarsHeld_ += position.barsHeld();
        }

        public double averageBarsHeld() {
            return safeDivide(0, totalBarsHeld(), count());
        }

        public int totalBarsHeld() {
            return totalBarsHeld_;
        }

        public double totalSlippage() {
            return sum(convert(positions, p => p.slippage()));
        }

        public double averageSlippage() {
            return average(convert(positions, p => p.slippage()));
        }
    }
}