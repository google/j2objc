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

package com.google.devtools.j2objc.util;

import com.google.devtools.j2objc.jdt.BindingConverter;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Utility methods for working with elements.
 */
public final class ElementUtil {

  // Flags defined in JVM spec, table 4.1. These constants are also defined in
  // java.lang.reflect.Modifier, but aren't public.
  public static final int ACC_BRIDGE = 0x40;
  public static final int ACC_VARARGS = 0x80;
  public static final int ACC_SYNTHETIC = 0x1000;
  public static final int ACC_ANNOTATION = 0x2000;
  public static final int ACC_ENUM = 0x4000;

  // Not defined in JVM spec, but used by reflection support.
  public static final int ACC_ANONYMOUS = 0x8000;

  // Class files can only use the lower 16 bits.
  public static final int ACC_FLAG_MASK = 0xFFFF;

  public static boolean isStatic(Element element) {
    return hasModifier(element, Modifier.STATIC);
  }

  public static boolean isDefault(Element element) {
    // Indirectly check whether Modifier.DEFAULT exists, since it was
    // added in Java 8.
    try {
      Modifier m = Modifier.valueOf("DEFAULT");
      return hasModifier(element, m);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isFinal(Element element) {
    return hasModifier(element, Modifier.FINAL);
  }

  public static boolean isPublic(Element element) {
    return hasModifier(element, Modifier.PUBLIC);
  }

  public static boolean isPrivate(Element element) {
    return hasModifier(element, Modifier.PRIVATE);
  }

  public static boolean isVolatile(VariableElement element) {
    return hasModifier(element, Modifier.VOLATILE);
  }

  public static TypeElement getDeclaringClass(Element element) {
    Element enclosingElement = element.getEnclosingElement();
    return enclosingElement instanceof TypeElement ? (TypeElement) enclosingElement : null;
  }

  public static ExecutableElement getDeclaringMethod(Element element) {
    Element enclosingElement = element.getEnclosingElement();
    return enclosingElement instanceof ExecutableElement
        ? (ExecutableElement) enclosingElement : null;
  }

  public static boolean isPrimitiveConstant(VariableElement element) {
    return isFinal(element) && element.asType().getKind().isPrimitive()
        && element.getConstantValue() != null
        // Exclude local variables declared final.
        && getDeclaringClass(element) != null;
  }

  public static boolean isStringConstant(VariableElement element) {
    Object constantValue = element.getConstantValue();
    return constantValue != null && constantValue instanceof String
        && UnicodeUtils.hasValidCppCharacters((String) constantValue);
  }

  /**
   * Returns whether this variable will be declared in global scope in ObjC.
   */
  public static boolean isGlobalVar(VariableElement element) {
    return isStatic(element) || isPrimitiveConstant(element);
  }

  /**
   * Returns whether this variable will be an ObjC instance variable.
   */
  public static boolean isInstanceVar(VariableElement element) {
    return element.getKind() == ElementKind.FIELD && !isGlobalVar(element);
  }

  public static boolean isAbstract(Element element) {
    return hasModifier(element, Modifier.ABSTRACT);
  }

  public static boolean isNative(Element element) {
    return hasModifier(element, Modifier.NATIVE);
  }

  public static boolean isSynchronized(Element element) {
    return hasModifier(element, Modifier.SYNCHRONIZED);
  }

  public static boolean isSynthetic(int modifiers) {
    return (modifiers & ACC_SYNTHETIC) != 0;
  }

  public static boolean isPackageInfo(TypeElement type) {
    return type.getSimpleName().toString().equals(NameTable.PACKAGE_INFO_CLASS_NAME);
  }

  /**
   * Tests if this type element is private to it's source file. A public type declared
   * within a private type is considered private.
   */
  public static boolean isPrivateInnerType(TypeElement type) {
    if (isPrivate(type)) {
      return true;
    }
    NestingKind nestingKind = type.getNestingKind();
    if (nestingKind == NestingKind.ANONYMOUS || nestingKind == NestingKind.LOCAL) {
      return true;
    }
    TypeElement declaringClass = getDeclaringClass(type);
    if (declaringClass != null) {
      return isPrivateInnerType(declaringClass);
    }
    return false;
  }

  /**
   * Determines if a type element can access fields and methods from an outer class.
   */
  public static boolean hasOuterContext(TypeElement type) {
    if (getDeclaringClass(type) == null) {
      return false;
    }
    // Local types can't be declared static, but if the declaring method is
    // static then the local type is effectively static.
    ExecutableElement declaringMethod = getDeclaringMethod(type);
    if (declaringMethod != null) {
      return !ElementUtil.isStatic(declaringMethod);
    }
    return !ElementUtil.isStatic(type);
  }

  /**
   * Returns the inner type with the specified name.
   */
  public static TypeElement findDeclaredType(TypeElement type, String name) {
    for (Element child : type.getEnclosedElements()) {
      if (child instanceof TypeElement && child.getSimpleName().toString().equals(name)) {
        return (TypeElement) child;
      }
    }
    return null;
  }

  private static boolean hasModifier(Element element, Modifier modifier) {
    return element.getModifiers().contains(modifier);
  }

  public static boolean isType(Element element) {
    ElementKind kind = element.getKind();
    return kind == ElementKind.ANNOTATION_TYPE || kind == ElementKind.CLASS
        || kind == ElementKind.ENUM || kind == ElementKind.INTERFACE
        || kind == ElementKind.TYPE_PARAMETER;
  }

  public static boolean isVariable(Element element) {
    ElementKind kind = element.getKind();
    return kind == ElementKind.FIELD || kind == ElementKind.LOCAL_VARIABLE
        || kind == ElementKind.PARAMETER || kind == ElementKind.EXCEPTION_PARAMETER
        || kind == ElementKind.RESOURCE_VARIABLE;
  }

  public static boolean isField(Element element) {
    ElementKind kind = element.getKind();
    return kind == ElementKind.FIELD || kind == ElementKind.ENUM_CONSTANT;
  }

  public static boolean isWeakReference(VariableElement varElement) {
    IVariableBinding var = (IVariableBinding) BindingConverter.unwrapElement(varElement);
    if (var.getName().startsWith("this$")
        && BindingUtil.isWeakOuterAnonymousClass(var.getDeclaringClass())) {
      return true;
    }
    return BindingUtil.hasNamedAnnotation(var, "Weak")
        || BindingUtil.hasWeakPropertyAttribute(var)
        || (var.getName().startsWith("this$")
        && BindingUtil.hasNamedAnnotation(var.getDeclaringClass(), "WeakOuter"));
  }
}
