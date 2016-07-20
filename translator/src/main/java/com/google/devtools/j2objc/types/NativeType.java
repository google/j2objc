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

package com.google.devtools.j2objc.types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

/**
 * TypeMirror for native C types.
 *
 * @author Nathan Braswell
 */
public class NativeType implements TypeMirror {

  private final String name;

  public NativeType(String name) {
    this.name = name;
  }

  // TODO(user): enable this Override and the ones below when Java 8 is minimum version.
  // @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    return Collections.emptyList();
  }

  @Override
  public String toString() {
    return name;
  }

  //@Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return null;
  }

  //@Override
  @SuppressWarnings("unchecked")
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    return (A[]) Array.newInstance(annotationType, 0);
  }

  @Override
  public TypeKind getKind() {
    return TypeKind.OTHER;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitUnknown(this, p);
  }
}
