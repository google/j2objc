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
import com.google.devtools.j2objc.jdt.BindingConverter;
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Superclass node for classes, enums and annotation declarations.
 */
public abstract class AbstractTypeDeclaration extends BodyDeclaration
    implements CommonTypeDeclaration {

  private TypeElement typeElement = null;
  protected final ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  protected final ChildList<BodyDeclaration> bodyDeclarations =
      ChildList.create(BodyDeclaration.class, this);
  protected final ChildList<Statement> classInitStatements =
      ChildList.create(Statement.class, this);

  AbstractTypeDeclaration() {}

  public AbstractTypeDeclaration(AbstractTypeDeclaration other) {
    super(other);
    typeElement = other.getTypeElement();
    name.copyFrom(other.getName());
    bodyDeclarations.copyFrom(other.getBodyDeclarations());
  }

  // TODO(tball): remove when all subclasses are converted.
  public AbstractTypeDeclaration(ITypeBinding typeBinding) {
    super(typeBinding);
    this.typeElement = BindingConverter.getTypeElement(typeBinding);
    name.set(new SimpleName(typeElement));
  }

  public AbstractTypeDeclaration(TypeElement typeElement) {
    super();
    this.typeElement = typeElement;
    name.set(new SimpleName(typeElement));
  }

  // TODO(tball): remove when all subclasses are converted.
  public ITypeBinding getTypeBinding() {
    return BindingConverter.unwrapTypeElement(typeElement);
  }

  // TODO(tball): remove when all subclasses are converted.
  public void setTypeBinding(ITypeBinding typeBinding) {
    this.typeElement = BindingConverter.getTypeElement(typeBinding);
  }

  @Override
  public TypeElement getTypeElement() {
    return typeElement;
  }

  public AbstractTypeDeclaration setTypeElement(TypeElement newTypeElement) {
    typeElement = newTypeElement;
    return this;
  }

  public SimpleName getName() {
    return name.get();
  }

  public AbstractTypeDeclaration setName(SimpleName newName) {
    name.set(newName);
    return this;
  }

  @Override
  public List<BodyDeclaration> getBodyDeclarations() {
    return bodyDeclarations;
  }

  public BodyDeclaration setBodyDeclarations(List<BodyDeclaration> newDeclarations) {
    bodyDeclarations.replaceAll(newDeclarations);
    return this;
  }

  public List<Statement> getClassInitStatements() {
    return classInitStatements;
  }

  AbstractTypeDeclaration setClassInitStatements(List<Statement> statements) {
    classInitStatements.replaceAll(statements);
    return this;
  }

  @Override
  public void validateInner() {
    super.validateInner();
    Preconditions.checkNotNull(typeElement);
    Preconditions.checkNotNull(name.get());
  }

  public AbstractTypeDeclaration addBodyDeclaration(BodyDeclaration decl) {
    bodyDeclarations.add(decl);
    return this;
  }

  public AbstractTypeDeclaration addBodyDeclaration(int index, BodyDeclaration decl) {
    bodyDeclarations.add(index, decl);
    return this;
  }

  public AbstractTypeDeclaration addClassInitStatement(int index, Statement stmt) {
    classInitStatements.add(index, stmt);
    return this;
  }
}
