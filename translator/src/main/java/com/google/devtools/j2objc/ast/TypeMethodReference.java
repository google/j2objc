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

/**
 * Type method reference expression AST node type (added in JLS8, section 15.13).
 */
public class TypeMethodReference extends MethodReference {
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  private ChildLink<Type> type = ChildLink.create(Type.class, this);

  public TypeMethodReference(org.eclipse.jdt.core.dom.TypeMethodReference jdtNode) {
    super(jdtNode);
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
    type.set((Type) TreeConverter.convert(jdtNode.getType()));
  }

  public TypeMethodReference(TypeMethodReference other) {
    super(other);
    name.set(other.getName());
    type.set(other.getType());
  }

  public SimpleName getName() {
    return name.get();
  }

  public Type getType() {
    return type.get();
  }

  @Override
  public TypeMethodReference copy() {
    return new TypeMethodReference(this);
  }

  @Override
  public Kind getKind() {
    return Kind.TYPE_METHOD_REFERENCE;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }
}
