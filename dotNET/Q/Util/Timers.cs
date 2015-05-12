
using System;
using System.Collections.Generic;
using System.Threading;

namespace Q.Util {
    public class Timers<T> : Objects {
        readonly Dictionary<T, Timer> timers = new Dictionary<T, Timer>();

        public void add(T key, DateTime time, Action action) {
            
            lock(timers) {
                LogC.verbose(() => "T+ (" + identity() + "): " + key + " for:" + ymdHuman(time));
                Bomb.existing(timers, key);
                Timer timer;
                timerManager().atTime(time, () => { lock(timers) timers.Remove(key); action(); }, out timer);
                timers[key] = timer;
            }
        }

        public void add(T key, DateTime time, Action<T> action) {
            add(key, time, () => action(key));
        }

        internal bool has(T key) {
            lock(timers) {
                return timers.ContainsKey(key);
            }
        }

        public void replace(T key, DateTime time, Action action) {
            lock(timers) {
                LogC.verbose(() => "TR (" + identity() + "): " + key + " for:" + ymdHuman(time));
                remove(key);
                add(key, time, action);
            }
        }

        public void replace(T key, DateTime time, Action<T> action) {
            replace(key, time, () => action(key));
        }

        public bool remove(T key) {
            lock(timers) {
                Timer existing;
                if (timers.TryGetValue(key, out existing)) {
                    LogC.verbose(() => "T- (" + identity() + "): " + key);
                    existing.Dispose();
                    timers.Remove(key);
                    return true;
                }
                LogC.verbose(() => "T* (" + identity() + "): " + key);
                return false;
            }
        }

        public bool clear() {
            var removedOne = false;
            lock(timers) {
                LogC.verbose(() => "T! (" + identity() + ")");
                each(item => removedOne = removedOne || remove(item));
            }
            return removedOne;
        }

        public void each(Action<T> onItem) {
            lock(timers) {
                eachCopiedKey(timers, onItem);
            }
        }
    }
}
