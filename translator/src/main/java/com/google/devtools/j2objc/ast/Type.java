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

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Base class for all type nodes.
 */
public abstract class Type extends TreeNode {

  private ITypeBinding typeBinding;

  public Type(org.eclipse.jdt.core.dom.Type jdtNode) {
    super(jdtNode);
    typeBinding = Types.getTypeBinding(jdtNode);
  }

  public Type(Type other) {
    super(other);
  }

  public ITypeBinding getTypeBinding() {
    return typeBinding;
  }

  @Override
  public abstract Type copy();
}
