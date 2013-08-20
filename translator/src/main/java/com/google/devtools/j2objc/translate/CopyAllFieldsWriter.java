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

import com.google.common.collect.Lists;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;

/**
 * Writes the copyAllFieldsTo method in order to support correct Java clone()
 * behavior.
 *
 * @author Keith Stanger
 */
public class CopyAllFieldsWriter extends ErrorReportingASTVisitor {

  private static final IOSMethod COPY_ALL_PROPERTIES =
      IOSMethod.create("NSObject copyAllFieldsTo:(id)other");

  // Binding for the declaration of copyAllFieldsTo in NSObject.
  private final IOSMethodBinding nsObjectCopyAll;

  public CopyAllFieldsWriter() {
    nsObjectCopyAll = IOSMethodBinding.newMethod(
        COPY_ALL_PROPERTIES, Modifier.PUBLIC, Types.resolveJavaType("void"),
        Types.resolveIOSType("NSObject"));
    nsObjectCopyAll.addParameter(Types.resolveIOSType("id"));
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    ITypeBinding type = Types.getTypeBinding(node);
    List<IVariableBinding> fields = getNonStaticFields(type);
    if (fields.size() == 0) {
      return;
    }

    AST ast = node.getAST();
    String typeName = NameTable.getFullName(type);
    IOSMethod iosMethod = IOSMethod.create(
        String.format("%s copyAllFieldsTo:(%s *)other", typeName, typeName));
    IOSMethodBinding methodBinding = IOSMethodBinding.newMethod(
        iosMethod, Modifier.PUBLIC, Types.resolveJavaType("void"), type);
    methodBinding.addParameter(type);
    GeneratedVariableBinding copyParamBinding = new GeneratedVariableBinding(
        "other", 0, type, false, true, null, methodBinding);

    MethodDeclaration declaration = ASTFactory.newMethodDeclaration(node.getAST(), methodBinding);
    ASTUtil.getBodyDeclarations(node).add(declaration);

    SingleVariableDeclaration copyParam =
        ASTFactory.newSingleVariableDeclaration(ast, copyParamBinding);
    ASTUtil.getParameters(declaration).add(copyParam);

    Block body = ast.newBlock();
    declaration.setBody(body);
    List<Statement> statements = ASTUtil.getStatements(body);

    SuperMethodInvocation superCall = ASTFactory.newSuperMethodInvocation(ast, nsObjectCopyAll);
    ASTUtil.getArguments(superCall).add(ASTFactory.newSimpleName(ast, copyParamBinding));
    statements.add(ast.newExpressionStatement(superCall));

    for (IVariableBinding field : fields) {
      statements.add(ast.newExpressionStatement(ASTFactory.newAssignment(ast,
          ASTFactory.newFieldAccess(ast, field, ASTFactory.newSimpleName(ast, copyParamBinding)),
          ASTFactory.newSimpleName(ast, field))));
    }
  }

  private static List<IVariableBinding> getNonStaticFields(ITypeBinding type) {
    List<IVariableBinding> fields = Lists.newArrayList();
    for (IVariableBinding field : type.getDeclaredFields()) {
      if (!BindingUtil.isStatic(field)) {
        fields.add(field);
      }
    }
    return fields;
  }
}
