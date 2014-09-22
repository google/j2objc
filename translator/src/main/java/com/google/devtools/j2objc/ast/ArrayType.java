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

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Array type node. Array types are expressed recursively, one dimension at a
 * time.
 */
public class ArrayType extends Type {

  private ChildLink<Type> componentType = ChildLink.create(Type.class, this);

  public ArrayType(org.eclipse.jdt.core.dom.ArrayType jdtNode) {
    super(jdtNode);
    componentType.set((Type) TreeConverter.convert(jdtNode.getComponentType()));
  }

  public ArrayType(ArrayType other) {
    super(other);
    componentType.copyFrom(other.getComponentType());
  }

  public ArrayType(ITypeBinding typeBinding) {
    super(typeBinding);
    assert typeBinding.isArray();
    componentType.set(Type.newType(typeBinding.getComponentType()));
  }

  @Override
  public Kind getKind() {
    return Kind.ARRAY_TYPE;
  }

  public Type getComponentType() {
    return componentType.get();
  }

  public void setComponentType(Type newComponentType) {
    componentType.set(newComponentType);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      componentType.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public ArrayType copy() {
    return new ArrayType(this);
  }
}
