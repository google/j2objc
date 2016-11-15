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

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

/**
 * Node for a qualified name. Defined recursively as a simple name preceded by a name.
 */
public class QualifiedName extends Name {

  private ChildLink<Name> qualifier = ChildLink.create(Name.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);

  public QualifiedName() {}

  public QualifiedName(QualifiedName other) {
    super(other);
    qualifier.copyFrom(other.getQualifier());
    name.copyFrom(other.getName());
  }

  public QualifiedName(Element element, TypeMirror type, Name qualifier) {
    super(element);
    this.qualifier.set(qualifier);
    name.set(new SimpleName(element, type));
  }

  @Override
  public Kind getKind() {
    return Kind.QUALIFIED_NAME;
  }

  @Override
  public TypeMirror getTypeMirror() {
    SimpleName nameNode = name.get();
    return nameNode != null ? nameNode.getTypeMirror() : null;
  }

  public Name getQualifier() {
    return qualifier.get();
  }

  public QualifiedName setQualifier(Name newQualifier) {
    qualifier.set(newQualifier);
    return this;
  }

  public SimpleName getName() {
    return name.get();
  }

  public QualifiedName setName(SimpleName newName) {
    name.set(newName);
    return this;
  }

  @Override
  public boolean isQualifiedName() {
    return true;
  }

  @Override
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
