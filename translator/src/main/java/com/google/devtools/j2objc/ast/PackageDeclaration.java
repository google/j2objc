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
import java.util.List;
import javax.lang.model.element.PackageElement;

/**
 * Tree node for a package declaration.
 */
public class PackageDeclaration extends TreeNode {

  private PackageElement packageElement = null;
  private ChildLink<Javadoc> javadoc = ChildLink.create(Javadoc.class, this);
  private ChildList<Annotation> annotations = ChildList.create(Annotation.class, this);
  private ChildLink<Name> name = ChildLink.create(Name.class, this);

  public PackageDeclaration(PackageDeclaration other) {
    super(other);
    packageElement = other.getPackageElement();
    javadoc.copyFrom(other.getJavadoc());
    annotations.copyFrom(other.getAnnotations());
    name.copyFrom(other.getName());
  }

  // An unmodified instance represents the default package.
  public PackageDeclaration() {
    name.set(new SimpleName(""));
  }

  @Override
  public Kind getKind() {
    return Kind.PACKAGE_DECLARATION;
  }

  public PackageElement getPackageElement() {
    return packageElement;
  }

  public PackageDeclaration setPackageElement(PackageElement newElement) {
    packageElement = newElement;
    return this;
  }

  public Name getName() {
    return name.get();
  }

  public PackageDeclaration setName(Name newName) {
    name.set(newName);
    return this;
  }

  public Javadoc getJavadoc() {
    return javadoc.get();
  }

  public PackageDeclaration setJavadoc(Javadoc doc) {
    javadoc.set(doc);
    return this;
  }

  public List<Annotation> getAnnotations() {
    return annotations;
  }

  public PackageDeclaration addAnnotation(Annotation a) {
    annotations.add(a);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      javadoc.accept(visitor);
      annotations.accept(visitor);
      name.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public PackageDeclaration copy() {
    return new PackageDeclaration(this);
  }

  @Override
  public void validateInner() {
    super.validateInner();
    Preconditions.checkNotNull(name);
    Preconditions.checkNotNull(packageElement);
  }

  public boolean isDefaultPackage() {
    return name.get().getFullyQualifiedName().isEmpty();
  }
}
