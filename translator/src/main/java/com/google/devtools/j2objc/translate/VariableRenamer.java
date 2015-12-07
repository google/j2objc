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

import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnonymousClassDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.util.BindingUtil;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Detects variable name collision scenarios and renames variables accordingly.
 *
 * @author Keith Stanger
 */
public class VariableRenamer extends TreeVisitor {

  private List<Set<String>> fieldNameStack = new ArrayList<>();
  private Set<ITypeBinding> renamedTypes = new HashSet<>();

  private void collectAndRenameFields(ITypeBinding type, Set<IVariableBinding> fields) {
    if (type == null) {
      return;
    }
    type = type.getTypeDeclaration();
    collectAndRenameFields(type.getSuperclass(), fields);
    if (!renamedTypes.contains(type)) {
      renamedTypes.add(type);
      Set<String> superFieldNames = new HashSet<>();
      for (IVariableBinding superField : fields) {
        superFieldNames.add(superField.getName());
      }
      // Look for static methods that might conflict with a static variable when functionized.
      Set<String> staticMethodNames = new HashSet<>();
      for (IMethodBinding method : type.getDeclaredMethods()) {
        if (BindingUtil.isStatic(method) && method.getParameterTypes().length == 0) {
          staticMethodNames.add(nameTable.getFunctionName(method));
        }
      }
      for (IVariableBinding field : type.getDeclaredFields()) {
        String fieldName = field.getName();
        if (BindingUtil.isGlobalVar(field)) {
          if (staticMethodNames.contains(fieldName)) {
            while (staticMethodNames.contains(fieldName)) {
              fieldName += "_";
            }
            nameTable.setVariableName(field, fieldName);
          }
        } else if (!BindingUtil.isStatic(field) && superFieldNames.contains(fieldName)) {
          fieldName += "_" + type.getName();
          nameTable.setVariableName(field, fieldName);
        }
      }
    }
    for (IVariableBinding field : type.getDeclaredFields()) {
      if (!BindingUtil.isStatic(field)) {
        fields.add(field);
      }
    }
  }

  private void pushType(ITypeBinding type) {
    Set<IVariableBinding> fields = new HashSet<>();
    collectAndRenameFields(type, fields);
    Set<String> fullFieldNames = new HashSet<>();
    for (IVariableBinding field : fields) {
      fullFieldNames.add(nameTable.getVariableShortName(field));
    }
    fieldNameStack.add(fullFieldNames);
  }

  private void popType() {
    fieldNameStack.remove(fieldNameStack.size() - 1);
  }

  @Override
  public void endVisit(SimpleName node) {
    IVariableBinding var = TreeUtil.getVariableBinding(node);
    if (var == null) {
      return;
    }
    var = var.getVariableDeclaration();
    if (var.isField()) {
      // Make sure fields for the declaring type are renamed.
      collectAndRenameFields(var.getDeclaringClass(), new HashSet<IVariableBinding>());
    } else {
      // Local variable or parameter. Rename if it shares a name with a field.
      String varName = var.getName();
      assert fieldNameStack.size() > 0;
      Set<String> fieldNames = fieldNameStack.get(fieldNameStack.size() - 1);
      if (fieldNames.contains(varName)) {
        nameTable.setVariableName(var, varName + "Arg");
      }
    }
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    pushType(node.getTypeBinding());
    return true;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    popType();
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    pushType(node.getTypeBinding());
    return true;
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    popType();
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    pushType(node.getTypeBinding());
    return true;
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    popType();
  }

  @Override
  public boolean visit(AnonymousClassDeclaration node) {
    pushType(node.getTypeBinding());
    return true;
  }

  @Override
  public void endVisit(AnonymousClassDeclaration node) {
    popType();
  }

  @Override
  public boolean visit(LambdaExpression node) {
    pushType(node.getTypeBinding());
    return true;
  }

  @Override
  public void endVisit(LambdaExpression node) {
    popType();
  }
}
