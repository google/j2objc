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

package java.lang.reflect;

import junit.framework.TestCase;

import java.util.concurrent.ScheduledExecutorService;

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

}
