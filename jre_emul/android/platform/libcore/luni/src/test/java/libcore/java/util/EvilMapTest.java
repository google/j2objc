/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.util;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class EvilMapTest extends junit.framework.TestCase {
  public static class EvilMap<K,V> extends AbstractMap<K,V> {
    private final Set<Entry<K,V>> entries = new HashSet<Entry<K,V>>();

    public EvilMap() {
      entries.add(new AbstractMap.SimpleEntry("hi", "there"));
    }

    // Claim we're empty...
    @Override public int size() { return 0; }
    // ...but potentially return many entries.
    @Override public Set<Entry<K, V>> entrySet() { return entries; }

    // Dummy implementation, not relevant for this test but
    // necessary to implement AbstractMap.
    @Override public V put(K key, V val) { return val; }
  }

  // https://code.google.com/p/android/issues/detail?id=48055
  public void test_48055_HashMap() throws Exception {
    Map<String, String> evil = new EvilMap<String, String>();
    evil.put("hi", "there");
    // Corrupt one HashMap...
    HashMap<String, String> map = new HashMap<String, String>(evil);
    // ...and now they're all corrupted.
    HashMap<String, String> map2 = new HashMap<String, String>();
    assertNull(map2.get("hi"));
  }

  public void test_48055_Hashtable() throws Exception {
    Map<String, String> evil = new EvilMap<String, String>();
    evil.put("hi", "there");
    // Corrupt one Hashtable...
    Hashtable<String, String> map = new Hashtable<String, String>(evil);
    // ...and now they're all corrupted.
    Hashtable<String, String> map2 = new Hashtable<String, String>();
    assertNull(map2.get("hi"));
  }

  public void test_48055_LinkedHashMap() throws Exception {
    Map<String, String> evil = new EvilMap<String, String>();
    evil.put("hi", "there");
    // Corrupt one LinkedHashMap...
    LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(evil);
    // ...and now they're all corrupted.
    LinkedHashMap<String, String> map2 = new LinkedHashMap<String, String>();
    assertNull(map2.get("hi"));
  }

  public void test_48055_WeakHashMap() throws Exception {
    Map<String, String> evil = new EvilMap<String, String>();
    evil.put("hi", "there");
    // Corrupt one WeakHashMap...
    WeakHashMap<String, String> map = new WeakHashMap<String, String>(evil);
    // ...and now they're all corrupted.
    WeakHashMap<String, String> map2 = new WeakHashMap<String, String>();
    assertNull(map2.get("hi"));
  }

  public void test_48055_IdentityHashMap() throws Exception {
    Map<String, String> evil = new EvilMap<String, String>();
    evil.put("hi", "there");
    // Corrupt one IdentityHashMap...
    IdentityHashMap<String, String> map = new IdentityHashMap<String, String>(evil);
    // ...and now they're all corrupted.
    IdentityHashMap<String, String> map2 = new IdentityHashMap<String, String>();
    assertNull(map2.get("hi"));
  }

  public void test_48055_ConcurrentHashMap() throws Exception {
    Map<String, String> evil = new EvilMap<String, String>();
    evil.put("hi", "there");
    // Corrupt one ConcurrentHashMap...
    ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>(evil);
    // ...and now they're all corrupted.
    ConcurrentHashMap<String, String> map2 = new ConcurrentHashMap<String, String>();
    assertNull(map2.get("hi"));
  }


}
