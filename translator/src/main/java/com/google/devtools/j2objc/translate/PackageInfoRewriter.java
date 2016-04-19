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
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedTypeBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.j2objc.annotations.ObjectiveCName;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * Creates a TypeDeclaration for a package-info.java file.
 * Not a TreeVisitor because it only needs to visit the CompilationUnit.
 */
public class PackageInfoRewriter {

  private final CompilationUnit unit;
  private final Types typeEnv;

  public static void run(CompilationUnit unit) {
    if (unit.getMainTypeName().endsWith(NameTable.PACKAGE_INFO_MAIN_TYPE)) {
      new PackageInfoRewriter(unit).run();
    }
  }

  private PackageInfoRewriter(CompilationUnit unit) {
    this.unit = unit;
    typeEnv = unit.getTypeEnv();
  }

  private void run() {
    PackageDeclaration pkg = unit.getPackage();

    String prefix = getPackagePrefix(pkg);
    if ((TreeUtil.getRuntimeAnnotationsList(pkg.getAnnotations()).isEmpty() && prefix == null)
        || !TranslationUtil.needsReflection(pkg)) {
      return;
    }

    // The package prefix does not get renamed for the package_info type, so we
    // generate the camel-cased name here so that NameTable doesn't rename it
    // later.
    String typeName = NameTable.camelCaseQualifiedName(pkg.getPackageBinding().getName())
        + NameTable.PACKAGE_INFO_MAIN_TYPE;
    GeneratedTypeBinding typeBinding = GeneratedTypeBinding.newTypeBinding(
        typeName, typeEnv.getNSObject(), false);
    typeBinding.setModifiers(BindingUtil.ACC_SYNTHETIC | Modifier.PRIVATE);
    TypeDeclaration typeDecl = new TypeDeclaration(typeBinding);
    TreeUtil.moveList(pkg.getAnnotations(), typeDecl.getAnnotations());

    if (prefix != null) {
      typeDecl.getBodyDeclarations().add(createPrefixMethod(prefix, typeBinding));
    }

    unit.getTypes().add(0, typeDecl);
  }

  private static String getPackagePrefix(PackageDeclaration pkg) {
    Annotation objcName = TreeUtil.getAnnotation(ObjectiveCName.class, pkg.getAnnotations());
    if (objcName != null) {
      return (String) BindingUtil.getAnnotationValue(objcName.getAnnotationBinding(), "value");
    }
    return null;
  }

  private MethodDeclaration createPrefixMethod(String prefix, ITypeBinding type) {
    GeneratedMethodBinding binding = GeneratedMethodBinding.newMethod(
        "__prefix", Modifier.STATIC | BindingUtil.ACC_SYNTHETIC, typeEnv.getNSString(), type);
    MethodDeclaration method = new MethodDeclaration(binding);
    method.setHasDeclaration(false);
    Block body = new Block();
    method.setBody(body);
    body.getStatements().add(new ReturnStatement(new StringLiteral(prefix, typeEnv)));
    return method;
  }
}
