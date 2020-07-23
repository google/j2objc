/*
 * Copyright (C) 2007 The Android Open Source Project
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

package dalvik.system;

import android.compat.annotation.UnsupportedAppUsage;

/**
 * Stub implementation of dalvik.system.VMStack.
 */

/**
 * Provides a limited interface to the Dalvik VM stack. This class is mostly
 * used for implementing security checks.
 *
 * @hide
 */
@libcore.api.CorePlatformApi

public final class VMStack {
  private VMStack() {
  }

  /**
   * Stub implementation of getCallingClassLoader()
   *
   *  Returns the defining class loader of the caller's caller.
   *
   * @return the requested class loader, or {@code null} if this is the
   * bootstrap class loader.
   * @deprecated Use {@code ClassLoader.getClassLoader(sun.reflect.Reflection.getCallerClass())}.
   * Note that that can return {@link BootClassLoader} on Android where the RI
   * would have returned null.
   */
  @Deprecated
  public static ClassLoader getCallingClassLoader() {
    return null;
  }

  /**
   * Returns the class of the caller's caller.
   *
   * @return the requested class, or {@code null}.
   * @deprecated Use {@link sun.reflect.Reflection#getCallerClass()}.
   */
  @Deprecated
  public static Class<?> getStackClass1() {
    try {
      // j2objc implementation.
      StackTraceElement[] stack = new Throwable().getStackTrace();
      return Class.forName(stack[2].getClassName());
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  /**
   * Implementation unique to J2ObjC
   *
   * Returns the class of the caller's caller's caller.
   *
   * @return the requested class, or {@code null}.
   */
  @UnsupportedAppUsage
  public static Class<?> getStackClass2() {
    try {
      // j2objc implementation, duplicated from above to avoid adding a stack frame.
      StackTraceElement[] stack = new Throwable().getStackTrace();
      return Class.forName(stack[3].getClassName());
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  /**
   * Stub implementation of getClosestUserClassLoader()
   *
   * Returns the first ClassLoader on the call stack that isn't the
   * bootstrap class loader.
   */
  public static ClassLoader getClosestUserClassLoader() {
    return null;
  }

  /**
   * Implementation unique to J2ObjC
   *
   * Retrieves the stack trace from the specified thread.
   *
   * @param t thread of interest
   * @return an array of stack trace elements, or null if the thread
   * doesn't have a stack trace (e.g. because it exited)
   */
  @UnsupportedAppUsage
  public static StackTraceElement[] getThreadStackTrace(Thread t) {
    return t.getStackTrace();
  }

  /**
   * Cannot be implemented in J2ObjC
   *
   * Retrieves an annotated stack trace from the specified thread.
   *
   * @param t thread of interest
   * @return an array of annotated stack frames, or null if the thread
   * doesn't have a stack trace (e.g. because it exited)
   *
   @libcore.api.CorePlatformApi
   @FastNative
   native public static AnnotatedStackTraceElement[]
   getAnnotatedThreadStackTrace(Thread t);
   */

  /**
   * Implementation unique to J2ObjC
   *
   * Retrieves a partial stack trace from the specified thread into
   * the provided array.
   *
   * @param t thread of interest
   * @param stackTraceElements preallocated array for use when only the top of stack is
   * desired. Unused elements will be filled with null values.
   * @return the number of elements filled
   */
  @UnsupportedAppUsage
  public static int fillStackTraceElements(Thread t,
                                           StackTraceElement[] stackTraceElements) {
    StackTraceElement[] stackTrace = t.getStackTrace();
    for (int i = 0; i < stackTraceElements.length; i++) {
      stackTraceElements[i] = stackTrace[i];
    }
    return stackTraceElements.length;
  }
}
