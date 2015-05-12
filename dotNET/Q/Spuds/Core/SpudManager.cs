using System;
using System.Collections.Generic;
using Q.Util;

namespace Q.Spuds.Core {
    public class SpudManager : Objects {
        
        readonly List<SpudBase> rootSpuds = new List<SpudBase>();
        readonly List<SpudBase> allSpuds = new List<SpudBase>();
        readonly Dictionary<SpudBase, bool> dirtySpuds = new Dictionary<SpudBase, bool>();
        readonly Dictionary<SpudBase, SpudBase> havePushedDown = new Dictionary<SpudBase, SpudBase>();
        int barCount_;
        bool alreadyPushed;
        bool inPushDown;
        public event Action onLive;
        public event Action onRecalculate;

        public SpudManager() {
            onLive += doNothing;
            onRecalculate += doNothing;
        }

        public void register(SpudBase s) {
            rootSpuds.Add(s);
            allSpuds.Add(s);
            lock(dirtySpuds)
                dirtySpuds[s] = true;
        }

        public void newBar() {
            pushDownIfNeeded();
            alreadyPushed = false;
        }

        void pushDownIfNeeded() {
            if(!alreadyPushed)
                pushDown();
        }

        void pushDown() {
            havePushedDown.Clear();
            inPushDown = true;
            each(rootSpuds, delegate(SpudBase s) { 
                Bomb.unless(typeof(IRootSpud).IsAssignableFrom(s.GetType()), 
                    () => "a non-RootSpud has no dependencies!  Did you forget dependsOn()?  Type:" + s.GetType().FullName);
                s.pushDown();
            });
            inPushDown = false;
            Bomb.unless(havePushedDown.Count == allSpuds.Count,
                () => "not all spuds pushed down. All:\n" + toShortString(allSpuds) + "\nPushed Down:\n" + toShortString(havePushedDown));
            barCount_++;
            alreadyPushed = true;
        }

        public void pushedDown(SpudBase spud) {
            requireInPushDown();
            havePushedDown[spud] = spud;
        }

        void requireInPushDown() {
            Bomb.unless(inPushDown, () => "pushedDown() or hasPushedDown() called when not in SpudManager.pushDown().  Did you pushdown outside of manager?");
        }

        public void recalculate() {
            lock(dirtySpuds) {
                each(copy(dirtySpuds.Keys), s => { if (s.canUpdate()) s.updateThyself(); });
                dirtySpuds.Clear();
            }
            onRecalculate();
        }

        public int barCount() {
            return barCount_;
        }

        public void newTick() {
            pushDownIfNeeded();
        }

        public void hasParent(SpudBase child) {
            rootSpuds.Remove(child);
        }

        public bool isEmpty() {
            return isEmpty(rootSpuds);
        }

        public Spud<T> constant<T>(T value) {
            return new RootSpud<T>(this, value);
        }

        public void goLive() {
            onLive();
        }

        public bool hasPushedDown(SpudBase spud) {
            requireInPushDown();
            return havePushedDown.ContainsKey(spud);
        }

        public void makeDirty(SpudBase spud) {
            lock(dirtySpuds)
                dirtySpuds[spud] = true;
        }

        
        public void makeClean(SpudBase spud) {
            lock(dirtySpuds)
                dirtySpuds.Remove(spud);
        }

        public void remove(SpudBase spud) {
            spud.beDead();
            allSpuds.Remove(spud);
            lock(dirtySpuds)
                if(dirtySpuds.ContainsKey(spud)) dirtySpuds.Remove(spud);
            if(rootSpuds.Contains(spud)) rootSpuds.Remove(spud);
            if(havePushedDown.ContainsKey(spud)) havePushedDown.Remove(spud);
        }
    }
}