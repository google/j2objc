/*
 * Copyright (C) 2007 The Android Open Source Project
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

package dalvik.system;

import android.compat.annotation.UnsupportedAppUsage;

import dalvik.annotation.optimization.FastNative;

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
   * Returns the defining class loader of the caller's caller.
   *
   * @return the requested class loader, or {@code null} if this is the
   *         bootstrap class loader.
   * @deprecated Use {@code ClassLoader.getClassLoader(sun.reflect.Reflection.getCallerClass())}.
   *         Note that that can return {@link BootClassLoader} on Android where the RI
   *         would have returned null.
   */
  @UnsupportedAppUsage
  @FastNative
  @Deprecated
  native public static ClassLoader getCallingClassLoader();

  /**
   * Returns the class of the caller's caller.
   *
   * @return the requested class, or {@code null}.
   * @deprecated Use {@link sun.reflect.Reflection#getCallerClass()}.
   */
  @Deprecated
  public static Class<?> getStackClass1() {
    return getStackClass2();
  }

  /**
   * Returns the class of the caller's caller's caller.
   *
   * @return the requested class, or {@code null}.
   */
  @UnsupportedAppUsage
  @FastNative
  native public static Class<?> getStackClass2();

  /**
   * Returns the first ClassLoader on the call stack that isn't the
   * bootstrap class loader.
   */
  @FastNative
  public native static ClassLoader getClosestUserClassLoader();

  /**
   * Retrieves the stack trace from the specified thread.
   *
   * @param t
   *      thread of interest
   * @return an array of stack trace elements, or null if the thread
   *      doesn't have a stack trace (e.g. because it exited)
   */
  @UnsupportedAppUsage
  @FastNative
  native public static StackTraceElement[] getThreadStackTrace(Thread t);

  /**
   * Retrieves an annotated stack trace from the specified thread.
   *
   * @param t
   *      thread of interest
   * @return an array of annotated stack frames, or null if the thread
   *      doesn't have a stack trace (e.g. because it exited)
   */
  @libcore.api.CorePlatformApi
  @FastNative
  native public static AnnotatedStackTraceElement[]
  getAnnotatedThreadStackTrace(Thread t);

  /**
   * Retrieves a partial stack trace from the specified thread into
   * the provided array.
   *
   * @param t
   *      thread of interest
   * @param stackTraceElements
   *      preallocated array for use when only the top of stack is
   *      desired. Unused elements will be filled with null values.
   * @return the number of elements filled
   */
  @UnsupportedAppUsage
  @FastNative
  native public static int fillStackTraceElements(Thread t,
                                                  StackTraceElement[] stackTraceElements);
}
