using System;
using System.Collections;
using System.Collections.Generic;
using System.Data;
using System.Diagnostics;
using System.IO;
using System.Runtime.CompilerServices;
using System.Runtime.Serialization.Formatters.Binary;
using System.Threading;
using java.util;
using util;
using ArrayList=java.util.ArrayList;
using JList = java.util.List;
using JDate = java.util.Date;

namespace Q.Util {
    [Serializable]
    public class Objects {

        static readonly DateTime EPOCH = new DateTime(1970, 1, 1, 0, 0, 0, 0);
        public static readonly DateTime SQL_MAX_DATE = date("9999/12/31");
        public static readonly DateTime SQL_MIN_DATE = date("1753/01/01");
        static readonly Dictionary<Action, bool> workItems = new Dictionary<Action, bool>();
        static readonly TimerManager timers  = new TimerManager();
        static readonly Dictionary<string, int> envNotified = new Dictionary<string, int>();

        public static KeyValuePair<K, V> tuple<K, V>(K k, V v) { return new KeyValuePair<K, V>(k, v); }

        public static T first<T>(IEnumerable<T> ts) {
            Bomb.ifNull(ts, () => "null ts!");
            var e = ts.GetEnumerator();
            Bomb.unless(e.MoveNext(), () => "can't get first of empty enumerable");
            return e.Current;
        }

        public static T second<T>(IEnumerable<T> ts) {
            return nth(ts, 2);
        }

        public static T nth<T>(IEnumerable<T> ts, int n) {
            Bomb.ifNull(ts, () => "null ts!");
            var e = ts.GetEnumerator();
            zeroTo(n, i => Bomb.unless(e.MoveNext(), () => "can't get " + (i + 1) + "th of empty enumerable"));
            return e.Current;
        }

        public static T last<T>(IList<T> ts) {
            requireNonEmpty(ts);
            return ts[ts.Count - 1];
        }
                
        public static T penultimate<T>(IList<T> ts) {
            Bomb.unless(ts.Count >= 2, () => "no penultimate in " + toShortString(ts));
            return ts[ts.Count - 2];
        }

        public static IEnumerable<T> rest<T>(IEnumerable<T> ts) {
            Bomb.ifNull(ts, () => "null ts!");
            var e = ts.GetEnumerator();
            Bomb.unless(e.MoveNext(), () => "can't get rest of empty enumerable");
            while(e.MoveNext()) yield return e.Current;
        }

        // these methods are non-static, so that delegate comparisons to them work.
        protected void doNothing() {
            if (alwaysFalse()) LogC.info("I'm doing nothing with " + this); // hide from static method suggestion
        }
        protected void doNothing<T>(T t) { if (alwaysFalse()) doNothing(); }
        protected void doNothing<S, T>(S t1, T t2) { if (alwaysFalse()) doNothing(); }
        protected static bool alwaysFalse() { return false; }

        static void requireNonEmpty<T>(IList<T> ts) {
            Bomb.when(Bomb.ifNull(ts, () => "null ts!").Count < 1, () => "empty list passed to requireNonEmpty");
        }

        public static bool isEmpty(IEnumerable e) {
            return !hasContent(e);
        }

        public static bool isEmpty(List list) {
            return !hasContent(list);
        }

        public static bool isEmpty(String s) {
            return !hasContent(s);
        }

        public static bool hasContent(IEnumerable e) {
            return e.GetEnumerator().MoveNext();
        }

        public static bool hasContent(List e) {
            return e.iterator().hasNext();
        }

        public static bool hasContent(string s) {
            return s != null && s.Trim().Length != 0;
        }

        public static bool hasContent(DataRow row, string key) {
            return row.IsNull(key) ? false : hasContent((string) row[key]);
        }

        public static T the<T>(IList<T> ts) {
            Bomb.unless(ts.Count == 1, ()=> "ts does not have exactly 1 element.  has: " + ts.Count + " elements.\n" + toShortString(ts));
            return ts[0];
        }

