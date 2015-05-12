namespace Q.Trading {
    public class MultiPairSystem<T, PG> : MultiSystem<Pair, T> where T : PairSystem where PG : PairGenerator {

        public MultiPairSystem(QREBridgeBase bridge) : base(bridge) {
            var generator = PairGenerator.create<PG>(typeof (PG), arguments());
            generator.eachPair(pair => addSystem(pair, PairSystem.create<T>(typeof (T).FullName, bridge, pair)));
        }
    }
}