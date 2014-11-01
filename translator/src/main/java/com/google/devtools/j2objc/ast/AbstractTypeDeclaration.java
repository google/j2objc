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

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;

/**
 * Superclass node for classes, enums and annotation declarations.
 */
public abstract class AbstractTypeDeclaration extends BodyDeclaration {

  private ITypeBinding typeBinding = null;
  protected final ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  protected final ChildList<BodyDeclaration> bodyDeclarations =
      ChildList.create(BodyDeclaration.class, this);
  protected final ChildList<Statement> classInitStatements =
      ChildList.create(Statement.class, this);

  public AbstractTypeDeclaration(org.eclipse.jdt.core.dom.AbstractTypeDeclaration jdtNode) {
    super(jdtNode);
    typeBinding = jdtNode.resolveBinding();
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
    for (Object bodyDecl : jdtNode.bodyDeclarations()) {
      bodyDeclarations.add((BodyDeclaration) TreeConverter.convert(bodyDecl));
    }
  }

  public AbstractTypeDeclaration(AbstractTypeDeclaration other) {
    super(other);
    typeBinding = other.getTypeBinding();
    name.copyFrom(other.getName());
    bodyDeclarations.copyFrom(other.getBodyDeclarations());
  }

  public AbstractTypeDeclaration(ITypeBinding typeBinding) {
    super(typeBinding);
    this.typeBinding = typeBinding;
    name.set(new SimpleName(typeBinding));
  }

  public ITypeBinding getTypeBinding() {
    return typeBinding;
  }

  public void setTypeBinding(ITypeBinding typeBinding) {
    this.typeBinding = typeBinding;
  }

  public SimpleName getName() {
    return name.get();
  }

  public void setName(SimpleName newName) {
    name.set(newName);
  }

  public List<BodyDeclaration> getBodyDeclarations() {
    return bodyDeclarations;
  }

  public List<Statement> getClassInitStatements() {
    return classInitStatements;
  }

  @Override
  public void validateInner() {
    super.validateInner();
    Preconditions.checkNotNull(typeBinding);
    Preconditions.checkNotNull(name.get());
  }
}
