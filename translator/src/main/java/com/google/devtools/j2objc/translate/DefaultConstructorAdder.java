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

package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;

import org.eclipse.jdt.core.dom.IMethodBinding;

import java.util.HashSet;
import java.util.Set;

/**
 * Adds the implicie default constructors for classes that have no declared
 * constructors.
 */
public class DefaultConstructorAdder extends TreeVisitor {

  @Override
  public void endVisit(TypeDeclaration node) {
    visitType(node);
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    visitType(node);
  }

  private void visitType(AbstractTypeDeclaration node) {
    Set<IMethodBinding> declaredInAst = new HashSet<>();
    for (MethodDeclaration methodDecl : TreeUtil.getMethodDeclarations(node)) {
      declaredInAst.add(methodDecl.getMethodBinding());
    }
    for (IMethodBinding methodBinding : node.getTypeBinding().getDeclaredMethods()) {
      if (!declaredInAst.contains(methodBinding) && isDefaultConstructor(methodBinding)) {
        addDefaultConstructor(node, methodBinding);
      }
    }
  }

  private boolean isDefaultConstructor(IMethodBinding method) {
    return method.isConstructor() && method.getParameterTypes().length == 0;
  }

  private void addDefaultConstructor(AbstractTypeDeclaration node, IMethodBinding methodBinding) {
    MethodDeclaration methodDecl = new MethodDeclaration(methodBinding);
    methodDecl.setBody(new Block());
    node.addBodyDeclaration(methodDecl);
  }
}
