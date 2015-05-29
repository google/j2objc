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
 * Super method reference AST node type (added in JLS8, section 15.13).
 */
public class SuperMethodReference extends MethodReference {
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  private ChildLink<Name> qualifier = ChildLink.create(Name.class, this);

  public SuperMethodReference(org.eclipse.jdt.core.dom.SuperMethodReference jdtNode) {
    super(jdtNode);
    typeBinding = jdtNode.resolveTypeBinding();
    for (Object x : jdtNode.typeArguments()) {
      typeArguments.add((Type) TreeConverter.convert(x));
    }
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
    qualifier.set((Name) TreeConverter.convert(jdtNode.getQualifier()));
  }

  public SuperMethodReference(SuperMethodReference other) {
    super(other);
    typeBinding = other.getTypeBinding();
    typeArguments.copyFrom(other.typeArguments());
    name.copyFrom(other.getName());
    qualifier.copyFrom(other.getQualifier());
  }

  public SimpleName getName() {
    return name.get();
  }

  public Name getQualifier() {
    return qualifier.get();
  }

  @Override
  public Expression copy() {
    return new SuperMethodReference(this);
  }

  @Override
  public Kind getKind() {
    return Kind.SUPER_METHOD_REFERENCE;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }
}
