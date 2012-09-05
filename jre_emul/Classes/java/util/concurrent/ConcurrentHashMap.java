/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util.concurrent;

import java.util.Hashtable;

/**
 * An inefficient implementation of ConcurrentMap where the underlying map
 * is a HashTable.
 */
public class ConcurrentHashMap<K, V> extends Hashtable<K, V> implements ConcurrentMap<K, V> {

  public V putIfAbsent(K key, V value) {
    synchronized (this) {
      V oldValue = get(key);
      return oldValue == null ? put(key, value) : oldValue;
    }
  }

  public boolean remove(Object key, Object value) {
    synchronized (this) {
      if (containsKey(key) && get(key).equals(value)) {
        remove(key);
        return true;
      } else {
        return false;
      }
    }
  }

  public boolean replace(K key, V oldValue, V newValue) {
    synchronized (this) {
      if (containsKey(key) && get(key).equals(oldValue)) {
        put(key, newValue);
        return true;
      } else {
        return false;
      }
    }
  }

  public V replace(K key, V value) {
    synchronized (this) {
      V oldValue = get(key);
      return oldValue != null ? put(key, value) : null;
    }
  }
}
