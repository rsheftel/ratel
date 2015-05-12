using System;
using System.Collections.Generic;
using NUnit.Framework;
using O=Q.Util.Objects;

namespace Q.Util {
    [TestFixture]
    public class TestTimers : DbTestCase {
        readonly List<bool> called = new List<bool> { false, false };

        public override void setUp() {
            base.setUp();
            called[0] = false;
            called[1] = false;
        }

        [Test]
        public void testNestedTimers() {
            var timers = new Timers<string>();
            O.freezeNow("2008/11/11 11:11:11");
            timers.add("first", millisAhead(100), name => {
                call(1);
                timers.replace("first", millisAhead(100), nameLater => {
                    didCall(1);
                    call(1);
                    timers.replace("first", millisAhead(100), s => call(0));
                });
            });
            Objects.wait(() => didCall(0));
            Objects.wait(() => didCall(1));
        }

        [Test]
        public void testTimerSequence() {
            Objects.freezeNow("2008/11/11 11:11:11");
            double currentState = -1;
            var sequence = new TimerSequence<double>(d => { currentState = d; });
            sequence.add(1.1, 500);
            sequence.add(2.2, 500);
            sequence.add(3.3);
            IsFalse(sequence.running());
            // starts in 1.1
            sequence.startAsOf(O.now());
            O.wait(() => currentState == 1.1);
            IsTrue(sequence.running());
            Objects.advanceNow(500);
            O.wait(() => currentState == 2.2);
            Objects.advanceNow(500);
            O.wait(() => currentState == 3.3);
            IsFalse(sequence.running());
            
            sequence.startAsOf(O.now());
            O.wait(() => currentState == 1.1);
            Objects.advanceNow(500);
            O.wait(() => currentState == 2.2);
            sequence.startAsOf(O.now());
            O.wait(() => currentState == 1.1);
            Objects.advanceNow(500);
            O.wait(() => currentState == 2.2);
            Objects.advanceNow(500);
            O.wait(() => currentState == 3.3);

            // starts in 2.2
            var asOf = O.now();
            Objects.advanceNow(500);
            sequence.startAsOf(asOf);
            O.wait(() => currentState == 2.2);
            Objects.advanceNow(500);
            O.wait(() => currentState == 3.3);

            // starts in 3.3
            asOf = O.now();
            currentState = -1;
            Objects.advanceNow(1000);
            sequence.startAsOf(asOf);
            O.wait(() => currentState == 3.3);
        }

        [Test]
        public void testTimers() {
            var timers = new Timers<double>();
            O.freezeNow("2008/11/11 11:11:11");
            timers.add(0.0, millisAhead(100), () => call(0));
            O.wait(() => didCall(0));
            IsFalse(timers.has(0.0));
            timers.add(0.0, millisAhead(100), () => call(0));
            timers.add(1.0, millisAhead(100), () => call(1));
            O.wait(() => didCall(0));
            O.wait(() => didCall(1));
            timers.add(0.0, millisAhead(1000), () => call(0));
            Bombs(() => timers.add(0.0, millisAhead(100), () => call(1)), "value exists");
            timers.replace(0.0, millisAhead(100), () => call(1));
            O.wait(() => didCall(1));
            IsFalse(timers.remove(0.0));
            timers.replace(0.0, millisAhead(200), () => call(1));
            IsTrue(timers.remove(0.0));
            O.sleep(300);
            IsFalse(didCall(1));
            timers.add(0.0, millisAhead(800), () => call(0)); // adding removeOne tracking to Timers::clear method added 
            timers.add(1.0, millisAhead(800), () => call(1)); // enough overhead to require loosening this deadline by 400 millis.
            timers.clear();
            O.sleep(300);
            IsFalse(didCall(0));
            IsFalse(didCall(1));
            timers.add(1.0, Objects.now().AddMilliseconds(100), d => call( (int) d));
            O.wait(() => didCall(1));
            IsFalse(didCall(0));
        }

        static DateTime millisAhead(int millis) {
            return O.now().AddMilliseconds(millis);
        }

        bool didCall(int index) {
            if(called[index]) {
                called[index] = false;
                return true;
            }
            return false;
        }

        void call(int index) {
            called[index] = true;
        }
    }
}
