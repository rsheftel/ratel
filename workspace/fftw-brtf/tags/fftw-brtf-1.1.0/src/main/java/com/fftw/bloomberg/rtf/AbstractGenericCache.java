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
public abstract class AbstractGenericCache<K, V> {
    private Map<K, V> cache = new HashMap<K, V>();


    public AbstractGenericCache() {

    }

    public AbstractGenericCache(Map<K, V> newPositions) {
        cache.clear();
        cache.putAll(newPositions);
    }

    public int size() {
        return cache.size();
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

    /**
     * All subclasses must implement to generated the logic that builds a key from the item.
     *
     * @param value
     * @return
     */
    public abstract K getKeyForItem(V value);
}
