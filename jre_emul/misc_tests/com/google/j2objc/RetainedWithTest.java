/*
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

package com.google.j2objc;

import com.google.j2objc.annotations.AutoreleasePool;
import com.google.j2objc.annotations.RetainedWith;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tests the {@link RetainedWith} annotation.
 *
 * @author Keith Stanger
 */
public class RetainedWithTest extends TestCase {

  private static Set<Integer> finalizedObjects = new HashSet<>();

  @Override
  public void tearDown() throws Exception {
    finalizedObjects.clear();
    super.tearDown();
  }

  static class Base {
    protected void finalize() {
      finalizedObjects.add(System.identityHashCode(this));
    }
  }

  static class A extends Base implements Cloneable {
    @RetainedWith B b = new B(this);

    public A clone() {
      try {
        return (A) super.clone();
      } catch (CloneNotSupportedException e) {
        throw new AssertionError(e);
      }
    }
  }

  static class B extends Base implements Cloneable {
    A a;
    B(A a) {
      this.a = a;
    }

    public B clone() {
      try {
        return (B) super.clone();
      } catch (CloneNotSupportedException e) {
        throw new AssertionError(e);
      }
    }
  }

  @AutoreleasePool
  private void newA(List<Integer> objectCodes) {
    A a = new A();
    objectCodes.add(System.identityHashCode(a));
    objectCodes.add(System.identityHashCode(a.b));
  }

  public void testObjectPairIsDeallocated() {
    List<Integer> objectCodes = new ArrayList<>();
    newA(objectCodes);
    for (Integer i : objectCodes) {
      assertTrue(finalizedObjects.contains(i));
    }
  }

  static class Symmetric extends Base {
    @RetainedWith Symmetric other;
    Symmetric() {
      other = new Symmetric(this);
    }
    Symmetric(Symmetric other) {
      this.other = other;
    }
  }

  @AutoreleasePool
  private void newSymmetric(List<Integer> objectCodes) {
    Symmetric s = new Symmetric();
    objectCodes.add(System.identityHashCode(s));
    objectCodes.add(System.identityHashCode(s.other));
  }

  public void testSymmetricObjectPairIsDeallocated() {
    List<Integer> objectCodes = new ArrayList<>();
    newSymmetric(objectCodes);
    for (Integer i : objectCodes) {
      assertTrue(finalizedObjects.contains(i));
    }
  }

  @AutoreleasePool
  private void newAPlusClone(List<Integer> objectCodes) {
    A a = new A();
    A a2 = a.clone();
    assertSame(a.b, a2.b);
    // We allow this reassignment of a2.b because the child's return reference points at "a" not
    // "a2". It is important to support setting the child reference to null after cloning the
    // parent.
    a2.b = null;
    objectCodes.add(System.identityHashCode(a));
    objectCodes.add(System.identityHashCode(a2));
    objectCodes.add(System.identityHashCode(a.b));
  }

  public void testCloneParentObject() {
    List<Integer> objectCodes = new ArrayList<>();
    newAPlusClone(objectCodes);
    for (Integer i : objectCodes) {
      assertTrue(finalizedObjects.contains(i));
    }
  }

  @AutoreleasePool
  private void newAPlusCloneChild(List<Integer> objectCodes) {
    A a = new A();
    B b = a.b.clone();
    assertSame(a, b.a);
    objectCodes.add(System.identityHashCode(a));
    objectCodes.add(System.identityHashCode(a.b));
    objectCodes.add(System.identityHashCode(b));
  }

  public void testCloneChildObject() {
    List<Integer> objectCodes = new ArrayList<>();
    newAPlusCloneChild(objectCodes);
    for (Integer i : objectCodes) {
      assertTrue(finalizedObjects.contains(i));
    }
  }