        public static T the<T>(IEnumerable<T> ts) {
            var e = ts.GetEnumerator();
            Bomb.unless(e.MoveNext(), () => "ts is empty");
            var result = e.Current;
            Bomb.when(e.MoveNext(), () => "ts has more than one element: " + toShortString(ts));
            return result;
        }

        public static T the<T>(List list) {
            return the(list<T>(list));
        }

        public static IEnumerable<T> unique<T>(IEnumerable<T> ts) {
            var d = new Dictionary<T, bool>();
            each(ts, delegate(T t) { d[t] = true; });
            return d.Keys;
        }

        public static IEnumerable<T> nCopies<T>(int n, T copy) {
            for(var i = 0; i < n; i++) yield return copy;
        }

        public static List<T> list<T>(Collection ts) {
            var result = new List<T>();
            var i = ts.iterator();
            while (i.hasNext()) result.Add((T) i.next());
            return result;
        }

        public static List<T> list<T>(IEnumerable<T> ts) {
            var result = new List<T>();
            result.AddRange(ts);
            return result;
        }

        public static List<T> list<T>(IEnumerator<T> ts) {
            var result = new List<T>();
            while(ts.MoveNext()) result.Add(ts.Current);
            return result;
        }

        public static List<T> list<T>(params T[] ts) {
            return list((IEnumerable<T>) ts);
        }


        public static void wait(Predicate isTrue) {
            wait(40, 50, isTrue);
        }

        public static void wait(int numWaits, int waitMillis, Predicate isTrue) {
            for(var i = 0; i < numWaits; i++) {
                if (isTrue()) return;
                sleep(waitMillis);
            }
            Bomb.toss("condition for wait() never satisfied");
        }

        public static Dictionary<K, V> dictionary<K, V>() {
            return new Dictionary<K, V>();
        }

        public static Dictionary<K, V> dictionaryOne<K, V>(K key, V value) {
            return new Dictionary<K, V> {{key, value}};
        }

        public static Dictionary<K, V> dictionary<K, V>(IEnumerable<K> keys, IEnumerable<V> values) {
            var result = new Dictionary<K, V>();
            each(keys, values, delegate(K k, V v) { result[k] = v; });
            return result;
        }

        public static Dictionary<K, V> dictionary<K, V>(IEnumerable<K> keys, Converter<K, V> toValue) {
            return dictionary(keys, convert(keys, toValue));
        }

        public static Dictionary<K, V> dictionary<K, V>(IEnumerable<KeyValuePair<K, V>> entries) {
            var result = new Dictionary<K, V>();
            each(entries, entry => result.Add(entry.Key, entry.Value));
            return result;
        }

        public static string bracket(string s) {
            return "[" + s + "]";
        }

        public static string toShortString(double d) {
            if (d < 1.0 / 1000000.0) return "" + (pretty(d * 1000000000)) + "n";
            if (d < 1.0 / 1000.0) return "" + (pretty(d * 1000000)) + "u";
            if (d < 1) return "" + (pretty(d * 1000)) + "m";
            if (d < 1000) return "" + pretty(d);
            if (d < 1000000) return "" + (pretty(d / 1000)) + "K";
            if (d < 1000000000) return "" + (pretty(d / 1000000)) + "M";
            return "" + (pretty(d / 1000000000)) + "G";
        }

        static string pretty(double d) {
            return "" + d.ToString("0.00");
        }

        public static string prettyNumber(double value) {
            return value == Math.Truncate(value) ? value.ToString("n0") : value.ToString(Math.Abs(value) > 10 ? "n2" : "n5");
        }

        public static string toShortString<K, V>(Dictionary<K, V> dictionary) {
            return toShortString(convert(dictionary, pair => pair + ""));
        }

        public static string toShortString(IEnumerable list) {
            var strings = convert<object, string>(list, o => o + "");
            return toShortString(strings);
        }

