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
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.jdt.JdtAnnotationBinding;
import com.google.devtools.j2objc.jdt.JdtMethodBinding;
import com.google.devtools.j2objc.jdt.JdtTypeBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;

import java.util.Arrays;
import java.util.List;

/**
 * Binding class for methods created during translation.
 *
 * @author Tom Ball
 */
public class GeneratedMethodBinding extends JdtMethodBinding {

  private final JdtMethodBinding delegate;
  private final String name;
  private int modifiers;
  private final List<ITypeBinding> parameters = Lists.newArrayList();
  private final JdtTypeBinding returnType;
  private final JdtMethodBinding methodDeclaration;
  private JdtTypeBinding declaringClass;
  private final boolean varargs;
  private final boolean isConstructor;

  public GeneratedMethodBinding(
      IMethodBinding delegate, String name, int modifiers, ITypeBinding returnType,
      IMethodBinding methodDeclaration, ITypeBinding declaringClass, boolean isConstructor,
      boolean varargs) {
    super(null);
    this.delegate = BindingConverter.wrapBinding(delegate);
    this.name = Preconditions.checkNotNull(name);
    this.modifiers = modifiers;
    this.returnType = BindingConverter.wrapBinding(returnType);
    this.methodDeclaration = BindingConverter.wrapBinding(methodDeclaration);
    this.declaringClass = BindingConverter.wrapBinding(declaringClass);
    this.isConstructor = isConstructor;
    this.varargs = varargs;
  }

  /**
   * Clone a method binding, so parameters can be added to it.
   */
  public GeneratedMethodBinding(IMethodBinding m) {
    this(null, m.getName(), m.getModifiers(), m.getReturnType(), null, m.getDeclaringClass(),
        m.isConstructor(), m.isVarargs());
    addParameters(m);
  }

  public static GeneratedMethodBinding newNamedMethod(String name, IMethodBinding m) {
    return new GeneratedMethodBinding(null, name, m.getModifiers(),
        m.getReturnType(), null, m.getDeclaringClass(), m.isConstructor(), m.isVarargs());
  }

  public static GeneratedMethodBinding newMethod(
      String name, int modifiers, ITypeBinding returnType, ITypeBinding declaringClass) {
    return new GeneratedMethodBinding(
        null, name, modifiers, returnType, null, declaringClass, false, false);
  }

  public static GeneratedMethodBinding newConstructor(
      ITypeBinding clazz, int modifiers, Types typeEnv) {
    return new GeneratedMethodBinding(
        null, NameTable.INIT_NAME, modifiers, typeEnv.mapTypeName("void"), null, clazz, true,
        false);
  }

  @Override
  public int getKind() {
    return IBinding.METHOD;
  }

  @Override
  public int getModifiers() {
    return modifiers;
  }

  @Override
  public boolean isSynthetic() {
    return BindingUtil.isSynthetic(this);
  }

  @Override
  public String getKey() {
    StringBuilder sb = new StringBuilder("GeneratedMethodBinding:");
    sb.append(declaringClass == null ? "null" : declaringClass.getKey());
    sb.append('.').append(name).append('(');
    for (ITypeBinding paramType : parameters) {
      sb.append(paramType.getKey());
    }
    sb.append(')').append(returnType.getKey());
    return sb.toString();
  }

  @Override
  public boolean isEqualTo(IBinding binding) {
    return equals(binding);
  }

  @Override
  public boolean isConstructor() {
    return isConstructor;
  }

  @Override
  public boolean isDefaultConstructor() {
    return isConstructor && parameters.isEmpty();
  }

  @Override
  public String getName() {
    return name;
  }

  public String getJavaName() {
    return delegate != null ? delegate.getName() : name;
  }

  @Override
  public ITypeBinding getDeclaringClass() {
    return declaringClass;
  }

  public void setDeclaringClass(ITypeBinding newClass) {
    declaringClass = BindingConverter.wrapBinding(newClass);
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

  @Override
  public IAnnotationBinding[] getParameterAnnotations(int paramIndex) {
    return new JdtAnnotationBinding[0];
  }

  @Override
  public ITypeBinding[] getParameterTypes() {
    return parameters.toArray(new ITypeBinding[parameters.size()]);
  }

  public void addParameter(ITypeBinding param) {
    parameters.add(BindingConverter.wrapBinding(param));
  }

  public void addParameter(int index, ITypeBinding param) {
    parameters.add(index, BindingConverter.wrapBinding(param));
  }

  public void addParameters(IMethodBinding method) {
    parameters.addAll(Arrays.asList(BindingConverter.wrapBinding(method).getParameterTypes()));
  }

  public List<ITypeBinding> getParameters() {
    return parameters;
  }

  @Override
  public ITypeBinding getReturnType() {
    return returnType;
  }

  @Override
  public ITypeBinding[] getExceptionTypes() {
    // Obj-C doesn't have declared exceptions
    return new JdtTypeBinding[0];
  }

  @Override
  public ITypeBinding[] getTypeParameters() {
    return new ITypeBinding[0];
  }

  @Override
  public boolean isAnnotationMember() {
    return false;
  }

  @Override
  public boolean isGenericMethod() {
    return false;
  }

  @Override
  public boolean isParameterizedMethod() {
    return false;
  }

  @Override
  public ITypeBinding[] getTypeArguments() {
    return new JdtTypeBinding[0];
  }

  @Override
  public IMethodBinding getMethodDeclaration() {
    return methodDeclaration != null ? methodDeclaration : this;
  }

  @Override
  public boolean isRawMethod() {
    return false;
  }

  @Override
  public boolean isSubsignature(IMethodBinding otherMethod) {
    return delegate != null && delegate.isSubsignature(otherMethod);
  }

  @Override
  public boolean isVarargs() {
    return varargs;
  }

  @Override
  public boolean overrides(IMethodBinding method) {
    return delegate != null && (delegate.equals(method) || delegate.overrides(method));
  }

  public void setModifiers(int modifiers) {
    this.modifiers = modifiers;
  }

  public void addModifiers(int modifiersToAdd) {
    modifiers |= modifiersToAdd;
  }

  public void removeModifiers(int modifiersToRemove) {
    modifiers &= ~modifiersToRemove;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((declaringClass == null) ? 0 : declaringClass.hashCode());
    result = prime * result + modifiers;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
    result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
    result = prime * result + (varargs ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof GeneratedMethodBinding)) {
      return false;
    }
    GeneratedMethodBinding other = (GeneratedMethodBinding) obj;
    return name.equals(other.name)
        && modifiers == other.modifiers
        && varargs == other.varargs
        // The returnType is null for constructors, so test equality first.
        && (returnType == null ? other.returnType == null : returnType.equals(other.returnType))
        && declaringClass.equals(other.declaringClass)
        && parameters.equals(other.parameters);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    ASTNode.printModifiers(modifiers, sb);
    sb.append(returnType != null ? returnType.getName() : "<no type>");
    sb.append(' ');
    sb.append((name != null) ? name : "<no name>");
    sb.append('(');
    ITypeBinding[] params = getParameterTypes();
    for (int i = 0; i < params.length; i++) {
      sb.append(params[i].getName());
      if ((i + 1) < params.length) {
        sb.append(", ");
      }
    }
    sb.append(')');
    return sb.toString();
  }

  public ITypeBinding getDeclaredReceiverType() {
    return null;
  }
}
