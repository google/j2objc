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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import java.util.List;

/**
 * Node for a javadoc-style comment.
 */
public class Javadoc extends Comment {

  public enum OwnerType {
    FIELD, METHOD, PACKAGE, TYPE, OTHER
  }

  private ChildList<TagElement> tags = ChildList.create(TagElement.class, this);
  private final OwnerType ownerType;

  public Javadoc(org.eclipse.jdt.core.dom.Javadoc jdtNode) {
    super(jdtNode);
    for (Object tag : jdtNode.tags()) {
      tags.add((TagElement) TreeConverter.convert(tag));
    }
    ownerType = ownerType(jdtNode);
  }

  public Javadoc(Javadoc other) {
    super(other);
    tags.copyFrom(other.getTags());
    this.ownerType = other.ownerType;
  }

  @Override
  public Kind getKind() {
    return Kind.JAVADOC;
  }

  public List<TagElement> getTags() {
    return tags;
  }

  @Override
  public boolean isDocComment() {
    return true;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      tags.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public Javadoc copy() {
    return new Javadoc(this);
  }

  public OwnerType getOwnerType() {
    return ownerType;
  }

  private OwnerType ownerType(org.eclipse.jdt.core.dom.Javadoc jdtNode) {
    ASTNode parentNode = jdtNode.getParent();
    if (parentNode instanceof PackageDeclaration) {
      return OwnerType.PACKAGE;
    } else if (parentNode instanceof AbstractTypeDeclaration) {
      return OwnerType.TYPE;
    } else if (parentNode instanceof MethodDeclaration) {
      return OwnerType.METHOD;
    } else if (parentNode instanceof FieldDeclaration) {
      return OwnerType.FIELD;
    }

    // Other includes AnnotationTypeMemberDeclaration, and any future
    // Javadoc types that aren't supported.
    return OwnerType.OTHER;
  }
}
