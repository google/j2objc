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

import com.google.common.collect.Iterables;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.List;

/**
 * Generates the accessor methods used by JRE reflection code to get the runtime
 * annotations for types, methods and fields.
 *
 * @author Tom Ball, Keith Stanger
 */
public class RuntimeAnnotationGenerator extends AbstractSourceGenerator {

  private final NameTable nameTable;

  private RuntimeAnnotationGenerator(SourceBuilder builder, NameTable nameTable) {
    super(builder);
    this.nameTable = nameTable;
  }

  public static void printPackageAnnotationMethod(SourceBuilder builder, PackageDeclaration node) {
    new RuntimeAnnotationGenerator(builder, TreeUtil.getCompilationUnit(node).getNameTable())
        .printPackageAnnotationMethod(node);
  }

  public static void printTypeAnnotationMethods(
      SourceBuilder builder, AbstractTypeDeclaration node) {
    RuntimeAnnotationGenerator annotationGen = new RuntimeAnnotationGenerator(
        builder, TreeUtil.getCompilationUnit(node).getNameTable());
    annotationGen.printTypeAnnotationsMethod(node);
    annotationGen.printMethodAnnotationMethods(TreeUtil.getMethodDeclarations(node));
    annotationGen.printFieldAnnotationMethods(node);
  }

  public void printPackageAnnotationMethod(PackageDeclaration node) {
    List<Annotation> runtimeAnnotations = TreeUtil.getRuntimeAnnotationsList(node.getAnnotations());
    if (runtimeAnnotations.size() > 0) {
      println("\n+ (IOSObjectArray *)__annotations {");
      printAnnotationCreate(runtimeAnnotations);
    }
  }

  public void printTypeAnnotationsMethod(AbstractTypeDeclaration decl) {
    List<Annotation> runtimeAnnotations = TreeUtil.getRuntimeAnnotationsList(decl.getAnnotations());
    if (runtimeAnnotations.size() > 0) {
      println("\n+ (IOSObjectArray *)__annotations {");
      printAnnotationCreate(runtimeAnnotations);
    }
  }

  public void printMethodAnnotationMethods(Iterable<MethodDeclaration> methods) {
    for (MethodDeclaration method : methods) {
      List<Annotation> runtimeAnnotations =
          TreeUtil.getRuntimeAnnotationsList(method.getAnnotations());
      if (runtimeAnnotations.size() > 0) {
        printf("\n+ (IOSObjectArray *)__annotations_%s {\n", methodKey(method.getMethodBinding()));
        printAnnotationCreate(runtimeAnnotations);
      }
      printParameterAnnotationMethods(method);
    }
  }

  private void printParameterAnnotationMethods(MethodDeclaration method) {
    List<SingleVariableDeclaration> params = method.getParameters();

    // Quick test to see if there are any parameter annotations.
    boolean hasAnnotations = false;
    for (SingleVariableDeclaration param : params) {
      if (!Iterables.isEmpty(TreeUtil.getRuntimeAnnotations(param.getAnnotations()))) {
        hasAnnotations = true;
        break;
      }
    }

    if (hasAnnotations) {
      // Print array of arrays, with an element in the outer array for each parameter.
      printf("\n+ (IOSObjectArray *)__annotations_%s_params {\n",
          methodKey(method.getMethodBinding()));
      print("  return [IOSObjectArray arrayWithObjects:(id[]) { ");
      for (int i = 0; i < params.size(); i++) {
        if (i > 0) {
          print(", ");
        }
        SingleVariableDeclaration param = params.get(i);
        List<Annotation> runtimeAnnotations =
            TreeUtil.getRuntimeAnnotationsList(param.getAnnotations());
        if (runtimeAnnotations.size() > 0) {
          print("[IOSObjectArray arrayWithObjects:(id[]) { ");
          printAnnotations(runtimeAnnotations);
          printf(" } count:%d type:JavaLangAnnotationAnnotation_class_()]",
                 runtimeAnnotations.size());
        } else {
          print("[IOSObjectArray arrayWithLength:0 type:JavaLangAnnotationAnnotation_class_()]");
        }
      }
      printf(" } count:%d type:IOSClass_arrayOf("
          + "JavaLangAnnotationAnnotation_class_())];\n}\n", params.size());
    }
  }

