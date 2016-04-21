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

import com.google.common.collect.Iterables;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.ArrayCreation;
import com.google.devtools.j2objc.ast.ArrayInitializer;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.NullLiteral;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.FunctionBinding;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedTypeBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adds fields and properties to annotation types.
 * Generates reflection methods to provide the runtime annotations on types,
 * methods and fields.
 */
public class AnnotationRewriter extends TreeVisitor {

  private static final GeneratedTypeBinding ANNOTATION_TYPE = GeneratedTypeBinding.newTypeBinding(
      "java.lang.annotation.Annotation", null, true);

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    addAnnotationReflectionMethods(node);

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
      IMethodBinding memberBinding = member.getMethodBinding();
      ITypeBinding memberType = memberBinding.getReturnType();
      String propName = NameTable.getAnnotationPropertyName(memberBinding);
      GeneratedVariableBinding field = new GeneratedVariableBinding(
          propName, BindingUtil.ACC_SYNTHETIC, memberType, true, false, type, null);
      node.getBodyDeclarations().add(new FieldDeclaration(field, null));
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
      IMethodBinding memberBinding = member.getMethodBinding();
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
    node.getBodyDeclarations().add(NativeDeclaration.newInnerDeclaration(
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

      IMethodBinding memberBinding = member.getMethodBinding();
      ITypeBinding memberType = memberBinding.getReturnType();
      String propName = NameTable.getAnnotationPropertyName(memberBinding);

      GeneratedMethodBinding defaultGetterBinding = GeneratedMethodBinding.newMethod(
          propName + "Default", Modifier.STATIC | BindingUtil.ACC_SYNTHETIC, memberType, type);
      MethodDeclaration defaultGetter = new MethodDeclaration(defaultGetterBinding);
      defaultGetter.setHasDeclaration(false);
      Block defaultGetterBody = new Block();
      defaultGetter.setBody(defaultGetterBody);
      defaultGetterBody.getStatements().add(new ReturnStatement(TreeUtil.remove(defaultExpr)));
      node.getBodyDeclarations().add(defaultGetter);
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
      constructorDecl.getParameters().add(new SingleVariableDeclaration(param));
      String rhs = memberType.isPrimitive() ? propName : "RETAIN_(" + propName + ")";
      stmts.add(new NativeStatement("self->" + fieldName + " = " + rhs + ";"));
    }

    stmts.add(new NativeStatement("return self;"));
    node.getBodyDeclarations().add(constructorDecl);
  }

  private MethodDeclaration createAnnotationTypeMethod(ITypeBinding type) {
    GeneratedMethodBinding annotationTypeBinding = GeneratedMethodBinding.newMethod(
        "annotationType", BindingUtil.ACC_SYNTHETIC, typeEnv.getIOSClass(), type);
    MethodDeclaration annotationTypeMethod = new MethodDeclaration(annotationTypeBinding);
    annotationTypeMethod.setHasDeclaration(false);
    Block annotationTypeBody = new Block();
    annotationTypeMethod.setBody(annotationTypeBody);
    annotationTypeBody.getStatements().add(new ReturnStatement(new TypeLiteral(type, typeEnv)));
    return annotationTypeMethod;
  }

  private MethodDeclaration createDescriptionMethod(ITypeBinding type) {
    GeneratedMethodBinding descriptionBinding = GeneratedMethodBinding.newMethod(
        "description", BindingUtil.ACC_SYNTHETIC, typeEnv.getNSString(), type);
    MethodDeclaration descriptionMethod = new MethodDeclaration(descriptionBinding);
    descriptionMethod.setHasDeclaration(false);
    Block descriptionBody = new Block();
    descriptionMethod.setBody(descriptionBody);
    descriptionBody.getStatements().add(new ReturnStatement(
        new StringLiteral("@" + type.getBinaryName() + "()", typeEnv)));
    return descriptionMethod;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    addAnnotationReflectionMethods(node);
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    addAnnotationReflectionMethods(node);
  }

  private void addAnnotationReflectionMethods(AbstractTypeDeclaration node) {
    if (!TranslationUtil.needsReflection(node)) {
      return;
    }
    addTypeAnnotationMethod(node);
    addFieldAnnotationMethods(node);
    addMethodAnnotationMethods(node);
  }

  private void addTypeAnnotationMethod(AbstractTypeDeclaration node) {
    List<Annotation> runtimeAnnotations = TreeUtil.getRuntimeAnnotationsList(node.getAnnotations());
    if (!runtimeAnnotations.isEmpty()) {
      addMethod(node, "__annotations", createAnnotations(runtimeAnnotations));
    }
  }

  private void addFieldAnnotationMethods(AbstractTypeDeclaration node) {
    for (FieldDeclaration field : TreeUtil.getFieldDeclarations(node)) {
      List<Annotation> runtimeAnnotations =
          TreeUtil.getRuntimeAnnotationsList(field.getAnnotations());
      if (!runtimeAnnotations.isEmpty()) {
        for (VariableDeclarationFragment var : field.getFragments()) {
          String methodName = "__annotations_" + var.getName().getIdentifier() + "_";
          addMethod(node, methodName, createAnnotations(runtimeAnnotations));
        }
      }
    }
  }