        public static string toShortString<T>(IEnumerable<T> list) {
            var content = "";
            var index = 0;
            foreach (var t in list) {
                content += index + ": " + t + "\n";
                index++;
                if (content.Length <= 5000) continue;
                content += "\n... continued...";
                break;
            }
            if (content.Length < 50) content = bracket(commaSep(list));
            return content;
        }

        public static string toShortString<T>(List<T> list, Converter<T, string> toString) {
            return toShortString(convert(list, toString));
        }

        public static string commaSep<T>(IEnumerable<T> list) {
            return join(", ", list);
        }

        public static string commaSep(params object[] os) {
            return commaSep((IEnumerable<object>) os);
        }

        public static string join<T>(string delimeter, IEnumerable<T> list) {
            var content = "";
            each(
                list,
                delegate(bool last, T t) {
                    content += t;
                    if (!last) content += delimeter;
                });
            return content;
        }

        public static IEnumerable<R> convert<T, R>(IEnumerable<T> ts, Converter<T, R> convert) {
            foreach (var t in ts) yield return convert(t);
        }
        public delegate R Converter<T1, T2,  R>(T1 t1, T2 t2);
        public static IEnumerable<R> convert<T, R>(IEnumerable<T> ts, Converter<int, T, R> makeR) {
            var i = 0;
            return convert(ts, t => makeR(i++, t));
        }

        public static Dictionary<NewTKey, NewTValue> convert<TKey, TValue, NewTKey, NewTValue>(
            Dictionary<TKey, TValue> d,
            Converter<TKey, NewTKey> convertKey,
            Converter<TValue, NewTValue> convertValue
        ) {
            return dictionary(convert(d.Keys, convertKey), convert(d.Values, convertValue));
        }

        public static List<R> collect<T, R>(IEnumerable<T> source, Converter<T, IEnumerable<R>> produceRs) {
            var result = new List<R>();
            var i = 0;
            foreach(var item in source) 
                try {
                    result.AddRange(produceRs(item));
                    i++;
                } catch(Exception e) {
                    handleEachError(source, i, item, e);
                }
            return result;
        }

        public static DateTime now() {
            return date(Dates.now());
        }

        public static DateTime reallyNow() {
            return DateTime.Now;
        }

        public static bool alwaysTrue<T>(T t) {
            return true;
        }

        public static void info(string s) {
            LogC.info(s);
        }

        public static void err(string s) {
            LogC.err(s);
        }

        public static DateTime date(string s) {
            return date(jDate(s));
        }

        public static JDate jDate(DateTime t) {
            return new JDate((long) (t.ToUniversalTime() - EPOCH).TotalMilliseconds);
        }

        public static DateTime date(JDate t) {
            var millis = t.getTime();
            return EPOCH.AddMilliseconds(millis).ToLocalTime();
        }

        public static void eachKey<K, V>(IDictionary<K, V> map, Action<K> run) {
            var i = 0;
            foreach(var key in map.Keys) 
                try {
                    run(key);
                    i++;
                } catch(Exception e) {
                    handleEachError(map.Keys, i, key, e);
                }
        }

        protected static void eachCopiedKey<K, V>(IDictionary<K, V> map, Action<K> run) {
            each(list<K>(map.Keys), run);
        }

        public static void eachValue<K, V>(IDictionary<K, V> map, Action<V> run) {
            each(map.Values, run);
        }

        public static string paren<T>(T s) {
            return "(" + s + ")";
        }

        public static JDate jDate(string date) {
            return Dates.date(date);
        }
        
        protected static List<KeyValuePair<K, V>> pairsSortedByValue<K, V>(Dictionary<K, V> dict) where V : IComparable<V>  {
            return sort(dict, (a, b) => b.Value.CompareTo(a.Value));
        }

