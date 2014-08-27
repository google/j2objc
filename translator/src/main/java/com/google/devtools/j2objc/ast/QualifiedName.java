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

import org.eclipse.jdt.core.dom.IBinding;

/**
 * Node for a qualified name. Defined recursively as a simple name preceded by a name.
 */
public class QualifiedName extends Name {

  private ChildLink<Name> qualifier = ChildLink.create(Name.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);

  public QualifiedName(org.eclipse.jdt.core.dom.QualifiedName jdtNode) {
    super(jdtNode);
    qualifier.set((Name) TreeConverter.convert(jdtNode.getQualifier()));
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
  }

  public QualifiedName(QualifiedName other) {
    super(other);
    qualifier.copyFrom(other.getQualifier());
    name.copyFrom(other.getName());
  }

  public QualifiedName(IBinding binding, Name qualifier) {
    super(binding);
    this.qualifier.set(qualifier);
    name.set(new SimpleName(binding));
  }

  @Override
  public Kind getKind() {
    return Kind.QUALIFIED_NAME;
  }

  public Name getQualifier() {
    return qualifier.get();
  }

  public SimpleName getName() {
    return name.get();
  }

  @Override
  public boolean isQualifiedName() {
    return true;
  }

  public String getFullyQualifiedName() {
    return qualifier.get().getFullyQualifiedName() + "." + name.get().getIdentifier();
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      qualifier.accept(visitor);
      name.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public QualifiedName copy() {
    return new QualifiedName(this);
  }
}
