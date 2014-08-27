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

package com.google.devtools.j2objc.ast;

import com.google.devtools.j2objc.util.BindingUtil;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;

/**
 * Base node class for a name.
 */
public abstract class Name extends Expression {

  private IBinding binding;

  public Name(org.eclipse.jdt.core.dom.Name jdtNode) {
    super(jdtNode);
    binding = jdtNode.resolveBinding();
  }

  public Name(Name other) {
    super(other);
    binding = other.getBinding();
  }

  public Name(IBinding binding) {
    this.binding = binding;
  }

  public static Name newName(Name qualifier, IBinding binding) {
    return qualifier == null ? new SimpleName(binding) : new QualifiedName(binding, qualifier);
  }

  public static Name newName(List<? extends IBinding> path) {
    Name name = null;
    for (IBinding binding : path) {
      name = newName(name, binding);
    }
    return name;
  }

  public abstract String getFullyQualifiedName();

  public IBinding getBinding() {
    return binding;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return BindingUtil.toTypeBinding(binding);
  }

  public void setBinding(IBinding newBinding) {
    binding = newBinding;
  }

  public boolean isQualifiedName() {
    return false;
  }

  @Override
  public abstract Name copy();
}
