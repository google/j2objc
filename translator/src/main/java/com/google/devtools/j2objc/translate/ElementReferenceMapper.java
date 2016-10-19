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

import javax.lang.model.element.ExecutableElement;

import org.eclipse.jdt.core.dom.Modifier;

import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.CodeReferenceMap.Builder;
import com.google.devtools.j2objc.util.ProguardNameUtil;

/**
 * Reference-mapping code for TreeShaker functionality that uses the visitor pattern
 * to identify all elements in the source code
 * 
 * @author Priyank Malvania
 */
public class ElementReferenceMapper extends UnitTreeVisitor {

  private Builder codeMapBuilder = CodeReferenceMap.builder();

  public ElementReferenceMapper(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    visitType(node);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    visitType(node);
  }

  @Override
  public void endVisit(FieldDeclaration field) {
    for (VariableDeclarationFragment fragment : field.getFragments()) {
      String className = fragment.getVariableElement()
          .getEnclosingElement().getSimpleName().toString();
      String fragmentIdentifier = fragment.getName().getIdentifier();

      codeMapBuilder.addDeadField(className, fragmentIdentifier);
    }
  }

  @Override
  public void endVisit(MethodDeclaration method) {
    if (Modifier.isNative(method.getModifiers())) {
      return;
    }
    ExecutableElement methodElement = method.getExecutableElement();
    String className = methodElement.getEnclosingElement().getSimpleName().toString();
    String methodName = ProguardNameUtil.getProGuardName(methodElement);
    String methodSignature = ProguardNameUtil.getProGuardSignature(methodElement, typeUtil);

    codeMapBuilder.addDeadMethod(className, methodName, methodSignature);
  }

  private void visitType(AbstractTypeDeclaration node) {
    codeMapBuilder.addDeadClass(elementUtil.getBinaryName(node.getTypeElement()));
  }

  public CodeReferenceMap getCodeMap() {
    return codeMapBuilder.build();
  }
}
