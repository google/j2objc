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

import com.google.common.collect.Lists;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Binding class for types backed by external classes. This is used to
 * introduce references to new types that were not referenced in the
 * original compilation.
 *
 * @author Tom Ball
 */
public class ClassTypeBinding extends AbstractTypeBinding {

  private final Class<?> clazz;

  public static ITypeBinding newTypeBinding(Class<?> clazz) {
    ITypeBinding knownBinding = Types.mapTypeName(clazz.getName());
    return knownBinding != null ? knownBinding : Types.mapType(new ClassTypeBinding(clazz));
  }

  private ClassTypeBinding(Class<?> clazz) {
    this.clazz = clazz;
  }

  @Override
  public String getBinaryName() {
    String pkg = clazz.getPackage().getName().replace('.', '/');
    String cls = clazz.getSimpleName();
    Class<?> outer = clazz.getEnclosingClass();
    while (outer != null) {
      cls = outer.getSimpleName() + '$' + cls;
      outer = outer.getEnclosingClass();
    }
    return pkg + '/' + cls + ';';
  }

  @Override
  public ITypeBinding getComponentType() {
    if (clazz.isArray()) {
      return new ClassTypeBinding(clazz.getComponentType());
    }
    return null;
  }

  @Override
  public IVariableBinding[] getDeclaredFields() {
    Field[] fields = clazz.getDeclaredFields();
    IVariableBinding[] result = new IVariableBinding[fields.length];
    for (int i = 0; i < fields.length; i++) {
      result[i] = new ClassVariableBinding(fields[i]);
    }
    return result;
  }

  @Override
  public IMethodBinding[] getDeclaredMethods() {
    List<IMethodBinding> result = Lists.newArrayList();
    for (Method m : clazz.getDeclaredMethods()) {
      result.add(new ClassMethodBinding(m));
    }
    for (Constructor<?> c : clazz.getDeclaredConstructors()) {
      result.add(new ClassMethodBinding(c));
    }
    return result.toArray(new IMethodBinding[result.size()]);
  }

  @Override
  public IPackageBinding getPackage() {
    return new GeneratedPackageBinding(clazz.getPackage().getName());
  }

  @Override
  public int getModifiers() {
    return clazz.getModifiers();
  }

  @Override
  public String getName() {
    return clazz.getSimpleName();
  }

  @Override
  public String getQualifiedName() {
    return clazz.getName();
  }

  @Override
  public ITypeBinding getSuperclass() {
    Class<?> supercls = clazz.getSuperclass();
    return supercls != null ? newTypeBinding(supercls) : null;
  }

  @Override
  public boolean isAnnotation() {
    return clazz.isAnnotation();
  }

  @Override
  public boolean isArray() {
    return clazz.isArray();
  }

  @Override
  public boolean isAssignmentCompatible(ITypeBinding variableType) {
    if (variableType instanceof ClassTypeBinding) {
      return clazz.isAssignableFrom(((ClassTypeBinding) variableType).clazz);
    }
    // TODO(tball): figure out how to compare classes and JDT types.
    return isEqualTo(variableType);
  }

  @Override
  public boolean isClass() {
    return !isAnnotation() && !isArray() && !isEnum() && !isInterface();
  }

  @Override
  public boolean isEnum() {
    return clazz.isEnum();
  }

  @Override
  public boolean isInterface() {
    return clazz.isInterface();
  }

  @Override
  public String getKey() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isEqualTo(IBinding binding) {
    if (binding instanceof ClassTypeBinding) {
      return clazz.equals(((ClassTypeBinding) binding).clazz);
    } else if (binding instanceof ITypeBinding) {
      return clazz.getName().equals(((ITypeBinding) binding).getQualifiedName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return clazz.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != ClassTypeBinding.class) {
      return false;
    }
    return clazz.equals(((ClassTypeBinding) obj).clazz);
  }

  @Override
  public String toString() {
    return clazz.toString();
  }
}
