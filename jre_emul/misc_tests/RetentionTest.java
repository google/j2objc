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

import junit.framework.TestCase;

/** @author Michał Pociecha-Łoś */
public class RetentionTest extends TestCase {
  public void testArrayAccess() {
    Object[] array = new Object[1];
    array[0] = new Object();
    runInNewThread(
        () -> {
          Object object = array[0];
          runInNewThread(
              () -> {
                array[0] = null;
              });
          object.hashCode(); // should not crash
        });
  }

  // TODO(micapolos): Uncomment once fixed.
  // public void testFieldAccess() {
  //   Ref ref = new Ref();
  //   ref.object = new Object();
  //   runInNewThread(
  //       () -> {
  //         Object object = ref.object;
  //         runInNewThread(
  //             () -> {
  //               ref.object = null;
  //             });
  //         object.hashCode(); // should not crash
  //       });
  // }

  // TODO(micapolos): Uncomment once fixed.
  // public void testFieldGetter() {
  //   Ref ref = new Ref();
  //   ref.object = new Object();
  //   runInNewThread(
  //       () -> {
  //         Object object = ref.get();
  //         runInNewThread(
  //             () -> {
  //               ref.object = null;
  //             });
  //         object.hashCode(); // should not crash
  //       });
  // }

  // TODO(micapolos): Uncomment once fixed.
  // public void testAutoreleasePoolInForLoop() {
  //   Object object = null;
  //   for (@AutoreleasePool int i = 0; i < 1; i++) {
  //     object = new Object();
  //   }
  //   object.hashCode(); // should not crash
  // }

  static void runInNewThread(Runnable runnable) {
    Thread thread = new Thread(runnable);
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  static class Ref {
    Object object;

    Object get() {
      return object;
    }
  }
}