  public void addMethodAnnotationMethods(AbstractTypeDeclaration node) {
    for (MethodDeclaration method : TreeUtil.getMethodDeclarations(node)) {
      List<Annotation> runtimeAnnotations =
          TreeUtil.getRuntimeAnnotationsList(method.getAnnotations());
      if (!runtimeAnnotations.isEmpty()) {
        String methodName = "__annotations_" + methodKey(method.getMethodBinding());
        addMethod(node, methodName, createAnnotations(runtimeAnnotations));
      }
      addParameterAnnotationMethods(node, method);
    }
  }

  private void addParameterAnnotationMethods(
      AbstractTypeDeclaration node, MethodDeclaration method) {
    List<SingleVariableDeclaration> params = method.getParameters();

    // Quick test to see if there are any parameter annotations.
    boolean hasAnnotations = false;
    for (SingleVariableDeclaration param : params) {
      if (!Iterables.isEmpty(TreeUtil.getRuntimeAnnotations(param.getAnnotations()))) {
        hasAnnotations = true;
        break;
      }
    }
    if (!hasAnnotations) {
      return;
    }

    List<Expression> subArrays = new ArrayList<>();
    for (SingleVariableDeclaration param : params) {
      List<Annotation> runtimeAnnotations =
          TreeUtil.getRuntimeAnnotationsList(param.getAnnotations());
      if (runtimeAnnotations.isEmpty()) {
        subArrays.add(new ArrayCreation(
            GeneratedTypeBinding.newArrayType(ANNOTATION_TYPE), typeEnv, 0));
      } else {
        subArrays.add(createAnnotations(runtimeAnnotations));
      }
    }

    String methodName = "__annotations_" + methodKey(method.getMethodBinding()) + "_params";
    addMethod(node, methodName, createObjectArray(
        subArrays, GeneratedTypeBinding.newArrayType(ANNOTATION_TYPE)));
  }

  private void addMethod(AbstractTypeDeclaration node, String name, Expression result) {
    GeneratedMethodBinding binding = GeneratedMethodBinding.newMethod(
        name, Modifier.STATIC | BindingUtil.ACC_SYNTHETIC, result.getTypeBinding(),
        node.getTypeBinding());
    MethodDeclaration decl = new MethodDeclaration(binding);
    decl.setHasDeclaration(false);
    Block body = new Block();
    decl.setBody(body);
    body.getStatements().add(new ReturnStatement(result));
    node.getBodyDeclarations().add(decl);
  }

  private Expression createAnnotations(List<Annotation> annotations) {
    List<Expression> expressions = new ArrayList<>();
    for (Annotation annotation : annotations) {
      expressions.add(createAnnotation(annotation.getAnnotationBinding()));
    }
    return createObjectArray(expressions, ANNOTATION_TYPE);
  }

  private Expression createObjectArray(List<Expression> expressions, ITypeBinding componentType) {
    ITypeBinding arrayType = GeneratedTypeBinding.newArrayType(componentType);
    ArrayCreation creation = new ArrayCreation(arrayType, typeEnv);
    ArrayInitializer initializer = new ArrayInitializer(arrayType);
    initializer.getExpressions().addAll(expressions);
    creation.setInitializer(initializer);
    return creation;
  }

  private Expression createAnnotation(IAnnotationBinding annotationBinding) {
    ITypeBinding annotationType = annotationBinding.getAnnotationType();
    FunctionBinding binding = new FunctionBinding(
        "create_" + nameTable.getFullName(annotationType), annotationType, annotationType);
    FunctionInvocation invocation = new FunctionInvocation(binding, annotationType);
    for (IMemberValuePairBinding valueBinding :
         BindingUtil.getSortedMemberValuePairs(annotationBinding)) {
      binding.addParameter(valueBinding.getMethodBinding().getReturnType());
      invocation.getArguments().add(createAnnotationValue(valueBinding.getValue()));
    }
    return invocation;
  }

  private Expression createAnnotationValue(Object value) {
    if (value == null) {
      return new NullLiteral();
    } else if (value instanceof IVariableBinding) {
      return new SimpleName((IVariableBinding) value);
    } else if (value instanceof ITypeBinding) {
      return new TypeLiteral((ITypeBinding) value, typeEnv);
    } else if (value instanceof IAnnotationBinding) {
      return createAnnotation((IAnnotationBinding) value);
    } else if (value instanceof Object[]) {
      Object[] array = (Object[]) value;
      List<Expression> generatedValues = new ArrayList<>();
      for (Object elem : array) {
        generatedValues.add(createAnnotationValue(elem));
      }
      return createObjectArray(generatedValues, typeEnv.getNSObject());
    } else {  // Boolean, Character, Number, String
      return TreeUtil.newLiteral(value, typeEnv);
    }
  }

  private String parameterKey(IMethodBinding method) {
    StringBuilder sb = new StringBuilder();
    ITypeBinding[] parameterTypes = method.getParameterTypes();
    for (int i = 0; i < parameterTypes.length; i++) {
      if (i == 0) {
        sb.append(NameTable.capitalize(nameTable.parameterKeyword(parameterTypes[i])));
      } else {
        sb.append(nameTable.parameterKeyword(parameterTypes[i]));
      }
      sb.append('_');
    }
    return sb.toString();
  }

  private String methodKey(IMethodBinding method) {
    StringBuilder sb = new StringBuilder(NameTable.getMethodName(method));
    sb.append(parameterKey(method));
    return sb.toString();
  }
}
