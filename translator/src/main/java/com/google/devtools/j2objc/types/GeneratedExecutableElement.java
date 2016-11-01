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
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.ast.DebugASTPrinter;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Element class for methods created during translation.
 *
 * @author Nathan Braswell
 */
public class GeneratedExecutableElement extends GeneratedElement implements ExecutableElement {

  private final String selector;
  private final List<VariableElement> parameters = Lists.newArrayList();
  private final TypeMirror returnType;
  private final boolean varargs;
  private final IMethodBinding binding = new Binding();

  public GeneratedExecutableElement(
      String name, String selector, ElementKind kind, TypeMirror returnType,
      Element enclosingElement, boolean varargs) {
    super(Preconditions.checkNotNull(name), checkElementKind(kind), enclosingElement, true);
    this.selector = selector;
    this.returnType = returnType;
    this.varargs = varargs;
  }

  public static GeneratedExecutableElement newMethodWithSelector(
      String selector, TypeMirror returnType, Element enclosingElement) {
    return new GeneratedExecutableElement(
        selector, selector, ElementKind.METHOD, returnType, enclosingElement, false);
  }

  public static GeneratedExecutableElement newConstructor(
      TypeElement enclosingElement, TypeUtil typeUtil) {
    return new GeneratedExecutableElement(
        NameTable.INIT_NAME, null, ElementKind.CONSTRUCTOR, typeUtil.getVoidType(),
        enclosingElement, false);
  }

  private static ElementKind checkElementKind(ElementKind kind) {
    Preconditions.checkArgument(kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR);
    return kind;
  }

  public String getSelector() {
    return selector;
  }

  public GeneratedExecutableElement addParameter(VariableElement param) {
    parameters.add(param);
    return this;
  }

  @Override
  public GeneratedExecutableElement addModifiers(Modifier... newModifiers) {
    return (GeneratedExecutableElement) super.addModifiers(newModifiers);
  }

  @Override
  public GeneratedExecutableElement addModifiers(Collection<? extends Modifier> newModifiers) {
    return (GeneratedExecutableElement) super.addModifiers(newModifiers);
  }

  @Override
  public GeneratedExecutableElement removeModifiers(Modifier... modifiersToRemove) {
    return (GeneratedExecutableElement) super.removeModifiers(modifiersToRemove);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    DebugASTPrinter.printModifiers(ElementUtil.fromModifierSet(getModifiers()), sb);
    sb.append(returnType != null ? returnType.toString() : "<no type>");
    sb.append(' ');
    sb.append(getName());
    sb.append('(');
    boolean notFirst = false;
    for (VariableElement p : getParameters()) {
      if (notFirst) {
        sb.append(", ");
      }
      sb.append(p.asType().toString());
      notFirst = true;
    }
    sb.append(')');
    return sb.toString();
  }

  @Override
  public TypeMirror asType() {
    return new Mirror();
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitExecutable(this, p);
  }

  @Override
  public List<? extends TypeParameterElement> getTypeParameters() {
    throw new AssertionError("not implemented");
  }

  @Override
  public TypeMirror getReturnType() {
    return returnType;
  }

  @Override
  public List<VariableElement> getParameters() {
    return parameters;
  }

  @Override
  public TypeMirror getReceiverType() {
    throw new AssertionError("not implemented");
  }

  @Override
  public boolean isVarArgs() {
    return varargs;
  }

