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

import com.google.common.base.Preconditions;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

/**
 * Node for a simple (unqualified) name.
 */
public class SimpleName extends Name {

  private TypeMirror type;
  private String identifier;

  public SimpleName() {}

  public SimpleName(SimpleName other) {
    super(other);
    type = other.getTypeMirror();
    identifier = other.getIdentifier();
  }

  public SimpleName(Element element, TypeMirror type) {
    super(element);
    this.type = type;
    identifier = element.getSimpleName().toString();
  }

  public SimpleName(Element element) {
    this(element, element.asType());
  }

  public SimpleName(String identifier) {
    super((Element) null);
    this.identifier = identifier;
  }

  @Override
  public Kind getKind() {
    return Kind.SIMPLE_NAME;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return type;
  }

  public SimpleName setTypeMirror(TypeMirror newType) {
    type = newType;
    return this;
  }

  public String getIdentifier() {
    return identifier;
  }

  public SimpleName setIdentifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  @Override
  public String getFullyQualifiedName() {
    return identifier;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }

  @Override
  public SimpleName copy() {
    return new SimpleName(this);
  }

  @Override
  public void validateInner() {
    super.validateInner();
    Preconditions.checkNotNull(identifier);
  }
}