        protected static List<K> keysSortedByValue<K, V>(Dictionary<K, V>  map, Comparison<V> compare) {
            var cheapest = list<KeyValuePair<K, V>>(map.GetEnumerator());
            return keysSortedByValue(cheapest, compare);
        }

        protected static List<K> keysSortedByValue<K, V>(List<KeyValuePair<K, V>> cheapest, Comparison<V> compare) {
            cheapest.Sort((left, right) => compare(left.Value, right.Value));
            return list(convert(cheapest, kv => kv.Key));
        }

        public static List<T> accept<T>(IEnumerable<T> ts, Predicate<T> filter) {
            var result = new List<T>();
            foreach(var t in ts)  
                if (filter(t))  
                    result.Add(t);
            return result;
        }

        public static List<DataRow> accept(DataRowCollection rows, Predicate<DataRow> filter) {
            return accept(list<DataRow>(rows), filter);
        }

        public static List<KeyValuePair<K, V>> accept<K, V>(IEnumerable<KeyValuePair<K, V>> entries, Predicate<K, V> filter) {
            return accept(entries, entry => filter(entry.Key, entry.Value));
        }

        public static bool exists<K, V>(IEnumerable<KeyValuePair<K, V>> entries, Predicate<K, V> filter) {
            return exists(entries, entry => filter(entry.Key, entry.Value));
        }

        public static List<KeyValuePair<K, V>> reject<K, V>(IEnumerable<KeyValuePair<K, V>> entries, Predicate<K, V> filter) {
            return reject(entries, entry => filter(entry.Key, entry.Value));
        }

        public static bool exists<T>(IEnumerable<T> ts, Predicate<T> filter) {
            return hasContent(accept(ts, filter));
        }

        public static bool exists(DataRowCollection ts, Predicate<DataRow> filter) {
            return exists(list<DataRow>(ts), filter);
        }

        public static List<T> reject<T>(IEnumerable<T> ts, Predicate<T> filter) {
            return accept(ts, t => !filter(t));
        }

        public static void sleep(int millis) {
            Thread.Sleep(millis);
        }

        // wow this should perform horribly in profiling since it 
        // forces a 3x date calc/flipflop - java to millis to java to c#
        public static DateTime date(long millisSinceEpoch) {
            return date(new JDate(millisSinceEpoch));
        }

        public static List<T> sort<T>(IEnumerable<T> ts) {
            var result = list(ts);
            result.Sort();
            return result;
        }

        public static List<T> sort<T>(IEnumerable<T> ts, Comparison<T> compare) {
            var result = list(ts);
            result.Sort(compare);
            return result;
        }

        public static List<T> reverse<T>(IEnumerable<T> ts) {
            var result = list(ts);
            result.Reverse();
            return result;
        }
        
        public static IEnumerable<T> reverse<T>(IList<T> ts) {
            for(var i = ts.Count - 1; i >= 0; i--) yield return ts[i];
        }
        
        public static bool reDebug() {
            return "TRUE".Equals(Environment.GetEnvironmentVariable("RE_DEBUG"));
        }

        protected static int numDelegates(Delegate d) {
            return d.GetInvocationList().GetLength(0);
        }

        public static void freezeNow() {
            Dates.freezeNow();
        }

        public static void freezeNow(string date) {
            Dates.freezeNow(date);
        }

        public static void zeroTo(int count, Action<int> onI) {
            for(var i = 0; i < count; i++) onI(i);
        }
        #region each
        public static void each<T>(IEnumerable<T> items, Action<int, bool, T> run) {
            var enumerator = items.GetEnumerator();
            var i = 0;
            var hasNext = enumerator.MoveNext();
            var current = hasNext ? enumerator.Current : default(T);
            try {
                while (hasNext) {
                    hasNext = enumerator.MoveNext();
                    run(i, !hasNext, current);
                    current = hasNext ? enumerator.Current : default(T);
                    i++;
                }
            } catch (Exception e) {
                handleEachError(items, i, current, e);
            }
        }