  @Override
  public boolean isDefault() {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<? extends TypeMirror> getThrownTypes() {
    throw new AssertionError("not implemented");
  }

  @Override
  public AnnotationValue getDefaultValue() {
    throw new AssertionError("not implemented");
  }

  public IMethodBinding asMethodBinding() {
    return binding;
  }

  /**
   * The associated ExecutableType.
   * TODO(kstanger): Make private when BindingConverter is removed.
   */
  public class Mirror implements ExecutableType {

    private final List<? extends TypeMirror> parameterTypes =
        Lists.transform(parameters, param -> param.asType());

    public IMethodBinding asMethodBinding() {
      return GeneratedExecutableElement.this.asMethodBinding();
    }

    @Override
    public TypeKind getKind() {
      return TypeKind.EXECUTABLE;
    }

    @Override
    public TypeMirror getReturnType() {
      return GeneratedExecutableElement.this.getReturnType();
    }

    @Override
    public List<? extends TypeMirror> getParameterTypes() {
      return parameterTypes;
    }

    @Override
    public List<? extends TypeVariable> getTypeVariables() {
      throw new AssertionError("not implemented");
    }

    @Override
    public List<? extends TypeMirror> getThrownTypes() {
      return GeneratedExecutableElement.this.getThrownTypes();
    }

    @Override
    public TypeMirror getReceiverType() {
      return GeneratedExecutableElement.this.getReceiverType();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
      return GeneratedExecutableElement.this.getAnnotation(annotationType);
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
      return GeneratedExecutableElement.this.getAnnotationMirrors();
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
      return GeneratedExecutableElement.this.getAnnotationsByType(annotationType);
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
      return v.visitExecutable(this, p);
    }
  }

  /**
   * An associated IMethodBinding implementation.
   */
  public class Binding implements IMethodBinding {

    public ExecutableElement asElement() {
      return GeneratedExecutableElement.this;
    }

    @Override
    public int getKind() {
      return IBinding.METHOD;
    }

    @Override
    public String getKey() {
      return getName();
    }

    @Override
    public boolean isEqualTo(IBinding binding) {
      return binding == this;
    }

    @Override
    public boolean isSynthetic() {
      return GeneratedExecutableElement.this.isSynthetic();
    }

    @Override
    public int getModifiers() {
      return ElementUtil.fromModifierSet(GeneratedExecutableElement.this.getModifiers())
          | (isSynthetic() ? BindingUtil.ACC_SYNTHETIC : 0);
    }

    @Override
    public ITypeBinding getDeclaredReceiverType() {
      return null;
    }

    @Override
    public ITypeBinding getDeclaringClass() {
      return BindingConverter.unwrapTypeElement(
          ElementUtil.getDeclaringClass(GeneratedExecutableElement.this));
    }

    @Override
    public Object getDefaultValue() {
      return null;
    }

    @Override
    public ITypeBinding[] getExceptionTypes() {
      return new ITypeBinding[0];
    }

    @Override
    public IMethodBinding getMethodDeclaration() {
      return this;
    }

    @Override
    public String getName() {
      return GeneratedExecutableElement.this.getSimpleName().toString();
    }

    @Override
    public IAnnotationBinding[] getParameterAnnotations(int paramIndex) {
      return new IAnnotationBinding[0];
    }

    @Override
    public ITypeBinding[] getParameterTypes() {
      ITypeBinding[] paramTypes = new ITypeBinding[parameters.size()];
      for (int i = 0; i < parameters.size(); i++) {
        paramTypes[i] = BindingConverter.unwrapTypeMirrorIntoTypeBinding(
            parameters.get(i).asType());
      }
      return paramTypes;
    }

    @Override
    public ITypeBinding getReturnType() {
      return BindingConverter.unwrapTypeMirrorIntoTypeBinding(returnType);
    }

    @Override
    public ITypeBinding[] getTypeArguments() {
      return new ITypeBinding[0];
    }

    @Override
    public ITypeBinding[] getTypeParameters() {
      return new ITypeBinding[0];
    }

    @Override
    public boolean isAnnotationMember() {
      return false;
    }

    @Override
    public boolean isConstructor() {
      return GeneratedExecutableElement.this.getKind() == ElementKind.CONSTRUCTOR;
    }

    @Override
    public boolean isDefaultConstructor() {
      return false;
    }

    @Override
    public boolean isGenericMethod() {
      return false;
    }

    @Override
    public boolean isParameterizedMethod() {
      return false;
    }

    @Override
    public boolean isRawMethod() {
      return false;
    }

    @Override
    public boolean isSubsignature(IMethodBinding otherMethod) {
      return false;
    }

    @Override
    public boolean isVarargs() {
      return GeneratedExecutableElement.this.isVarArgs();
    }

    @Override
    public boolean overrides(IMethodBinding method) {
      return false;
    }

    @Override
    public IJavaElement getJavaElement() {
      throw new AssertionError("not implemented");
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
      return new IAnnotationBinding[0];
    }

    // Internal JDT has a different version than external.
    @SuppressWarnings("MissingOverride")
    public IBinding getDeclaringMember() {
      return null;
    }
  }
}
