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
import com.google.devtools.j2objc.ast.ArrayCreation;
import com.google.devtools.j2objc.ast.ArrayInitializer;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NullLiteral;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.types.FunctionBinding;
import com.google.devtools.j2objc.types.GeneratedTypeBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that generates the functions that return runtime annotations.
 */
class RuntimeAnnotationGenerator {

  private static final GeneratedTypeBinding ANNOTATION_TYPE = GeneratedTypeBinding.newTypeBinding(
      "java.lang.annotation.Annotation", null, true);

  private final AbstractTypeDeclaration typeNode;
  private final Types typeEnv;
  private final NameTable nameTable;
  private final String className;

  private int funcCount = 0;

  public RuntimeAnnotationGenerator(AbstractTypeDeclaration typeNode) {
    this.typeNode = typeNode;
    CompilationUnit unit = TreeUtil.getCompilationUnit(typeNode);
    typeEnv = unit.getTypeEnv();
    nameTable = unit.getNameTable();
    className = nameTable.getFullName(typeNode.getTypeBinding());
  }

  /**
   * Generate a function that returns the annotations for a BodyDeclarations node.
   */
  public String createFunction(BodyDeclaration decl) {
    List<Annotation> runtimeAnnotations = TreeUtil.getRuntimeAnnotationsList(decl.getAnnotations());
    if (runtimeAnnotations.isEmpty()) {
      return null;
    }
    return addFunction(createAnnotations(runtimeAnnotations));
  }

  /**
   * Generate a function that returns the 2-dimentional array of annotations for method parameters.
   */
  public String createParamsFunction(MethodDeclaration method) {
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
      return null;
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

    return addFunction(createObjectArray(
        subArrays, GeneratedTypeBinding.newArrayType(ANNOTATION_TYPE)));
  }

  private String addFunction(Expression result) {
    String name = className + "__Annotations$" + funcCount++;
    FunctionDeclaration decl = new FunctionDeclaration(
        name, result.getTypeBinding(), typeNode.getTypeBinding());
    decl.addModifiers(Modifier.PRIVATE);
    Block body = new Block();
    decl.setBody(body);
    body.addStatement(new ReturnStatement(result));
    typeNode.addBodyDeclaration(decl);
    return name;
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
      binding.addParameters(valueBinding.getMethodBinding().getReturnType());
      invocation.addArgument(createAnnotationValue(valueBinding.getValue()));
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
}
