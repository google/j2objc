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
import com.google.devtools.j2objc.ast.CompilationUnit;
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
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Adds fields and properties to annotation types.
 * Generates reflection methods to provide the runtime annotations on types,
 * methods and fields.
 */
public class AnnotationRewriter extends UnitTreeVisitor {

  public AnnotationRewriter(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    TypeElement type = node.getTypeElement();
    if (!ElementUtil.isRuntimeAnnotation(type)) {
      return;
    }
    List<AnnotationTypeMemberDeclaration> members = TreeUtil.getAnnotationMembers(node);
    List<BodyDeclaration> bodyDecls = node.getBodyDeclarations();

    Map<ExecutableElement, VariableElement> fieldElements = createMemberFields(node, members);
    addMemberProperties(node, members, fieldElements);
    addDefaultAccessors(node, members);
    bodyDecls.add(createAnnotationTypeMethod(type));
    bodyDecls.add(createDescriptionMethod(type));
    addConstructor(node, fieldElements);
  }

  // Create an instance field for each member.
  private Map<ExecutableElement, VariableElement> createMemberFields(
      AnnotationTypeDeclaration node, List<AnnotationTypeMemberDeclaration> members) {
    TypeElement type = node.getTypeElement();
    Map<ExecutableElement, VariableElement> fieldElements = new HashMap<>();
    for (AnnotationTypeMemberDeclaration member : members) {
      ExecutableElement memberElement = member.getExecutableElement();
      String propName = NameTable.getAnnotationPropertyName(memberElement);
      VariableElement field = GeneratedVariableElement.newField(
          propName, memberElement.getReturnType(), type);
      node.addBodyDeclaration(new FieldDeclaration(field, null));
      fieldElements.put(memberElement, field);
    }
    return fieldElements;
  }

  // Generate the property declarations and synthesize statements.
  private void addMemberProperties(
      AnnotationTypeDeclaration node, List<AnnotationTypeMemberDeclaration> members,
      Map<ExecutableElement, VariableElement> fieldElements) {
    if (members.isEmpty()) {
      return;
    }
    StringBuilder propertyDecls = new StringBuilder();
    StringBuilder propertyImpls = new StringBuilder();
    for (AnnotationTypeMemberDeclaration member : members) {
      ExecutableElement memberElement = member.getExecutableElement();
      String propName = NameTable.getAnnotationPropertyName(memberElement);
      String memberTypeStr = nameTable.getObjCType(memberElement.getReturnType());

      String fieldName = nameTable.getVariableShortName(fieldElements.get(memberElement));
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
    TypeElement type = node.getTypeElement();
    for (AnnotationTypeMemberDeclaration member : members) {
      ExecutableElement memberElement = member.getExecutableElement();
      AnnotationValue defaultValue = memberElement.getDefaultValue();
      if (defaultValue == null || defaultValue.getValue() == null) {
        continue;
      }

      TypeMirror memberType = memberElement.getReturnType();
      String propName = NameTable.getAnnotationPropertyName(memberElement);

      ExecutableElement defaultGetterElement = GeneratedExecutableElement.newMethodWithSelector(
          propName + "Default", memberType, type)
          .addModifiers(Modifier.STATIC);
      MethodDeclaration defaultGetter = new MethodDeclaration(defaultGetterElement);
      defaultGetter.setHasDeclaration(false);
      Block defaultGetterBody = new Block();
      defaultGetter.setBody(defaultGetterBody);
      defaultGetterBody.addStatement(new ReturnStatement(
          translationUtil.createAnnotationValue(memberType, defaultValue)));
      node.addBodyDeclaration(defaultGetter);
    }
  }

  private void addConstructor(
      AnnotationTypeDeclaration node, Map<ExecutableElement, VariableElement> fieldElements) {
    TypeElement type = node.getTypeElement();
    String typeName = nameTable.getFullName(type);
    FunctionDeclaration constructorDecl =
        new FunctionDeclaration("create_" + typeName, type.asType());
    Block constructorBody = new Block();
    constructorDecl.setBody(constructorBody);
    List<Statement> stmts = constructorBody.getStatements();

    stmts.add(new NativeStatement(UnicodeUtils.format(
        "%s *self = AUTORELEASE([[%s alloc] init]);", typeName, typeName)));

    for (ExecutableElement memberElement : ElementUtil.getSortedAnnotationMembers(type)) {
      TypeMirror memberType = memberElement.getReturnType();
      String propName = NameTable.getAnnotationPropertyName(memberElement);
      String fieldName = nameTable.getVariableShortName(fieldElements.get(memberElement));

      VariableElement param = GeneratedVariableElement.newParameter(propName, memberType, null);
      constructorDecl.addParameter(new SingleVariableDeclaration(param));
      String rhs = TypeUtil.isReferenceType(memberType) ? "RETAIN_(" + propName + ")" : propName;
      stmts.add(new NativeStatement("self->" + fieldName + " = " + rhs + ";"));
    }

    stmts.add(new NativeStatement("return self;"));
    node.addBodyDeclaration(constructorDecl);
  }

  private MethodDeclaration createAnnotationTypeMethod(TypeElement type) {
    ExecutableElement annotationTypeElement = GeneratedExecutableElement.newMethodWithSelector(
        "annotationType", typeUtil.getJavaClass().asType(), type);
    MethodDeclaration annotationTypeMethod = new MethodDeclaration(annotationTypeElement);
    annotationTypeMethod.setHasDeclaration(false);
    Block annotationTypeBody = new Block();
    annotationTypeMethod.setBody(annotationTypeBody);
    annotationTypeBody.addStatement(new ReturnStatement(new TypeLiteral(type.asType(), typeUtil)));
    return annotationTypeMethod;
  }

  private MethodDeclaration createDescriptionMethod(TypeElement type) {
    ExecutableElement descriptionElement = GeneratedExecutableElement.newMethodWithSelector(
        "description", typeUtil.getJavaString().asType(), type);
    MethodDeclaration descriptionMethod = new MethodDeclaration(descriptionElement);
    descriptionMethod.setHasDeclaration(false);
    Block descriptionBody = new Block();
    descriptionMethod.setBody(descriptionBody);
    descriptionBody.addStatement(new ReturnStatement(
        new StringLiteral("@" + elementUtil.getBinaryName(type) + "()", typeUtil)));
    return descriptionMethod;
  }
}
