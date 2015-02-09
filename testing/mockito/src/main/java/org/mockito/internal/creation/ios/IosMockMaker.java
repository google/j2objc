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

package org.mockito.internal.creation.ios;

import org.mockito.exceptions.base.MockitoException;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationSettings;
import org.mockito.plugins.MockMaker;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

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
    Set<Class> interfacesSet = settings.getExtraInterfaces();
    Class<?>[] extraInterfaces = interfacesSet.toArray(new Class[interfacesSet.size()]);
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
        objc_allocateClassPair([OrgMockitoInternalCreationIosIosMockMaker_ClassProxy class],
            [name UTF8String], 0);
    jint interfaceCount = interfaces->size_;
    for (jint i = 0; i < interfaceCount; i++) {
      IOSClass *intrface = (IOSClass *) [interfaces objectAtIndex:i];
      if (![intrface isInterface]) {
        @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc]
                            initWithNSString:[intrface description]]);
      }
      class_addProtocol(proxyClass, intrface.objcProtocol);
    }
    objc_registerClassPair(proxyClass);
    return IOSClass_fromClass(proxyClass);
  ]-*/;

  static class ClassProxy {
    InvocationHandler $__handler;

    InvocationHandler getHandler() {
      return $__handler;
    }

    void setHandler(InvocationHandler handler) {
      $__handler = handler;
    }

    /*-[
    static IOSClass* getMethodDescription(Class cls, SEL aSelector,
        struct objc_method_description *md) {
      IOSClass *mockClass = IOSClass_fromClass(cls);
      IOSClass *mockedClass =
          [OrgMockitoInternalCreationIosIosMockMaker_proxyCache_ getWithId:mockClass];

      // Check for an instance method with this selector.
      unsigned nMethods;
      Method *instanceMethods = class_copyMethodList(mockedClass.objcClass, &nMethods);
      for (unsigned i = 0; i < nMethods; i++) {
        SEL sel = method_getName(instanceMethods[i]);
        if (sel == aSelector) {
          struct objc_method_description *methodDescription =
              method_getDescription(instanceMethods[i]);
          memcpy(md, methodDescription, sizeof(struct objc_method_description));
          free(instanceMethods);
          return mockedClass;
        }
      }
      free(instanceMethods);

      // See if it's a class method.
      Method *classMethods =
          class_copyMethodList(object_getClass(mockedClass.objcClass), &nMethods);
      for (unsigned i = 0; i < nMethods; i++) {
        SEL sel = method_getName(classMethods[i]);
        if (sel == aSelector) {
          struct objc_method_description *methodDescription =
              method_getDescription(classMethods[i]);
          memcpy(md, methodDescription, sizeof(struct objc_method_description));
          free(classMethods);
          return mockedClass;
        }
      }
      free(classMethods);

      // Check the extra interfaces for the method.
      unsigned int outCount;
      Protocol * __unsafe_unretained *interfaces = class_copyProtocolList(cls, &outCount);
      for (unsigned i = 0; i < outCount; i++) {
        struct objc_method_description methodDescription =
            protocol_getMethodDescription(interfaces[i], aSelector, YES, YES);
        if (methodDescription.name && sel_isEqual(aSelector, methodDescription.name)) {
          memcpy(md, &methodDescription, sizeof(struct objc_method_description));
          free(interfaces);
          return IOSClass_fromProtocol(interfaces[i]);
        }
      }
      free(interfaces);
      return nil;
    }

    - (NSMethodSignature *)methodSignatureForSelector:(SEL)aSelector {
      struct objc_method_description methodDescription;
      if (getMethodDescription([self class], aSelector, &methodDescription)) {
        return [NSMethodSignature signatureWithObjCTypes:methodDescription.types];
      }
      return nil;
    }

    // Forwards a message to the invocation handler for this proxy.
    - (void)forwardInvocation:(NSInvocation *)anInvocation {
      SEL selector = [anInvocation selector];
      struct objc_method_description methodDescription;
      IOSClass *clazz = getMethodDescription([self class], selector, &methodDescription);
      if (clazz) {
        JavaLangReflectMethod *method =
            [clazz findMethodWithTranslatedName:NSStringFromSelector(selector)];
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
        return;  // success!
      }
      [self doesNotRecognizeSelector:_cmd];
    }
    ]-*/
  }

}
