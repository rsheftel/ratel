using System;
using System.Collections.Generic;
using O=Q.Util.Objects;

namespace Q.Util {
    public class LazyDictionary<K, V> : Objects {
        readonly Converter<K, V> creator;
        readonly Dictionary<K, V> data = new Dictionary<K, V>();

        public LazyDictionary(Converter<K, V> creator) {
            this.creator = creator;
        }

        public V get(K key) {
            if(!data.ContainsKey(key)) data[key] = creator(key);
            return data[key];
        }

        public void remove(K key) {
            data.Remove(key);
        }

        public void clear() {
            data.Clear();
        }

        public IEnumerable<V> values() {
            return data.Values;
        }

        public int size() {
            return data.Keys.Count;
        }

        public void overwrite(K key, V value) {
            data[key] = value;
        }

        public IEnumerable<K> keys() {
            return data.Keys;
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (obj.GetType() != typeof (LazyDictionary<K, V>)) return false;
            var other = (LazyDictionary<K, V>) obj;
            return dictionaryEquals(data, other.data);
        }

        public override int GetHashCode() {
            return (data != null ? data.GetHashCode() : 0);
        }

        public void add(K key) {
            get(key);
        }

        public bool has(K k) {
            return data.ContainsKey(k);
        }
    }
}