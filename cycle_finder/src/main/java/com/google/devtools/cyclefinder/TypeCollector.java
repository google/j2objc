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

package com.google.devtools.cyclefinder;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.devtools.j2objc.ast.AnonymousClassDeclaration;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.Map;

/**
 * Recursively visits links between type bindings, collecting all reachable
 * types.
 */
class TypeCollector {

  private Map<String, ITypeBinding> allTypes = Maps.newHashMap();

  private static Map<ITypeBinding, String> renamings = Maps.newHashMap();

  public Map<String, ITypeBinding> getTypes() {
    return allTypes;
  }

  public static String getNameForType(ITypeBinding type) {
    String name = renamings.get(type);
    if (name != null) {
      return name;
    }
    name = type.getName();
    if (!Strings.isNullOrEmpty(name)) {
      return name;
    }
    return type.getKey();
  }

  public void visitType(ITypeBinding type) {
    if (type == null) {
      return;
    }
    type = getElementType(type);
    if (allTypes.containsKey(type.getKey()) || type.isPrimitive() || type.isRawType()) {
      return;
    }
    if (hasNestedWildcard(type)) {
      // Avoid infinite recursion caused by nested wildcard types.
      return;
    }
    allTypes.put(type.getKey(), type);
    visitType(type.getSuperclass());
    visitType(type.getDeclaringClass());
    for (IVariableBinding field : type.getDeclaredFields()) {
      ITypeBinding fieldType = field.getType();
      for (ITypeBinding typeParam : fieldType.getTypeArguments()) {
        visitType(typeParam);
      }
      visitType(fieldType);
    }
    for (ITypeBinding interfaze : type.getInterfaces()) {
      visitType(interfaze);
    }
  }

  private static boolean hasWildcard(ITypeBinding type) {
    if (type.isWildcardType()) {
      return true;
    }
    for (ITypeBinding typeParam : type.getTypeArguments()) {
      if (hasWildcard(typeParam)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasNestedWildcard(ITypeBinding type) {
    ITypeBinding bound = type.getBound();
    if (bound != null && hasWildcard(bound)) {
      return true;
    }
    for (ITypeBinding typeParam : type.getTypeArguments()) {
      if (hasNestedWildcard(typeParam)) {
        return true;
      }
    }
    return false;
  }

  public void visitAST(final CompilationUnit unit) {
    unit.accept(new TreeVisitor() {
      @Override
      public boolean visit(TypeDeclaration node) {
        visitType(node.getTypeBinding());
        return true;
      }
      @Override
      public boolean visit(AnonymousClassDeclaration node) {
        ITypeBinding binding = node.getTypeBinding();
        visitType(binding);
        renamings.put(binding, "anonymous:" + node.getLineNumber());
        return true;
      }
      @Override
      public boolean visit(ClassInstanceCreation node) {
        visitType(node.getTypeBinding());
        return true;
      }
      @Override
      public boolean visit(MethodInvocation node) {
        visitType(node.getTypeBinding());
        return true;
      }
    });
  }

  private ITypeBinding getElementType(ITypeBinding type) {
    if (type.isArray()) {
      return type.getElementType();
    }
    return type;
  }
}
