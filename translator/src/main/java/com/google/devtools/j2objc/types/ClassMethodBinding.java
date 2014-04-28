/*
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

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Binding class for methods and constructors defined in external classes.
 *
 * @see ClassTypeBinding
 * @author Tom Ball
 */
public class ClassMethodBinding extends AbstractBinding implements IMethodBinding {

  private final Method method;
  private final Constructor<?> cons;

  public ClassMethodBinding(Method method) {
    assert method != null;
    this.method = method;
    this.cons = null;
  }

  public ClassMethodBinding(Constructor<?> constructor) {
    assert constructor != null;
    this.method = null;
    this.cons = constructor;
  }

  @Override
  public IAnnotationBinding[] getAnnotations() {
    // TODO(tball): implement as needed.
    return new IAnnotationBinding[0];
  }

  @Override
  public int getKind() {
    return METHOD;
  }

  @Override
  public int getModifiers() {
    return method != null ? method.getModifiers() : cons.getModifiers();
  }

  @Override
  public String getKey() {
    // TODO(tball): implement as needed.
    return null;
  }

  @Override
  public boolean isEqualTo(IBinding binding) {
    if (binding instanceof ClassMethodBinding) {
      return equals(binding);
    }
    return false;
  }

  @Override
  public boolean isConstructor() {
    return cons != null;
  }

  @Override
  public boolean isDefaultConstructor() {
    return cons != null && cons.getParameterTypes().length == 0;
  }

  @Override
  public String getName() {
    return method != null ? method.getName() : cons.getName();
  }

  @Override
  public ITypeBinding getDeclaringClass() {
    return ClassTypeBinding.newTypeBinding(
        method != null ? method.getDeclaringClass() : cons.getDeclaringClass());
  }

  @Override
  public Object getDefaultValue() {
    // TODO(tball): implement as needed.
    return null;
  }

  @Override
  public IAnnotationBinding[] getParameterAnnotations(int paramIndex) {
    // TODO(tball): implement as needed.
    return null;
  }

  @Override
  public ITypeBinding[] getParameterTypes() {
    Class<?>[] params = method != null ? method.getParameterTypes() : cons.getParameterTypes();
    ITypeBinding[] result = new ITypeBinding[params.length];
    for (int i = 0; i < params.length; i++) {
      result[i] = ClassTypeBinding.newTypeBinding(params[i]);
    }
    return result;
  }

  @Override
  public ITypeBinding getReturnType() {
    return ClassTypeBinding.newTypeBinding(
        method != null ? method.getReturnType() : cons.getDeclaringClass());
  }

  @Override
  public ITypeBinding[] getExceptionTypes() {
    Class<?>[] exceps = method != null ? method.getExceptionTypes() : cons.getExceptionTypes();
    ITypeBinding[] result = new ITypeBinding[exceps.length];
    for (int i = 0; i < exceps.length; i++) {
      result[i] = ClassTypeBinding.newTypeBinding(exceps[i]);
    }
    return result;
  }

  @Override
  public ITypeBinding[] getTypeParameters() {
    // TODO(tball): implement as needed.
    return null;
  }

  @Override
  public boolean isAnnotationMember() {
    // TODO(tball): implement as needed.
    return false;
  }

  @Override
  public boolean isGenericMethod() {
    // TODO(tball): implement as needed.
    return false;
  }

  @Override
  public boolean isParameterizedMethod() {
    // TODO(tball): implement as needed.
    return false;
  }

  @Override
  public ITypeBinding[] getTypeArguments() {
    // TODO(tball): implement as needed.
    return null;
  }

  @Override
  public IMethodBinding getMethodDeclaration() {
    return this;
  }

  @Override
  public boolean isRawMethod() {
    // TODO(tball): implement as needed.
    return false;
  }

  @Override
  public boolean isSubsignature(IMethodBinding otherMethod) {
    // TODO(tball): implement as needed.
    return false;
  }

  @Override
  public boolean isVarargs() {
    return method != null ? method.isVarArgs() : cons.isVarArgs();
  }

  @Override
  public boolean overrides(IMethodBinding method) {
    // TODO(tball): implement as needed.
    return false;
  }

  @Override
  public int hashCode() {
    return method != null ? method.hashCode() : cons.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ClassMethodBinding) {
      return (method != null && method.equals(((ClassMethodBinding) obj).method) ||
          cons.equals(((ClassMethodBinding) obj).cons));
    }
    return false;
  }

  @Override
  public String toString() {
    return method != null ? method.toString() : cons.toString();
  }
}