        static void handleEachError<T>(IEnumerable<T> items, int i, T current, Exception e) {
            var currentStr = "tostring failed.";
            try {
                currentStr = current.ToString();
            } catch (Exception tose) {
                LogC.err("tostring on item failed", tose);
            }
            throw Bomb.toss("failed@" + i + ", processing: " + currentStr + " in " + toShortString(items), e);
        }

        public static void each<K, V>(IEnumerable<KeyValuePair<K, V>> entries, Action<K, V> run) {
            var i = 0;
            foreach(var entry in entries) 
                try {
                    run(entry.Key, entry.Value);
                    i++;
                } catch(Exception e) {
                    handleEachError(entries, i, entry, e);
                }
        }

        public static void each<T>(IEnumerable<T> items, Action<T> run) {
            var i = 0;
            foreach(var item in items) 
                try {
                    run(item);
                    i++;
                } catch(Exception e) {
                    handleEachError(items, i, item, e);
                }
        }

        public static void eachRest<T>(IEnumerable<T> items, Action<T> run) {
            var first = true;
            var i = 0;
            foreach(var item in items) {
                if(first) { first = false; continue; }
                try {
                    run(item);
                    i++;
                } catch(Exception e) {
                    handleEachError(items, i, item, e);
                }
            }
        }

        public static void each<T>(IEnumerable<T> items, Action<bool, T> run) {
            each(items, (index, last, t) => run(last, t));
        }

        public static void eachIt<T>(IEnumerable<T> items, Action<int, T> run) {
            each(items, (index, last, t) => run(index, t));
        }

        public static void each<T>(List items, Action<T> run) {
            each(list<T>(items), run);
        }

        public static void each(DataRowCollection items, Action<DataRow> run) {
            each(list<DataRow>(items), run);
        }

        public static void each<T1, T2>(IEnumerable<T1> ones, IEnumerable<T2> twos, Action<int, T1, T2> run) {
            var twosEnum = twos.GetEnumerator();
            each(
                ones,
                delegate(int i, bool last, T1 one) {
                    if (!twosEnum.MoveNext()) Bomb.toss(mismatched(ones, twos));
                    run(i, one, twosEnum.Current);
                });
            Bomb.when(twosEnum.MoveNext(), () => mismatched(ones, twos));
        }

        public static void each<T1, T2, T3>(IEnumerable<T1> ones, IEnumerable<T2> twos, IEnumerable<T3> threes, Action<int, T1, T2, T3> run) {
            var twosEnum = twos.GetEnumerator();
            var threesEnum = threes.GetEnumerator();
            each(
                ones,
                delegate(int i, bool last, T1 one) {
                    if (!twosEnum.MoveNext()) Bomb.toss(mismatched(ones, twos));
                    if (!threesEnum.MoveNext()) Bomb.toss(mismatched(ones, threes));
                    run(i, one, twosEnum.Current, threesEnum.Current);
                });
            Bomb.when(twosEnum.MoveNext(), () => mismatched(ones, twos));
            Bomb.when(threesEnum.MoveNext(), () => mismatched(ones, threes));
        }

        static string mismatched<T1, T2>(IEnumerable<T1> ones, IEnumerable<T2> twos) {
            return "mismatched counts: \nONES: \n" + toShortString(ones) + "\nTWOS\n" + toShortString(twos);
        }

        public static void each<T1, T2>(IEnumerable<T1> ones, IEnumerable<T2> twos, Action<T1, T2> run) {
            each(ones, twos, (i, t1, t2) => run(t1, t2));
        }

        public static void each<T1, T2, T3>(IEnumerable<T1> ones, IEnumerable<T2> twos, IEnumerable<T3> threes, Action<T1, T2, T3> run) {
            each(ones, twos, threes, (i, t1, t2, t3) => run(t1, t2, t3));
        }

