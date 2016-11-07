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
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Element class for variables and parameters created during translation.
 *
 * @author Nathan Braswell
 */
public class GeneratedVariableElement extends GeneratedElement implements VariableElement {

  private final TypeMirror type;
  private boolean nonnull = false;
  private String typeQualifiers;
  private final IVariableBinding binding = new Binding();

  private GeneratedVariableElement(
      String name, TypeMirror type, ElementKind kind, Element enclosingElement, boolean synthetic) {
    super(Preconditions.checkNotNull(name), checkElementKind(kind), enclosingElement, synthetic);
    this.type = type;
  }

  public static GeneratedVariableElement mutableCopy(VariableElement var) {
    return new GeneratedVariableElement(
        var.getSimpleName().toString(), var.asType(), var.getKind(), var.getEnclosingElement(),
        ElementUtil.isSynthetic(var));
  }

  public static GeneratedVariableElement newField(
      String name, TypeMirror type, Element enclosingElement) {
    return new GeneratedVariableElement(name, type, ElementKind.FIELD, enclosingElement, true);
  }

  public static GeneratedVariableElement newParameter(
      String name, TypeMirror type, Element enclosingElement) {
    return new GeneratedVariableElement(name, type, ElementKind.PARAMETER, enclosingElement, true);
  }

  public static GeneratedVariableElement newLocalVar(
      String name, TypeMirror type, Element enclosingElement) {
    return new GeneratedVariableElement(
        name, type, ElementKind.LOCAL_VARIABLE, enclosingElement, true);
  }

  private static ElementKind checkElementKind(ElementKind kind) {
    Preconditions.checkArgument(
        kind == ElementKind.FIELD || kind == ElementKind.LOCAL_VARIABLE
        || kind == ElementKind.PARAMETER);
    return kind;
  }

  @Override
  public TypeMirror asType() {
    return type;
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitVariable(this, p);
  }

  @Override
  public Object getConstantValue() {
    return null;
  }

  public boolean isNonnull() {
    return nonnull;
  }

  public GeneratedVariableElement setNonnull(boolean value) {
    nonnull = value;
    return this;
  }

  /**
   * Sets the qualifiers that should be added to the variable declaration. Use
   * an asterisk ('*') to delimit qualifiers that should apply to a pointer from
   * qualifiers that should apply to the pointee type. For example setting the
   * qualifier as "__strong * const" on a string array will result in a
   * declaration of "NSString * __strong * const".
   */
  public GeneratedVariableElement setTypeQualifiers(String qualifiers) {
    typeQualifiers = qualifiers;
    return this;
  }

  public String getTypeQualifiers() {
    return typeQualifiers;
  }

  @Override
  public GeneratedVariableElement addAnnotationMirrors(
      Collection<? extends AnnotationMirror> newAnnotations) {
    return (GeneratedVariableElement) super.addAnnotationMirrors(newAnnotations);
  }

  @Override
  public GeneratedVariableElement addModifiers(Modifier... modifiers) {
    return (GeneratedVariableElement) super.addModifiers(modifiers);
  }

  public IVariableBinding asVariableBinding() {
    return binding;
  }

  /**
   * An associated IMethodBinding implementation.
   */
  public class Binding implements IVariableBinding {

    public GeneratedVariableElement asElement() {
      return GeneratedVariableElement.this;
    }

    @Override
    public int getKind() {
      return IBinding.VARIABLE;
    }

    @Override
    public String getName() {
      return GeneratedVariableElement.this.getName();
    }

    @Override
    public String getKey() {
      throw new AssertionError("not implemented");
    }

    @Override
    public ITypeBinding getType() {
      return BindingConverter.unwrapTypeMirrorIntoTypeBinding(asType());
    }

    @Override
    public boolean isField() {
      return GeneratedVariableElement.this.getKind().isField();
    }

    @Override
    public boolean isParameter() {
      return GeneratedVariableElement.this.getKind() == ElementKind.PARAMETER;
    }

    @Override
    public boolean isEnumConstant() {
      return GeneratedVariableElement.this.getKind() == ElementKind.ENUM_CONSTANT;
    }

    @Override
    public Object getConstantValue() {
      return GeneratedVariableElement.this.getConstantValue();
    }

    @Override
    public IMethodBinding getDeclaringMethod() {
      Element enclosing = GeneratedVariableElement.this.getEnclosingElement();
      return enclosing != null && ElementUtil.isExecutableElement(enclosing)
          ? BindingConverter.unwrapExecutableElement((ExecutableElement) enclosing) : null;
    }

    @Override
    public ITypeBinding getDeclaringClass() {
      Element enclosing = GeneratedVariableElement.this.getEnclosingElement();
      return enclosing != null && ElementUtil.isTypeElement(enclosing)
          ? BindingConverter.unwrapTypeElement((TypeElement) enclosing) : null;
    }

    @Override
    public IVariableBinding getVariableDeclaration() {
      return this;
    }

    @Override
    public boolean isSynthetic() {
      return GeneratedVariableElement.this.isSynthetic();
    }

    @Override
    public int getModifiers() {
      return ElementUtil.fromModifierSet(GeneratedVariableElement.this.getModifiers())
          | (isSynthetic() ? ElementUtil.ACC_SYNTHETIC : 0);
    }

    @Override
    public boolean isEffectivelyFinal() {
      return false;
    }

    @Override
    public boolean isRecovered() {
      return false;
    }

    @Override
    public boolean isDeprecated() {
      return false;
    }

    @Override
    public IAnnotationBinding[] getAnnotations() {
      List<? extends AnnotationMirror> mirrors = getAnnotationMirrors();
      IAnnotationBinding[] result = new IAnnotationBinding[mirrors.size()];
      for (int i = 0; i < mirrors.size(); i++) {
        result[i] = BindingConverter.unwrapAnnotationMirror(mirrors.get(i));
      }
      return result;
    }

    @Override
    public int getVariableId() {
      throw new AssertionError("not implemented");
    }

    @Override
    public IJavaElement getJavaElement() {
      throw new AssertionError("not implemented");
    }

    @Override
    public boolean isEqualTo(IBinding binding) {
      return this == binding;
    }
  }
}
