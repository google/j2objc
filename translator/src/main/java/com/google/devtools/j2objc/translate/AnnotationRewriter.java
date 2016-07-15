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
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.UnicodeUtils;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adds fields and properties to annotation types.
 * Generates reflection methods to provide the runtime annotations on types,
 * methods and fields.
 */
public class AnnotationRewriter extends TreeVisitor {

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    ITypeBinding type = node.getTypeBinding();
    if (!BindingUtil.isRuntimeAnnotation(type)) {
      return;
    }
    List<AnnotationTypeMemberDeclaration> members = TreeUtil.getAnnotationMembers(node);
    List<BodyDeclaration> bodyDecls = node.getBodyDeclarations();

    Map<IMethodBinding, IVariableBinding> fieldBindings = createMemberFields(node, members);
    addMemberProperties(node, members, fieldBindings);
    addDefaultAccessors(node, members);
    bodyDecls.add(createAnnotationTypeMethod(type));
    bodyDecls.add(createDescriptionMethod(type));
    addConstructor(node, fieldBindings);
  }

  // Create an instance field for each member.
  private Map<IMethodBinding, IVariableBinding> createMemberFields(
      AnnotationTypeDeclaration node, List<AnnotationTypeMemberDeclaration> members) {
    ITypeBinding type = node.getTypeBinding();
    Map<IMethodBinding, IVariableBinding> fieldBindings = new HashMap<>();
    for (AnnotationTypeMemberDeclaration member : members) {
      IMethodBinding memberBinding = (IMethodBinding)
          BindingConverter.unwrapElement(member.getElement());
      ITypeBinding memberType = memberBinding.getReturnType();
      String propName = NameTable.getAnnotationPropertyName(memberBinding);
      GeneratedVariableBinding field = new GeneratedVariableBinding(
          propName, BindingUtil.ACC_SYNTHETIC, memberType, true, false, type, null);
      node.addBodyDeclaration(new FieldDeclaration(field, null));
      fieldBindings.put(memberBinding, field);
    }
    return fieldBindings;
  }

  // Generate the property declarations and synthesize statements.
  private void addMemberProperties(
      AnnotationTypeDeclaration node, List<AnnotationTypeMemberDeclaration> members,
      Map<IMethodBinding, IVariableBinding> fieldBindings) {
    if (members.isEmpty()) {
      return;
    }
    StringBuilder propertyDecls = new StringBuilder();
    StringBuilder propertyImpls = new StringBuilder();
    for (AnnotationTypeMemberDeclaration member : members) {
      IMethodBinding memberBinding = (IMethodBinding)
          BindingConverter.unwrapElement(member.getElement());
      ITypeBinding memberType = memberBinding.getReturnType();
      String propName = NameTable.getAnnotationPropertyName(memberBinding);
      String memberTypeStr = nameTable.getObjCType(memberType);

      String fieldName = nameTable.getVariableShortName(fieldBindings.get(memberBinding));
      propertyDecls.append(UnicodeUtils.format("@property (readonly) %s%s%s;\n",
          memberTypeStr, memberTypeStr.endsWith("*") ? "" : " ", propName));
      if (NameTable.needsObjcMethodFamilyNoneAttribute(propName)) {
        propertyDecls.append(UnicodeUtils.format(
            "- (%s)%s OBJC_METHOD_FAMILY_NONE;\n", memberTypeStr, propName));
      }
      propertyImpls.append(UnicodeUtils.format("@synthesize %s = %s;\n", propName, fieldName));
    }
    node.addBodyDeclaration(NativeDeclaration.newInnerDeclaration(
        propertyDecls.toString(), propertyImpls.toString()));
  }

  // Create accessors for properties that have default values.
  private void addDefaultAccessors(
      AnnotationTypeDeclaration node, List<AnnotationTypeMemberDeclaration> members) {
    ITypeBinding type = node.getTypeBinding();
    for (AnnotationTypeMemberDeclaration member : members) {
      Expression defaultExpr = member.getDefault();
      if (defaultExpr == null) {
        continue;
      }

      IMethodBinding memberBinding = (IMethodBinding)
          BindingConverter.unwrapElement(member.getElement());
      ITypeBinding memberType = memberBinding.getReturnType();
      String propName = NameTable.getAnnotationPropertyName(memberBinding);

      GeneratedMethodBinding defaultGetterBinding = GeneratedMethodBinding.newMethod(
          propName + "Default", Modifier.STATIC | BindingUtil.ACC_SYNTHETIC, memberType, type);
      MethodDeclaration defaultGetter = new MethodDeclaration(defaultGetterBinding);
      defaultGetter.setHasDeclaration(false);
      Block defaultGetterBody = new Block();
      defaultGetter.setBody(defaultGetterBody);
      defaultGetterBody.addStatement(new ReturnStatement(TreeUtil.remove(defaultExpr)));
      node.addBodyDeclaration(defaultGetter);
    }
  }

  private void addConstructor(
      AnnotationTypeDeclaration node, Map<IMethodBinding, IVariableBinding> fieldBindings) {
    ITypeBinding type = node.getTypeBinding();
    String typeName = nameTable.getFullName(type);
    FunctionDeclaration constructorDecl = new FunctionDeclaration("create_" + typeName, type, type);
    Block constructorBody = new Block();
    constructorDecl.setBody(constructorBody);
    List<Statement> stmts = constructorBody.getStatements();

    stmts.add(new NativeStatement(UnicodeUtils.format(
        "%s *self = AUTORELEASE([[%s alloc] init]);", typeName, typeName)));

    for (IMethodBinding memberBinding : BindingUtil.getSortedAnnotationMembers(type)) {
      ITypeBinding memberType = memberBinding.getReturnType();
      String propName = NameTable.getAnnotationPropertyName(memberBinding);
      String fieldName = nameTable.getVariableShortName(fieldBindings.get(memberBinding));

      GeneratedVariableBinding param = new GeneratedVariableBinding(
          propName, 0, memberType, false, true, null, null);
      constructorDecl.addParameter(new SingleVariableDeclaration(param));
      String rhs = memberType.isPrimitive() ? propName : "RETAIN_(" + propName + ")";
      stmts.add(new NativeStatement("self->" + fieldName + " = " + rhs + ";"));
    }

    stmts.add(new NativeStatement("return self;"));
    node.addBodyDeclaration(constructorDecl);
  }

  private MethodDeclaration createAnnotationTypeMethod(ITypeBinding type) {
    GeneratedMethodBinding annotationTypeBinding = GeneratedMethodBinding.newMethod(
        "annotationType", BindingUtil.ACC_SYNTHETIC, typeEnv.getIOSClass(), type);
    MethodDeclaration annotationTypeMethod = new MethodDeclaration(annotationTypeBinding);
    annotationTypeMethod.setHasDeclaration(false);
    Block annotationTypeBody = new Block();
    annotationTypeMethod.setBody(annotationTypeBody);
    annotationTypeBody.addStatement(new ReturnStatement(new TypeLiteral(type, typeEnv)));
    return annotationTypeMethod;
  }

  private MethodDeclaration createDescriptionMethod(ITypeBinding type) {
    GeneratedMethodBinding descriptionBinding = GeneratedMethodBinding.newMethod(
        "description", BindingUtil.ACC_SYNTHETIC, typeEnv.getNSString(), type);
    MethodDeclaration descriptionMethod = new MethodDeclaration(descriptionBinding);
    descriptionMethod.setHasDeclaration(false);
    Block descriptionBody = new Block();
    descriptionMethod.setBody(descriptionBody);
    descriptionBody.addStatement(new ReturnStatement(
        new StringLiteral("@" + type.getBinaryName() + "()", typeEnv)));
    return descriptionMethod;
  }
}
