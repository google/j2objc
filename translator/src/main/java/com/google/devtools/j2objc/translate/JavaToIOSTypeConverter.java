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

import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.types.Types;

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;

/**
 * ObjectiveCTypeConverter: converts code that references core Java types to
 * similar iOS Foundation types. For example, Object maps to NSObject, and
 * String to NSString. Arrays are also converted, but because their contents are
 * fixed-size and contain nulls, custom classes are used.
 *
 * @author Tom Ball
 */
public class JavaToIOSTypeConverter extends TreeVisitor {

  // TODO(kstanger): Replace visitors below with visitors on the subclasses of Type.
  private void convertType(Type original) {
    if (original == null) {
      return;
    }
    ITypeBinding binding = original.getTypeBinding();
    ITypeBinding newBinding = Types.mapType(binding);
    if (binding != newBinding) {
      original.replaceWith(Type.newType(newBinding));
    }
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    ITypeBinding binding = node.getTypeBinding();
    if (binding != Types.mapType(binding)) {
      // don't try to translate the types being mapped
      return;
    }

    if (!node.isInterface()) {
      Type superClass = node.getSuperclassType();
      if (superClass == null) {
        node.setSuperclassType(Type.newType(Types.getNSObject()));
      } else {
        convertType(superClass);
      }
    }
    List<Type> interfaces = node.getSuperInterfaceTypes();
    for (int i = 0; i < interfaces.size(); i++) {
      convertType(interfaces.get(i));
    }
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    convertType(node.getReturnType());
    for (SingleVariableDeclaration parameter : node.getParameters()) {
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
