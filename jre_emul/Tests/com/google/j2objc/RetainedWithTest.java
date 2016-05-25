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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

  static class A extends Base {
    @RetainedWith B b = new B(this);
  }

  static class B extends Base {
    A a;
    B(A a) {
      this.a = a;
    }
  }

  @AutoreleasePool
  private void newA(List<Integer> objectCodes) {
    A a = new A();
    objectCodes.add(System.identityHashCode(a));
    objectCodes.add(System.identityHashCode(a.b));
  }

  public void testObjectPairIsDeallocated() {
    List<Integer> objectCodes = new ArrayList<Integer>();
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
    List<Integer> objectCodes = new ArrayList<Integer>();
    newSymmetric(objectCodes);
    for (Integer i : objectCodes) {
      assertTrue(finalizedObjects.contains(i));
    }
  }
}
