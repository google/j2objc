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

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.ArrayCreation;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.j2objc.annotations.ReflectionSupport;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * General collection of utility methods.
 *
 * @author Keith Stanger
 */
public final class TranslationUtil {

  public static boolean needsReflection(AbstractTypeDeclaration node) {
    return needsReflection(node.getTypeBinding());
  }

  public static boolean needsReflection(PackageDeclaration node) {
    return needsReflection(getReflectionSupportLevel(getAnnotation(node, ReflectionSupport.class)));
  }

  public static boolean needsReflection(ITypeBinding type) {
    while (type != null) {
      ReflectionSupport.Level level = getReflectionSupportLevel(
          BindingUtil.getAnnotation(type, ReflectionSupport.class));
      if (level != null) {
        return level == ReflectionSupport.Level.FULL;
      }
      type = type.getDeclaringClass();
    }
    return !Options.stripReflection();
  }

  private static boolean needsReflection(ReflectionSupport.Level level) {
    if (level != null) {
      return level == ReflectionSupport.Level.FULL;
    } else {
      return !Options.stripReflection();
    }
  }

  public static ReflectionSupport.Level getReflectionSupportLevel(
      IAnnotationBinding reflectionSupport) {
    if (reflectionSupport == null) {
      return null;
    }
    Object level = BindingUtil.getAnnotationValue(reflectionSupport, "value");
    if (level instanceof IVariableBinding) {
      return ReflectionSupport.Level.valueOf(((IVariableBinding) level).getName());
    }
    return null;
  }

  /**
   * The IPackageBinding does not provide the annotations so we must iterate the
   * annotation from the tree.
   */
  private static IAnnotationBinding getAnnotation(
      PackageDeclaration node, Class<?> annotationClass) {
    for (Annotation annotation : node.getAnnotations()) {
      IAnnotationBinding binding = annotation.getAnnotationBinding();
      if (BindingUtil.typeEqualsClass(binding.getAnnotationType(), annotationClass)) {
        return binding;
      }
    }
    return null;
  }

  /**
   * If possible give this expression an unbalanced extra retain. If a non-null
   * result is returned, then the returned expression has an unbalanced extra
   * retain and the passed in expression is removed from the tree and must be
   * discarded. If null is returned then the passed in expression is left
   * untouched. The caller must ensure the result is eventually consumed.
   */
  public static Expression retainResult(Expression node) {
    switch (node.getKind()) {
      case ARRAY_CREATION:
        ((ArrayCreation) node).setHasRetainedResult(true);
        return TreeUtil.remove(node);
      case CLASS_INSTANCE_CREATION:
        ((ClassInstanceCreation) node).setHasRetainedResult(true);
        return TreeUtil.remove(node);
      case METHOD_INVOCATION:
        MethodInvocation invocation = (MethodInvocation) node;
        Expression expr = invocation.getExpression();
        IMethodBinding method = invocation.getMethodBinding();
        if (expr != null && method instanceof IOSMethodBinding
            && ((IOSMethodBinding) method).getSelector().equals(NameTable.AUTORELEASE_METHOD)) {
          return TreeUtil.remove(expr);
        }
        break;
    }
    return null;
  }

  public static IMethodBinding findDefaultConstructorBinding(ITypeBinding type, Types typeEnv) {
    // Search for a non-varargs match.
    for (IMethodBinding m : type.getDeclaredMethods()) {
      if (m.isConstructor() && m.getParameterTypes().length == 0) {
        return m;
      }
    }
    // Search for a varargs match. Choose the most specific. (JLS 15.12.2.5)
    IMethodBinding result = null;
    for (IMethodBinding m : type.getDeclaredMethods()) {
      ITypeBinding[] paramTypes = m.getParameterTypes();
      if (m.isConstructor() && m.isVarargs() && paramTypes.length == 1) {
        if (result == null || paramTypes[0].isAssignmentCompatible(result.getParameterTypes()[0])) {
          result = m;
        }
      }
    }
    if (result != null) {
      return result;
    }
    // Sometimes there won't be a default constructor (eg. enums), so just
    // create our own binding.
    return GeneratedMethodBinding.newConstructor(type, type.getModifiers(), typeEnv);
  }
}
