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

import java.util.Collections;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Node type for an enum declaration.
 */
public class EnumDeclaration extends AbstractTypeDeclaration {

  // DeadCodeEliminator will set this field if this enum is marked as unused
  private boolean stripSuperInterfaces = false;

  private ChildList<EnumConstantDeclaration> enumConstants =
      ChildList.create(EnumConstantDeclaration.class, this);

  public EnumDeclaration() {}

  public EnumDeclaration(EnumDeclaration other) {
    super(other);
    stripSuperInterfaces = other.stripSuperInterfaces;
    enumConstants.copyFrom(other.getEnumConstants());
  }

  public EnumDeclaration(TypeElement typeElement) {
    super(typeElement);
  }

  @Override
  public Kind getKind() {
    return Kind.ENUM_DECLARATION;
  }

  public List<? extends TypeMirror> getSuperInterfaceTypeMirrors() {
    return stripSuperInterfaces ? Collections.emptyList() : getTypeElement().getInterfaces();
  }

  public void stripSuperInterfaces() {
    stripSuperInterfaces = true;
  }

  public List<EnumConstantDeclaration> getEnumConstants() {
    return enumConstants;
  }

  public EnumDeclaration addEnumConstant(EnumConstantDeclaration constant) {
    enumConstants.add(constant);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      javadoc.accept(visitor);
      annotations.accept(visitor);
      name.accept(visitor);
      enumConstants.accept(visitor);
      bodyDeclarations.accept(visitor);
      classInitStatements.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public EnumDeclaration copy() {
    return new EnumDeclaration(this);
  }
}
