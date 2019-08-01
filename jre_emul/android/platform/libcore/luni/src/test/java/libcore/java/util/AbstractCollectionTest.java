/*
 * Copyright (C) 2012 Google Inc.
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

package libcore.java.util;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;
import junit.framework.TestCase;

public final class AbstractCollectionTest extends TestCase {
  // http://code.google.com/p/android/issues/detail?id=36519
  public void test_toArray() throws Exception {
    final ConcurrentHashMap<Integer, Integer> m = new ConcurrentHashMap<Integer, Integer>();
    final AtomicBoolean finished = new AtomicBoolean(false);

    Thread reader = new Thread(new Runnable() {
      @Override public void run() {
        while (!finished.get()) {
          m.values().toArray();
          m.values().toArray(new Integer[m.size()]);
        }
      }
    });

    Thread mutator = new Thread(new Runnable() {
      @Override public void run() {
        for (int i = 0; i < 100; ++i) {
          m.put(-i, -i);
        }
        for (int i = 0; i < 4096; ++i) {
          m.put(i, i);
          m.remove(i);
        }
        finished.set(true);
      }
    });

    reader.start();
    mutator.start();
    reader.join();
    mutator.join();
  }

  // http://b/31052838
  public void test_empty_removeAll_null() {
    try {
      new EmptyCollection().removeAll(null);
      fail("Should have thrown");
    } catch (NullPointerException expected) {
    }
  }

  // http://b/31052838
  public void test_empty_retainAll_null() {
    try {
      new EmptyCollection().retainAll(null);
      fail("Should have thrown");
    } catch (NullPointerException expected) {
    }
  }

  /**
   * An AbstractCollection that does not override removeAll() / retainAll().
   */
  private static class EmptyCollection extends AbstractCollection<Object> {
    @Override public Iterator iterator() { return Collections.emptySet().iterator(); }
    @Override public int size() { return 0; }
  }

}
