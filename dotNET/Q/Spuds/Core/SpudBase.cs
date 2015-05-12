using System;
using System.Collections.Generic;
using Q.Util;

namespace Q.Spuds.Core {
    public abstract class SpudBase : Objects {
        public readonly SpudManager manager;
        public abstract void pushDown();
        public abstract int count();
        public abstract void updateThyself();
        protected readonly List<SpudBase> children = new List<SpudBase>();
        protected readonly List<SpudBase> parents = new List<SpudBase>();
        bool isDead;
        bool isDirty_ = true;

        protected SpudBase(SpudManager manager) {
            this.manager = manager;
            manager.register(this);
        }

        public void eachChild(Action<SpudBase> run) {
            each(children, run);
        }

        public static ComparableSpud<T> comparable<T>(Spud<T> spud) where T : IComparable<T> {
            return new ComparableSpudWrapper<T>(spud);
        }

        public bool hasContent() {
            return count() > 0;
        }

        public void beDirty() {
            thyselfBeDirty();
            each(children, child => child.beDirty());
        }

        protected void requireAlive() {
            if(isDead) Bomb.toss("spud is dead " + this);
        }

        protected void thyselfBeDirty() {
            manager.makeDirty(this);
            isDirty_ = true;
        }

        public void beClean() {
            manager.makeClean(this);
            isDirty_ = false;
        }

        public bool isDirty() {
            return isDirty_; 
        }

        public T dependsOn<T>(T parent) where T : SpudBase {
            requireAlive();
            parent.allowsChildren();
            manager.hasParent(this);
            Bomb.when(hasDescendent(parent), () => "cannot have a dependency loop");
            parent.addChild(this);
            parents.Add(parent);
            return parent;
        }

        protected virtual void allowsChildren() {}

        public void removeChild(SpudBase child) {
            Bomb.unless(children.Remove(child), () => "can't remove nonexistant child!");
        }

        bool hasDescendent(SpudBase spud) {
            return children.Contains(spud) || hasContent(accept(children, child => child.hasDescendent(spud)));
        }

        void addChild(SpudBase child) {
            requireAlive();
            children.Add(child);
        }

        protected void updateAndClean() {
            if(!isDirty()) return;
            updateThyself();
            beClean();
        }

        public abstract void recalculate();
        public abstract bool canUpdate();

        public void beDead() {
            isDead = true;
        }

        public void prepare() {}
    }

    class ComparableSpudWrapper<T> : ComparableSpud<T> where T : IComparable<T> {
        readonly Spud<T> spud;

        public ComparableSpudWrapper(Spud<T> spud) : base(spud.manager) {
            this.spud = dependsOn(spud);
        }

        protected override T calculate() {
            return spud[0];
        }
    }
}


