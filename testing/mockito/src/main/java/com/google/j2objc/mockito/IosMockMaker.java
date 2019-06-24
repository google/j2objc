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

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationSettings;
import org.mockito.plugins.MockMaker;

/*-[
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/reflect/Method.h"
]-*/

/**
 * MockMaker implementation for iOS. Unlike the JRE and Android versions,
 * this class creates mocks for classes using the Objective-C runtime.
 *
 * @author Tom Ball
 */
public final class IosMockMaker implements MockMaker {

  // to find previously created types
  private static Map<String, WeakReference<Class<?>>> classCache =
      new HashMap<String, WeakReference<Class<?>>>();
  private static final Map<Class<?>, Class<?>> proxyCache = new WeakHashMap<Class<?>, Class<?>>();

  private static int nextClassNameIndex = 0;

  @Override
  @SuppressWarnings("unchecked")
  public <T> T createMock(MockCreationSettings<T> settings, MockHandler handler) {
    Class<T> typeToMock = settings.getTypeToMock();
    @SuppressWarnings("rawtypes")
    Set<Class<?>> interfacesSet = settings.getExtraInterfaces();
    Class<?>[] extraInterfaces = interfacesSet.toArray(new Class<?>[interfacesSet.size()]);
    InvocationHandler invocationHandler = new InvocationHandlerAdapter(handler);

    if (typeToMock.isInterface()) {
        // support interfaces via java.lang.reflect.Proxy
        @SuppressWarnings("rawtypes")
        Class[] classesToMock = new Class[extraInterfaces.length + 1];
        classesToMock[0] = typeToMock;
        System.arraycopy(extraInterfaces, 0, classesToMock, 1, extraInterfaces.length);
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        // newProxyInstance returns the type of typeToMock
        T mock = (T) Proxy.newProxyInstance(contextClassLoader, classesToMock, invocationHandler);
        return mock;

    } else {
        try {
            Class<? extends T> proxyClass = getProxyClass(typeToMock, extraInterfaces);
            T mock = proxyClass.newInstance();
            ((ClassProxy) mock).setHandler(invocationHandler);
            return mock;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new MockitoException("Failed to mock " + typeToMock, e);
        }
    }
  }

