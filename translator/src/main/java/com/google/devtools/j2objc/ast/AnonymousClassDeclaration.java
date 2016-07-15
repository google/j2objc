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

import com.google.devtools.j2objc.jdt.BindingConverter;
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Anonymous class declaration node. Must be the child of a
 * ClassInstanceCreation node.
 */
public final class AnonymousClassDeclaration extends TreeNode {

  private TypeElement element = null;
  private final ChildList<BodyDeclaration> bodyDeclarations =
      ChildList.create(BodyDeclaration.class, this);

  public AnonymousClassDeclaration() {}

  public AnonymousClassDeclaration(AnonymousClassDeclaration other) {
    super(other);
    element = other.getElement();
    bodyDeclarations.copyFrom(other.getBodyDeclarations());
  }

  @Override
  public Kind getKind() {
    return Kind.ANONYMOUS_CLASS_DECLARATION;
  }

  // TODO(tball): remove when all translators are converted.
  public ITypeBinding getTypeBinding() {
    return (ITypeBinding) BindingConverter.unwrapElement(element);
  }

  public TypeElement getElement() {
    return element;
  }

  public AnonymousClassDeclaration setElement(TypeElement newElement) {
    element = newElement;
    return this;
  }

  public List<BodyDeclaration> getBodyDeclarations() {
    return bodyDeclarations;
  }

  public AnonymousClassDeclaration setBodyDeclarations(List<BodyDeclaration> newBodyDeclarations) {
    bodyDeclarations.replaceAll(newBodyDeclarations);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      bodyDeclarations.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public AnonymousClassDeclaration copy() {
    return new AnonymousClassDeclaration(this);
  }

  public AnonymousClassDeclaration addBodyDeclaration(BodyDeclaration decl) {
    bodyDeclarations.add(decl);
    return this;
  }
}
