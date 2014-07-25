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
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;

/**
 * Writes the copyAllFieldsTo method in order to support correct Java clone()
 * behavior.
 *
 * @author Keith Stanger
 */
public class CopyAllFieldsWriter extends TreeVisitor {

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
    ITypeBinding type = node.getTypeBinding();
    List<IVariableBinding> fields = getNonStaticFields(type);
    if (fields.size() == 0) {
      return;
    }

    String typeName = NameTable.getFullName(type);
    IOSMethod iosMethod = IOSMethod.create(
        String.format("%s copyAllFieldsTo:(%s *)other", typeName, typeName));
    int modifiers = Modifier.PUBLIC | BindingUtil.ACC_SYNTHETIC;
    IOSMethodBinding methodBinding = IOSMethodBinding.newMethod(
        iosMethod, modifiers, Types.resolveJavaType("void"), type);
    methodBinding.addParameter(type);
    GeneratedVariableBinding copyParamBinding = new GeneratedVariableBinding(
        "other", 0, type, false, true, null, methodBinding);

    MethodDeclaration declaration = new MethodDeclaration(methodBinding);
    node.getBodyDeclarations().add(declaration);

    SingleVariableDeclaration copyParam = new SingleVariableDeclaration(copyParamBinding);
    declaration.getParameters().add(copyParam);

    Block body = new Block();
    declaration.setBody(body);
    List<Statement> statements = body.getStatements();

    SuperMethodInvocation superCall = new SuperMethodInvocation(nsObjectCopyAll);
    superCall.getArguments().add(new SimpleName(copyParamBinding));
    statements.add(new ExpressionStatement(superCall));

    for (IVariableBinding field : fields) {
      statements.add(new ExpressionStatement(new Assignment(
          new FieldAccess(field, new SimpleName(copyParamBinding)),
          new SimpleName(field))));
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
