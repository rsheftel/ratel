using System;
using System.Collections.Generic;
using System.Threading;

namespace Q.Util {
    public class TimerSequence<T> : Objects {
        readonly Action<T> onChangeState;
        readonly List<State> states = new List<State>();
        Timer timer;

        public TimerSequence(Action<T> onChangeState) {
            this.onChangeState = onChangeState;
        }

        public void add(T state, int durationMillis) {
            states.Add(new State(state, durationMillis));
        }

        public void add(T state) {
            states.Add(new State(state, null));
        }

        public void startAsOf(DateTime time) {
            stop();
            var elapsedMillis = now().Subtract(time).TotalMilliseconds;
            foreach (var state in states) {
                if (!state.isTerminal() && state.durationMillis <= elapsedMillis) {
                    elapsedMillis -= state.durationMillis.Value;
                    continue;
                }
                onChangeState(state.state);
                if (!state.isTerminal()) 
                    timerManager().atTime(now().AddMilliseconds(state.durationMillis.Value - elapsedMillis), () => startAsOf(time), out timer);
                return;
            }
        }

        public void stop() {
            if (timer == null) return;
            timer.Dispose();
            timer = null;
        }

        class State {
            public readonly T state;
            public readonly int? durationMillis;

            public State(T state, int? durationMillis) {
                this.state = state;
                this.durationMillis = durationMillis;
            }

            public bool isTerminal() {
                return !durationMillis.HasValue;
            }
        }

        public bool running() {
            return timer != null;
        }
    }
}