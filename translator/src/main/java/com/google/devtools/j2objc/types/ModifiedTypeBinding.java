/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
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

package com.google.devtools.j2objc.types;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * A dynamic proxy class that returns a type binding for a TypeDeclaration
 * whose superclass or interface list have been modified. For example, the
 * {@link com.google.devtools.j2objc.DeadCodeEliminator} updates the types
 * it changed using bindings returned by this class.
 *
 * @author Tom Ball
 */
public class ModifiedTypeBinding implements InvocationHandler {
  ITypeBinding original;
  ITypeBinding superclass;
  ITypeBinding[] interfaces;

  /**
   * Return a binding for a specified type declaration.
   */
  public static ITypeBinding bind(TypeDeclaration type) {
    ITypeBinding originalBinding = Types.getTypeBinding(type);
    Type superType = type.getSuperclassType();
    ITypeBinding superclass = superType != null ? Types.getTypeBinding(superType) : null;
    @SuppressWarnings("unchecked")
    List<ITypeBinding> interfaceList = type.superInterfaceTypes(); // safe by definition
    ITypeBinding[] interfaces = new ITypeBinding[interfaceList.size()];
    for (int i = 0; i < interfaces.length; i++) {
      interfaces[i] = Types.getTypeBinding(interfaceList.get(i));
    }

    Class<?> delegateClass = originalBinding.getClass();
    return (ITypeBinding) Proxy.newProxyInstance(delegateClass.getClassLoader(),
        delegateClass.getInterfaces(),
        new ModifiedTypeBinding(originalBinding, superclass, interfaces));
  }

  private ModifiedTypeBinding(ITypeBinding original, ITypeBinding superclass,
      ITypeBinding[] interfaces) {
    this.original = original;
    this.superclass = superclass;
    this.interfaces = interfaces;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String methodName = method.getName();
    if (methodName.equals("getSuperclass")) {
      return superclass;
    }
    if (methodName.equals("getInterfaces")) {
      return interfaces;
    }
    return method.invoke(original, args);
  }

}
