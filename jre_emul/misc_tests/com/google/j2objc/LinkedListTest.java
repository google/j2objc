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
import junit.framework.TestCase;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Functional tests for correct memory behavior of LinkedList.
 *
 * @author Lukhnos Liu
 */
public class LinkedListTest  extends TestCase {
  // The default stack size is 8 MB on x64. Inside -dealloc, each call to another -dealloc takes
  // 48 bytes, or 6 words (previous rbp, return addr, self, selector, and two additional words saved
  // by the method). 8 MB / 48 bytes = 174762. The actual number to cause stack overflow is slightly
  // lower as the stack is already being used for other things when the first -dealloc is called,
  // but let's just use a bigger plus-one number here.
  static final int SIZE_LARGE = 174763;
  static final int SIZE_SMALL = 1000;

  @AutoreleasePool
  public void testLongList() {
    LinkedList<Integer> list = new LinkedList<>();
    insertIntoList(list, SIZE_LARGE);
    assertEquals(SIZE_LARGE, list.size());
  }

  @AutoreleasePool
  public void testLongListWithClear() {
    LinkedList<Integer> list = new LinkedList<>();
    insertIntoList(list, SIZE_LARGE);
    list.clear();
    assertTrue(list.isEmpty());
  }

  @AutoreleasePool
  public void testLongListWithOneByOneRemoval() {
    final int count = SIZE_LARGE;
    LinkedList<Integer> list = new LinkedList<>();
    insertIntoList(list, count);
    for (int i = 0; i < count; i++) {
      list.remove();
    }
    assertTrue(list.isEmpty());
  }

  @AutoreleasePool
  public void testLongListWithIteratorRemoval() throws InterruptedException {
    LinkedList<Integer> list = new LinkedList<>();
    insertIntoList(list, SIZE_LARGE);
    int previous = -1;
    Iterator<Integer> it = list.iterator();
    while (it.hasNext()) {
      int next = it.next();
      assertTrue(next - previous == 1);
      previous = next;
      it.remove();

    }
    assertTrue(list.isEmpty());
  }

  @AutoreleasePool
  public void testLongListWithDescendingIteratorRemoval() {
    final int count = SIZE_LARGE * 2;
    LinkedList<Integer> list = new LinkedList<>();
    insertIntoList(list, count);
    int previous = count;
    Iterator<Integer> it = list.descendingIterator();

    // ReverseLinkIterator.remove() does not need the eager chain-breaking code like its forward
    // counterpart LinkIterator.remove() does, and this is because if we remove a list in reverse,
    // all removed nodes' next will point to the same node that was the next of the removed chain,
    // and so when those removed nodes' -dealloc is called, the method will be releasing the same
    // next_ node.
    //
    // We design this test case to remove from the middle, so that all removed nodes' next points to
    // the first node in the second half of the list. This test would fail if this second half was
    // not broken up properly during the clean up process.
    for (int i = 0; i < count / 2; i++) {
      assertTrue(it.hasNext());
      it.next();
      previous--;
    }

    while (it.hasNext()) {
      int next = it.next();
      assertTrue(previous - next == 1);
      previous = next;
      it.remove();
    }
    assertFalse(list.isEmpty());
  }

  @AutoreleasePool
  public void testShortList() {
    LinkedList<Integer> list = new LinkedList<>();
    insertIntoList(list, SIZE_SMALL);
    assertEquals(SIZE_SMALL, list.size());
  }

  void insertIntoList(LinkedList<Integer> list, int numElements) {
    for (int i = 0; i < numElements; i++) {
      list.add(i);
    }
  }
}
