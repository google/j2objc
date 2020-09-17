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

import com.google.j2objc.util.AutoreleasePool;
import junit.framework.TestCase;

/** @author Michał Pociecha-Łoś */
public class RetentionTest extends TestCase {
  public void testArrayAccess() {
    Object[] array = new Object[1];
    AutoreleasePool.run(
        () -> {
          array[0] = new Object();
        });
    Object object = array[0];
    AutoreleasePool.run(
        () -> {
          array[0] = null;
        });
    object.hashCode(); // should not crash
  }

  public void testFieldAccess() {
    Ref ref = new Ref();
    AutoreleasePool.run(() -> {
      ref.object = new Object();
    });
    Object object = ref.object;
    AutoreleasePool.run(() -> {
      ref.object = null;
    });
    object.hashCode(); // should not crash
  }

  public void testFieldGetter() {
    Ref ref = new Ref();
    AutoreleasePool.run(() -> {
      ref.object = new Object();
    });
    Object object = ref.get();
    AutoreleasePool.run(() -> {
      ref.object = null;
    });
    object.hashCode(); // should not crash
  }

  // TODO(micapolos): Uncomment once fixed.
  // public void testAutoreleasePoolInForLoop() {
  //   Object object = null;
  //   for (@AutoreleasePool int i = 0; i < 1; i++) {
  //     object = new Object();
  //   }
  //   object.hashCode(); // should not crash
  // }

  static class Ref {
    Object object;

    Object get() {
      return object;
    }
  }
}
