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

package com.google.devtools.j2objc.gen;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The base class for TypeDeclarationGenerator and TypeImplementationGenerator,
 * providing common routines.
 *
 * @author Tom Ball, Keith Stanger
 */
public abstract class TypeGenerator extends AbstractSourceGenerator {

  // TODO(kstanger): Make this accessible via a getter method
  protected final AbstractTypeDeclaration node;

  private final List<BodyDeclaration> declarations;

  protected TypeGenerator(SourceBuilder builder, AbstractTypeDeclaration node) {
    super(builder);
    this.node = node;
    this.declarations = filterDeclarations(node.getBodyDeclarations());
  }

  protected boolean shouldPrintDeclaration(BodyDeclaration decl) {
    return true;
  }

  private List<BodyDeclaration> filterDeclarations(Iterable<BodyDeclaration> declarations) {
    List<BodyDeclaration> filteredDecls = Lists.newArrayList();
    for (BodyDeclaration decl : declarations) {
      if (shouldPrintDeclaration(decl)) {
        filteredDecls.add(decl);
      }
    }
    return filteredDecls;
  }

  private static final Predicate<BodyDeclaration> IS_STATIC = new Predicate<BodyDeclaration>() {
    public boolean apply(BodyDeclaration decl) {
      return Modifier.isStatic(decl.getModifiers());
    }
  };

  private static final Predicate<BodyDeclaration> NOT_STATIC = Predicates.not(IS_STATIC);

  private static final Predicate<BodyDeclaration> IS_OUTER_DECL = new Predicate<BodyDeclaration>() {
    public boolean apply(BodyDeclaration decl) {
      return decl instanceof FunctionDeclaration;
    }
  };

  private static final Predicate<BodyDeclaration> IS_INNER_DECL = new Predicate<BodyDeclaration>() {
    public boolean apply(BodyDeclaration decl) {
      switch (decl.getKind()) {
        case METHOD_DECLARATION:
        case NATIVE_DECLARATION:
          return true;
      }
      return false;
    }
  };

  protected abstract void printFunctionDeclaration(FunctionDeclaration decl);
  protected abstract void printMethodDeclaration(MethodDeclaration decl);
  protected abstract void printNativeDeclaration(NativeDeclaration decl);

  private void printDeclaration(BodyDeclaration declaration) {
    switch (declaration.getKind()) {
      case FUNCTION_DECLARATION:
        printFunctionDeclaration((FunctionDeclaration) declaration);
        return;
      case METHOD_DECLARATION:
        printMethodDeclaration((MethodDeclaration) declaration);
        return;
      case NATIVE_DECLARATION:
        printNativeDeclaration((NativeDeclaration) declaration);
        return;
      default:
        break;
    }
  }

  protected void printDeclarations(Iterable<? extends BodyDeclaration> declarations) {
    for (BodyDeclaration declaration : declarations) {
      printDeclaration(declaration);
    }
  }

  protected boolean isInterfaceType() {
    return node.getTypeBinding().isInterface();
  }

  protected static boolean hasPrivateDeclaration(BodyDeclaration decl) {
    if (Options.hidePrivateMembers() || decl instanceof FunctionDeclaration) {
      return Modifier.isPrivate(decl.getModifiers());
    }
    return false;
  }

  protected Iterable<FieldDeclaration> getInstanceFields() {
    if (isInterfaceType()) {
      return Collections.emptyList();
    }
    return Iterables.filter(Iterables.filter(declarations, FieldDeclaration.class), NOT_STATIC);
  }

  protected Iterable<FieldDeclaration> getStaticFields() {
    Iterable<FieldDeclaration> fieldDecls = Iterables.filter(declarations, FieldDeclaration.class);
    // All variables declared in interface types are static.
    if (!isInterfaceType()) {
      fieldDecls = Iterables.filter(fieldDecls, IS_STATIC);
    }
    return fieldDecls;
  }

