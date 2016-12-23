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

import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedTypeElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.j2objc.annotations.ObjectiveCName;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Creates a TypeDeclaration for a package-info.java file.
 * Not a TreeVisitor because it only needs to visit the CompilationUnit.
 */
public class PackageInfoRewriter {

  private final CompilationUnit unit;
  private final TypeUtil typeUtil;
  private final TranslationUtil translationUtil;

  public static void run(CompilationUnit unit) {
    if (unit.getMainTypeName().endsWith(NameTable.PACKAGE_INFO_CLASS_NAME)) {
      new PackageInfoRewriter(unit).run();
    }
  }

  private PackageInfoRewriter(CompilationUnit unit) {
    this.unit = unit;
    typeUtil = unit.getEnv().typeUtil();
    translationUtil = unit.getEnv().translationUtil();
  }

  private void run() {
    PackageDeclaration pkg = unit.getPackage();
    List<Annotation> annotations = pkg.getAnnotations();
    List<Annotation> runtimeAnnotations = TreeUtil.getRuntimeAnnotationsList(annotations);
    String prefix = getPackagePrefix(pkg);
    boolean needsReflection = translationUtil.needsReflection(pkg);

    // Remove compile time annotations.
    annotations.retainAll(runtimeAnnotations);

    if ((annotations.isEmpty() && prefix == null) || !needsReflection) {
      return;
    }

    TypeElement typeElement =
        GeneratedTypeElement.newPackageInfoClass(pkg.getPackageElement(), typeUtil);
    TypeDeclaration typeDecl = new TypeDeclaration(typeElement);
    TreeUtil.moveList(pkg.getAnnotations(), typeDecl.getAnnotations());

    if (prefix != null) {
      typeDecl.addBodyDeclaration(createPrefixMethod(prefix, typeElement));
    }

    unit.addType(0, typeDecl);
  }

  private static String getPackagePrefix(PackageDeclaration pkg) {
    Annotation objcName = TreeUtil.getAnnotation(ObjectiveCName.class, pkg.getAnnotations());
    if (objcName != null) {
      return (String) ElementUtil.getAnnotationValue(objcName.getAnnotationMirror(), "value");
    }
    return null;
  }

  private MethodDeclaration createPrefixMethod(String prefix, TypeElement type) {
    ExecutableElement element = GeneratedExecutableElement.newMethodWithSelector(
        "__prefix", typeUtil.getJavaString().asType(), type)
        .addModifiers(Modifier.STATIC);
    MethodDeclaration method = new MethodDeclaration(element);
    method.setHasDeclaration(false);
    Block body = new Block();
    method.setBody(body);
    body.addStatement(new ReturnStatement(new StringLiteral(prefix, typeUtil)));
    return method;
  }
}
