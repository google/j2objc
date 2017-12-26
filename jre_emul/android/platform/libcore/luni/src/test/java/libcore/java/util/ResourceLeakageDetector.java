/*
 * Copyright (C) 2014 The Android Open Source Project
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

/**
 * Detects resource leakages for resources that are protected by <code>CloseGuard</code> mechanism.
 *
 * <p>If multiple instances of this are active at the same time, i.e. have been created but not yet
 * had their {@link #checkForLeaks()} method called then while they will report all the leakages
 * detected they may report the leakages caused by the code being tested by another detector.
 *
 * <p>The underlying CloseGuardMonitor is loaded using reflection to ensure that this will run,
 * albeit doing nothing, on the reference implementation.
 */
public class ResourceLeakageDetector {
  /** The class for the CloseGuardMonitor, null if not supported. */
  private static final Class<?> CLOSE_GUARD_MONITOR_CLASS;

  static {
    ClassLoader classLoader = ResourceLeakageDetector.class.getClassLoader();
    Class<?> clazz;
    try {
      // Make sure that the CloseGuard class exists; this ensures that this is not running
      // on a RI JVM.
      classLoader.loadClass("dalvik.system.CloseGuard");

      // Load the monitor class for later instantiation.
      clazz = classLoader.loadClass("dalvik.system.CloseGuardMonitor");

    } catch (ClassNotFoundException e) {
      System.err.println("Resource leakage will not be detected; "
          + "this is expected in the reference implementation");
      e.printStackTrace(System.err);

      // Ignore, probably running in reference implementation.
      clazz = null;
    }

    CLOSE_GUARD_MONITOR_CLASS = clazz;
  }

  /**
   * The underlying CloseGuardMonitor that will perform the post test checks for resource
   * leakage.
   */
  private Runnable postTestChecker;

  /**
   * Create a new detector.
   *
   * @return The new {@link ResourceLeakageDetector}, its {@link #checkForLeaks()} method must be
   * called otherwise it will not clean up properly after itself.
   */
  public static ResourceLeakageDetector newDetector()
      throws Exception {
    return new ResourceLeakageDetector();
  }

  private ResourceLeakageDetector()
      throws Exception {
    if (CLOSE_GUARD_MONITOR_CLASS != null) {
      postTestChecker = (Runnable) CLOSE_GUARD_MONITOR_CLASS.newInstance();
    }
  }

  /**
   * Detect any leaks that have arisen since this was created.
   *
   * @throws Exception If any leaks were detected.
   */
  public void checkForLeaks() throws Exception {
    // If available check for resource leakage.
    if (postTestChecker != null) {
      postTestChecker.run();
    }
  }
}
