package com.fftw.bloomberg.rtf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A generic cache of items.
 * <p/>
 * As a cache, each item is unique, so duplicates will not be allowed.  If duplicate keys are added, the last
 * item added will replace previous instances.
 */
public class GenericCache<K, V> {
    private Map<K, V> cache = new HashMap<K, V>();


    public GenericCache() {

    }

    public GenericCache(Map<K, V> newPositions) {
        cache.clear();
        cache.putAll(newPositions);
    }

    public V addCacheItem(K key, V value) {

        if (cache.containsKey(key)) {
            cache.remove(key);
        }
        return cache.put(key, value);
    }

    public V getCacheItem(K key) {
        return cache.get(key);
    }

    public Collection<V> values() {
        return cache.values();
    }

    public void clear() {
        cache.clear();
    }

    public String dump() {
        StringBuilder sb = new StringBuilder(cache.size() * 2240);

        for (V bp : cache.values()) {
            sb.append(bp).append("\n");
        }

        return sb.toString();
    }
}
