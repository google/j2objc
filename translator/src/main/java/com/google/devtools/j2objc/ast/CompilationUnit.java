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

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.List;

/**
 * Tree node for a Java compilation unit.
 */
public class CompilationUnit extends TreeNode {

  // TODO(kstanger): Eventually remove this.
  private final org.eclipse.jdt.core.dom.CompilationUnit jdtNode;
  private ChildLink<PackageDeclaration> packageDeclaration = ChildLink.create(this);
  private ChildList<Comment> comments = ChildList.create(this);
  private ChildList<AbstractTypeDeclaration> types = ChildList.create(this);

  public CompilationUnit(org.eclipse.jdt.core.dom.CompilationUnit jdtNode) {
    super(jdtNode);
    this.jdtNode = Preconditions.checkNotNull(jdtNode);
    packageDeclaration.set((PackageDeclaration) TreeConverter.convert(jdtNode.getPackage()));
    for (Object comment : jdtNode.getCommentList()) {
      // Comments are not normally parented in the JDT AST. Javadoc nodes are
      // normally parented by the BodyDeclaration they apply do, so here we only
      // keep the unparented comments to avoid duplicate comment nodes.
      ASTNode commentParent = ((ASTNode) comment).getParent();
      if (commentParent == null || commentParent == jdtNode) {
        comments.add((Comment) TreeConverter.convert(comment));
      }
    }
    for (Object type : jdtNode.types()) {
      types.add((AbstractTypeDeclaration) TreeConverter.convert(type));
    }
  }

  public CompilationUnit(CompilationUnit other) {
    super(other);
    this.jdtNode = other.jdtNode;
    packageDeclaration.copyFrom(other.getPackage());
    comments.copyFrom(other.getCommentList());
    types.copyFrom(other.getTypes());
  }

  public org.eclipse.jdt.core.dom.CompilationUnit jdtNode() {
    return jdtNode;
  }

  public PackageDeclaration getPackage() {
    return packageDeclaration.get();
  }

  public void setPackage(PackageDeclaration newPackageDeclaration) {
    packageDeclaration.set(newPackageDeclaration);
  }

  public List<Comment> getCommentList() {
    return comments;
  }

  public List<AbstractTypeDeclaration> getTypes() {
    return types;
  }

  // TODO(kstanger): Find an effective way to lookup the line number for a node.
  // Possibly by adding a line number field to TreeNodee.
  public int getLineNumber(int position) {
    return jdtNode.getLineNumber(position);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      packageDeclaration.accept(visitor);
      comments.accept(visitor);
      types.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public CompilationUnit copy() {
    return new CompilationUnit(this);
  }

  @Override
  public void validate() {
    super.validate();
    Preconditions.checkNotNull(jdtNode);
  }
}
