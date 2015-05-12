using System;
using Q.Spuds.Core;
using Q.Util;

namespace Q.Trading {
    public abstract class DynamicExit : Spud<double> {
        protected readonly Position position;
        readonly string name;
        readonly Converter<double, OrderDetails> createDetails;

        protected static readonly Converter<double, OrderDetails> STOP = level => new Stop(level);
        protected static readonly Converter<double, OrderDetails> LIMIT = level => new Limit(level);
        protected static readonly Converter<double, OrderDetails> PROTECTIVE_STOP = level => new ProtectiveStop(level, level);

        protected DynamicExit(Position position, string name, Converter<double, OrderDetails> createDetails, SpudManager manager) : base(manager) {
            this.position = position;
            Bomb.when(position.isClosed(), () => "cannot put exit on a closed position");
            this.name = name;
            this.createDetails = createDetails;
        }

        public virtual void placeOrder(System system) {
            var level = exitLevel();
            system.placeOrder(position.exit(name, createDetails(level), OneBar.ONE));
        }

        protected abstract double exitLevel();

        protected override double calculate() {
            return exitLevel();
        }

        public bool positionClosed() {
            return position.isClosed();
        }

        public void remove() {
            cleanup();
            manager.remove(this);
        }

        protected override void allowsChildren() {
            throw Bomb.toss("DynamicExit spud does not allow children!");
        }

        protected abstract void cleanup();

        public void onPositionClosed(Action doOnPositionClosed) {
            position.onPositionClosed += doOnPositionClosed;
        }
    }
}