        public static void eachUntilNull<T>(Producer<T> makeT, Action<T> onT) where T : class {
            while(trueDat()) {
                var t = makeT();
                if(t == null) return;
                onT(t);
            }
        }

        public static bool trueDat() {
            return true;
        }
        #endregion

        public static string ymdHuman(JDate time) {
            return Dates.ymdHuman(time);
        }

        public static string ymdHuman(DateTime time) {
            return Dates.ymdHuman(jDate(time));
        }

        public static string hostname() {
            return Environment.MachineName;
        }

        public static double safeDivide(double useWhenDividedByZero, double numerator, double denominator) {
            if (denominator == 0) return useWhenDividedByZero;
            return numerator / denominator;
        }

        public static double sum(IEnumerable<double> doubles) {
            var result = 0.0;
            each(doubles, d => { result += d; });
            return result;
        }
        
        public static float sum(IEnumerable<float> floats) {
            var result = 0.0F;
            each(floats, d => { result += d; });
            return result;
        }
        
        public static int sum(IEnumerable<int> ints) {
            var result = 0;
            each(ints, i => { result += i; });
            return result;
        }

        public static double max(IEnumerable<double> doubles) {
            var result = first(doubles);
            each(rest(doubles), d => { result = Math.Max(result, d); });
            return result;
        }

        public static float max(IEnumerable<float> floats) {
            var result = first(floats);
            each(rest(floats), f => { result = Math.Max(result, f); });
            return result;
        }

        public static T max<T>(IEnumerable<T> ts) where T : IComparable {
            var result = first(ts);
            each(rest(ts), t => { result = result.CompareTo(t) > 0 ? result : t; });
            return result;
        }

        public static double min(IEnumerable<double> doubles) {
            var result = first(doubles);
            each(rest(doubles), d => { result = Math.Min(result, d); });
            return result;
        }

        public static int count<T>(IEnumerable<T> ts) {
            return count(ts, alwaysTrue);
        }

        public static int count<T>(IEnumerable<T> ts, Predicate<T> include) {
            var result = 0;
            each(ts, t => result = result + (include(t) ? 1 : 0));
            return result;
        }

        public static double average(IEnumerable<double> doubles) {
            var sum = 0.0;
            var count = 0;
            each(doubles, d => { sum += d; count++; });
            return sum / count;
        }
                
        public static double standardDeviation(IEnumerable<double> doubles) {
            var mean = average(doubles);
            var sum = 0.0;
            var count = -1;
            each(doubles, d => { sum += Math.Pow(d - mean, 2); count++; });
            Bomb.when(count == -1, ()=> "cannot take standard deviation of empty list");
            return count == 0 ? 0 : Math.Sqrt(sum / count);
        }

        public static double populationStandardDeviation(IEnumerable<double> doubles) {
            var sum = 0.0;
            var sumSquares = 0.0;
            var count = 0;
            each(doubles, d => { sum += d; sumSquares += d * d; count++; });
            Bomb.when(count == 0, ()=> "cannot take population standard deviation of empty list");
            var average = sum / count;
            return Math.Sqrt(sumSquares / count - average * average);
        }

        protected static IEnumerable<double> subtract(List<double> d1, List<double> d2) {
            var result = new List<double>(d1.Count);
            each(d1, d2, (one, two) => result.Add(one-two));
            return result;
        }

        protected static List<double> cumulativeMax(List<double> doubles) {
            if(isEmpty(doubles)) return new List<double>();
            var result = new List<double>(doubles.Count) {first(doubles)};
            eachRest(doubles, next => result.Add(Math.Max(next, last(result))));
            return result;
        }

        public static List<double> cumulativeSum(List<double> doubles) {
            if(isEmpty(doubles)) return new List<double>();
            var result = new List<double>(doubles.Count) {first(doubles)};
            eachRest(doubles, d => result.Add(last(result) + d));
            return result;
        }

        public static List<T> copy<T>(IEnumerable<T> ts) {
            return new List<T>(ts);
        }

