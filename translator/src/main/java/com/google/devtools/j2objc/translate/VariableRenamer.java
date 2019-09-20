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
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Detects variable name collision scenarios and renames variables accordingly.
 *
 * @author Keith Stanger
 */
public class VariableRenamer extends UnitTreeVisitor {

  private List<Set<String>> fieldNameStack = new ArrayList<>();
  private Set<TypeElement> renamedTypes = new HashSet<>();

  public VariableRenamer(CompilationUnit unit) {
    super(unit);
  }

  private void collectAndRenameFields(TypeElement type, Set<VariableElement> fields) {
    if (type == null) {
      return;
    }
    collectAndRenameFields(ElementUtil.getSuperclass(type), fields);
    if (!renamedTypes.contains(type)) {
      renamedTypes.add(type);
      Set<String> superFieldNames = new HashSet<>();
      for (VariableElement superField : fields) {
        superFieldNames.add(superField.getSimpleName().toString());
      }
      // Look for methods that might conflict with a static variable when functionized.
      Set<String> staticMethodNames = new HashSet<>();
      for (ExecutableElement method : ElementUtil.getExecutables(type)) {
        if (method.getParameters().size() == 0) {
          staticMethodNames.add(nameTable.getFunctionName(method));
        }
      }
      for (VariableElement field : ElementUtil.getDeclaredFields(type)) {
        String fieldName = field.getSimpleName().toString();
        if (ElementUtil.isEnum(type) && ElementUtil.isStatic(field) && fieldName.equals("values")) {
          ErrorUtil.error(
              "\"values\" field in "
                  + type.getQualifiedName()
                  + " collides with the generated Enum values field. "
                  + "Consider using ObjectiveCName to rename it.");
        }
        if (ElementUtil.isGlobalVar(field)) {
          if (staticMethodNames.contains(fieldName)) {
            while (staticMethodNames.contains(fieldName)) {
              fieldName += "_";
            }
            nameTable.setVariableName(field, fieldName);
          }
        } else if (!ElementUtil.isStatic(field) && superFieldNames.contains(fieldName)) {
          fieldName += "_" + type.getSimpleName();
          nameTable.setVariableName(field, fieldName);
        }
      }
    }
    for (VariableElement field : ElementUtil.getDeclaredFields(type)) {
      if (!ElementUtil.isStatic(field)) {
        fields.add(field);
      }
    }
  }

  private void pushType(TypeElement type) {
    Set<VariableElement> fields = new HashSet<>();
    collectAndRenameFields(type, fields);
    Set<String> fullFieldNames = new HashSet<>();
    for (VariableElement field : fields) {
      fullFieldNames.add(nameTable.getVariableShortName(field));
    }
    fieldNameStack.add(fullFieldNames);
  }

  private void popType() {
    fieldNameStack.remove(fieldNameStack.size() - 1);
  }

  @Override
  public void endVisit(SimpleName node) {
    VariableElement var = TreeUtil.getVariableElement(node);
    if (var == null) {
      return;
    }
    if (var.getKind().isField()) {
      // Make sure fields for the declaring type are renamed.
      collectAndRenameFields(ElementUtil.getDeclaringClass(var), new HashSet<VariableElement>());
    } else {
      // Local variable or parameter. Rename if it shares a name with a field.
      String varName = ElementUtil.getName(var);
      assert fieldNameStack.size() > 0;
      Set<String> fieldNames = fieldNameStack.get(fieldNameStack.size() - 1);
      if (fieldNames.contains(varName)) {
        nameTable.setVariableName(var, varName + "Arg");
      }
    }
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    pushType(node.getTypeElement());
    return true;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    popType();
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    pushType(node.getTypeElement());
    return true;
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    popType();
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    pushType(node.getTypeElement());
    return true;
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    popType();
  }

  @Override
  public boolean visit(LambdaExpression node) {
    pushType(node.getTypeElement());
    return true;
  }

  @Override
  public void endVisit(LambdaExpression node) {
    popType();
  }
}
