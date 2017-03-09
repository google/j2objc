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
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.io.IOException;
import javax.lang.model.type.TypeMirror;

/**
 * Unit tests for the Java 8 compound types.
 *
 * @author Tom Ball
 */
public class CompoundTypeTest extends GenerationTest {

  // Test TypeUtil.isIntersection(TypeMirror).
  public void testIsCompound() throws Exception {
    String source = "interface Test<T> extends java.util.Comparator<T> {"
        + "  default Test<T> thenTesting(Test<? super T> other) { "
        + "    return (Test<T> & java.io.Serializable) (c1, c2) -> { "
        + "    int res = compare(c1, c2); "
        + "    return (res != 0) ? res : other.compare(c1, c2); }; }}";
    CompilationUnit unit = compileType("Test", source);
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    int methodsFound = 0;
    for (BodyDeclaration body : decl.getBodyDeclarations()) {
      if (body instanceof MethodDeclaration) {
        MethodDeclaration method = (MethodDeclaration) body;
        if (ElementUtil.getName(method.getExecutableElement()).equals("thenTesting")) {
          // Verify a normal type isn't marked as compound.
          TypeMirror returnType = method.getReturnTypeMirror();
          assertFalse(TypeUtil.isIntersection(returnType));
          // The method's return type isn't compound, but the cast expression in
          // its return statement is.
          ReturnStatement stmt = (ReturnStatement) method.getBody().getStatements().get(0);
          assertTrue(TypeUtil.isIntersection(stmt.getExpression().getTypeMirror()));
          methodsFound++;
        }
      }
    }
    assertEquals(1, methodsFound);
  }

  // Test NameTable.getObjCType(TypeMirror).
  public void testCompoundTypeFullName() throws IOException {
    String source = "package foo.bar; interface Test<T> extends java.util.Comparator<T> {"
        + "  default Test<T> thenTesting(Test<? super T> other) { "
        + "    return (Test<T> & java.io.Serializable) (c1, c2) -> { "
        + "    int res = compare(c1, c2); "
        + "    return (res != 0) ? res : other.compare(c1, c2); }; }}";
    CompilationUnit unit = compileType("Test", source);
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    for (BodyDeclaration body : decl.getBodyDeclarations()) {
      if (body instanceof MethodDeclaration) {
        MethodDeclaration method = (MethodDeclaration) body;
        if (ElementUtil.getName(method.getExecutableElement()).equals("thenTesting")) {
          // The method's return type isn't compound, but the cast expression in
          // its return statement is.
          ReturnStatement stmt = (ReturnStatement) method.getBody().getStatements().get(0);
          TypeMirror mirror = stmt.getExpression().getTypeMirror();
          String typeName = unit.getEnv().nameTable().getObjCType(mirror);
          assertEquals("id<FooBarTest, JavaIoSerializable>", typeName);
          return;
        }
      }
    }
    fail("thenTesting method not found");
  }

  // Verify that an include for ".h" isn't generated with a compound type.
  public void testCompoundTypeImport() throws IOException {
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