        public static bool any<T>(IEnumerable<T> ts, Predicate<T> predicate) {
            foreach (var t in ts) if (predicate(t)) return true;
            return false;
        }

        public static List<TARGET> convert<SOURCE, TARGET>(IEnumerable items, Converter<SOURCE, TARGET> convertOne) where SOURCE : class {
            var result = new List<TARGET>();
            each(list<SOURCE>(items), o => result.Add(convertOne( o )));
            return result;
        }

        public static List<T> list<T>(IEnumerable items) {
            var result = new List<T>();
            foreach(var o in items) result.Add( (T) o);
            return result;
        }

        public static IEnumerable<T> enumerable<T>(IEnumerable items) {
            foreach(var o in items) yield return (T) o;
        }

        [Obsolete("uses should not be checked in")] 
        public static void debug<T>(T t, string name) {
            LogC.debug(t, name + paren(t.GetType().FullName + "#" + t.GetHashCode()));
        }

        [Obsolete("uses should not be checked in")] 
        public static void debug(string message) {
            LogC.debug(message);
        }

        public static void advanceNow(int millis) {
            freezeNow(now().AddMilliseconds(millis));
        }

        public static void freezeNow(DateTime time) {
            Dates.freezeNow(jDate(time));
        }

        public static bool isBeforeNow(IComparable<DateTime> time) {
            return time.CompareTo(now()) < 0;
        }

        public static bool all<T>(IEnumerable<T> ts, Predicate<T> filter) {
            return !exists(ts, t => !filter(t));
        }

        protected static int processId() {
            return Process.GetCurrentProcess().Id;
        }

        public static bool dictionaryEquals<K, V>(IDictionary<K, V> ones, IDictionary<K, V> twos) {
            if(ones.Count != twos.Count) return false;
            var result = true;
            eachKey(ones, key => {if(!ones[key].Equals(twos[key])) result = false;});
            return result;
        }

        public static bool listEquals<T>(IEnumerable<T> one, IEnumerable<T> two) {
            var result = true;
            each(one, two, (o, t) => { if (!o.Equals(t)) result = false;});
            return result;
        }

        protected static ArrayList jList<T>(IEnumerable<T> ts, Converter<T, java.lang.Object> toJ) {
            var result = new ArrayList();
            each(ts, t => result.add(toJ(t)));
            return result;
        }

        public static List<T> list<J, T>(object jList, Converter<java.lang.Object, T> toC) where J : java.lang.Object {
            var js = list<J>((JList) jList);
            return convert(js, toC);
        }

        public static JList jStrings(params string[] ss) {
            return jList(ss);
        }

        public static ArrayList jList(IEnumerable<string> ss) {
            var result = new ArrayList();
            each(ss, s => result.add(s));
            return result;
        }

        public static List<string> split(string delim, string s) {
            return list<string>(Strings.split(delim, s));
        }

        public static void queueWorkItem(Action action) {
            WaitCallback wc = o => {
                                  action();
                                  lock(workItems) workItems.Remove(action);
                              };
            lock(workItems) workItems.Add(action, true);
            ThreadPool.QueueUserWorkItem(wc);
        }

        public static void waitForAllWorkItems(int numWaits, int waitTimeMillis) {
            wait(numWaits, waitTimeMillis, () => { lock (workItems) return isEmpty(workItems); });
        }

        public static T condensedSql<T>(string s, Creator<T> result) {
            var debugState = Log.debugSql();
            try {
                Log.doNotDebugSqlForever();
                LogC.info(s);
                Log.info(s);
                return result();
            } finally {
                Log.setDebugSqlStateForever(debugState);
            }
        }

        protected int identity() {
            return identity(this);
        }

        public static int identity(object o) {
            return RuntimeHelpers.GetHashCode(o);
        }

