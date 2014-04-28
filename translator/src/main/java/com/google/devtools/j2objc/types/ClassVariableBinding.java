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
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.lang.reflect.Field;

/**
 * Binding class for variables defined in external classes.
 *
 * @see ClassTypeBinding
 * @author Tom Ball
 */
public class ClassVariableBinding extends AbstractBinding implements IVariableBinding {
  private final Field field;

  public ClassVariableBinding(Field field) {
    this.field = field;
  }

  @Override
  public IAnnotationBinding[] getAnnotations() {
    // TODO(tball): implement as needed.
    return null;
  }

  @Override
  public int getKind() {
    return VARIABLE;
  }

  @Override
  public int getModifiers() {
    return field.getModifiers();
  }

  @Override
  public String getKey() {
    // TODO(tball): implement as needed.
    return null;
  }

  @Override
  public boolean isEqualTo(IBinding binding) {
    return equals(binding);
  }

  @Override
  public boolean isField() {
    return !isEnumConstant();
  }

  @Override
  public boolean isEnumConstant() {
    return field.isEnumConstant();
  }

  @Override
  public boolean isParameter() {
    return false;
  }

  @Override
  public String getName() {
    return field.getName();
  }

  @Override
  public ITypeBinding getDeclaringClass() {
    return ClassTypeBinding.newTypeBinding(field.getDeclaringClass());
  }

  @Override
  public ITypeBinding getType() {
    return ClassTypeBinding.newTypeBinding(field.getType());
  }

  @Override
  public int getVariableId() {
    return 0;
  }

  @Override
  public Object getConstantValue() {
    return null;
  }

  @Override
  public IMethodBinding getDeclaringMethod() {
    return null;
  }

  @Override
  public IVariableBinding getVariableDeclaration() {
    return this;
  }

  @Override
  public int hashCode() {
    return field.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != ClassVariableBinding.class) {
      return false;
    }
    return field.equals(((ClassVariableBinding) obj).field);
  }
}