  @AutoreleasePool
  private void newAPlusReassignChild(List<Integer> objectCodes) {
    A a = new A();
    objectCodes.add(System.identityHashCode(a));
    objectCodes.add(System.identityHashCode(a.b));
    a.b = new B(a);
    objectCodes.add(System.identityHashCode(a.b));
  }

  public void testReassignChild() {
    List<Integer> objectCodes = new ArrayList<>();
    newAPlusReassignChild(objectCodes);
    for (Integer i : objectCodes) {
      assertTrue(finalizedObjects.contains(i));
    }
  }

  private abstract class MapFactory<K> {

    final K key;

    MapFactory(K key) {
      this.key = key;
    }

    public abstract Map<K, ValueType> newMap();

    public K getKey() {
      return key;
    }
  }

  // We use this class as a value to insert in our maps so we can verity that the map has been
  // deallocated.
  private static class ValueType {
    protected void finalize() {
      finalizedObjects.add(System.identityHashCode(this));
    }
  }

  enum Color { RED, GREEN, BLUE }

  private static class MapTest<K> {

    private Set<K> keys;
    private Collection<ValueType> values;
    private Set<Map.Entry<K, ValueType>> entrySet;

    @AutoreleasePool
    private void createMapChildren(MapFactory<K> factory,
        List<Integer> objectCodes) {
      // Use separate maps for each of the views to ensure that each view type is strengthening its
      // reference to the map.
      Map<K, ValueType> m1 = factory.newMap();
      Map<K, ValueType> m2 = factory.newMap();
      Map<K, ValueType> m3 = factory.newMap();
      ValueType v = new ValueType();
      m1.put(factory.getKey(), v);
      m2.put(factory.getKey(), v);
      m3.put(factory.getKey(), v);
      keys = m1.keySet();
      values = m2.values();
      entrySet = m3.entrySet();
      objectCodes.add(System.identityHashCode(v));
    }

    @AutoreleasePool
    private void checkMapChildren(MapFactory<K> factory, List<Integer> objectCodes) {
      createMapChildren(factory, objectCodes);
      // Call some methods to make sure they still exist and can access the parent
      assertEquals(1, keys.size());
      assertEquals(1, values.size());
      assertEquals(1, entrySet.size());
      assertTrue(keys.contains(factory.getKey()));
      assertFalse(values.contains(new ValueType()));
      assertEquals(factory.getKey(), entrySet.iterator().next().getKey());
      keys = null;
      values = null;
      entrySet = null;
    }

    private void run(MapFactory<K> factory) {
      List<Integer> objectCodes = new ArrayList<>();
      checkMapChildren(factory, objectCodes);
      for (Integer i : objectCodes) {
        assertTrue(finalizedObjects.contains(i));
      }
    }
  }

  public void testMapChildren() {
    new MapTest<>().run(new MapFactory<Object>(new Object()) {
      public Map<Object, ValueType> newMap() {
        return new IdentityHashMap<>();
      }
    });
    new MapTest<>().run(new MapFactory<Object>(new Object()) {
      public Map<Object, ValueType> newMap() {
        return new WeakHashMap<>();
      }
    });
    new MapTest<Color>().run(new MapFactory<Color>(Color.RED) {
      public Map<Color, ValueType> newMap() {
        return new EnumMap<>(Color.class);
      }
    });
    new MapTest<>().run(new MapFactory<Object>(new Object()) {
      public Map<Object, ValueType> newMap() {
        return new HashMap<>();
      }
    });
    new MapTest<Integer>().run(new MapFactory<Integer>(5) {
      public Map<Integer, ValueType> newMap() {
        return new TreeMap<>();
      }
    });
    new MapTest<>().run(new MapFactory<Object>(new Object()) {
      public Map<Object, ValueType> newMap() {
        return new Hashtable<>();
      }
    });
    new MapTest<>().run(new MapFactory<Object>(new Object()) {
      public Map<Object, ValueType> newMap() {
        return new ConcurrentHashMap<>();
      }
    });
  }
}
