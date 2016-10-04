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
import com.google.devtools.j2objc.util.ElementUtil;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Element class for types created during translation.
 *
 * @author Keith Stanger
 */
public class GeneratedTypeElement extends GeneratedElement implements TypeElement {

  private final TypeMirror superclass;
  private List<TypeMirror> interfaces = new ArrayList<>();
  private final NestingKind nestingKind;
  private final ITypeBinding binding = new Binding();

  public GeneratedTypeElement(
      String name, ElementKind kind, Element enclosingElement, TypeMirror superclass,
      NestingKind nestingKind) {
    super(Preconditions.checkNotNull(name), checkElementKind(kind), enclosingElement);
    this.superclass = superclass;
    this.nestingKind = nestingKind;
  }

  private static ElementKind checkElementKind(ElementKind kind) {
    Preconditions.checkArgument(kind.isClass() || kind.isInterface());
    return kind;
  }

  @Override
  public TypeMirror asType() {
    return new Mirror();
  }

  @Override
  public Name getQualifiedName() {
    return getSimpleName();
  }

  @Override
  public TypeMirror getSuperclass() {
    return superclass;
  }

  @Override
  public NestingKind getNestingKind() {
    return nestingKind;
  }

  @Override
  public List<? extends TypeMirror> getInterfaces() {
    return interfaces;
  }

  public void addInterface(TypeMirror t) {
    interfaces.add(t);
  }

  public void addInterfaces(Collection<? extends TypeMirror> types) {
    interfaces.addAll(types);
  }

  @Override
  public List<? extends TypeParameterElement> getTypeParameters() {
    return Collections.emptyList();
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitType(this, p);
  }

  public ITypeBinding asTypeBinding() {
    return binding;
  }

  /**
   * The associated TypeMirror.
   * TODO(kstanger): Make private when BindingConverter is removed.
   */
  public class Mirror implements DeclaredType {

    public ITypeBinding asTypeBinding() {
      return GeneratedTypeElement.this.asTypeBinding();
    }

    @Override
    public TypeKind getKind() {
      return TypeKind.DECLARED;
    }

    @Override
    public Element asElement() {
      return GeneratedTypeElement.this;
    }

    @Override
    public TypeMirror getEnclosingType() {
      Element enclosingElement = getEnclosingElement();
      return enclosingElement == null ? null : enclosingElement.asType();
    }

    @Override
    public List<? extends TypeMirror> getTypeArguments() {
      return Collections.emptyList();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
      return GeneratedTypeElement.this.getAnnotation(annotationType);
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
      return GeneratedTypeElement.this.getAnnotationMirrors();
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
      return GeneratedTypeElement.this.getAnnotationsByType(annotationType);
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
      return v.visitDeclared(this, p);
    }
  }

  /**
   * An associated ITypeBinding implementation.
   */
  public class Binding extends AbstractTypeBinding implements ITypeBinding {

    private final IPackageBinding packageBinding = new GeneratedPackageBinding("");

    public TypeElement asElement() {
      return GeneratedTypeElement.this;
    }

    @Override
    public String getKey() {
      return getName();
    }

    @Override
    public boolean isTopLevel() {
      return nestingKind == NestingKind.TOP_LEVEL;
    }

    @Override
    public boolean isEqualTo(IBinding binding) {
      return binding == this;
    }

    @Override
    public String getBinaryName() {
      return getQualifiedName();
    }

    @Override
    public String getName() {
      return GeneratedTypeElement.this.getName();
    }

    @Override
    public String getQualifiedName() {
      return getName();
    }

    @Override
    public int getModifiers() {
      return ElementUtil.fromModifierSet(GeneratedTypeElement.this.getModifiers());
    }

    @Override
    public ITypeBinding getDeclaringClass() {
      Element enclosing = getEnclosingElement();
      while (enclosing != null && !ElementUtil.isTypeElement(enclosing)) {
        enclosing = enclosing.getEnclosingElement();
      }
      return BindingConverter.unwrapTypeElement((TypeElement) enclosing);
    }

    @Override
    public ITypeBinding getSuperclass() {
      return BindingConverter.unwrapTypeMirrorIntoTypeBinding(superclass);
    }

    @Override
    public ITypeBinding[] getInterfaces() {
      ITypeBinding[] result = new ITypeBinding[interfaces.size()];
      int i = 0;
      for (TypeMirror m : interfaces) {
        result[i++] = BindingConverter.unwrapTypeMirrorIntoTypeBinding(m);
      }
      return result;
    }

    @Override
    public IPackageBinding getPackage() {
      return packageBinding;
    }

    @Override
    public boolean isAssignmentCompatible(ITypeBinding variableType) {
      return variableType == this;
    }
  }
}
