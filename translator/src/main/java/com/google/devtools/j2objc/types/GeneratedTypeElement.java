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
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

/**
 * Element class for types created during translation.
 *
 * @author Keith Stanger
 */
public class GeneratedTypeElement extends GeneratedElement implements TypeElement {

  private final TypeMirror superclass;
  private List<TypeMirror> interfaces = new ArrayList<>();
  private final NestingKind nestingKind;
  private final Name qualifiedName;
  private final String header;
  private final boolean isIosType;

  protected GeneratedTypeElement(
      String name, ElementKind kind, Element enclosingElement, TypeMirror superclass,
      NestingKind nestingKind, String header, boolean isIosType, boolean synthetic) {
    super(Preconditions.checkNotNull(name), checkElementKind(kind), enclosingElement, synthetic);
    this.superclass = superclass;
    this.nestingKind = nestingKind;
    qualifiedName = new NameImpl(getQualifiedPrefix(enclosingElement) + name);
    this.header = header;
    this.isIosType = isIosType;
  }

  public static GeneratedTypeElement mutableCopy(TypeElement element) {
    return new GeneratedTypeElement(
        element.getSimpleName().toString(), element.getKind(), element.getEnclosingElement(),
        element.getSuperclass(), element.getNestingKind(), ElementUtil.getHeader(element),
        ElementUtil.isIosType(element), ElementUtil.isSynthetic(element));
  }

  private static GeneratedTypeElement newEmulatedType(
      String qualifiedName, ElementKind kind, TypeMirror superclass) {
    int idx = qualifiedName.lastIndexOf('.');
    String packageName = idx < 0 ? "" : qualifiedName.substring(0, idx);
    PackageElement packageElement = new GeneratedPackageElement(packageName);
    return new GeneratedTypeElement(
        qualifiedName.substring(idx + 1), kind, packageElement, superclass, NestingKind.TOP_LEVEL,
        null, false, false);
  }

  public static GeneratedTypeElement newEmulatedClass(String qualifiedName, TypeMirror superclass) {
    return newEmulatedType(qualifiedName, ElementKind.CLASS, superclass);
  }

  public static GeneratedTypeElement newEmulatedInterface(String qualifiedName) {
    return newEmulatedType(qualifiedName, ElementKind.INTERFACE, null);
  }

  public static GeneratedTypeElement newIosType(
      String name, ElementKind kind, TypeElement superclass, String header) {
    return new GeneratedTypeElement(
        name, kind, null, superclass != null ? superclass.asType() : null, NestingKind.TOP_LEVEL,
        header, true, false);
  }

  public static GeneratedTypeElement newIosClass(
      String name, TypeElement superclass, String header) {
    return newIosType(name, ElementKind.CLASS, superclass, header);
  }

  public static GeneratedTypeElement newIosInterface(String name, String header) {
    return newIosType(name, ElementKind.INTERFACE, null, header);
  }

  public static GeneratedTypeElement newPackageInfoClass(
      PackageElement pkgElem, TypeUtil typeUtil) {
    return (GeneratedTypeElement) new GeneratedTypeElement(
        NameTable.PACKAGE_INFO_CLASS_NAME, ElementKind.CLASS, pkgElem,
        typeUtil.getJavaObject().asType(), NestingKind.TOP_LEVEL, null, false, false)
        .addModifiers(Modifier.PRIVATE);
  }

  private static ElementKind checkElementKind(ElementKind kind) {
    Preconditions.checkArgument(kind.isClass() || kind.isInterface());
    return kind;
  }

  private static String getQualifiedPrefix(Element enclosing) {
    if (enclosing == null) {
      return "";
    } else if (ElementUtil.isTypeElement(enclosing)) {
      return ((TypeElement) enclosing).getQualifiedName().toString() + '.';
    } else if (ElementUtil.isPackage(enclosing)) {
      PackageElement pkg = (PackageElement) enclosing;
      return pkg.isUnnamed() ? "" : pkg.getQualifiedName().toString() + '.';
    } else {
      return getQualifiedPrefix(enclosing.getEnclosingElement());
    }
  }

  @Override
  public TypeMirror asType() {
    return new Mirror();
  }

  @Override
  public Name getQualifiedName() {
    return qualifiedName;
  }

  public String getHeader() {
    return header;
  }

  public boolean isIosType() {
    return isIosType;
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

  public List<? extends TypeMirror> getDirectSupertypes() {
    List<TypeMirror> result = new ArrayList<>(interfaces);
    if (superclass != null) {
      result.add(0, superclass);  // Superclass must be first.
    }
    return result;
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

  /**
   * The associated TypeMirror.
   * TODO(kstanger): Make private when javac conversion is complete.
   */
  public class Mirror extends AbstractTypeMirror implements DeclaredType {

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
      TypeElement declaringClass = ElementUtil.getDeclaringClass(GeneratedTypeElement.this);
      return declaringClass == null ? null : declaringClass.asType();
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

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Mirror && ((Mirror) obj).asElement().equals(GeneratedTypeElement.this);
    }

    @Override
    public int hashCode() {
      return 31 * GeneratedTypeElement.this.hashCode();
    }

    @Override
    public String toString() {
      return GeneratedTypeElement.this.getQualifiedName().toString();
    }
  }
}