  @Override
  public MockHandler getHandler(Object mock) {
    InvocationHandlerAdapter adapter = getInvocationHandlerAdapter(mock);
    return adapter != null ? adapter.getHandler() : null;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public void resetMock(Object mock, MockHandler newHandler, MockCreationSettings settings) {
    InvocationHandlerAdapter adapter = getInvocationHandlerAdapter(mock);
    adapter.setHandler(newHandler);
  }

  private InvocationHandlerAdapter getInvocationHandlerAdapter(Object mock) {
      if (Proxy.isProxyClass(mock.getClass())) {
          InvocationHandler invocationHandler = Proxy.getInvocationHandler(mock);
          return invocationHandler instanceof InvocationHandlerAdapter
                  ? (InvocationHandlerAdapter) invocationHandler
                  : null;
      }
      if (mock instanceof ClassProxy) {
        InvocationHandler invocationHandler = ((ClassProxy) mock).getHandler();
        return invocationHandler instanceof InvocationHandlerAdapter
                ? (InvocationHandlerAdapter) invocationHandler
                : null;
    }

      return null;
  }

  @SuppressWarnings("unchecked")
  <T> Class<T> getProxyClass(Class<T> typeToMock, Class<?>[] interfaces) {
    synchronized (classCache) {
      String className = typeToMock.getName();
      Class<?> newClass;
      WeakReference<Class<?>> ref = classCache.get(className);
      if (ref == null) {
        String nextClassName = className + "$Proxy" + nextClassNameIndex++;
        newClass = generateClassProxy(nextClassName.replace('.', '/'), typeToMock, interfaces);
        classCache.put(className, new WeakReference<Class<?>>(newClass));
        synchronized (proxyCache) {
          proxyCache.put(newClass, typeToMock);
        }
      } else {
          newClass = ref.get();
          assert newClass != null : "\nclassName=\"" + className + "\""
                                  + "\nclassCache=\"" + classCache + "\""
                                  + "\nproxyCache=\"" + proxyCache + "\"";
      }
      return (Class<T>) newClass;
    }
  }

  private static native <T> Class<T> generateClassProxy(String name, Class<T> classToMock,
      Class<?>[] interfaces) throws IllegalArgumentException /*-[
    Class proxyClass =
        objc_allocateClassPair([ComGoogleJ2objcMockitoIosMockMaker_ClassProxy class],
            [name UTF8String], 0);
    jint interfaceCount = interfaces->size_;
    for (jint i = 0; i < interfaceCount; i++) {
      IOSClass *intrface = (IOSClass *) [interfaces objectAtIndex:i];
      if (![intrface isInterface]) {
        @throw create_JavaLangIllegalArgumentException_initWithNSString_([intrface description]);
      }
      class_addProtocol(proxyClass, intrface.objcProtocol);
    }
    objc_registerClassPair(proxyClass);
    return IOSClass_fromClass(proxyClass);
  ]-*/;

  static class ClassProxy {
    InvocationHandler invocationHandler;

    InvocationHandler getHandler() {
      return invocationHandler;
    }

    void setHandler(InvocationHandler handler) {
      invocationHandler = handler;
    }

    /*-[
    static JavaLangReflectMethod *FindMethod(id self, SEL selector) {
      IOSClass *mockedClass =
          [ComGoogleJ2objcMockitoIosMockMaker_proxyCache getWithId:[self java_getClass]];
      return [mockedClass getMethodWithSelector:sel_getName(selector)];
    }
    ]-*/

    /*-[
    + (BOOL)isSubclassOfClass:(Class)aClass {
      IOSClass *mockedClass =
          [ComGoogleJ2objcMockitoIosMockMaker_proxyCache getWithId:[self java_getClass]];
      return self == aClass
          || [[self superclass] isSubclassOfClass:aClass]
          || [[mockedClass objcClass] isSubclassOfClass:aClass];
    }
    ]-*/

    /*-[
    - (BOOL)isKindOfClass:(Class)aClass {
      return [[self class] isSubclassOfClass:aClass];
    }
    ]-*/


    /*-[
    - (NSMethodSignature *)methodSignatureForSelector:(SEL)aSelector {
      return [FindMethod(self, aSelector) getSignature];
    }
    ]-*/

    /*-[
    // Forwards a message to the invocation handler for this proxy.
    - (void)forwardInvocation:(NSInvocation *)anInvocation {
      SEL selector = [anInvocation selector];
      JavaLangReflectMethod *method = FindMethod(self, selector);
      if (!method) {
        [self doesNotRecognizeSelector:_cmd];
      }

      IOSObjectArray *paramTypes = [method getParameterTypes];
      NSUInteger numArgs = paramTypes->size_;
      IOSObjectArray *args = [IOSObjectArray arrayWithLength:numArgs type:NSObject_class_()];
      for (unsigned i = 0; i < numArgs; i++) {
        J2ObjcRawValue arg;
        [anInvocation getArgument:&arg atIndex:i + 2];
        id javaArg = [paramTypes->buffer_[i] __boxValue:&arg];
        [args replaceObjectAtIndex:i withObject:javaArg];
      }

      id<JavaLangReflectInvocationHandler> handler = [self getHandler];
      id javaResult = [handler invokeWithId:self
                   withJavaLangReflectMethod:method
                           withNSObjectArray:args];
      IOSClass *returnType = [method getReturnType];
      if (returnType != [IOSClass voidClass]) {
        J2ObjcRawValue result;
        [[method getReturnType] __unboxValue:javaResult toRawValue:&result];
        [anInvocation setReturnValue:&result];
      }
    }
    ]-*/
  }

  @Override
  public TypeMockability isTypeMockable(final Class<?> type) {
    return new TypeMockability() {
      @Override
      public boolean mockable() {
        return !type.isPrimitive() && !Modifier.isFinal(type.getModifiers());
      }

      @Override
      public String nonMockableReason() {
        if (mockable()) {
          return "";
        }
        if (type.isPrimitive()) {
          return "primitive type";
        }
        if (Modifier.isFinal(type.getModifiers())) {
          return "final class";
        }
        return "not handled type";
      }
    };
  }
}

/*-[
  // Defines an embedded resource defining this class as the default MockMaker.
  // To update it, see j2objc/testing/mockito/README.
  static jbyte _mockito_extensions_org_mockito_plugins_MockMaker[] = {
    0x63, 0x6F, 0x6D, 0x2E, 0x67, 0x6F, 0x6F, 0x67, 0x6C, 0x65,
    0x2E, 0x6A, 0x32, 0x6F, 0x62, 0x6A, 0x63, 0x2E, 0x6D, 0x6F,
    0x63, 0x6B, 0x69, 0x74, 0x6F, 0x2E, 0x49, 0x6F, 0x73, 0x4D,
    0x6F, 0x63, 0x6B, 0x4D, 0x61, 0x6B, 0x65, 0x72, 0x0A,
  };
  J2OBJC_RESOURCE(_mockito_extensions_org_mockito_plugins_MockMaker, 39, 0x76f6e007);
]-*/
