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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;

/**
 * Base class for annotation nodes.
 */
public abstract class Annotation extends Expression {

  private AnnotationMirror annotationMirror = null;
  protected ChildLink<Name> typeName = ChildLink.create(Name.class, this);

  protected Annotation() {}

  protected Annotation(Annotation other) {
    super(other);
    annotationMirror = other.getAnnotationMirror();
    typeName.copyFrom(other.getTypeName());
  }

  public AnnotationMirror getAnnotationMirror() {
    return annotationMirror;
  }

  public Annotation setAnnotationMirror(AnnotationMirror mirror) {
    annotationMirror = mirror;
    return this;
  }

  public Name getTypeName() {
    return typeName.get();
  }

  public Annotation setTypeName(Name newName) {
    typeName.set(newName);
    return this;
  }

  public boolean isSingleMemberAnnotation() {
    return false;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return annotationMirror.getAnnotationType();
  }
}
