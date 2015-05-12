using System;
using NUnit.Framework;
using Q.Util;

namespace Q.Spuds.Core {
    [TestFixture]
    public class TestSpudDependencyTree : QAsserts {

        class DependentSpud : Spud<double> {
            readonly Spud<double> parent;
            bool recalculated;

            public DependentSpud(Spud<double> parent) : base(parent.manager) {
                this.parent = dependsOn(parent);
            }

            protected override double calculate() {
                recalculated = true;
                return parent + 1.0;
            }

            public bool checkAndReset() {
                var result = recalculated;
                recalculated = false;
                return result;
            }
        }

        class DependsOnDependent : Spud<double> {
            readonly Spud<double> parent;
            bool recalculated;

            public DependsOnDependent(Spud<double> root) : base(root.manager) {
                parent = dependsOn(new DependentSpud(root));
            }

            protected override double calculate() {
                recalculated = true;
                return parent + 1.0;
            }

            public bool checkAndReset() {
                var result = recalculated;
                recalculated = false;
                return result;
            }
        }

        class MakesLoop : Spud<double> {
            public MakesLoop(SpudManager manager, int size) : base(manager) {
                Spud<double> last = this;
                zeroTo(size, i => {
                    Spud<double> next = new RootSpud<double>(manager);
                    next.dependsOn(last);
                    last = next;
                });
                dependsOn(last);
            }

            protected override double calculate() {
                throw new NotImplementedException();
            }
        }        
        class DoesNotDependsOnProperly : Spud<double> {
            public DoesNotDependsOnProperly(SpudBase spud) : base(spud.manager) {}
            protected override double calculate() { return Double.NaN; }
        }

        [Test]
        public void testDependencies() {
            var manager = new SpudManager();
            var root = new RootSpud<double>(manager);
            var otherRoot = new RootSpud<double>(manager);
            var plusOne = new DependentSpud(root);
            var plusTwo = new DependsOnDependent(root);
            var otherPlusOne = new DependentSpud(otherRoot);
            root.set(7);
            AreEqual(8, plusOne[0]);
            AreEqual(9, plusTwo[0]);
            IsTrue(plusOne.checkAndReset());
            IsTrue(plusTwo.checkAndReset());
            otherRoot.set(15);
            IsFalse(plusOne.checkAndReset());
            IsFalse(plusTwo.checkAndReset());
            IsFalse(otherPlusOne.checkAndReset());
            AreEqual(16.0, otherPlusOne[0]);
            IsTrue(otherPlusOne.checkAndReset());
            AreEqual(8, plusOne[0]);
            AreEqual(9, plusTwo[0]);

            root.set(11);
            manager.newBar();

            AreEqual(12, plusOne[1]);
            AreEqual(13, plusTwo[1]);
            IsTrue(plusOne.checkAndReset());
            IsTrue(plusTwo.checkAndReset());
            
            IsFalse(plusOne.checkAndReset());
            IsFalse(plusTwo.checkAndReset());
            root.set(30);
            AreEqual(31, plusOne[0]);
            AreEqual(32, plusTwo[0]);
            IsTrue(plusOne.checkAndReset());
            IsTrue(plusTwo.checkAndReset());
            IsFalse(otherPlusOne.checkAndReset());
        }

        [Test]
        public void testLoopsBork() {
            var manager = new SpudManager();
            Bombs(() => new MakesLoop(manager, 10), "loop");
        }

        [Test]
        public void testPushDownOnNonRetrievedSpud() {
            var manager = new SpudManager();
            var a = new RootSpud<double>(manager);
            var child = new DependentSpud(a);
            a.set(1.0);
            manager.newBar();
            AreEqual(2.0, child[1]);
        }
               
        [Test]
        public void testInvalidSpudBlowsUp() {
            var manager = new SpudManager();
            Spud<double> v = new RootSpud<double>(manager);
            new DoesNotDependsOnProperly(v);
            Bombs(manager.newBar, "failed@1", "dependsOn");
        }

    }
}