/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.google.j2objc.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import junit.framework.TestCase;

public class ProxyTest extends TestCase {

  public void testInvocationOfSuperInterfaceMethod() throws Exception {
    final boolean[] testResult = new boolean[1];
    InvocationHandler handler = new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args)
          throws Throwable {
        if (method.getName().equals("submit")) {
          testResult[0] = true;
        }
        return null;
      }
    };

    ScheduledExecutorService service = (ScheduledExecutorService) Proxy.newProxyInstance(
        ProxyTest.class.getClassLoader(),
        new Class<?>[] { ScheduledExecutorService.class },
        handler);
    // Invoke submit(), which is defined in ScheduledExecutorService's super-interface,
    // ExecutorService.
    service.submit(new Runnable() {
      @Override
      public void run() {}
    });
    assertTrue("proxied submit method not invoked", testResult[0]);
  }

  // Issue #910: verify proxy object's equals, hashCode and toString methods
  // are passed to the InvocationHandler.
  public void testObjectMethodsInvoked() throws Exception {
    final Set<String> calledMethods = new HashSet<>();
    InvocationHandler invocationHandler = new InvocationHandler() {
      @Override
      public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        calledMethods.add(methodName);

        switch (methodName) {
          case "helloInt":
            return 123;
          case "equals":
            return o == args[0];
          case "hashCode":
            return -123;
          case "toString":
            return "hello" + o.hashCode();
          default:
            return method.invoke(o, args);
        }
      }
    };
    ShowMe showMe = (ShowMe) Proxy.newProxyInstance(getClass().getClassLoader(),
        new Class[] { ShowMe.class }, invocationHandler);

    // Call methods that should be handled by InvocationHandler.
    assertEquals(123, showMe.helloInt());
    assertEquals("hello-123", showMe.toString());
    assertFalse(showMe.equals(new ShowMe() {
      @Override
      public int helloInt() {
        return 423;
      }
    }));
    assertEquals(-123, showMe.hashCode());

    assertTrue(calledMethods.contains("helloInt"));
    assertTrue(calledMethods.contains("toString"));
    assertTrue(calledMethods.contains("equals"));
    assertTrue(calledMethods.contains("hashCode"));
  }

  // Issue #910: verify proxy object's equals, hashCode and toString methods
  // are used if invocation handler invokes them.
  public void testObjectMethodDefaultsInvoked() throws Exception {
    final Set<String> calledMethods = new HashSet<>();
    InvocationHandler invocationHandler = new InvocationHandler() {
      @Override
      public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        calledMethods.add(methodName);

        switch (methodName) {
          case "helloInt":
            return 123;
          default:
            return method.invoke(o, args);
        }
      }
    };
    ShowMe showMe = (ShowMe) Proxy.newProxyInstance(getClass().getClassLoader(),
        new Class[] { ShowMe.class }, invocationHandler);

    // Call methods that should be handled by InvocationHandler.
    assertEquals(123, showMe.helloInt());

    // Check return values from Proxy.proxy_* methods.
    int hashCode = showMe.hashCode();
    assertEquals("JavaLangReflectProxy@" + Integer.toHexString(hashCode), showMe.toString());
    assertFalse(showMe.equals(new ShowMe() {
      @Override
      public int helloInt() {
        return 423;
      }
    }));

    assertTrue(calledMethods.contains("helloInt"));
    assertTrue(calledMethods.contains("toString"));
    assertTrue(calledMethods.contains("equals"));
    assertTrue(calledMethods.contains("hashCode"));
  }

  static interface ShowMe {
    int helloInt();
  }
}