  public void printFieldAnnotationMethods(AbstractTypeDeclaration node) {
    for (FieldDeclaration field : TreeUtil.getFieldDeclarations(node)) {
      List<Annotation> runtimeAnnotations =
          TreeUtil.getRuntimeAnnotationsList(field.getAnnotations());
      if (!runtimeAnnotations.isEmpty()) {
        for (VariableDeclarationFragment var : field.getFragments()) {
          printf("\n+ (IOSObjectArray *)__annotations_%s_ {\n", var.getName().getIdentifier());
          printAnnotationCreate(runtimeAnnotations);
        }
      }
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

  private void printAnnotationCreate(List<Annotation> runtimeAnnotations) {
    print("  return [IOSObjectArray arrayWithObjects:(id[]) { ");
    printAnnotations(runtimeAnnotations);
    printf(" } count:%d type:JavaLangAnnotationAnnotation_class_()];\n}\n",
           runtimeAnnotations.size());
  }

  private void printAnnotations(Iterable<Annotation> runtimeAnnotations) {
    boolean first = true;
    for (Annotation annotation : runtimeAnnotations) {
      if (first) {
        first = false;
      } else {
        print(", ");
      }
      printAnnotation(annotation.getAnnotationBinding());
    }
  }

  private void printAnnotation(IAnnotationBinding annotation) {
    if (Options.useReferenceCounting()) {
      print('[');
    }
    printf("[[%s alloc] init", nameTable.getFullName(annotation.getAnnotationType()));
    printAnnotationParameters(annotation);
    print(']');
    if (Options.useReferenceCounting()) {
      print(" autorelease]");
    }
  }

  // Prints an annotation's values as a constructor argument list. If
  // the annotation type declares default values, then for any value that
  // isn't specified in the annotation will use the default.
  private void printAnnotationParameters(IAnnotationBinding annotation) {
    IMemberValuePairBinding[] valueBindings = BindingUtil.getSortedMemberValuePairs(annotation);
    for (int i = 0; i < valueBindings.length; i++) {
      if (i > 0) {
        print(' ');
      }
      IMemberValuePairBinding valueBinding = valueBindings[i];
      print(i == 0 ? "With" : "with");
      printf("%s:", NameTable.capitalize(
          NameTable.getAnnotationPropertyName(valueBinding.getMethodBinding())));
      Object value = valueBinding.getValue();
      printAnnotationValue(value);
    }
  }

  private void printAnnotationValue(Object value) {
    if (value == null) {
      print("nil");
    } else if (value instanceof IVariableBinding) {
      IVariableBinding var = (IVariableBinding) value;
      ITypeBinding declaringClass = var.getDeclaringClass();
      printf("%s_get_%s()", nameTable.getFullName(declaringClass), var.getName());
    } else if (value instanceof ITypeBinding) {
      printf("%s_class_()", nameTable.getFullName((ITypeBinding) value));
    } else if (value instanceof String) {
      print(LiteralGenerator.generateStringLiteral((String) value));
    } else if (value instanceof Number || value instanceof Character || value instanceof Boolean) {
      print(value.toString());
    } else if (value.getClass().isArray()) {
      print("[IOSObjectArray arrayWithObjects:(id[]) { ");
      Object[] array = (Object[]) value;
      for (int i = 0; i < array.length; i++) {
        if (i > 0) {
          print(", ");
        }
        printAnnotationValue(array[i]);
      }
      printf(" } count:%d type:NSObject_class_()]", array.length);
    } else if (value instanceof IAnnotationBinding) {
      printAnnotation((IAnnotationBinding) value);
    } else {
      assert false : "unknown annotation value type";
    }
  }
}
