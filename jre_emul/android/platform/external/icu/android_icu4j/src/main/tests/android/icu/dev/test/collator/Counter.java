/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 1996-2015, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.dev.test.collator;


import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Counter<T> implements Iterable<T>, Comparable<Counter<T>> {
  Map<T,RWLong> map;
  Comparator<T> comparator;
  
  public Counter() {
    this(null);
  }
  
  public Counter(Comparator<T> comparator) {
    if (comparator != null) {
      this.comparator = comparator;
      map = new TreeMap<T, RWLong>(comparator);
    } else {
      map = new LinkedHashMap<T, RWLong>();
    }
  }
  
  static private final class RWLong implements Comparable<RWLong> {
    // the uniqueCount ensures that two different RWIntegers will always be different
    static int uniqueCount;
    public long value;
    private final int forceUnique;
    {
      synchronized (RWLong.class) { // make thread-safe
        forceUnique = uniqueCount++;
      }
    }

    public int compareTo(RWLong that) {
      if (that.value < value) return -1;
      if (that.value > value) return 1;
      if (this == that) return 0;
      synchronized (this) { // make thread-safe
        if (that.forceUnique < forceUnique) return -1;
      }
      return 1; // the forceUnique values must be different, so this is the only remaining case
    }
    public String toString() {
      return String.valueOf(value);
    }
  }

  public Counter<T> add(T obj, long countValue) {
    RWLong count = map.get(obj);
    if (count == null) map.put(obj, count = new RWLong());
    count.value += countValue;
    return this;
  }

  public long getCount(T obj) {
      return get(obj);
    }

  public long get(T obj) {
      RWLong count = map.get(obj);
      return count == null ? 0 : count.value;
    }

  public Counter<T> clear() {
    map.clear();
    return this;
  }

  public long getTotal() {
    long count = 0;
    for (T item : map.keySet()) {
      count += map.get(item).value;
    }
    return count;
  }

  public int getItemCount() {
    return size();
  }
  
  private static class Entry<T> {
    RWLong count;
    T value;
    int uniqueness;
    public Entry(RWLong count, T value, int uniqueness) {
      this.count = count;
      this.value = value;
      this.uniqueness = uniqueness;
    }
  }
  
  private static class EntryComparator<T> implements Comparator<Entry<T>>{
    int countOrdering;
    Comparator<T> byValue;
    
    public EntryComparator(boolean ascending, Comparator<T> byValue) {
      countOrdering = ascending ? 1 : -1;
      this.byValue = byValue;
    }
    public int compare(Entry<T> o1, Entry<T> o2) {
      if (o1.count.value < o2.count.value) return -countOrdering;
      if (o1.count.value > o2.count.value) return countOrdering;
      if (byValue != null) {
        return byValue.compare(o1.value, o2.value);
      }
      return o1.uniqueness - o2.uniqueness;
    }
  }

  public Set<T> getKeysetSortedByCount(boolean ascending) {
    return getKeysetSortedByCount(ascending, null);
  }
  
  public Set<T> getKeysetSortedByCount(boolean ascending, Comparator<T> byValue) {
    Set<Entry<T>> count_key = new TreeSet<Entry<T>>(new EntryComparator<T>(ascending, byValue));
    int counter = 0;
    for (T key : map.keySet()) {
      count_key.add(new Entry<T>(map.get(key), key, counter++));
    }
    Set<T> result = new LinkedHashSet<T>();
    for (Entry<T> entry : count_key) {
       result.add(entry.value);
    }
    return result;
  }

  public Set<T> getKeysetSortedByKey() {
    Set<T> s = new TreeSet<T>(comparator);
    s.addAll(map.keySet());
    return s;
  }

//public Map<T,RWInteger> getKeyToKey() {
//Map<T,RWInteger> result = new HashMap<T,RWInteger>();
//Iterator<T> it = map.keySet().iterator();
//while (it.hasNext()) {
//Object key = it.next();
//result.put(key, key);
//}
//return result;
//}

  public Set<T> keySet() {
    return map.keySet();
  }

  public Iterator<T> iterator() {
    return map.keySet().iterator();
  }

  public Map<T, RWLong> getMap() {
    return map; // older code was protecting map, but not the integer values.
  }

  public int size() {
    return map.size();
  }

  public String toString() {
    return map.toString();
  }

  public Counter<T> addAll(Collection<T> keys, int delta) {
    for (T key : keys) {
      add(key, delta);
    }
    return this;
  }
  
  public Counter<T> addAll(Counter<T> keys) {
    for (T key : keys) {
      add(key, keys.getCount(key));
    }
    return this;
  }

  public int compareTo(Counter<T> o) {
    Iterator<T> i = map.keySet().iterator();
    Iterator<T> j = o.map.keySet().iterator();
    while (true) {
      boolean goti = i.hasNext();
      boolean gotj = j.hasNext();
      if (!goti || !gotj) {
        return goti ? 1 : gotj ? -1 : 0;
      }
      T ii = i.next();
      T jj = i.next();
      int result = ((Comparable<T>)ii).compareTo(jj);
      if (result != 0) {
        return result;
      }
      final long iv = map.get(ii).value;
      final long jv = o.map.get(jj).value;
      if (iv != jv) return iv < jv ? -1 : 0;
    }
  }

  public Counter<T> increment(T key) {
    return add(key, 1);
  }

public boolean containsKey(T key) {
    return map.containsKey(key);
}

public boolean equals(Object o) {
    return map.equals(o);
}

public int hashCode() {
    return map.hashCode();
}

public boolean isEmpty() {
    return map.isEmpty();
}

public Counter<T> remove(T key) {
    map.remove(key);
    return this;
}

//public RWLong put(T key, RWLong value) {
//    return map.put(key, value);
//}
//
//public void putAll(Map<? extends T, ? extends RWLong> t) {
//    map.putAll(t);
//}
//
//public Set<java.util.Map.Entry<T, Long>> entrySet() {
//    return map.entrySet();
//}
//
//public Collection<RWLong> values() {
//    return map.values();
//}

}
