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

package com.google.devtools.j2objc.jdt;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * When transitioned to Java 8, this should implement IntersectionType,
 * getKind() should return TypeKind.INTERSECTION, accept should visitIntersection,
 * and this should implement getBounds().
 */
public class JdtIntersectionType extends JdtTypeMirror implements IntersectionType {

  private final List<? extends TypeMirror> bounds;

  JdtIntersectionType(ITypeBinding binding, List<? extends TypeMirror> bounds) {
    super(binding);
    this.bounds = bounds;
  }

  static JdtIntersectionType fromJdtIntersection(ITypeBinding t) {
    List<TypeMirror> bounds = new ArrayList<>();
    ITypeBinding superclass = t.getSuperclass();
    if (superclass != null) {
      bounds.add(BindingConverter.getType(superclass));
    }
    for (ITypeBinding intrface : t.getInterfaces()) {
      bounds.add(BindingConverter.getType(intrface));
    }
    return new JdtIntersectionType(t, bounds);
  }

  @Override
  public TypeKind getKind() {
    return TypeKind.INTERSECTION;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitIntersection(this, p);
  }

  @Override
  public List<? extends TypeMirror> getBounds() {
    return bounds;
  }

  @Override
  public String toString() {
    return Joiner.on('&').join(bounds);
  }
}
