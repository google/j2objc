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

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * Utility methods getting Proguard names for Elements.
 *
 * @author Priyank Malvania
 */
public final class ProguardNameUtil {

  private static final Joiner innerClassJoiner = Joiner.on('$');

  /**
   * Get the ProGuard name of a method.
   * For non-constructors this is the method's name.
   * For constructors of top-level classes, this is the name of the class.
   * For constructors of inner classes, this is the $-delimited name path
   * from the outermost class declaration to the inner class declaration.
   */
  public static String getProGuardName(ExecutableElement method) {
    if (!ElementUtil.isConstructor(method)
        || ElementUtil.getDeclaringClass(method).getNestingKind() != NestingKind.MEMBER) {
      return ElementUtil.getName(method);
    }
    TypeElement parent = ElementUtil.getDeclaringClass(method);
    assert parent != null;
    List<String> components = Lists.newLinkedList(); // LinkedList is faster for prepending.
    do {
      components.add(0, ElementUtil.getName(parent));
      parent = ElementUtil.getDeclaringClass(parent);
    } while (parent != null);
    return innerClassJoiner.join(components);
  }

  /**
   * Get the ProGuard signature of a method.
   */
  public static String getProGuardSignature(ExecutableElement method, TypeUtil typeUtil) {
    StringBuilder sb = new StringBuilder("(");

    // If the method is an inner class constructor, prepend the outer class type.
    if (ElementUtil.isConstructor(method)) {
      TypeElement declaringClass = ElementUtil.getDeclaringClass(method);
      if (ElementUtil.hasOuterContext(declaringClass)) {
        TypeElement outerClass = ElementUtil.getDeclaringClass(declaringClass);
        sb.append(typeUtil.getSignatureName(outerClass.asType()));
      }
    }

    for (VariableElement param : method.getParameters()) {
      sb.append(typeUtil.getSignatureName(param.asType()));
    }

    sb.append(')');
    TypeMirror returnType = method.getReturnType();
    if (returnType != null) {
      sb.append(typeUtil.getSignatureName(returnType));
    }
    return sb.toString();
  }
}
