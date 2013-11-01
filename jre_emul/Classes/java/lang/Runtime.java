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

package java.lang;

/**
 * Simple iOS version of java.lang.Runtime.  No code was shared, just its
 * public API.
 *
 * @author Tom Ball
 */
public class Runtime {

  /**
   * Holds the Singleton global instance of Runtime.
   */
  private static final Runtime instance = new Runtime();

  private Runtime() {}

  public static Runtime getRuntime() {
    return instance;
  }

  public native int availableProcessors() /*-[
    return [[NSProcessInfo processInfo] processorCount];
  ]-*/;

  public native void exit(int status) /*-[
    // exit() calls any functions registered with atexit().
    exit(status);
  ]-*/;

  public native void halt(int status) /*-[
    // _Exit() doesn't call any functions registered with atexit().
    _Exit(status);
  ]-*/;

  public void gc() {
    // No garbage collector, so do nothing.
  }

  public native void addShutdownHook(Thread hook) /*-[
    atexit_b(^{
      [hook start];
    });
  ]-*/;
}
