/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * IOSMethodBinding: synthetic binding for an iOS method.
 *
 * @author Tom Ball
 */
public class IOSMethodBinding extends GeneratedMethodBinding {

  private final String selector;

  private IOSMethodBinding(
      String selector, IMethodBinding original, int modifiers, ITypeBinding returnType,
      IMethodBinding methodDeclaration, ITypeBinding declaringClass, boolean varargs) {
    super(original, selector, modifiers, returnType, methodDeclaration, declaringClass, false,
          varargs);
    this.selector = selector;
  }

  public static IOSMethodBinding newMappedMethod(String selector, IMethodBinding original) {
    ITypeBinding returnType =
        original.isConstructor() ? original.getDeclaringClass() : original.getReturnType();
    IOSMethodBinding binding = new IOSMethodBinding(
        selector, original, original.getModifiers(), returnType, null, original.getDeclaringClass(),
        original.isVarargs());
    binding.addParameters(original);
    return binding;
  }

  public static IOSMethodBinding newMethod(
      String selector, int modifiers, ITypeBinding returnType, ITypeBinding declaringClass) {
    return new IOSMethodBinding(selector, null, modifiers, returnType, null, declaringClass, false);
  }

  public static IOSMethodBinding newTypedInvocation(IOSMethodBinding m, ITypeBinding returnType) {
    IOSMethodBinding binding = new IOSMethodBinding(
        m.getSelector(), null, m.getModifiers(), returnType, m, m.getDeclaringClass(),
        m.isVarargs());
    binding.addParameters(m);
    return binding;
  }

  public String getSelector() {
    return selector;
  }
}
