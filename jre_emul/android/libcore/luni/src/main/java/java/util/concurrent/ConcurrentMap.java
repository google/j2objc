/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

import java.util.Map;

// BEGIN android-note
// removed link to collections framework docs
// END android-note

/**
 * A {@link java.util.Map} providing additional atomic
 * {@code putIfAbsent}, {@code remove}, and {@code replace} methods.
 *
 * <p>Memory consistency effects: As with other concurrent
 * collections, actions in a thread prior to placing an object into a
 * {@code ConcurrentMap} as a key or value
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * actions subsequent to the access or removal of that object from
 * the {@code ConcurrentMap} in another thread.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public interface ConcurrentMap<K,V> extends Map<K,V> {
    /**
     * If the specified key is not already associated
     * with a value, associate it with the given value.
     * This is equivalent to
     *  <pre> {@code
     * if (!map.containsKey(key))
     *   return map.put(key, value);
     * else
     *   return map.get(key);}</pre>
     *
     * except that the action is performed atomically.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     *         {@code null} if there was no mapping for the key.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with the key,
     *         if the implementation supports null values.)
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by this map
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in this map
     * @throws NullPointerException if the specified key or value is null,
     *         and this map does not permit null keys or values
     * @throws IllegalArgumentException if some property of the specified key
     *         or value prevents it from being stored in this map
     */
    V putIfAbsent(K key, V value);

    /**
     * Removes the entry for a key only if currently mapped to a given value.
     * This is equivalent to
     *  <pre> {@code
     * if (map.containsKey(key) && map.get(key).equals(value)) {
     *   map.remove(key);
     *   return true;
     * } else
     *   return false;}</pre>
     *
     * except that the action is performed atomically.
     *
     * @param key key with which the specified value is associated
     * @param value value expected to be associated with the specified key
     * @return {@code true} if the value was removed
     * @throws UnsupportedOperationException if the {@code remove} operation
     *         is not supported by this map
     * @throws ClassCastException if the key or value is of an inappropriate
     *         type for this map
     *         (<a href="../Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key or value is null,
     *         and this map does not permit null keys or values
     *         (<a href="../Collection.html#optional-restrictions">optional</a>)
     */
    boolean remove(Object key, Object value);

    /**
     * Replaces the entry for a key only if currently mapped to a given value.
     * This is equivalent to
     *  <pre> {@code
     * if (map.containsKey(key) && map.get(key).equals(oldValue)) {
     *   map.put(key, newValue);
     *   return true;
     * } else
     *   return false;}</pre>
     *
     * except that the action is performed atomically.
     *
     * @param key key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @return {@code true} if the value was replaced
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by this map
     * @throws ClassCastException if the class of a specified key or value
     *         prevents it from being stored in this map
     * @throws NullPointerException if a specified key or value is null,
     *         and this map does not permit null keys or values
     * @throws IllegalArgumentException if some property of a specified key
     *         or value prevents it from being stored in this map
     */
    boolean replace(K key, V oldValue, V newValue);

    /**
     * Replaces the entry for a key only if currently mapped to some value.
     * This is equivalent to
     *  <pre> {@code
     * if (map.containsKey(key)) {
     *   return map.put(key, value);
     * } else
     *   return null;}</pre>
     *
     * except that the action is performed atomically.
     *
     * @param key key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     *         {@code null} if there was no mapping for the key.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with the key,
     *         if the implementation supports null values.)
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by this map
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in this map
     * @throws NullPointerException if the specified key or value is null,
     *         and this map does not permit null keys or values
     * @throws IllegalArgumentException if some property of the specified key
     *         or value prevents it from being stored in this map
     */
    V replace(K key, V value);
}
