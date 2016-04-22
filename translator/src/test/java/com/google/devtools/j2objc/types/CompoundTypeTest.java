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

package com.google.devtools.j2objc.types;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.SourceVersion;

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.io.IOException;

/**
 * Unit tests for the Java 8 compound types.
 *
 * @author Tom Ball
 */
public class CompoundTypeTest extends GenerationTest {

  // Test BindingUtil.isIntersectionType(ITypeBinding).
  public void testIsCompound() throws Exception {
    Options.setSourceVersion(SourceVersion.JAVA_8);
    createParser();
    String source = "interface Test<T> extends java.util.Comparator<T> {"
        + "  default Test<T> thenTesting(Test<? super T> other) { "
        + "    return (Test<T> & java.io.Serializable) (c1, c2) -> { "
        + "    int res = compare(c1, c2); "
        + "    return (res != 0) ? res : other.compare(c1, c2); }; }}";
    CompilationUnit unit = translateType("Test", source);
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    int methodsFound = 0;
    for (BodyDeclaration body : decl.getBodyDeclarations()) {
      if (body instanceof MethodDeclaration) {
        MethodDeclaration method = (MethodDeclaration) body;
        // Verify a normal type isn't marked as compound.
        if (method.getName().getIdentifier().equals("reversed")) {
          ITypeBinding binding = method.getReturnType().getTypeBinding();
          assertFalse(BindingUtil.isIntersectionType(binding));
          methodsFound++;
        }
      }
      if (body instanceof FunctionDeclaration) {
        FunctionDeclaration function = (FunctionDeclaration) body;
        // While this one should be.
        if (function.getName().equals("Test_thenTestingWithTest_")) {
          // The function's return type isn't compound, but the cast expression in
          // its return statement is.
          ReturnStatement stmt = (ReturnStatement) function.getBody().getStatements().get(0);
          ITypeBinding binding = stmt.getExpression().getTypeBinding();
          assertTrue(BindingUtil.isIntersectionType(binding));
          methodsFound++;
        }
      }
    }
    assertEquals(2, methodsFound);
  }

  // Test NameTable.getFullName(ITypeBinding).
  public void testCompoundTypeFullName() throws IOException {
    Options.setSourceVersion(SourceVersion.JAVA_8);
    createParser();
    String source = "package foo.bar; interface Test<T> extends java.util.Comparator<T> {"
        + "  default Test<T> thenTesting(Test<? super T> other) { "
        + "    return (Test<T> & java.io.Serializable) (c1, c2) -> { "
        + "    int res = compare(c1, c2); "
        + "    return (res != 0) ? res : other.compare(c1, c2); }; }}";
    CompilationUnit unit = translateType("Test", source);
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    for (BodyDeclaration body : decl.getBodyDeclarations()) {
      if (body instanceof FunctionDeclaration) {
        FunctionDeclaration function = (FunctionDeclaration) body;
        if (function.getName().equals("FooBarTest_thenTestingWithFooBarTest_")) {
          // The function's return type isn't compound, but the cast expression in
          // its return statement is.
          ReturnStatement stmt = (ReturnStatement) function.getBody().getStatements().get(0);
          ITypeBinding binding = stmt.getExpression().getTypeBinding();
          String typeName = unit.getNameTable().getObjCType(binding);
          assertEquals("id<FooBarTest, JavaIoSerializable>", typeName);
          return;
        }
      }
    }
    fail("thenTesting function not found");
  }

  // Verify that an include for ".h" isn't generated with a compound type.
  public void testCompoundTypeImport() throws IOException {
    Options.setSourceVersion(SourceVersion.JAVA_8);
    createParser();
    String source = "interface Test<T> extends java.util.Comparator<T> {"
        + "  default Test<T> thenTesting(Test<? super T> other) { "
        + "    return (Test<T> & java.io.Serializable) (c1, c2) -> { "
        + "    int res = compare(c1, c2); "
        + "    return (res != 0) ? res : other.compare(c1, c2); }; }}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertNotInTranslation(translation, "#include \".h\"");
    assertTranslation(translation, "#include \"Test.h\"");
  }
}
