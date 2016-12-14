package com.google.devtools.j2objc.jdt;

import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * An associated IVariableBinding implementation for a GeneratedVariableElement.
 */
public class GeneratedVariableBinding implements IVariableBinding {
  private final GeneratedVariableElement element;

  public GeneratedVariableBinding(GeneratedVariableElement element) {
    this.element = element;
  }

  public GeneratedVariableElement asElement() {
    return element;
  }

  @Override
  public int getKind() {
    return IBinding.VARIABLE;
  }

  @Override
  public String getName() {
    return element.getName();
  }

  @Override
  public String getKey() {
    throw new AssertionError("not implemented");
  }

  @Override
  public ITypeBinding getType() {
    return BindingConverter.unwrapTypeMirrorIntoTypeBinding(element.asType());
  }

  @Override
  public boolean isField() {
    return element.getKind().isField();
  }

  @Override
  public boolean isParameter() {
    return element.getKind() == ElementKind.PARAMETER;
  }

  @Override
  public boolean isEnumConstant() {
    return element.getKind() == ElementKind.ENUM_CONSTANT;
  }

  @Override
  public Object getConstantValue() {
    return element.getConstantValue();
  }

  @Override
  public IMethodBinding getDeclaringMethod() {
    Element enclosing = element.getEnclosingElement();
    return enclosing != null && ElementUtil.isExecutableElement(enclosing)
        ? BindingConverter.unwrapExecutableElement((ExecutableElement) enclosing) : null;
  }

  @Override
  public ITypeBinding getDeclaringClass() {
    Element enclosing = element.getEnclosingElement();
    return enclosing != null && ElementUtil.isTypeElement(enclosing)
        ? BindingConverter.unwrapTypeElement((TypeElement) enclosing) : null;
  }

  @Override
  public IVariableBinding getVariableDeclaration() {
    return this;
  }

  @Override
  public boolean isSynthetic() {
    return element.isSynthetic();
  }

  @Override
  public int getModifiers() {
    return ElementUtil.fromModifierSet(element.getModifiers())
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
    List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
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
