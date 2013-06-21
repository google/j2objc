/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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

import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;

import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.List;

/**
 * ObjectiveCTypeConverter: converts code that references core Java types to
 * similar iOS Foundation types. For example, Object maps to NSObject, and
 * String to NSString. Arrays are also converted, but because their contents are
 * fixed-size and contain nulls, custom classes are used.
 *
 * @author Tom Ball
 */
public class JavaToIOSTypeConverter extends ErrorReportingASTVisitor {

  // TODO(user): Replace visitors below with visitors on the subclasses of Type.
  private void convertType(Type original) {
    if (original == null) {
      return;
    }
    ITypeBinding binding = Types.getTypeBinding(original);
    ITypeBinding newBinding = Types.mapType(binding);
    if (binding != newBinding) {
      ASTUtil.setProperty(original, ASTFactory.newType(original.getAST(), newBinding));
    }
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    ITypeBinding binding = Types.getTypeBinding(node);
    assert binding == Types.mapType(binding); // don't try to translate the
                                                 // types being mapped
    if (!node.isInterface()) {
      Type superClass = node.getSuperclassType();
      if (superClass == null) {
        node.setSuperclassType(ASTFactory.newType(node.getAST(), Types.getNSObject()));
      } else {
        convertType(superClass);
      }
    }
    List<Type> interfaces = ASTUtil.getSuperInterfaceTypes(node);
    for (int i = 0; i < interfaces.size(); i++) {
      convertType(interfaces.get(i));
    }
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    convertType(node.getReturnType2());
    for (SingleVariableDeclaration parameter : ASTUtil.getParameters(node)) {
      convertType(parameter.getType());
    }
  }

  @Override
  public void endVisit(FieldDeclaration node) {
    convertType(node.getType());
  }

  @Override
  public void endVisit(SingleVariableDeclaration node) {
    convertType(node.getType());
  }

  @Override
  public void endVisit(VariableDeclarationStatement node) {
    convertType(node.getType());
  }

  @Override
  public void endVisit(CastExpression node) {
    convertType(node.getType());
  }
}
