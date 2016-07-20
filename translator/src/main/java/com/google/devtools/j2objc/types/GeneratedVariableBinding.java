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

import com.google.common.base.Preconditions;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.jdt.JdtMethodBinding;
import com.google.devtools.j2objc.jdt.JdtTypeBinding;
import com.google.devtools.j2objc.jdt.JdtVariableBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;

import javax.annotation.Nullable;
import javax.lang.model.type.TypeMirror;

/**
 * Binding class for variables and parameters created during translation.
 *
 * @author Tom Ball
 */
public class GeneratedVariableBinding extends JdtVariableBinding {
  private final String name;
  private final int modifiers;
  private final JdtTypeBinding type;
  private JdtTypeBinding declaringClass;
  private JdtMethodBinding declaringMethod;  // optional
  private final boolean isParameter;
  private final boolean isField;
  private String typeQualifiers;

  public static final String PLACEHOLDER_NAME = "<placeholder-variable>";

  public GeneratedVariableBinding(String name, int modifiers, ITypeBinding type,
      boolean isField, boolean isParameter, @Nullable ITypeBinding declaringClass,
      @Nullable IMethodBinding declaringMethod) {
    super(null);
    Preconditions.checkNotNull(name);
    this.name = name;
    this.modifiers = modifiers;
    this.type = BindingConverter.wrapBinding(type);
    this.isParameter = isParameter;
    this.declaringClass = BindingConverter.wrapBinding(declaringClass);
    this.declaringMethod = BindingConverter.wrapBinding(declaringMethod);
    this.isField = isField;
  }

  public GeneratedVariableBinding(String name, int modifiers, TypeMirror type,
      boolean isField, boolean isParameter, @Nullable ITypeBinding declaringClass,
      @Nullable IMethodBinding declaringMethod) {
    this(name, modifiers, BindingConverter.unwrapTypeMirrorIntoTypeBinding(type), isField,
        isParameter, declaringClass, declaringMethod);
  }

  /**
   * For creating a mutable copy of an existing variable binding.
   */
  public GeneratedVariableBinding(IVariableBinding oldBinding) {
    this(oldBinding.getName(), oldBinding.getModifiers(), oldBinding.getType(),
        oldBinding.isField(), oldBinding.isParameter(), oldBinding.getDeclaringClass(),
        oldBinding.getDeclaringMethod());
  }

  public static GeneratedVariableBinding newPlaceholder() {
    return new GeneratedVariableBinding(PLACEHOLDER_NAME, 0, (TypeMirror) null,
        false, false, null, null);
  }

  public static boolean isPlaceholder(IVariableBinding var) {
    return var.getName().equals(PLACEHOLDER_NAME);
  }

  /**
   * Sets the qualifiers that should be added to the variable declaration. Use
   * an asterisk ('*') to delimit qualifiers that should apply to a pointer from
   * qualifiers that should apply to the pointee type. For example setting the
   * qualifier as "__strong * const" on a string array will result in a
   * declaration of "NSString * __strong * const".
   */
  public void setTypeQualifiers(String qualifiers) {
    typeQualifiers = qualifiers;
  }

  public String getTypeQualifiers() {
    return typeQualifiers;
  }

  @Override
  public int getKind() {
    return IBinding.VARIABLE;
  }

  @Override
  public int getModifiers() {
    return modifiers;
  }

  @Override
  public String getKey() {
    throw new AssertionError("not implemented");
  }

  @Override
  public boolean isEqualTo(IBinding binding) {
    return equals(binding);
  }

  @Override
  public boolean isField() {
    return isField;
  }

  @Override
  public boolean isEnumConstant() {
    return false;
  }

  @Override
  public boolean isParameter() {
    return isParameter;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public JdtTypeBinding getDeclaringClass() {
    return declaringClass;
  }

  public void setDeclaringClass(ITypeBinding newBinding) {
    declaringClass = BindingConverter.wrapBinding(newBinding);
  }

  @Override
  public JdtTypeBinding getType() {
    return type;
  }

  @Override
  public int getVariableId() {
    throw new AssertionError("not implemented");
  }

  @Override
  public Object getConstantValue() {
    return null;
  }

  @Override
  public JdtMethodBinding getDeclaringMethod() {
    return declaringMethod;
  }

  @Override
  public JdtVariableBinding getVariableDeclaration() {
    return this;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    ASTNode.printModifiers(modifiers, sb);
    sb.append(type != null ? type.getName() : "<no type>");
    sb.append(" ");
    sb.append((name != null) ? name : "<no name>");
    return sb.toString();
  }

  public boolean isEffectivelyFinal() {
    return false;
  }
}
