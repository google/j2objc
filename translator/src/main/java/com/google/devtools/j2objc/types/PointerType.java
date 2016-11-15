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

import com.google.common.base.Preconditions;
import com.google.devtools.j2objc.jdt.BindingConverter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * TypeMirror implementation for a C-style pointer type.
 *
 * @author Keith Stanger
 */
public class PointerType implements TypeMirror {

  private final TypeMirror pointeeType;
  private final ITypeBinding binding;

  public PointerType(TypeMirror pointeeType) {
    this.pointeeType = Preconditions.checkNotNull(pointeeType);
    binding = new Binding(BindingConverter.unwrapTypeMirrorIntoTypeBinding(pointeeType));
  }

  public TypeMirror getPointeeType() {
    return pointeeType;
  }

  @Override
  public String toString() {
    return pointeeType.toString() + "*";
  }

  @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    return Collections.emptyList();
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return null;
  }

  @Override
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

  public ITypeBinding asBinding() {
    return binding;
  }

  /**
   * An associated ITypeBinding implementation.
   */
  public class Binding extends AbstractTypeBinding {

    private final ITypeBinding pointeeType;

    public Binding(ITypeBinding pointeeType) {
      this.pointeeType = Preconditions.checkNotNull(pointeeType);
    }

    public TypeMirror asTypeMirror() {
      return PointerType.this;
    }

    public ITypeBinding getPointeeType() {
      return pointeeType;
    }

    @Override
    public boolean isInterface() {
      return false;
    }

    @Override
    public boolean isClass() {
      return false;
    }

    @Override
    public boolean isArray() {
      return false;
    }

    @Override
    public String getKey() {
      return pointeeType.getKey() + "*";
    }

    @Override
    public String getName() {
      return pointeeType.getName() + "_p";
    }

    @Override
    public String getQualifiedName() {
      return pointeeType.getQualifiedName() + "*";
    }

    @Override
    public boolean isEqualTo(IBinding binding) {
      if (binding == this) {
        return true;
      }
      if (!(binding instanceof Binding)) {
        return false;
      }
      return pointeeType.isEqualTo(((Binding) binding).pointeeType);
    }

    @Override
    public boolean isAssignmentCompatible(ITypeBinding variableType) {
      if (!(variableType instanceof Binding)) {
        return false;
      }
      return pointeeType.isAssignmentCompatible(((Binding) variableType).pointeeType);
    }
  }
}