        public static int runCommand(string command, string args) {
            var process = Process.Start(command, args);
            if(process == null) throw Bomb.toss("cannot run fftwlogin.bat");
            process.WaitForExit();
            return process.ExitCode;
        }

        public static string username() {
            return Environment.UserName;
        }

        public static void systemExit(int exitCode) {
            Environment.Exit(exitCode);
        }

        public static byte[] serialize(object toSerialize) {
            var stream = new MemoryStream();
            new BinaryFormatter().Serialize(stream, toSerialize);
            return stream.ToArray();
        }

        public static object deserialize(byte[] bytes) {
            var stream = new MemoryStream(bytes);
            return new BinaryFormatter().Deserialize(stream);
        }

        public static void makeMax<T>(ref T accumulator, T point) where T : IComparable<T> {
            if (accumulator.CompareTo(point) < 0) accumulator = point;
        }

        public static void makeMin<T>(ref T accumulator, T point) where T : IComparable<T> {
            if (accumulator.CompareTo(point) > 0) accumulator = point;
        }

        protected static double secondsSince(DateTime startMetricCalc) {
            return (DateTime.Now - startMetricCalc).TotalSeconds;
        }

        public static T produce<T>(Producer<T> produce) {
            return produce();
        }

        public static IEnumerable<int> seq(int count) {
            for(var i = 0; i < count; i++) yield return i;
        }

        public static DataRow last(DataRowCollection rows) {
            return rows[rows.Count - 1];
        }

        public static DataRow first(DataRowCollection rows) {
            return rows[0];
        }

        public static T[] array<T>(IEnumerable<T> ts) {
            return list(ts).ToArray();
        }

        public static T[] array<T>(List<T> ts) {
            return ts.ToArray();
        }

        public static T[] array<T>(params T[] ts) {
            return ts;
        }

        public static TimerManager timerManager() {
            return timers;
        }

        protected static V requireAllMatchFirst<U, V>(IEnumerable<U> us, Converter<U, V> toV) {
            var firstV = toV(first(us));
            each(rest(us), u => Bomb.unless(firstV.Equals(toV(u)), () => firstV + " does not match " + toV(u)));
            return firstV;
        }

        public static int indexOf<T>(IEnumerable<T> ts, Predicate<T> isSelected){
            var i = 0;
            foreach(var t in ts) {
                if (isSelected(t)) return i;
                i++;
            }
            throw Bomb.toss("Could not find an appropriate element in " + toShortString(ts));
        }

        public static void runProcess(string exe, string parameters) {
            var process = Process.Start(new ProcessStartInfo(exe, parameters) { WindowStyle = ProcessWindowStyle.Hidden});
            if(process == null) throw Bomb.toss("failed to start process \n" + exe + " " + parameters);
            process.WaitForExit();
            Bomb.unless(process.ExitCode == 0, () => "command returned " + process.ExitCode + ".\n" + exe + " " + parameters);
        }

        public static Dictionary<V, K> invert<K, V>(Dictionary<K, V> input) {
            return dictionary(input.Values, input.Keys);
        }

        public static string ymdHuman(DateTime? time, string @default) {
            return time.HasValue ? ymdHuman(time.Value) : @default;
        }

        public static bool isEnvSet(string key, bool defalt) {
            var human = Environment.GetEnvironmentVariable(key);
            if (isEmpty(human)) {
                if (!envNotified.ContainsKey(key)) {
                    LogC.info("ENV: " + key + "=" + defalt + "(default)");
                    envNotified.Add(key, 1);
                }
                return defalt;
            }
            if (!envNotified.ContainsKey(key)) {
                LogC.info("ENV: " + key + "=" + human);
                envNotified.Add(key, 1);
            }
            return Strings.toBoolean(human, defalt);
        }

        public static Dictionary<K, V> dictionaryFromValues<K, V>(IEnumerable<V> vs, Func<V, K> toKey) {
            return dictionary(convert(vs, v => toKey(v)), vs);
        }
    }
}