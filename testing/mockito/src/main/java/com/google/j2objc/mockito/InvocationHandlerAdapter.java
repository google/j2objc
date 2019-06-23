// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.j2objc.mockito;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationFactory.RealMethodBehavior;
import org.mockito.invocation.MockHandler;

/**
 * InvocationHandler adapter, inspired by the dexmaker project's version.
 * https://code.google.com/p/dexmaker/
 */
public class InvocationHandlerAdapter implements InvocationHandler {
  private MockHandler handler;

  public InvocationHandlerAdapter(MockHandler handler) {
    this.handler = handler;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] rawArgs) throws Throwable {
    // args can be null if the method invoked has no arguments, but Mockito expects a non-null array
    Object[] args = rawArgs != null ? rawArgs : new Object[0];
    if (isEqualsMethod(method)) {
      return proxy == args[0];
    } else if (isHashCodeMethod(method)) {
      return System.identityHashCode(proxy);
    }

    RealMethodBehavior<Object> realMethod = new RealMethodBehavior<Object>() {
      @Override
      public Object call() throws Throwable {
        try {
          return method.invoke(proxy, rawArgs);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
      }
    };
    return handler.handle(Mockito.framework().getInvocationFactory().createInvocation(proxy,
        Mockito.withSettings().build(proxy.getClass().getSuperclass()), method, realMethod, args));
  }

  public MockHandler getHandler() {
    return handler;
  }

  public void setHandler(MockHandler handler) {
    this.handler = handler;
  }

  private static boolean isEqualsMethod(Method method) {
    return method.getName().equals("equals")
        && method.getParameterTypes().length == 1
        && method.getParameterTypes()[0] == Object.class;
  }

  private static boolean isHashCodeMethod(Method method) {
    return method.getName().equals("hashCode")
        && method.getParameterTypes().length == 0;
  }
}
