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

import org.eclipse.jdt.core.dom.IAnnotationBinding;

/**
 * Base class for annotation nodes.
 */
public abstract class Annotation extends TreeNode {

  private IAnnotationBinding annotationBinding = null;
  protected ChildLink<Name> typeName = ChildLink.create(Name.class, this);

  protected Annotation(org.eclipse.jdt.core.dom.Annotation jdtNode) {
    super(jdtNode);
    annotationBinding = jdtNode.resolveAnnotationBinding();
    typeName.set((Name) TreeConverter.convert(jdtNode.getTypeName()));
  }

  protected Annotation(Annotation other) {
    super(other);
    annotationBinding = other.getAnnotationBinding();
    typeName.copyFrom(other.getTypeName());
  }

  public IAnnotationBinding getAnnotationBinding() {
    return annotationBinding;
  }

  public Name getTypeName() {
    return typeName.get();
  }

  public boolean isSingleMemberAnnotation() {
    return false;
  }
}
