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
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.j2objc.annotations.ReflectionSupport;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
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
}
