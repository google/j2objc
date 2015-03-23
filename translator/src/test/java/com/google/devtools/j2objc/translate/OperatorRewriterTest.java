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

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.ast.Statement;

import java.io.IOException;
import java.util.List;

/**
 * Unit tests for {@link OperatorRewriter}.
 *
 * @author Keith Stanger
 */
public class OperatorRewriterTest extends GenerationTest {

  public void testSetFieldOnResultOfExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { String s; static Test getTest() { return null; } "
        + "void test(boolean b) { (b ? new Test() : getTest()).s = \"foo\"; } }", "Test", "Test.m");
    assertTranslation(translation,
        "Test_set_s_((b ? [new_Test_init() autorelease] : Test_getTest()), @\"foo\");");
  }

  public void testModAssignOperator() throws IOException {
    String source = "float a = 4.2f; a %= 2.1f; double b = 5.6; b %= 1.2; byte c = 3; c %= 2.3; "
        + "short d = 4; d %= 3.4; int e = 5; e %= 4.5; long f = 6; f %= 5.6; char g = 'a'; "
        + "g %= 6.7;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(14, stmts.size());
    assertEquals("ModAssignFloat(&a, 2.1f);", generateStatement(stmts.get(1)));
    assertEquals("ModAssignDouble(&b, 1.2);", generateStatement(stmts.get(3)));
    assertEquals("ModAssignByte(&c, 2.3);", generateStatement(stmts.get(5)));
    assertEquals("ModAssignShort(&d, 3.4);", generateStatement(stmts.get(7)));
    assertEquals("ModAssignInt(&e, 4.5);", generateStatement(stmts.get(9)));
    assertEquals("ModAssignLong(&f, 5.6);", generateStatement(stmts.get(11)));
    assertEquals("ModAssignChar(&g, 6.7);", generateStatement(stmts.get(13)));
  }

  public void testDoubleModulo() throws IOException {
    String translation = translateSourceFile(
      "public class A { "
      + "  double doubleMod(double one, double two) { return one % two; }"
      + "  float floatMod(float three, float four) { return three % four; }}",
      "A", "A.m");
    assertTranslation(translation, "return fmod(one, two);");
    assertTranslation(translation, "return fmodf(three, four);");
  }
}
