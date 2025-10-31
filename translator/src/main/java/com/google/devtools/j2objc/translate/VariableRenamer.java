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

import com.google.common.collect.ImmutableList;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CatchClause;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnhancedForStatement;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.ForStatement;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.RecordDeclaration;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Detects variable name collision scenarios and renames variables accordingly.
 *
 * @author Keith Stanger
 */
@SuppressWarnings("UngroupedOverloads")
public class VariableRenamer extends UnitTreeVisitor {

  private final Deque<Set<String>> fieldNameStack = new ArrayDeque<>();
  private final Set<TypeElement> renamedTypes = new HashSet<>();
  // Keep track of the variables and names in a scope so we can rename variables in a
  // scope-aware manner; start with a sentinel empty scope.
  private final Deque<Scope> scopes = new ArrayDeque<>(ImmutableList.of(new Scope()));

  // Keep track of variables that are in scope and the names that have already been used.
  private static class Scope {
    private final Set<VariableElement> variables = new HashSet<>();
    private final Set<String> usedNames = new HashSet<>();

    private Scope(Scope enclosingScope) {
      this.variables.addAll(enclosingScope.variables);
      this.usedNames.addAll(enclosingScope.usedNames);
    }

    private Scope() {}

    /**
     * Called when a variable reference has been seen.
     *
     * @return true if its the variable needs renaming, false if it is seen for the first time.
     */
    private boolean needsRenaming(VariableElement variable) {
      if (!variables.add(variable)) {
        // Variable has already been seen in the scope so its name was already resolved.
        return false;
      }
      return !usedNames.add(ElementUtil.getName(variable));
    }
  }

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
    fieldNameStack.push(fullFieldNames);
    // A nested type defines a new nested scope. The used names from the enclosing scope are kept
    // since the nested type can reference variables from the enclosing scope.
    Scope newScope = enterVariableScope();
    // Mark all the field names as unavailable for renaming.
    newScope.usedNames.addAll(fullFieldNames);
  }

  private void popType() {
    fieldNameStack.pop();
    scopes.pop();
  }

  @CanIgnoreReturnValue
  private Scope enterVariableScope() {
    Scope newScope = new Scope(scopes.peek());
    scopes.push(newScope);
    return newScope;
  }

  private void exitVariableScope() {
    scopes.pop();
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
      // Local variable or parameter.
      handleVariable(var);
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

  @Override
  public boolean visit(RecordDeclaration node) {
    pushType(node.getTypeElement());
    return true;
  }

  @Override
  public void endVisit(RecordDeclaration node) {
    popType();
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    enterVariableScope();
    return true;
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    exitVariableScope();
  }

  @Override
  public boolean visit(Block node) {
    enterVariableScope();
    return true;
  }

  @Override
  public void endVisit(Block node) {
    exitVariableScope();
  }

  @Override
  public boolean visit(ForStatement node) {
    enterVariableScope();
    return true;
  }

  @Override
  public void endVisit(ForStatement node) {
    exitVariableScope();
  }

  @Override
  public boolean visit(EnhancedForStatement node) {
    enterVariableScope();
    return true;
  }

  @Override
  public void endVisit(EnhancedForStatement node) {
    exitVariableScope();
  }

  @Override
  public boolean visit(CatchClause node) {
    enterVariableScope();
    return true;
  }

  @Override
  public void endVisit(CatchClause node) {
    exitVariableScope();
  }

  @Override
  public boolean visit(SingleVariableDeclaration variableDeclaration) {
    handleVariable(variableDeclaration.getVariableElement());
    return true;
  }

  @Override
  public boolean visit(VariableDeclarationFragment variableDeclaration) {
    handleVariable(variableDeclaration.getVariableElement());
    return true;
  }

  private void handleVariable(VariableElement variable) {
    if (variable.getKind().isField()) {
      return;
    }
    Scope scope = scopes.peek();
    if (scope.needsRenaming(variable)) {
      // Variable needs renaming. Preserve the logic that renames parameters with "Arg" suffix.
      String baseName = ElementUtil.getName(variable) + (isParameter(variable) ? "Arg" : "");
      String suffix = "";
      int count = 1;
      while (scope.usedNames.contains(baseName + suffix)) {
        suffix = "_" + count++;
      }
      String newName = baseName + suffix;
      nameTable.setVariableName(variable, newName);
      scope.usedNames.add(newName);
    }
  }

  private boolean isParameter(VariableElement variable) {
    Element element = variable.getEnclosingElement();
    if (element instanceof ExecutableElement executableElement) {
      return executableElement.getParameters().contains(variable);
    }
    return false;
  }
}