  protected Iterable<BodyDeclaration> getInnerDeclarations() {
    return Iterables.filter(declarations, IS_INNER_DECL);
  }

  protected Iterable<BodyDeclaration> getOuterDeclarations() {
    return Iterables.filter(declarations, IS_OUTER_DECL);
  }

  protected void printInnerDeclarations() {
    printDeclarations(getInnerDeclarations());
  }

  protected void printOuterDeclarations() {
    printDeclarations(getOuterDeclarations());
  }

  protected boolean hasInitializeMethod() {
    return !node.getClassInitStatements().isEmpty();
  }

  /**
   * Create an Objective-C method signature string.
   */
  protected String getMethodSignature(MethodDeclaration m) {
    StringBuilder sb = new StringBuilder();
    IMethodBinding binding = m.getMethodBinding();
    char prefix = Modifier.isStatic(m.getModifiers()) ? '+' : '-';
    String returnType = NameTable.getObjCType(binding.getReturnType());
    String selector = NameTable.getMethodSelector(binding);
    if (m.isConstructor()) {
      returnType = "instancetype";
    } else if (selector.equals("hash")) {
      // Explicitly test hashCode() because of NSObject's hash return value.
      returnType = "NSUInteger";
    }
    sb.append(String.format("%c (%s)", prefix, returnType));

    List<SingleVariableDeclaration> params = m.getParameters();
    String[] selParts = selector.split(":");

    if (params.isEmpty()) {
      assert selParts.length == 1 && !selector.endsWith(":");
      sb.append(selParts[0]);
    } else {
      assert params.size() == selParts.length;
      int baseLength = sb.length() + selParts[0].length();
      for (int i = 0; i < params.size(); i++) {
        if (i != 0) {
          sb.append('\n');
          sb.append(pad(baseLength - selParts[i].length()));
        }
        IVariableBinding var = params.get(i).getVariableBinding();
        String typeName = NameTable.getSpecificObjCType(var.getType());
        sb.append(String.format("%s:(%s)%s", selParts[i], typeName, NameTable.getName(var)));
      }
    }

    return sb.toString();
  }

  protected String getFunctionSignature(FunctionDeclaration function) {
    StringBuilder sb = new StringBuilder();
    String returnType = NameTable.getObjCType(function.getReturnType().getTypeBinding());
    returnType += returnType.endsWith("*") ? "" : " ";
    sb.append(returnType).append(function.getName()).append('(');
    for (Iterator<SingleVariableDeclaration> iter = function.getParameters().iterator();
         iter.hasNext(); ) {
      IVariableBinding var = iter.next().getVariableBinding();
      String paramType = NameTable.getSpecificObjCType(var.getType());
      paramType += (paramType.endsWith("*") ? "" : " ");
      sb.append(paramType + NameTable.getName(var));
      if (iter.hasNext()) {
        sb.append(", ");
      }
    }
    sb.append(')');
    return sb.toString();
  }

  /**
   * Create an Objective-C constructor from a list of annotation member
   * declarations.
   */
  protected String getAnnotationConstructorSignature(ITypeBinding annotation) {
    StringBuffer sb = new StringBuffer();
    sb.append("- (instancetype)init");
    IMethodBinding[] members = BindingUtil.getSortedAnnotationMembers(annotation);
    for (int i = 0; i < members.length; i++) {
      if (i == 0) {
        sb.append("With");
      } else {
        sb.append(" with");
      }
      IMethodBinding member = members[i];
      String name = NameTable.getAnnotationPropertyName(member);
      sb.append(NameTable.capitalize(name));
      sb.append(":(");
      sb.append(NameTable.getSpecificObjCType(member.getReturnType()));
      sb.append(')');
      sb.append(name);
      sb.append("__");
    }
    return sb.toString();
  }

  protected String generateExpression(Expression expr) {
    return StatementGenerator.generate(expr, false, getBuilder().getCurrentLine());
  }
}
