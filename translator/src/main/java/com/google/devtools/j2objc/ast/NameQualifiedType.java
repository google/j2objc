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

import javax.lang.model.type.TypeMirror;

/**
 * Node for a name-qualified type (added in JLS8, section 6.5.5.2).
 */
public class NameQualifiedType extends AnnotatableType {

  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  private ChildLink<Name> qualifier = ChildLink.create(Name.class, this);

  public NameQualifiedType(TypeMirror typeMirror) {
    super(typeMirror);
  }

  public NameQualifiedType(NameQualifiedType other) {
    super(other);
    name.copyFrom(other.getName());
    qualifier.copyFrom(other.getQualifier());
  }

  @Override
  public Kind getKind() {
    return Kind.NAME_QUALIFIED_TYPE;
  }

  public SimpleName getName() {
    return name.get();
  }
  
  public NameQualifiedType setName(SimpleName name) {
    this.name.set(name);
    return this;
  }

  public Name getQualifier() {
    return qualifier.get();
  }
  
  public NameQualifiedType setQualifier(Name qualifier) {
    this.qualifier.set(qualifier);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      name.accept(visitor);
      annotations.accept(visitor);
      qualifier.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public NameQualifiedType copy() {
    return new NameQualifiedType(this);
  }
}
