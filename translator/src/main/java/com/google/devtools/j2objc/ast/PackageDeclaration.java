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

import org.eclipse.jdt.core.dom.IPackageBinding;

import java.util.List;

/**
 * Tree node for a package declaration.
 */
public class PackageDeclaration extends TreeNode {

  private IPackageBinding packageBinding = null;
  private ChildLink<Javadoc> javadoc = ChildLink.create(Javadoc.class, this);
  private ChildList<Annotation> annotations = ChildList.create(Annotation.class, this);
  private ChildLink<Name> name = ChildLink.create(Name.class, this);

  public PackageDeclaration(org.eclipse.jdt.core.dom.PackageDeclaration jdtNode) {
    super(jdtNode);
    packageBinding = jdtNode.resolveBinding();
    javadoc.set((Javadoc) TreeConverter.convert(jdtNode.getJavadoc()));
    for (Object modifier : jdtNode.annotations()) {
      annotations.add((Annotation) TreeConverter.convert(modifier));
    }
    name.set((Name) TreeConverter.convert(jdtNode.getName()));
  }

  public PackageDeclaration(PackageDeclaration other) {
    super(other);
    packageBinding = other.getPackageBinding();
    javadoc.copyFrom(other.getJavadoc());
    annotations.copyFrom(other.getAnnotations());
    name.copyFrom(other.getName());
  }

  // Used by CompilationUnit to represent the default package.
  PackageDeclaration() {
    name.set(new SimpleName(""));
  }

  @Override
  public Kind getKind() {
    return Kind.PACKAGE_DECLARATION;
  }

  public IPackageBinding getPackageBinding() {
    return packageBinding;
  }

  public Name getName() {
    return name.get();
  }

  public void setName(Name newName) {
    name.set(newName);
  }

  public Javadoc getJavadoc() {
    return javadoc.get();
  }

  public List<Annotation> getAnnotations() {
    return annotations;
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
  }

  public boolean isDefaultPackage() {
    return name.get().getFullyQualifiedName().isEmpty();
  }
}
