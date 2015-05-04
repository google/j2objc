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

import java.util.ArrayList;
import java.util.List;

/*-[
#import "mach/mach.h"
]-*/

/**
 * Simple iOS version of java.lang.Runtime.  No code was shared, just its
 * public API.
 *
 * @author Tom Ball
 */
public class Runtime {

  private List<Thread> shutdownHooks = new ArrayList<Thread>();

  /**
   * Holds the Singleton global instance of Runtime.
   */
  private static final Runtime instance = new Runtime();

  private Runtime() {
    registerShutdownHooks();
  }

  public static Runtime getRuntime() {
    return instance;
  }

  public native int availableProcessors() /*-[
    return (int) [[NSProcessInfo processInfo] processorCount];
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

  public native long maxMemory() /*-[
    return (long long) [[NSProcessInfo processInfo] physicalMemory];
  ]-*/;

  public native long totalMemory() /*-[
    struct task_basic_info info;
    mach_msg_type_number_t size = sizeof(info);
    kern_return_t kerr = task_info(mach_task_self(), TASK_BASIC_INFO, (task_info_t) &info, &size);
    return (long long) (kerr == KERN_SUCCESS) ? info.resident_size : 0;
  ]-*/;

  public native long freeMemory() /*-[
    mach_port_t host_port = mach_host_self();
    mach_msg_type_number_t host_size = sizeof(vm_statistics_data_t) / sizeof(integer_t);
    vm_size_t pagesize;
    vm_statistics_data_t vm_stat;

    host_page_size(host_port, &pagesize);
    (void) host_statistics(host_port, HOST_VM_INFO, (host_info_t) &vm_stat, &host_size);
    return (long long) vm_stat.free_count * pagesize;
  ]-*/;

  public void addShutdownHook(Thread hook) {
    if (shutdownHooks == null) {
      throw new IllegalStateException("shutdown in progress");
    }
    if (hook.isAlive()) {
      throw new IllegalArgumentException("hook already started");
    }
    if (shutdownHooks.contains(hook)) {
      throw new IllegalArgumentException("hook previously added");
    }
    shutdownHooks.add(hook);
  }

  public boolean removeShutdownHook(Thread hook) {
    return shutdownHooks.remove(hook);
  }

  private void runShutdownHooks() {
    for (Thread t : shutdownHooks) {
      t.start();
    }
    shutdownHooks = null;  // Indicates the hooks were started.
  }

  private native void registerShutdownHooks() /*-[
    atexit_b(^{
      [self runShutdownHooks];
    });
  ]-*/;

  /**
   * No-op on iOS, since all code must be linked into app bundle.
   */
  public void load(String absolutePath) {}

  /**
   * No-op on iOS, since all code must be linked into app bundle.
   */
  public void loadLibrary(String nickname) {}

  /**
   * No-op on iOS, since it doesn't use garbage collection.
   */
  public void runFinalization() {}

  /**
   * No-op on iOS.
   */
  public void traceInstructions(boolean enable) {}

  /**
   * No-op on iOS.
   */
  public void traceMethodCalls(boolean enable) {}
}
