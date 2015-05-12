using System.Collections.Generic;
using Exception=System.Exception;

namespace Q.Util {

    public delegate T Producer<T>();
    public static class Bomb { // inheriting from Objects would bork completion 

        public static void unless(bool condition, Producer<string> producer) {
            if (!condition) toss(producer());
        }

        public static void when(bool condition, Producer<string> producer) {
            unless(!condition, producer);
        }

        public static Exception toss(string message) {
            throw new Exception(message);
        }

        public static Exception toss(string message, Exception e) {
            throw new Exception(message, e);
        }

        public static T ifNull<T>(T t, Producer<string> message) where T : class {
            when(t == null, message);
            return t;
        }

        public static T ifNull<T>(T? maybe, Producer<string> message) where T : struct {
            when(!maybe.HasValue, message);
            if (maybe != null) return maybe.Value;
            throw toss(message());
        }

        public static void unlessNull(object o, Producer<string> producer) {
            unless(o == null, producer);
        }

        public static V missing<K, V>(IDictionary<K, V> d, K k) {
            V value;
            unless(d.TryGetValue(k, out value), 
                () => "no value for " + k + " in " + Objects.toShortString(d));
            return value;
        }

        public static void existing<K, V>(IDictionary<K, V> d, K k) {
            when(d.ContainsKey(k), 
                () => "value exists for " + k + " in " + Objects.toShortString(d));
        }
    }
}