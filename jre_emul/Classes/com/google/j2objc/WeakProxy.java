/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.j2objc;

import com.google.j2objc.annotations.Weak;

/**
 * Proxies all calls to a weakly held instance. Useful for avoiding reference cycles.
 */
public class WeakProxy {

  @Weak
  private final Object delegate;

  private WeakProxy(Object o) {
    delegate = o;
  }

  public static <T> T forObject(T o) {
    return (T) new WeakProxy(o);
  }

  /*-[
  - (id)forwardingTargetForSelector:(SEL)selector {
    return delegate_;
  }

  - (BOOL)isKindOfClass:(Class)aClass {
    return [delegate_ isKindOfClass:aClass];
  }

  - (IOSClass *)java_getClass {
    return [delegate_ java_getClass];
  }
  ]-*/
}
