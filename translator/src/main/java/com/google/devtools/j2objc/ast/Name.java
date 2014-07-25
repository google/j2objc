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

import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.BindingUtil;

import org.eclipse.jdt.core.dom.IBinding;

/**
 * Base node class for a name.
 */
public abstract class Name extends Expression {

  private IBinding binding;

  public Name(org.eclipse.jdt.core.dom.Name jdtNode) {
    super(jdtNode);
    binding = Types.getBindingUnsafe(jdtNode);
  }

  public Name(Name other) {
    super(other);
    binding = other.getBinding();
  }

  public Name(IBinding binding) {
    super(BindingUtil.toTypeBinding(binding));
    this.binding = binding;
  }

  public abstract String getFullyQualifiedName();

  public IBinding getBinding() {
    return binding;
  }

  public boolean isQualifiedName() {
    return false;
  }

  @Override
  public abstract Name copy();
}
