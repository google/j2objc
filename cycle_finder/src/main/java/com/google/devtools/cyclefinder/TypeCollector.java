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

import com.google.common.collect.Maps;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.Map;

/**
 * Recursively visits links between type bindings, collecting all reachable
 * types.
 */
class TypeCollector {

  private Map<String, ITypeBinding> allTypes = Maps.newHashMap();

  public Map<String, ITypeBinding> getTypes() {
    return allTypes;
  }

  public void visitType(ITypeBinding type) {
    if (type == null) {
      return;
    }
    type = getElementType(type);
    if (allTypes.containsKey(type.getKey()) || type.isPrimitive()) {
      return;
    }
    if (type.isParameterizedType()) {
      for (ITypeBinding typeParam : type.getTypeArguments()) {
        if (typeParam.isWildcardType() && typeParam.getBound() != null
            && typeParam.getBound().isWildcardType()) {
          // Double wildcard, this might recurse infinitely.
          return;
        }
      }
    }
    allTypes.put(type.getKey(), type);
    visitType(type.getSuperclass());
    visitType(type.getDeclaringClass());
    for (IVariableBinding field : type.getDeclaredFields()) {
      // Directly checking the field type that has parameterized types
      // that are self-referential causes the JDT to generate new types
      // with each recursion:
      //
      // ImmutableMap<C,? extends ImmutableCollection><java.lang.Integer>>,
      // ImmutableMap<C,? extends ImmutableCollection<
      //    ? extends ImmutableCollection<java.lang.Integer>>>, etc.
      // ImmutableMap<C,? extends ImmutableCollection<
      //    ? extends ImmutableCollection<
      //    ? extends ImmutableCollection<java.lang.Integer>>>>, etc.
      //
      // Separately visiting the erasure of the field type and its type
      // arguments works around this issue. I'm not sure how to write a
      // unit test for this, however, so I added a cycle_finder target
      // to the Guava build, which has several of these cases.
      ITypeBinding fieldType = field.getType();
      boolean mayRecurse = false;
      for (ITypeBinding typeParam : fieldType.getTypeArguments()) {
        if (typeParam.isUpperbound()) {
          mayRecurse = true;
          break;
        }
        visitType(typeParam);
      }
      if (mayRecurse) {
        fieldType = fieldType.getErasure();
      }
      visitType(fieldType);
    }
    for (ITypeBinding interfaze : type.getInterfaces()) {
      visitType(interfaze);
    }
  }

  public void visitAST(ASTNode ast) {
    ast.accept(new ASTVisitor() {
      @Override
      public boolean visit(TypeDeclaration node) {
        visitType(node.resolveBinding());
        return true;
      }
      @Override
      public boolean visit(AnonymousClassDeclaration node) {
        visitType(node.resolveBinding());
        return true;
      }
      @Override
      public boolean visit(ClassInstanceCreation node) {
        visitType(node.resolveTypeBinding());
        return true;
      }
      @Override
      public boolean visit(MethodInvocation node) {
        visitType(node.resolveTypeBinding());
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
