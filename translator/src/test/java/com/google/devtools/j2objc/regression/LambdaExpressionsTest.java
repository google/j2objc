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

package com.google.devtools.j2objc.regression;

/**
 * Pulled from Eclipse:
 * <p>
 * https://github.com/eclipse/eclipse.jdt.core/tree/master/org.eclipse.jdt.core.tests.compiler/src/org/eclipse/jdt/core/tests/compiler/regression/LambdaExpressionsTest.java
 * <p>
 * Negative tests and tests which rely on compiler options have been removed. Rather than being
 * looked at as an actual test suite, we can use Eclipse's tests for new features, to understand
 * which bugs are being addressed, and which are waiting for community use before addressing.
 */
public class LambdaExpressionsTest extends AbstractRegressionTest {

  public void test001() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + "  int add(int x, int y);\n"
        + "}\n" + "public class X {\n" + "  public static void main(String[] args) {\n"
        + "    I i = (x, y) -> {\n" + "      return x + y;\n" + "    };\n"
        + "    System.out.println(i.add(1234, 5678));\n" + "  }\n" + "}\n", }, "6912");
  }

  public void test002() {
    this.runConformTest(new String[] { "X.java", "interface Greetings {\n"
        + "  void greet(String head, String tail);\n" + "}\n" + "public class X {\n"
        + "  public static void main(String[] args) {\n" + "    Greetings g = (x, y) -> {\n"
        + "      System.out.println(x + y);\n" + "    };\n"
        + "    g.greet(\"Hello, \", \"World!\");\n" + "  }\n" + "}\n", }, "Hello, World!");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406178, [1.8][compiler] Some functional
  // interfaces are wrongly rejected
  public void test003() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + "  void foo(int x, int y);\n"
        + "}\n" + "public class X {\n" + "  public static void main(String[] args) {\n"
        + "    BinaryOperator<String> binOp = (x,y) -> { return x+y; };\n"
        + "    System.out.println(\"SUCCESS\");\n"
        + "    // System.out.println(binOp.apply(\"SUCC\", \"ESS\")); // when lambdas run\n"
        + "  }\n" + "}\n", "BiFunction.java", "@FunctionalInterface\n"
        + "public interface BiFunction<T, U, R> {\n" + "    R apply(T t, U u);\n" + "}",
        "BinaryOperator.java", "@FunctionalInterface\n"
            + "public interface BinaryOperator<T> extends BiFunction<T,T,T> {\n" + "}" },
        "SUCCESS");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code
  // for lambdas with expression body.
  public void test005() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " String id(String s);\n"
        + "}\n" + "public class X {\n" + " public static void main(String[] args) {\n"
        + "   I i = (s) -> s;\n" + "   System.out.println(i.id(\"Hello\"));\n" + " }\n" + "}\n" },
        "Hello");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code
  // for lambdas with expression body.
  public void test006() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " String id(String s);\n"
        + "}\n" + "public class X {\n" + " public static void main(String[] args) {\n"
        + "   I i = (s) -> s + s;\n" + "   System.out.println(i.id(\"Hello\"));\n" + " }\n"
        + "}\n" }, "HelloHello");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code
  // for lambdas with expression body.
  public void test007() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void print(String s);\n"
        + "}\n" + "public class X {\n" + " public static void main(String[] args) {\n"
        + "   I i = (s) -> System.out.println(s);\n" + "   i.print(\"Hello\");\n" + " }\n"
        + "}\n" }, "Hello");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code
  // for lambdas with expression body.
  public void test008() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " String print(String s);\n"
        + "}\n" + "public class X {\n" + " public static void main(String[] args) {\n"
        + "   I i = (s) -> new String(s).toUpperCase();\n"
        + "   System.out.println(i.print(\"Hello\"));\n" + " }\n" + "}\n" }, "HELLO");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code
  // for lambdas with expression body.
  public void test009() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " String print(String s);\n"
        + "}\n" + "public class X {\n" + " public static void main(String[] args) {\n"
        + "   I i = (s) -> new String(s);\n" + "   System.out.println(i.print(\"Hello\"));\n"
        + " }\n" + "}\n" }, "Hello");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code
  // for lambdas with expression body.
  public void test010() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " int unbox(Integer i);\n"
        + "}\n" + "public class X {\n" + " public static void main(String[] args) {\n"
        + "   I i = (s) -> s;\n" + "   System.out.println(i.unbox(new Integer(1234)));\n" + " }\n"
        + "}\n" }, "1234");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code
  // for lambdas with expression body.
  public void test011() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " Integer box(int i);\n"
        + "}\n" + "public class X {\n" + " public static void main(String[] args) {\n"
        + "   I i = (s) -> s;\n" + "   System.out.println(i.box(1234));\n" + " }\n" + "}\n" },
        "1234");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code
  // for lambdas with expression body.
  public void test012() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X subType();\n" + "}\n"
        + "public class X {\n" + " public static void main(String[] args) {\n"
        + "   I i = () -> new Y();\n" + "   System.out.println(i.subType());\n" + " }\n" + "}\n"
        + "class Y extends X {\n" + "    public String toString() {\n"
        + "        return \"Some Y\";\n" + "    }\n" + "}" }, "Some Y");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406175, [1.8][compiler][codegen] Generate code
  // for lambdas with expression body.
  public void test013() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + "    void foo(String s);\n"
        + "}\n" + "public class X {\n" + "    public static void main(String [] args) {\n"
        + "        int in = 12345678;\n" + "        I i = (s) -> {\n"
        + "            I j = (s2) -> {\n" + "                System.out.println(s + s2 + in);  \n"
        + "            };\n" + "            j.foo(\"Number=\");\n" + "        };\n"
        + "        i.foo(\"The \");\n" + "    }\n" + "}\n" }, "The Number=12345678");
  }

  public void test014() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void doit();\n" + "}\n"
        + "public class X {\n" + "  public static void nonmain(String[] args) {\n"
        + "    int var = 2;\n" + "    I x2 = () -> {\n"
        + "      System.out.println(\"Argc = \" + args.length);\n"
        + "      for (int i = 0; i < args.length; i++) {\n"
        + "          System.out.println(\"Argv[\" + i + \"] = \" + args[i]);\n" + "      }\n"
        + "    };\n" + "    x2.doit();\n" + "    var=2;\n" + "  }\n"
        + "  public static void main(String[] args) {\n"
        + "      nonmain(new String[] {\"Hello! \", \"World!\" });\n" + "  }\n" + "}", },
        "Argc = 2\n" + "Argv[0] = Hello! \n" + "Argv[1] = World!");
  }

  public void test015() {
    this.runConformTest(
        new String[] { "X.java",
            "interface I {\n" + " void doit();\n" + "}\n"
                + "public class X {\n" + "  public static void main(String[] args) {\n"
                + "    try {\n" + "      new java.io.File((String) null).getCanonicalPath();\n"
                + "    } catch (NullPointerException | java.io.IOException ioe) {\n"
                + "      I x2 = () -> {\n"
                + "        System.out.println(ioe.getMessage()); // OK: args is not re-assignment since declaration/first assignment\n"
                + "      };\n" + "      x2.doit();\n" + "    };\n" + "  }\n" + "}\n" }, "null");
  }

  public void test016() {
    this.runConformTest(
        new String[] { "X.java",
            "interface I {\n" + " void doit();\n" + "}\n"
                + "public class X {\n" + "  public static void main(String[] args) {\n"
                + "    java.util.List<String> list = new java.util.ArrayList<>();\n"
                + "    list.add(\"SomeString\");\n" + "    for (String s : list) {\n"
                + "      I x2 = () -> {\n"
                + "        System.out.println(s); // OK: args is not re-assignment since declaration/first assignment\n"
                + "      };\n" + "      x2.doit();\n" + "    };\n" + "  }\n" + "\n" + "}\n", },
        "SomeString");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406181, [1.8][compiler][codegen]
  // IncompatibleClassChangeError when running code with lambda method
  public void test017() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + "  void foo(int x, int y);\n"
        + "}\n" + "public class X {\n" + "  public static void main(String[] args) {\n"
        + "    BinaryOperator<String> binOp = (x,y) -> { return x+y; }; \n"
        + "    System.out.println(binOp.apply(\"SUCC\", \"ESS\")); // when lambdas run\n" + "  }\n"
        + "}\n" + "@FunctionalInterface\n" + "interface BiFunction<T, U, R> { \n"
        + "    R apply(T t, U u);\n" + "}\n" + "@FunctionalInterface \n"
        + "interface BinaryOperator<T> extends BiFunction<T,T,T> { \n" + "}\n", }, "SUCCESS");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=405071, [1.8][compiler][codegen] Generate code
  // for array constructor references
  public void test018() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X [][][] copy (short x);\n"
        + "}\n" + "public class X  {\n" + " public static void main(String[] args) {\n"
        + "   I i = X[][][]::new;\n" + "       I j = X[][][]::new;\n"
        + "   X[][][] x = i.copy((short) 631);\n" + "   System.out.println(x.length);\n"
        + "       x = j.copy((short) 136);\n" + "   System.out.println(x.length);\n" + " }\n"
        + "}\n", }, "631\n" + "136");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=405071, [1.8][compiler][codegen] Generate code
  // for array constructor references
  public void test019() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X [][][] copy (int x);\n"
        + "}\n" + "public class X  {\n" + " public static void main(String[] args) {\n"
        + "   I i = X[][][]::new;\n" + "       I j = X[][][]::new;\n"
        + "   X[][][] x = i.copy(631);\n" + "   System.out.println(x.length);\n"
        + "       x = j.copy(136);\n" + "   System.out.println(x.length);\n" + " }\n" + "}\n", },
        "631\n" + "136");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=405071, [1.8][compiler][codegen] Generate code
  // for array constructor references
  public void test020() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X [][][] copy (Integer x);\n"
        + "}\n" + "public class X  {\n" + " public static void main(String[] args) {\n"
        + "   I i = X[][][]::new;\n" + "       I j = X[][][]::new;\n"
        + "   X[][][] x = i.copy(631);\n" + "   System.out.println(x.length);\n"
        + "       x = j.copy(136);\n" + "   System.out.println(x.length);\n" + " }\n" + "}\n", },
        "631\n" + "136");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=405071, [1.8][compiler][codegen] Generate code
  // for array constructor references
  public void test021() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X [][][] copy (Integer x);\n"
        + "}\n" + "public class X  {\n" + " public static void main(String[] args) {\n"
        + "   I i = X[][][]::new;\n" + "       I j = X[][][]::new;\n"
        + "   X[][][] x = i.copy(new Integer(631));\n" + "   System.out.println(x.length);\n"
        + "       x = j.copy(new Integer((short)136));\n" + "   System.out.println(x.length);\n"
        + " }\n" + "}\n", }, "631\n" + "136");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406388, [1.8][compiler][codegen] Runtime
  // evaluation of method reference produces
  // "BootstrapMethodError: call site initialization exception"
  public void test022() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + "    Object copy(int [] ia);\n"
        + "}\n" + "interface J {\n" + " int [] copy(int [] ia);\n" + "}\n"
        + "public class X  {\n" + "    public static void main(String [] args) {\n"
        + "        I i = int[]::<String>clone;\n"
        + "        int [] x = new int [] { 10, 20, 30 };\n"
        + "        int [] y = (int []) i.copy(x);\n"
        + "        if (x == y || x.length != y.length || x[0] != y[0] || x[1] != y[1] || x[2] != y[2]) {\n" + "         System.out.println(\"Broken\");\n" + "        } else {\n" + "         System.out.println(\"OK\");\n" + "        }\n" + "        J j = int []::clone;\n" + "        y = null;\n" + "        y = j.copy(x);\n" + "        if (x == y || x.length != y.length || x[0] != y[0] || x[1] != y[1] || x[2] != y[2]) {\n" + "         System.out.println(\"Broken\");\n" + "        } else {\n" + "         System.out.println(\"OK\");\n" + "        }\n" + "    }\n" + "}\n", }, "OK\n" + "OK");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406388, [1.8][compiler][codegen] Runtime
  // evaluation of method reference produces
  // "BootstrapMethodError: call site initialization exception"
  public void test023() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + "    Object copy(int [] ia);\n"
        + "}\n" + "\n" + "public class X  {\n" + "    public static void main(String [] args) {\n"
        + "        I i = int[]::<String>clone;\n"
        + "        int [] ia = (int []) i.copy(new int[10]);\n"
        + "        System.out.println(ia.length);\n" + "    }\n" + "}\n", }, "10");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406388, [1.8][compiler][codegen] Runtime
  // evaluation of method reference produces
  // "BootstrapMethodError: call site initialization exception"
  public void test024() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + "    YBase copy(Y ia);\n"
        + "}\n" + "public class X  {\n" + "    public static void main(String [] args) {\n"
        + "        I i = Y::<String>copy;\n" + "        YBase yb = i.copy(new Y());\n"
        + "        System.out.println(yb.getClass());\n" + "    }\n" + "}\n" + "class YBase {\n"
        + " public YBase copy() {\n" + "   return this;\n" + " }\n" + "}\n"
        + "class Y extends YBase {\n" + "}\n", }, "class Y");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406388, [1.8][compiler][codegen] Runtime
  // evaluation of method reference produces
  // "BootstrapMethodError: call site initialization exception"
  public void test025() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + "    int foo(int [] ia);\n"
        + "}\n" + "public class X  {\n" + "    public static void main(String [] args) {\n"
        + "        I i = int[]::<String>hashCode;\n" + "        i.foo(new int[10]);\n" + "    }\n"
        + "}\n", }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406589, [1.8][compiler][codegen] super call
  // misdispatched
  public void test026() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " Integer foo(int x, int y);\n"
        + "}\n" + "class Y {\n" + " int foo(int x, int y) {\n"
        + "   System.out.println(\"Y.foo(\" + x + \",\" + y + \")\");\n" + "   return foo(x, y);\n"
        + " }\n" + "}\n" + "public class X extends Y {\n" + " int foo(int x, int y) {\n"
        + "   System.out.println(\"X.foo(\" + x + \",\" + y + \")\");\n" + "   return x + y;\n"
        + " }\n" + " void goo() {\n" + "   I i = super::foo;\n"
        + "   System.out.println(i.foo(1234, 4321));\n" + " }\n"
        + " public static void main(String[] args) {\n" + "   new X().goo();\n" + " }\n" + "}\n", },
        "Y.foo(1234,4321)\n" + "X.foo(1234,4321)\n" + "5555");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406589, [1.8][compiler][codegen] super call
  // misdispatched
  public void test027() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " int foo(int x, int y);\n"
        + "}\n" + "interface J {\n" + " default int foo(int x, int y) {\n"
        + "   System.out.println(\"I.foo(\" + x + \",\" + y + \")\");\n" + "   return x + y;\n"
        + " }\n" + "}\n" + "public class X implements J {\n"
        + " public static void main(String[] args) {\n" + "   I i = new X().f();\n"
        + "   System.out.println(i.foo(1234, 4321));\n" + "   i = new X().g();\n" + "   try {\n"
        + "     System.out.println(i.foo(1234, 4321));\n" + "   } catch (Throwable e) {\n"
        + "     System.out.println(e.getMessage());\n" + "   }\n" + " }\n" + " I f() {\n"
        + "   return J.super::foo;\n" + " }\n" + " I g() {\n" + "   return new X()::foo;\n" + " }\n"
        + " public int foo(int x, int y) {\n" + "   throw new RuntimeException(\"Exception\");\n"
        + " }\n" + "}\n", }, "I.foo(1234,4321)\n" + "5555\n" + "Exception");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406584, Bug 406584 - [1.8][compiler][codegen]
  // ClassFormatError: Invalid method signature
  public void test028() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + "    Object copy();\n" + "}\n"
        + "public class X  {\n" + "    public static void main(String [] args) {\n"
        + "     int [] x = new int[] { 0xdeadbeef, 0xfeedface };\n"
        + "     I i = x::<String>clone;\n"
        + "       System.out.println(Integer.toHexString(((int []) i.copy())[0]));\n"
        + "       System.out.println(Integer.toHexString(((int []) i.copy())[1]));\n" + "    }\n"
        + "}\n", }, "deadbeef\n" + "feedface");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen]
  // java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method
  // newinvokespecial
  public void test029() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X.Y.Z makexyz(int val);\n"
        + "}\n" + "public class X {\n" + " public static void main(String args []) {\n"
        + "   new X().new Y().new Z().new P().goo();\n" + " }\n" + " class Y {\n" + "   class Z {\n"
        + "     Z(int val) {\n" + "       System.out.println(Integer.toHexString(val));\n"
        + "     } \n" + "     Z() {\n" + "     }\n" + "     class P {\n" + "       void goo() {\n"
        + "         I i = Z::new;\n" + "         i.makexyz(0xdeadbeef);\n" + "       }\n"
        + "       I i = Z::new;\n" + "       { i.makexyz(0xfeedface); }\n" + "     }\n" + "   }\n"
        + "   I i = Z::new;\n" + "   { i.makexyz(0xbeeffeed); }\n" + " }\n" + "}\n", },
        "beeffeed\n" + "feedface\n" + "deadbeef");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen]
  // java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method
  // newinvokespecial
  public void test030() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X.Y makeY();\n" + "}\n"
        + "public class X {\n" + " public class Y {\n" + "       public String toString() {\n"
        + "           return \"class Y\";\n" + "   }\n" + " }\n" + " void foo() {\n"
        + "   I i = Y::new;\n" + "   System.out.println(i.makeY());\n" + " }\n"
        + " public static void main(String[] args) {\n" + "   new X().foo();\n" + " }\n" + "}\n", },
        "class Y");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen]
  // java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method
  // newinvokespecial
  public void test031() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X.Y makeY(int x);\n" + "}\n"
        + "public class X {\n" + " class Y {\n" + "   String state; \n" + "   Y(int x) {\n"
        + "     state = Integer.toHexString(x);\n" + "   }\n" + "   public String toString() {\n"
        + "     return state;\n" + "   }\n" + " }\n" + " class Z extends Y {\n" + "   Z(int x) {\n"
        + "     super(x);\n" + "   }\n" + " }\n" + " public static void main(String[] args) {\n"
        + "   new X().f();\n" + " }\n" + " void f() {\n" + "   I i = Y::new;\n"
        + "   System.out.println(i.makeY(0xdeadbeef));\n" + " }\n" + "}\n", }, "deadbeef");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen]
  // java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method
  // newinvokespecial
  public void test032() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X.Y makeY(int x);\n" + "}\n"
        + "public class X {\n" + " class Y {\n" + "   String state; \n" + "   Y(int x) {\n"
        + "     state = Integer.toHexString(x);\n" + "   }\n" + "   public String toString() {\n"
        + "     return state;\n" + "   }\n" + " }\n" + " class Z extends Y {\n" + "   Z(int x) {\n"
        + "     super(x);\n" + "   }\n" + " }\n" + " public static void main(String[] args) {\n"
        + "   new X().f();\n" + " }\n" + " void f() {\n" + "   I i = Z::new;\n"
        + "   System.out.println(i.makeY(0xdeadbeef));\n" + " }\n" + "}\n", }, "deadbeef");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406588, [1.8][compiler][codegen]
  // java.lang.invoke.LambdaConversionException: Incorrect number of parameters for static method
  // newinvokespecial
  public void test033() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X.Y.Z makeY(int x);\n"
        + "}\n" + "public class X {\n" + " class Y {\n" + "   Y() {\n" + "   }\n" + "   class Z {\n"
        + "     String state;\n" + "     Z(int x) {\n" + "       state = Integer.toHexString(x);\n"
        + "     }\n" + "     public String toString() {\n" + "       return state;\n" + "     }\n"
        + "   }\n" + " }\n" + " class YS extends Y {\n" + "   YS() {\n" + "   }\n"
        + "   void f() {\n" + "     I i = Z::new;\n"
        + "     System.out.println(i.makeY(0xbeefface));\n" + "   }\n" + " }\n"
        + " public static void main(String[] args) {\n" + "   new X().new YS().f();\n" + " }\n"
        + "}\n", }, "beefface");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406319, [1.8][compiler][codegen] Generate code
  // for enclosing instance capture in lambda methods.
  public void test034() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + "    int foo();\n" + "}\n"
        + "public class X {\n" + "    int f = 1234;\n" + "    void foo() {\n"
        + "        int x = 4321;\n" + "        I i = () -> x + f;\n"
        + "        System.out.println(i.foo());\n" + "    }\n"
        + "    public static void main(String[] args) {\n" + "   new X().foo();\n" + " }\n"
        + "}\n", }, "5555");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406319, [1.8][compiler][codegen] Generate code
  // for enclosing instance capture in lambda methods.
  public void test035() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(int p, int q);\n"
        + "}\n" + "public class X {\n" + "   int f;\n" + " void foo(int outerp) {\n"
        + "       int locouter;\n" + "   I i = (int p, int q)  -> {\n" + "     class Local {\n"
        + "       void foo() {\n" + "               }\n" + "     };\n" + "     new Local();\n"
        + "   };\n" + "   }\n" + " public static void main(String[] args) {\n"
        + "   System.out.println(\"OK\");\n" + " }\n" + "}\n", }, "OK");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406319, [1.8][compiler][codegen] Generate code
  // for enclosing instance capture in lambda methods.
  public void test036() {
    this.runConformTest(new String[] { "X.java", "interface I {\n"
        + "    String foo(String x, String y);\n" + "}\n" + "public class X {\n"
        + "    String xf = \"Lambda \";\n" + "    String x() {\n" + "     String xl = \"code \";\n"
        + "     class Y {\n" + "     String yf = \"generation \";\n" + "     String y () {\n"
        + "       String yl = \"with \";\n" + "       class Z {\n"
        + "         String zf = \"instance \";\n" + "         String z () {\n"
        + "           String zl = \"and \";\n" + "           class P {\n"
        + "             String pf = \"local \";\n" + "             String p () {\n"
        + "               String pl = \"capture \";\n" + "               I i = (x1, y1) -> {\n"
        + "                 return (((I) ((x2, y2) -> {\n"
        + "                   return ( ((I) ((x3, y3) -> {\n"
        + "                     return xf + xl + yf + yl + zf + zl + pf + pl + x3 + y3;\n"
        + "                   })).foo(\"works \", \"fine \") + x2 + y2);\n"
        + "                 })).foo(\"in \", \"the \") + x1 + y1);\n" + "               };\n"
        + "               return i.foo(\"eclipse \", \"compiler \");\n" + "             }\n"
        + "           }\n" + "           return new P().p();\n" + "         }\n" + "       }\n"
        + "       return new Z().z();\n" + "     }\n" + "     }\n" + "     return new Y().y();\n"
        + "    }\n" + "    public static void main(String[] args) {\n"
        + " System.out.println(new X().x());\n" + "    }\n" + "}\n", },
        "Lambda code generation with instance and local capture works fine in the eclipse compiler");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406319, [1.8][compiler][codegen] Generate code
  // for enclosing instance capture in lambda methods.
  public void test037() {
    this.runConformTest(new String[] { "X.java", "interface I {\n"
        + "    String foo(String x, String y);\n" + "}\n" + "public class X {\n"
        + "    String xf = \"Lambda \";\n" + "    String x() {\n"
        + "     String xl = \"code \";\n" + "     class Y {\n"
        + "     String yf = \"generation \";\n" + "     String y () {\n"
        + "       String yl = \"with \";\n" + "       class Z {\n"
        + "         String zf = \"instance \";\n" + "         String z () {\n"
        + "           String zl = \"and \";\n" + "           class P {\n"
        + "             String pf = \"local \";\n" + "             String p () {\n"
        + "               String pl = \"capture \";\n" + "               I i = (x1, y1) -> {\n"
        + "                 return (((I) ((x2, y2) -> {\n"
        + "                   return ( ((I) ((x3, y3) -> {\n"
        + "                                           String exclaim = \"!\";\n"
        + "                     return xf + xl + yf + yl + zf + zl + pf + pl + x3 + y3 + x2 + y2 + x1 + y1 + exclaim;\n" + "                   })).foo(\"works \", \"fine \"));\n" + "                 })).foo(\"in \", \"the \"));\n" + "               };\n" + "               return i.foo(\"eclipse \", \"compiler \");\n" + "             }\n" + "           }\n" + "           return new P().p();\n" + "         }\n" + "       }\n" + "       return new Z().z();\n" + "     }\n" + "     }\n" + "     return new Y().y();\n" + "    }\n" + "    public static void main(String[] args) {\n" + " System.out.println(new X().x());\n" + "    }\n" + "}\n", }, "Lambda code generation with instance and local capture works fine in the eclipse compiler !");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406641, [1.8][compiler][codegen] Code generation
  // for intersection cast.
  public void test038() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + "}\n" + "interface J {\n"
        + "}\n" + "public class X implements I, J {\n"
        + " public static void main( String [] args) { \n" + "   f(new X());\n" + " }\n"
        + " static void f(Object o) {\n" + "   X x = (X & I & J) o;\n"
        + "       System.out.println(\"OK\");\n" + " }\n" + "}\n", }, "OK");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406641, [1.8][compiler][codegen] Code generation
  // for intersection cast.
  public void test039() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + "}\n" + "interface J {\n"
        + "}\n" + "public class X implements J {\n"
        + " public static void main( String [] args) { \n" + "   f(new X());\n" + " }\n"
        + " static void f(Object o) {\n" + "       try {\n" + "       X x = (X & I & J) o;\n"
        + "       } catch (ClassCastException e) {\n"
        + "           System.out.println(e.getMessage());\n" + "       }\n" + " }\n" + "}\n", },
        "X cannot be cast to I");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen]
  // "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
  public void test041() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X makeX(int x);\n" + "}\n"
        + "public class X {\n" + " class Z {\n" + "   void f() {\n" + "     I i = X::new;\n"
        + "     i.makeX(123456);\n" + "   }\n" + " }\n" + " private X(int x) {\n"
        + "   System.out.println(x);\n" + " }\n" + " X() {\n" + " }\n"
        + " public static void main(String[] args) {\n" + "   new X().new Z().f();\n" + " }\n"
        + "}\n", }, "123456");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen]
  // "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
  public void test042() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X makeX(int x);\n" + "}\n"
        + "public class X {\n" + " class Y extends X {\n" + "   class Z {\n" + "     void f() {\n"
        + "       I i = X::new;\n" + "       i.makeX(123456);\n" + "       i = Y::new;\n"
        + "       i.makeX(987654);\n" + "     }\n" + "   }\n" + "   private Y(int y) {\n"
        + "     System.out.println(\"Y(\" + y + \")\");\n" + "   }\n" + "   private Y() {\n"
        + "     \n" + "   }\n" + " }\n" + " private X(int x) {\n"
        + "   System.out.println(\"X(\" + x + \")\");\n" + " }\n" + "\n" + " X() {\n" + " }\n"
        + " public static void main(String[] args) {\n" + "   new X().new Y().new Z().f();\n"
        + " }\n" + "\n" + "}\n", }, "X(123456)\n" + "Y(987654)");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen]
  // "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
  public void test043() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X makeX(int x);\n" + "}\n"
        + "public class X {\n" + " class Y extends X {\n" + "   class Z extends X {\n"
        + "     void f() {\n" + "       I i = X::new;\n" + "       i.makeX(123456);\n"
        + "       i = Y::new;\n" + "       i.makeX(987654);\n" + "               i = Z::new;\n"
        + "               i.makeX(456789);\n" + "     }\n" + "         private Z(int z) {\n"
        + "       System.out.println(\"Z(\" + z + \")\");\n" + "     }\n" + "           Z() {\n"
        + "           }\n" + "       }\n" + "   private Y(int y) {\n"
        + "     System.out.println(\"Y(\" + y + \")\");\n" + "   }\n" + "   private Y() {\n"
        + "     \n" + "   }\n" + " }\n" + " private X(int x) {\n"
        + "   System.out.println(\"X(\" + x + \")\");\n" + " }\n" + "\n" + " X() {\n" + " }\n"
        + " public static void main(String[] args) {\n" + "   new X().new Y().new Z().f();\n"
        + " }\n" + "\n" + "}\n", }, "X(123456)\n" + "Y(987654)\n" + "Z(456789)");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen]
  // "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
  public void test044() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X makeX(int x);\n" + "}\n"
        + "public class X {\n" + " void foo() {\n" + "   int local;\n" + "   class Y extends X {\n"
        + "     class Z extends X {\n" + "       void f() {\n" + "         I i = X::new;\n"
        + "         i.makeX(123456);\n" + "         i = Y::new;\n" + "         i.makeX(987654);\n"
        + "         i = Z::new;\n" + "         i.makeX(456789);\n" + "       }\n"
        + "       private Z(int z) {\n" + "         System.out.println(\"Z(\" + z + \")\");\n"
        + "       }\n" + "       Z() {}\n" + "     }\n" + "     private Y(int y) {\n"
        + "       System.out.println(\"Y(\" + y + \")\");\n" + "     }\n" + "     private Y() {\n"
        + "     }\n" + "   }\n" + "   new Y().new Z().f();\n" + " }\n" + " private X(int x) {\n"
        + "   System.out.println(\"X(\" + x + \")\");\n" + " }\n" + "\n" + " X() {\n" + " }\n"
        + " public static void main(String[] args) {\n" + "   new X().foo();\n" + " }\n" + "}\n", },
        "X(123456)\n" + "Y(987654)\n" + "Z(456789)");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406760, [1.8][compiler][codegen]
  // "VerifyError: Bad type on operand stack" with qualified super method references
  public void test046() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " String doit();\n" + "}\n"
        + "public class X extends B {\n" + " class Y {\n" + "   class Z {\n" + "     void f() {\n"
        + "       \n" + "        I i = X.super::toString; // Verify error\n"
        + "        System.out.println(i.doit());\n"
        + "        i = X.this::toString; // This call gets dispatched OK.\n"
        + "        System.out.println(i.doit());\n" + "     }\n" + "   }\n" + " }\n" + " \n"
        + " public static void main(String[] args) {\n" + "   new X().new Y().new Z().f(); \n"
        + " }\n" + " \n" + " public String toString() {\n" + "   return \"X's toString\";\n"
        + " }\n" + "}\n" + "class B {\n" + " public String toString() {\n"
        + "   return \"B's toString\";\n" + " }\n" + "}\n", }, "B\'s toString\n" + "X\'s toString");
  }

  public void test047() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(int x, int y);\n"
        + "}\n" + "public class X {\n" + " public static void main(String[] args) {\n"
        + "   long lng = 1234;\n" + "   double d = 1234.5678;\n" + "   I i = (x, y) -> {\n"
        + "     System.out.println(\"long = \" + lng);\n"
        + "     System.out.println(\"args length = \" + args.length);\n"
        + "     System.out.println(\"double = \" + d);\n"
        + "     System.out.println(\"x = \" + x);\n" + "     System.out.println(\"y = \" + y);\n"
        + "   };\n" + "   i.foo(9876, 4321);\n" + " }\n" + "}\n", },
        "long = 1234\n" + "args length = 0\n" + "double = 1234.5678\n" + "x = 9876\n" + "y = 4321");
  }

  public void test048() {
    this.runConformTest(new String[] { "X.java", "interface I<T, J> {\n" + " void foo(T x, J y);\n"
        + "}\n" + "public class X {\n" + " public static void main(String[] args) {\n"
        + "   long lng = 1234;\n" + "   double d = 1234.5678;\n"
        + "   I<Object, Object> i = (x, y) -> {\n" + "     System.out.println(\"long = \" + lng);\n"
        + "     System.out.println(\"args length = \" + args.length);\n"
        + "     System.out.println(\"double = \" + d);\n"
        + "     System.out.println(\"x = \" + x);\n" + "     System.out.println(\"y = \" + y);\n"
        + "   };\n" + "   i.foo(9876, 4321);\n" + "   \n"
        + "   I<String, String> i2 = (x, y) -> {\n" + "     System.out.println(x);\n"
        + "     System.out.println(y);\n" + "   };\n" + "   i2.foo(\"Hello !\",  \"World\");\n"
        + " }\n" + "}\n", }, "long = 1234\n" + "args length = 0\n" + "double = 1234.5678\n"
        + "x = 9876\n" + "y = 4321\n" + "Hello !\n" + "World");
  }

  public void test049() {
    this.runConformTest(new String[] { "X.java", "interface I<T, J> {\n"
        + " void foo(X x, T t, J j);\n" + "}\n" + "public class X {\n"
        + " public static void main(String[] args) {\n" + "   I<String, String> i = X::foo;\n"
        + "   i.foo(new X(), \"Hello\", \"World!\");\n" + " }\n"
        + " void foo(String s, String t) {\n" + "   System.out.println(s);\n"
        + "   System.out.println(t);\n" + " }\n" + "}\n", }, "Hello\n" + "World!");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen]
  // "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
  public void test050() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(int x, int y);\n"
        + "}\n" + "public class X {\n" + " static private void add(int x, int y) {\n"
        + "   System.out.println(x + y);\n" + " }\n" + " private void multiply(int x, int y) {\n"
        + "   System.out.println(x * y);\n" + " }\n" + " static class Y {\n"
        + "   static private void subtract(int x, int y) {\n" + "     System.out.println(x - y);\n"
        + "   }\n" + "   private void divide (int x, int y) {\n"
        + "     System.out.println(x / y);\n" + "   }\n" + "   static void doy() {\n"
        + "     I i = X::add;\n" + "     i.foo(1234, 12);\n" + "     i = new X()::multiply;\n"
        + "     i.foo(12, 20);\n" + "     i = Y::subtract;\n" + "     i.foo(123,  13);\n"
        + "     i = new Y()::divide;\n" + "     i.foo(99, 9);\n" + "   }\n" + " }\n"
        + " public static void main(String[] args) {\n" + "   I i = X::add;\n"
        + "   i.foo(1234, 12);\n" + "   i = new X()::multiply;\n" + "   i.foo(12, 20);\n"
        + "   i = Y::subtract;\n" + "   i.foo(123,  13);\n" + "   i = new Y()::divide;\n"
        + "   i.foo(99, 9);\n" + "   Y.subtract(10,  7);\n" + "   Y.doy();\n" + " }\n" + "}\n", },
        "1246\n" + "240\n" + "110\n" + "11\n" + "3\n" + "1246\n" + "240\n" + "110\n" + "11");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen]
  // "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
  public void test051() {
    this.runConformTest(new String[] { "p2/B.java", "package p2;\n"
        + "import p1.*;               \n" + "interface I {\n" + " void foo();\n" + "}\n"
        + "interface J {\n" + " void foo();\n" + "}\n" + "public class B extends A {\n"
        + " class Y {\n" + "   void g() {\n" + "     I i = B::foo;\n" + "     i.foo();\n"
        + "     J j = new B()::goo;\n" + "     j.foo();\n" + "   }\n" + " }\n"
        + " public static void main(String[] args) {\n" + "   new B().new Y().g();\n" + " }\n"
        + "}\n", "p1/A.java", "package p1;\n" + "import p2.*;\n" + "public class A {\n"
        + " protected static void foo() {\n" + "     System.out.println(\"A's static foo\");\n"
        + " }\n" + " protected void goo() {\n" + "     System.out.println(\"A's instance goo\");\n"
        + " }\n" + "}" }, "A\'s static foo\n" + "A\'s instance goo");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406773, [1.8][compiler][codegen]
  // "java.lang.IncompatibleClassChangeError" caused by attempted invocation of private constructor
  public void test052() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(int x);\n" + "}\n"
        + "public class X {\n" + " void foo() {\n" + "   int local = 10;\n" + "   class Y {\n"
        + "     void foo(int x) {\n" + "       System.out.println(local);\n" + "     }\n"
        + "     void goo() {\n" + "       I i = this::foo;\n" + "       i.foo(10);\n" + "     }\n"
        + "   }\n" + "   new Y().goo();\n" + " }\n" + " public static void main(String[] args) {\n"
        + "   new X().foo();\n" + " }\n" + "}\n" }, "10");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406847, [1.8] lambda code compiles but then
  // produces IncompatibleClassChangeError when run
  public void test053() {
    this.runConformTest(new String[] { "X.java", "import java.util.*;\n" + "public class X {\n"
        + "  public static <E> void printItem(E value, int index) {\n"
        + "    String output = String.format(\"%d -> %s\", index, value);\n"
        + "    System.out.println(output);\n" + "  }\n"
        + "  public static void main(String[] argv) {\n"
        + "    List<String> list = Arrays.asList(\"A\",\"B\",\"C\");\n"
        + "    eachWithIndex(list,X::printItem);\n" + "  }\n"
        + "  interface ItemWithIndexVisitor<E> {\n"
        + "    public void visit(E item, int index);\n" + "  }\n"
        + "  public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {\n" + "    for (int i = 0; i < list.size(); i++) {\n" + "         visitor.visit(list.get(i), i);\n" + "    }\n" + "  }\n" + "}\n" }, "0 -> A\n" + "1 -> B\n" + "2 -> C");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406847, [1.8] lambda code compiles but then
  // produces IncompatibleClassChangeError when run
  public void test054() {
    this.runConformTest(
        new String[] { "X.java",
            "import java.util.*;\n" + "public class X {\n"
                + "  public static <E> void printItem(E value) {}\n"
                + "  public static void main(String[] argv) {\n" + "    List<String> list = null;\n"
                + "    eachWithIndex(list, X::printItem);\n" + "  }\n"
                + "  interface ItemWithIndexVisitor<E> {\n" + "    public void visit(E item);\n"
                + "  }\n"
                + "  public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {}\n"
                + "}\n" }, "");
  }

  public void test055() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(int i);\n" + "}\n"
        + "public class X {\n" + " public static void main(String[] args) {\n" + "   X x = null;\n"
        + "   I i = x::foo;\n" + " }\n" + " int foo(int x) {\n" + "   return x;\n" + " }\n"
        + "}\n" }, "");
  }

  public void test056() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(int i);\n" + "}\n"
        + "public class X {\n" + " public static void main(String[] args) {\n" + "   X x = null;\n"
        + "   I i = x::foo;\n" + "   try {\n" + "     i.foo(10);\n"
        + "   } catch (NullPointerException npe) {\n"
        + "     System.out.println(npe.getMessage());\n" + "   }\n" + " }\n" + " int foo(int x) {\n"
        + "   return x;\n" + " }\n" + "}\n" }, "null");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=410114, [1.8] CCE when trying to parse method
  // reference expression with inappropriate type arguments
  public void test057() {
    String source = "interface I {\n" + "    void foo(Y<String> y);\n" + "}\n"
        + "public class Y<T> {\n" + "    class Z<K> {\n" + "        Z(Y<String> y) {\n"
        + "            System.out.println(\"Y<T>.Z<K>:: new\");\n" + "        }\n"
        + "        public void bar() {\n"
        + "            I i = Y<String>.Z<Integer>::<String> new;\n"
        + "            i.foo(new Y<String>());\n" + "            i = Y<String>.Z<Integer>:: new;\n"
        + "            i.foo(new Y<String>());\n" + "            i = Y.Z:: new;\n"
        + "            i.foo(new Y<String>());\n" + "        }\n" + "    }\n"
        + " public void foo() {\n" + "   Z<String> z = new Z<String>(null);\n" + "   z.bar();\n"
        + " }\n" + " public static void main(String[] args) {\n"
        + "   Y<String> y = new Y<String>();\n" + "   y.foo();\n" + " }\n" + "}\n";
    this.runConformTest(new String[] { "Y.java", source },
        "Y<T>.Z<K>:: new\n" + "Y<T>.Z<K>:: new\n" + "Y<T>.Z<K>:: new\n" + "Y<T>.Z<K>:: new");
  }

  // Bug 411273 - [1.8][compiler] Bogus error about unhandled exceptions for unchecked exceptions
  // thrown by method reference.
  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=411273
  public void test058() {
    this.runConformTest(new String[] { "X.java", "public class X {\n"
        + " public static void main(String[] args) {\n" + "   F1 f = X::foo;\n"
        + "   System.out.println(\"Hello, World\");\n" + " }\n"
        + "    static int foo (int x) throws NumberFormatException { return 0; }\n" + "}\n"
        + "interface F1 { int X(int x);}\n" }, "Hello, World");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=420582, [1.8][compiler] Compiler should allow
  // creation of generic array creation with unbounded wildcard type arguments
  public void testGenericArrayCreation() {
    this.runConformTest(new String[] { "X.java", "interface I {\n"
        + " X<?, ?, ?>[] makeArray(int i);\n" + "}\n" + "public class X<T, U, V> {\n"
        + " public static void main(String [] args) {\n" + "   I i = X<?, ?, ?>[]::new; // OK.\n"
        + "   System.out.println(i.makeArray(1024).length);\n" + " }\n" + "}\n" + "" }, "1024");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=421607, [1.8][compiler] Verify Error with
  // intersection casts
  public void test421607() {
    runConformTest(new String[] { "X.java", "interface I {\n" + " public void foo();\n" + "}\n"
        + "class C implements I {\n" + " public void foo() {\n"
        + "   System.out.println(\"You will get here\");\n" + " }\n" + "}\n" + "public class X {\n"
        + " public static void main(String[] args) {\n" + "   ((C & I) (I) new C()).foo();\n"
        + " }\n" + "}\n" }, "You will get here");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=421712, [1.8][compiler]
  // java.lang.NoSuchMethodError with lambda expression in interface default method.
  public void test421712() {
    runConformTest(new String[] { "X.java", "interface F {\n" + " void foo();\n" + "}\n"
        + "interface I {\n" + " default void foo() {\n" + "   F f = () -> {\n" + "   };\n"
        + "   System.out.println(\"Lambda instantiated\");\n" + " }\n" + "}\n"
        + "public class X implements I {\n" + " public static void main(String argv[]) {\n"
        + "   X x = new X();\n" + "   x.foo();\n" + " }\n" + "}\n" }, "Lambda instantiated");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=422515, [1.8][compiler]
  // "Missing code implementation in the compiler" when lambda body accesses array variable
  public void test422515() {
    this.runConformTest(new String[] { "X.java", "public class X {\n"
        + "    public static void main(String[] args) throws InterruptedException {\n"
        + "        final int[] result = { 0 };\n"
        + "        Thread t = new Thread(() -> result[0] = 42);\n" + "        t.start();\n"
        + "        t.join();\n" + "        System.out.println(result[0]);\n" + "    }\n" + "}\n" },
        "42");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=422515, [1.8][compiler]
  // "Missing code implementation in the compiler" when lambda body accesses array variable
  public void test422515a() {
    this.runConformTest(new String[] { "X.java", "public class X {\n"
        + "    public static void main(String[] args) throws InterruptedException {\n"
        + "        final int[] result= { 0 };\n" + "        final int x = args.length + 42;\n"
        + "        Thread t = new Thread(() -> {\n" + "            result[0]= x;\n"
        + "        });\n" + "        t.start();\n" + "        t.join();\n"
        + "        System.out.println(result[0]);\n" + "    }\n" + "}\n" }, "42");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=422800, [1.8][compiler]
  // "Missing code implementation in the compiler" 2
  public void test422800() {
    this.runConformTest(new String[] { "X.java", "public class X {\n"
        + "    private String fField; // must be here; can be used or unused\n"
        + "    public void foo(Integer arg) {\n" + "        new Thread(() -> {\n"
        + "            arg.intValue();\n" + "        });\n" + "    }\n"
        + "    public static void main(String [] args) {\n" + "      System.out.println(\"OK\");\n"
        + "    }\n" + "}\n" }, "OK");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=421927, [1.8][compiler] Bad diagnostic:
  // Unnecessary cast from I to I for lambdas.
  public void test421927() {
    this.runConformTest(new String[] { "X.java", "interface I { \n" + " int foo();\n" + "}\n"
        + "public class X {\n" + "    static I i  = (I & java.io.Serializable) () -> 42;\n"
        + "    public static void main(String args[]) {\n"
        + "        System.out.println(i.foo());\n" + "    }\n" + "}\n" }, "42");
  }

  public void testReferenceExpressionInference1() {
    runConformTest(new String[] { "X.java", "interface I<E> {\n" + " E foo(E e);\n" + "}\n"
        + "public class X {\n" + " <T> T print(I<T> i) { return null; }\n" + " void test() {\n"
        + "   String s = print(this::bar);" + " }\n" + " <S> S bar(S s) { return s; }\n" + "}\n" });
  }

  public void testReferenceExpressionInference2() {
    runConformTest(new String[] { "X.java", "interface I<E,F> {\n" + " F foo(E e);\n" + "}\n"
        + "public class X {\n" + " <S,T,U> I<S,U> compose(I<S,T> i1, I<T,U> i2) { return null; }\n"
        + " void test() {\n" + "   I<X,String> x2s = compose(this::bar, this::i2s);" + " }\n"
        + " String i2s (Integer i) { return i.toString(); }\n"
        + " <V,W extends Number> W bar(V v) { return null; }\n" + "}\n" });
  }

  public void testReferenceExpressionInference3a() {
    runConformTest(new String[] { "X.java", "interface I<E,F> {\n" + " F foo(E e);\n" + "}\n"
        + "public class X {\n" + " <S,T,U> I<S,U> compose(I<S,T> i1, I<T,U> i2) { return null; }\n"
        + " void test() {\n" + "   I<X,String> x2s = compose(this::bar, this::<String>i2s);" + // help
                                                                                               // inference
                                                                                               // with
                                                                                               // an
                                                                                               // explicit
                                                                                               // type
                                                                                               // argument
        " }\n" + " <Z> Z i2s (Integer i) { return null; }\n"
        + " <V,W extends Number> W bar(V v) { return null; }\n" + "}\n" });
  }

  public void testLambdaInference1() {
    this.runConformTest(
        new String[] { "X.java",
            "import java.util.*;\n" + "public class X {\n"
                + "  public static void main(String[] argv) {\n" + "    List<String> list = null;\n"
                + "    eachWithIndex(list, s -> print(s));\n" + "  }\n"
                + "  static void print(String s) {}\n" + "  interface ItemWithIndexVisitor<E> {\n"
                + "    public void visit(E item);\n" + "  }\n"
                + "  public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {}\n"
                + "}\n" }, "");
  }

  public void testLambdaInference2() {
    this.runConformTest(
        new String[] { "X.java",
            "import java.util.*;\n" + "class A {}\n"
                + "class B extends A {\n" + " void bar() {}\n" + "}\n" + "public class X {\n"
                + "  public static void main(String[] argv) {\n"
                + "    someWithIndex(getList(), (B b) -> b.bar());\n" + "  }\n"
                + "  interface ItemWithIndexVisitor<E> {\n" + "    public void visit(E item);\n"
                + "  }\n"
                + "  public static <G> void someWithIndex(List<G> list, ItemWithIndexVisitor<G> visitor) {}\n"
                + "  static <I extends A> List<I> getList() { return null; }\n" + "}\n" }, "");
  }

  public void testBug419048_1() {
    runConformTest(new String[] { "X.java", "import java.util.*;\n" + "import java.util.stream.*;\n"
        + "public class X {\n" + " public void test() {\n"
        + "    List<Person> roster = new ArrayList<>();\n" + "        \n"
        + "        Map<String, Person> map = \n" + "            roster\n"
        + "                .stream()\n" + "                .collect(\n"
        + "                    Collectors.toMap(\n" + "                        p -> p.getLast(),\n"
        + "                        p -> p\n" + "                    ));\n" + " }\n" + "}\n"
        + "class Person {\n" + "  public String getLast() { return null; }\n" + "}\n" });
  }

  public void testBug419048_2() {
    runConformTest(new String[] { "X.java", "import java.util.*;\n"
        + "import java.util.function.*;\n" + "import java.util.stream.*;\n" + "public class X {\n"
        + " public void test() {\n" + "    List<Person> roster = new ArrayList<>();\n"
        + "        \n" + "        Map<String, Person> map = \n" + "            roster\n"
        + "                .stream()\n" + "                .collect(\n"
        + "                    Collectors.toMap(\n" + "                        Person::getLast,\n"
        + "                        Function.identity()\n" + "                    ));\n" + " }\n"
        + "}\n" + "class Person {\n" + "  public String getLast() { return null; }\n" + "}\n" });
  }

  public void testBug419048_3() {
    runConformTest(new String[] { "X.java", "import java.util.*;\n"
        + "import java.util.function.*;\n" + "import java.util.stream.*;\n" + "public class X {\n"
        + " public void test() {\n" + "    List<Person> roster = new ArrayList<>();\n"
        + "        \n" + "        Map<String, Person> map = \n" + "            roster\n"
        + "                .stream()\n" + "                .collect(\n"
        + "                    Collectors.toMap(\n"
        + "                        new Function<Person, String>() {\n"
        + "                            public String apply(Person p) { \n"
        + "                                return p.getLast(); \n"
        + "                            } \n" + "                        },\n"
        + "                        Function.identity()\n" + "                    ));\n" + " }\n"
        + "}\n" + "class Person {\n" + "  public String getLast() { return null; }\n" + "}\n" });
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=424226, [1.8] Cannot use static method from an
  // interface in static method reference
  public void test424226() {
    runConformTest(new String[] { "X.java", "public class X {\n" + " public void fun1() {\n"
        + "   FI fi = I::staticMethod; \n" + " }\n"
        + "   public static void main(String [] args) {\n" + "       System.out.println(\"OK\");\n"
        + "   }\n" + "}\n" + "@FunctionalInterface\n" + "interface FI {\n" + " void foo(); \n"
        + "}\n" + "interface I {\n" + " static FI staticMethod() {\n" + "   return null;\n" + " }\n"
        + "}\n" }, "OK");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=423684, [1.8][compiler] IllegalAccessError using
  // functional consumer calling inherited method
  public void test423684() {
    runConformTest(new String[] { "Test.java", "import java.util.Arrays;\n"
        + "import java.util.List;\n" + "import mypackage.MyPublicClass;\n"
        + "public class Test {\n" + "    public static void main(String[] args) {\n"
        + "        doesWork();\n" + "        doesNotWork();\n" + "    }\n"
        + "    public static void doesNotWork() {\n"
        + "        MyPublicClass victim = new MyPublicClass();\n"
        + "        List<String> items = Arrays.asList(\"first\", \"second\", \"third\");\n"
        + "        items.forEach(victim::doSomething); //illegal access error here\n"
        + "    }\n" + "    public static void doesWork() {\n"
        + "        MyPublicClass victim = new MyPublicClass();\n"
        + "        List<String> items = Arrays.asList(\"first\", \"second\", \"third\");\n"
        + "        for (String item : items) {\n" + "            victim.doSomething(item);\n"
        + "        }\n" + "    }\n" + "}\n", "mypackage/MyPublicClass.java", "package mypackage;\n"
        + "class MyPackagePrivateBaseClass {\n" + "    public void doSomething(String input) {\n"
        + "        System.out.println(input);\n" + "    }\n" + "}\n"
        + "public class MyPublicClass extends MyPackagePrivateBaseClass {\n" + "}\n" },
        "first\n" + "second\n" + "third\n" + "first\n" + "second\n" + "third");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=425152, [1.8] [compiler] NPE in
  // LambdaExpression.analyzeCode
  public void test425152() {
    runConformTest(new String[] { "Main.java", "interface Base { \n" + " Base get(int x);\n" + "}\n"
        + "class Main {\n" + "    <T> Base foo(Base b) { \n" + "        return null; \n"
        + "     }\n" + "    void bar(Base b) { }\n" + "    void testCase() {\n"
        + "        bar(foo((int p)->null));\n" + "     }\n" + "}\n" });
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=424628, [1.8][compiler] Multiple method
  // references to inherited method throws LambdaConversionException
  public void test424628() throws Exception {
    this.runConformTest(new String[] { "X.java", "public class X {\n"
        + "    public static interface Consumer<T> {\n" + "        void accept(T t);\n" + "    }\n"
        + "    \n" + "    public static class Base {\n"
        + "        public void method () { System.out.println(123); }\n" + "    }\n"
        + "    public static class Foo extends Base {}\n"
        + "    public static class Bar extends Base {}\n" + "\n"
        + "    public static void main (String[] args) {\n"
        + "        Consumer<Foo> foo = Foo::method;\n"
        + "        Consumer<Bar> bar = Bar::method;\n" + "        foo.accept(new Foo());\n"
        + "        bar.accept(new Bar());\n" + "    }\n" + "}\n", }, "123\n123");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=425712, [1.8][compiler] Valid program rejected by
  // the compiler.
  public void test425712() throws Exception {
    this.runConformTest(new String[] { "X.java", "public class X {\n" + "    {\n"
        + "        bar( () -> (char) 0); // [1]\n" + "    }\n" + "    void bar(FB fb) { }\n"
        + "    public static void main(String[] args) {\n" + "   System.out.println(\"OK\");\n"
        + " }\n" + "}\n" + "interface FB {\n" + " byte foo();\n" + "}\n", }, "OK");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=426074, [1.8][compiler] 18.5.2 Functional
  // interface parameterization inference problem with intersection types.
  public void test426074() throws Exception {
    this.runConformTest(new String[] { "X.java", "interface Functional<T> {\n"
        + "    void foo(T t);\n" + "}\n" + "interface I { }\n" + "public class X {\n"
        + " public static void main(String[] args) {\n"
        + "     Functional<? extends X> f = (Functional<? extends X> & I) (X c) -> {\n"
        + "       System.out.println(\"main\");\n" + "     };\n" + "     f.foo(null);\n" + "    }\n"
        + "}\n", }, "main");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=426411, [1.8][compiler] NoSuchMethodError at
  // runtime due to emission order of casts in intersection casts
  public void test426411() throws Exception {
    this.runConformTest(new String[] { "X.java", "import java.io.Serializable;\n"
        + "public class X {\n" + " public static void main(String argv[]) throws Exception {\n"
        + "   ((Serializable & AutoCloseable) (() -> {})).close();\n" + " }\n" + "}\n", }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=426411, [1.8][compiler] NoSuchMethodError at
  // runtime due to emission order of casts in intersection casts
  public void test426411b() throws Exception {
    this.runConformTest(new String[] { "X.java", "import java.io.Serializable;\n"
        + "interface AnotherAutoCloseable extends AutoCloseable {}\n" + "public class X {\n"
        + " public static void main(String argv[]) throws Exception {\n"
        + "   ((Serializable & AnotherAutoCloseable) (() -> {})).close();\n" + " }\n" + "}\n", },
        "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=426411, [1.8][compiler] NoSuchMethodError at
  // runtime due to emission order of casts in intersection casts
  public void test426411c() throws Exception {
    this.runConformTest(new String[] { "X.java", "import java.io.Serializable;\n"
        + "public class X {\n" + " public static void main(String argv[]) throws Exception {\n"
        + "   ((AutoCloseable & Serializable) (() -> {})).close();\n" + " }\n" + "}\n", }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=426411, [1.8][compiler] NoSuchMethodError at
  // runtime due to emission order of casts in intersection casts
  public void test426411d() throws Exception {
    this.runConformTest(new String[] { "X.java", "import java.io.Serializable;\n"
        + "interface AnotherAutoCloseable extends AutoCloseable {}\n" + "public class X {\n"
        + " public static void main(String argv[]) throws Exception {\n"
        + "   ((AnotherAutoCloseable & Serializable) (() -> {})).close();\n" + " }\n" + "}\n", },
        "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=426411, [1.8][compiler] NoSuchMethodError at
  // runtime due to emission order of casts in intersection casts
  public void test426411e() throws Exception {
    this.runConformTest(new String[] { "X.java", "import java.io.Serializable;\n"
        + "interface I {}\n" + "interface J extends I {\n" + "   static final int xyz = 99;\n"
        + "}\n" + "public class X {\n"
        + " public static void main(String argv[]) throws Exception {\n" + "   J j = new J() {};\n"
        + "   System.out.println(((I & J) j).xyz);\n" + " }\n" + "}\n", }, "99");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=426411, [1.8][compiler] NoSuchMethodError at
  // runtime due to emission order of casts in intersection casts
  public void test426411f() throws Exception {
    this.runConformTest(new String[] { "X.java", "import java.io.Serializable;\n"
        + "interface I {}\n" + "interface J extends I {\n" + "   final int xyz = 99;\n" + "}\n"
        + "public class X {\n" + " public static void main(String argv[]) throws Exception {\n"
        + "   J j = new J() {};\n" + "   System.out.println(((I & J) j).xyz);\n" + " }\n"
        + "}\n", }, "99");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=426086, [1.8] LambdaConversionException when
  // method reference to an inherited method is invoked from sub class
  public void test426086() throws Exception {
    this.runConformTest(new String[] { "X.java", "interface Functional {\n"
        + "    Long square(Integer a);\n" + "}\n" + "public class X {\n"
        + "    static class Base {\n" + "      private Long square(Integer a) {\n"
        + "             return Long.valueOf(a*a);\n" + "         } \n" + "    }\n"
        + "    static class SubClass extends Base {\n"
        + "        public Long callSquare(Integer i) {\n"
        + "            Functional fi = SubClass.super::square;\n"
        + "            return fi.square(i);\n" + "        }\n" + "    }\n"
        + "    public static void main(String argv[]) throws Exception {\n"
        + "     System.out.println(new SubClass().callSquare(-3));\n" + "    }\n" + "}\n", }, "9");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=426086, [1.8] LambdaConversionException when
  // method reference to an inherited method is invoked from sub class
  public void test426086a() throws Exception {
    this.runConformTest(new String[] { "X.java", "interface Functional {\n"
        + "    Long square(Integer a);\n" + "}\n" + "public class X {\n"
        + "    static class Base {\n" + "      private Long square(Integer a) {\n"
        + "             return Long.valueOf(a*a);\n" + "         } \n" + "    }\n"
        + "    static class SubClass extends Base {\n"
        + "        public Long callSquare(Integer i) {\n"
        + "            Functional fi = super::square;\n" + "            return fi.square(i);\n"
        + "        }\n" + "    }\n"
        + "    public static void main(String argv[]) throws Exception {\n"
        + "     System.out.println(new SubClass().callSquare(-3));\n" + "    }\n" + "}\n", }, "9");
  }

  // Bug 406744 - [1.8][compiler][codegen] LambdaConversionException seen when method reference
  // targets a varargs method.
  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=406744
  public void test406744a() {
    this.runConformTest(new String[] { "X.java", "interface I {\n"
        + " void foo(Integer a1, Integer a2, String a3);\n" + "}\n" + "class Y {\n"
        + " static void m(Number a1, Object... rest) {\n" + "   System.out.println(a1);\n"
        + "   print(rest);\n" + " }\n" + " static void print (Object [] o) {\n"
        + "   for (int i = 0; i < o.length; i++)\n" + "     System.out.println(o[i]);\n" + " }\n"
        + "}\n" + "public class X {\n" + " public static void main(String [] args) {\n"
        + "   I i = Y::m;\n" + "   i.foo(10, 20, \"10, 20\");\n" + " }\n" + "}\n", },
        "10\n" + "20\n" + "10, 20");
  }

  public void test406744b() {
    this.runConformTest(new String[] { "X.java", "interface I {\n"
        + " int foo(Integer a1, Integer a2, String a3);\n" + "}\n" + "class Y {\n"
        + " static int m(Number a1, Object... rest) {\n" + "   System.out.println(a1);\n"
        + "   print(rest);\n" + "   return 1;\n" + " }\n" + " static void print (Object [] o) {\n"
        + "   for (int i = 0; i < o.length; i++)\n" + "     System.out.println(o[i]);\n" + " }\n"
        + "}\n" + "public class X {\n" + " public static void main(String [] args) {\n"
        + "   I i = Y::m;\n" + "   i.foo(10, 20, \"10, 20\");\n" + " }\n" + "}\n", },
        "10\n" + "20\n" + "10, 20");
  }

  public void test406744c() {
    this.runConformTest(new String[] { "X.java", "interface I {\n"
        + " void foo(Integer a1, Integer a2, String a3);\n" + "}\n" + "class Y {\n"
        + "  Y(Number a1, Object... rest) {\n" + "   System.out.println(a1);\n"
        + "   print(rest);\n" + " }\n" + " static void m(Number a1, Object... rest) {\n"
        + "   System.out.println(a1);\n" + "   print(rest);\n" + " }\n"
        + " static void print (Object [] o) {\n" + "   for (int i = 0; i < o.length; i++)\n"
        + "     System.out.println(o[i]);\n" + " }\n" + "}\n" + "public class X {\n"
        + " public static void main(String [] args) {\n" + "   I i = Y::new;\n"
        + "   i.foo(10, 20, \"10, 20\");\n" + " }\n" + "}\n", }, "10\n" + "20\n" + "10, 20");
  }

  public void skippedtest406744d() {
    this.runConformTest(new String[] { "X.java", "interface I {\n"
        + " void foo(int a1, Integer a2, String a3);\n" + "}\n" + "interface Y {\n"
        + " static void m(float a1, Object... rest) {\n" + "   System.out.println(a1);\n"
        + "   print(rest);\n" + " }\n" + " static void print (Object [] o) {\n"
        + "   for (int i = 0; i < o.length; i++)\n" + "     System.out.println(o[i]);\n" + " }\n"
        + "}\n" + "public interface X extends Y{\n" + " public static void main(String [] args) {\n"
        + "   I i = Y::m;\n" + "   i.foo(10, 20, \"10, 20\");\n" + " }\n" + "}\n", },
        "10.0\n" + "20\n" + "10, 20");
  }

  public void test406744e() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " String method(int a);\n"
        + "}\n" + "class C {\n" + " static String foo(Integer... i) {\n" + "   return \"foo\";\n"
        + " }\n" + " static String goo(Integer bi, Integer... i) {\n" + "   return \"bar\";\n"
        + " }\n" + " public void foo() {\n" + "   I i;\n" + "   i = C::foo;\n"
        + "   System.out.println(i.method(0));\n" + "   i = C::goo;\n"
        + "   System.out.println(i.method(0));\n" + " }\n" + "}\n" + "public class X {\n"
        + " public static void main(String argv[])   {\n" + "   new C().foo();\n" + " }\n"
        + "}\n", }, "foo\n" + "bar");
  }

  public void test406744f() {
    this.runConformTest(new String[] { "X.java", "interface I {\n"
        + " void foo(Integer a1, Integer a2, String a3);\n" + "}\n" + "class Y {\n"
        + " void m(Number a1, Object... rest) {\n" + "   System.out.println(a1);\n"
        + "   print(rest);\n" + " }\n" + " static void print (Object [] o) {\n"
        + "   for (int i = 0; i < o.length; i++)\n" + "     System.out.println(o[i]);\n" + " }\n"
        + "}\n" + "public class X extends Y {\n" + " static void print (Object [] o) {\n"
        + "   for (int i = 0; i < o.length; i++)\n" + "     System.out.println(o[i]);\n" + " }\n"
        + " public static void main(String [] args) {\n" + "   new X().foo();\n" + " }\n"
        + " void foo() {\n" + "   I i = super::m;\n" + "   i.foo(10, 20, \"10, 20\");\n" + " }\n"
        + "}\n", }, "10\n" + "20\n" + "10, 20");
  }

  public void test406744g() {
    this.runConformTest(new String[] { "X.java", "interface I {\n"
        + " void foo(Integer a1, Integer a2, String a3);\n" + "}\n" + "class Y {\n"
        + " static void print (Object [] o) {\n" + "   for (int i = 0; i < o.length; i++)\n"
        + "     System.out.println(o[i]);\n" + " }\n" + "}\n" + "public class X extends Y {\n"
        + " private void m(Number a1, Object... rest) {\n" + "   System.out.println(a1);\n"
        + "   print(rest);\n" + " }\n" + " static void print (Object [] o) {\n"
        + "   for (int i = 0; i < o.length; i++)\n" + "     System.out.println(o[i]);\n" + " }\n"
        + " public static void main(String [] args) {\n" + "   new X().foo();\n" + " }\n"
        + " void foo() {\n" + "   I i = this::m;\n" + "   i.foo(10, 20, \"10, 20\");\n" + " }\n"
        + "}\n", }, "10\n" + "20\n" + "10, 20");
  }

  public void test406744h() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(int [] ia);\n"
        + "}\n" + "class Y {\n" + " void m(Object... rest) {\n"
        + "   System.out.println(\"Hello \" + rest.length);\n" + " }\n"
        + " static void print (Object [] o) {\n" + "   for (int i = 0; i < o.length; i++)\n"
        + "     System.out.println(o[i]);\n" + " }\n" + "}\n" + "public class X extends Y {\n"
        + " public static void main(String [] args) {\n" + "   new X().foo();\n" + " }\n"
        + " void foo() {\n" + "   I i = super::m;\n" + "   i.foo(new int [0]);\n" + " }\n"
        + "}\n", }, "Hello 1");
  }

  public void test406744i() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(int [] ia);\n"
        + "}\n" + "interface I1 {\n" + " void foo(int [] ia);\n" + "}\n" + "class Y {\n"
        + " void m(Object... rest) {\n" + "   System.out.println(\"Hello \" + rest.length);\n"
        + " }\n" + " static void print (Object [] o) {\n"
        + "   for (int i = 0; i < o.length; i++)\n" + "     System.out.println(o[i]);\n" + " }\n"
        + "}\n" + "public class X extends Y {\n" + " public static void main(String [] args) {\n"
        + "   new X().foo();\n" + " }\n" + " void foo() {\n" + "   I i = super::m;\n"
        + "   i.foo(new int [0]);\n" + "   I1 i1 = super::m;\n" + "   i1.foo(new int [0]);\n"
        + " }\n" + "}\n", }, "Hello 1\n" + "Hello 1");
  }

  public void test406744j() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(int [] ia);\n"
        + "}\n" + "class Y {\n" + " void m(Object... rest) {\n" + "   I i = this::n;\n"
        + "   i.foo(new int [0]);\n" + " }\n" + " void n(Object... rest) {\n"
        + "   System.out.println(\"Hello \" + rest.length);\n" + " }\n"
        + " static void print (Object [] o) {\n" + "   for (int i = 0; i < o.length; i++)\n"
        + "     System.out.println(o[i]);\n" + " }\n" + "}\n" + "public class X extends Y {\n"
        + " public static void main(String [] args) {\n" + "   new X().foo();\n" + " }\n"
        + " void foo() {\n" + "   I i = super::m;\n" + "   i.foo(new int [0]);\n" + " }\n"
        + "}\n", }, "Hello 1");
  }

  public void test406744k() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(int [] ia);\n"
        + "}\n" + "class Y {\n" + " static void m(Object... rest) {\n"
        + "   System.out.println(\"Hello \" + rest.length);\n" + " }\n"
        + " static void print (Object [] o) {\n" + "   for (int i = 0; i < o.length; i++)\n"
        + "     System.out.println(o[i]);\n" + " }\n" + "}\n" + "class Y1 extends Y { }\n"
        + "public class X {\n" + " public static void main(String [] args) {\n"
        + "   new X().foo();\n" + " }\n" + " void foo() {\n" + "   I i = Y::m;\n"
        + "   i.foo(new int [0]);\n" + "   i = Y1::m;\n" + "   i.foo(new int [0]);\n" + " }\n"
        + "}\n", }, "Hello 1\n" + "Hello 1");
  }

  public void test406744l() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(Integer i);\n"
        + "}\n" + "public class X {\n" + " static void foo(int ... x) {\n" + " }\n"
        + " public static void main(String[] args) {\n" + "   I i = X::foo;\n" + "   i.foo(1);\n"
        + "   System.out.println(\"Hello\");\n" + "}\n" + "}\n", }, "Hello");
  }

  public void test406744m() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(int i);\n" + "}\n"
        + "public class X {\n" + " static void foo(int ... x) {\n" + " }\n"
        + " public static void main(String[] args) {\n" + "   I i = X::foo;\n" + "   i.foo(1);\n"
        + "   System.out.println(\"Hello\");\n" + " }\n" + "}\n", }, "Hello");
  }

  public void test406744n() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(Integer i);\n"
        + "}\n" + "class Base {\n" + " void foo(Object ...objects) {\n"
        + "   System.out.println(\"Ok\");\n" + " }\n" + "}\n" + "public class X extends Base {\n"
        + " void foo(Object... objects) {\n" + "   throw new RuntimeException();\n" + " }\n"
        + " public static void main(String[] args) {\n" + "   new X().goo();\n" + " }\n"
        + " void goo() {\n" + "   I i = super::foo;\n" + "   i.foo(10);\n" + " }\n" + "}\n", },
        "Ok");
  }

  public void test406744o() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(int x);\n" + "}\n"
        + "class Base {\n" + " public void foo(int ...is) {\n" + "   System.out.println(\"foo\");\n"
        + " }\n" + "}\n" + "public class X extends Base {\n"
        + " public static void main( String[] args ) {\n" + "   I i = new X()::foo;\n"
        + "   i.foo(10);\n" + " }\n" + "}\n", }, "foo");
  }

  public void test406744p() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(int x);\n" + "}\n"
        + "public class X {\n" + " private void foo(int ...is) {\n"
        + "   System.out.println(\"foo\");\n" + " }\n"
        + " public static void main(String[] args ) {\n" + "   new X().new Y().foo();\n" + " }\n"
        + " class Y extends X {\n" + "   void foo() {\n" + "     I i = new X()::foo;\n"
        + "     i.foo(10);\n" + "   }\n" + " }\n" + "}\n", }, "foo");
  }

  public void test406744q() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(int x);\n" + "}\n"
        + "class Y {\n" + " public static void foo(int ...is) {\n"
        + "   System.out.println(\"Y.foo\");\n" + " }\n" + "}\n" + "public class X {\n"
        + " public static void foo(int ...is) {\n" + "   System.out.println(\"X.foo\");\n" + " }\n"
        + " public static void main(String[] args) {\n" + "   I i = X::foo;\n" + "   i.foo(10);\n"
        + "   i = Y::foo;\n" + "   i.foo(20);\n" + " }\n" + "}\n", }, "X.foo\n" + "Y.foo");
  }

  public void test406744r() {
    this.runConformTest(new String[] { "X.java", "interface I {\n"
        + " void foo(int t, int [] ia);\n" + "}\n" + "public class X {\n"
        + " public static void foo(Integer i, int ...is) {\n"
        + "   System.out.println(\"Y.foo\");\n" + " }\n"
        + " public static void main(String[] args) {\n" + "   I i = X::foo;\n"
        + "   i.foo(10, null);\n" + " }\n" + "}\n", }, "Y.foo");
  }

  public void test406744s() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X foo(int x);\n" + "}\n"
        + "public class X {\n" + " class Y extends X {\n" + "   Y(int ... x) {\n"
        + "     System.out.println(\"Y::Y\");\n" + "   }\n" + " }\n"
        + " public static void main(String[] args ) {\n" + "   new X().goo();\n" + " }\n"
        + " void goo() {\n" + "   I i = Y::new;\n" + "   i.foo(10);\n" + " }\n" + "}\n", }, "Y::Y");
  }

  public void test406744t() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X foo(int x);\n" + "}\n"
        + "public class X<T> {\n" + " class Y extends X {\n" + "     Y(int ... x) {\n"
        + "       System.out.println(\"Y::Y\");\n" + "     }\n" + " }\n"
        + " public static void main(String[] args ) {\n" + "   System.out.println(\"Hello\");\n"
        + "   new X().goo();\n" + " }\n" + " void goo() {\n" + "   I i = Y::new;\n"
        + "   i.foo(10);\n" + " }\n" + "}\n", }, "Hello\n" + "Y::Y");
  }

  public void test406744u() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X<String> foo(int x);\n"
        + "}\n" + "public class X<T> {  \n" + " class Y extends X<String> {\n"
        + "     Y(int ... x) {\n" + "       System.out.println(\"Y::Y\"); \n" + "     }\n" + " }\n"
        + " public static void main(String[] args ) {\n" + "   System.out.println(\"Hello\");\n"
        + "   new X<String>().goo();  \n" + " }\n" + " void goo() {\n" + "   I i = Y::new;\n"
        + "   i.foo(10); \n" + " }\n" + "}\n", }, "Hello\n" + "Y::Y");
  }

  public void test406744v() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X foo();\n" + "}\n"
        + "public class X {\n" + " private X(int ... is) {\n" + "   System.out.println(\"X::X\");\n"
        + " }\n" + " \n" + " public static void main(String[] args) {\n"
        + "   new X().new Y().goo();\n" + " }\n" + " public class Y {\n"
        + "   public void goo() {\n" + "     I i = X::new; \n" + "     i.foo();\n" + "   } \n"
        + " }\n" + "}\n", }, "X::X\n" + "X::X");
  }

  public void test406744w() {
    this.runConformTest(new String[] { "p2/B.java", "package p2;\n" + "import p1.*;\n"
        + "interface I {\n" + " void foo(int x);\n" + "}\n" + "interface J {\n"
        + " void foo(int x);\n" + "}\n" + "public class B extends A {\n" + " class Y {\n"
        + "   void g() {\n" + "     I i = B::foo;\n" + "     i.foo(10);\n"
        + "     J j = new B()::goo;\n" + "     j.foo(10);\n" + "   }\n" + " }\n"
        + " public static void main(String[] args) {\n" + "   new B().new Y().g();\n" + " }\n"
        + "}\n", "p1/A.java", "package p1;\n" + "import p2.*;\n" + "public class A {\n"
        + " protected static void foo(int ... is) {\n"
        + "     System.out.println(\"A's static foo\");\n" + " }\n"
        + " protected void goo(int ... is) {\n" + "     System.out.println(\"A's instance goo\");\n"
        + " }\n" + "}\n" }, "A\'s static foo\n" + "A\'s instance goo");
  }

  public void test406744x() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " void foo(int x);\n" + "}\n"
        + "public class X {\n" + " class Y {\n" + "   void goo() {\n" + "     I i = X::goo;\n"
        + "     i.foo(10);\n" + "   }\n" + " }\n" + " private static void goo(Integer i) {\n"
        + "   System.out.println(i);\n" + " }\n" + " public static void main(String[] args) {\n"
        + "    new X().new Y().goo(); \n" + " }\n" + "}\n" }, "10");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=427483, [Java 8] Variables in lambdas sometimes
  // can't be resolved
  public void test427483() {
    this.runConformTest(new String[] { "X.java", "import java.util.TreeSet;\n"
        + "public class X {\n" + " public static void main(String[] args) {\n"
        + "   new TreeSet<>((String qn1, String qn2) -> {\n" + "     boolean b = true;\n"
        + "     System.out.println(b); // ok\n" + "     if (b) {\n"
        + "     } // Eclipse says: b cannot be resolved or is not a field\n"
        + "     return qn1.compareTo(qn2);\n" + "   });\n" + " }\n" + "}\n" }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=427627, [1.8] List.toArray not compiled correctly
  // (NoSuchMethodError) within Lambda
  public void test427627() {
    this.runConformTest(new String[] { "X.java", "import java.util.ArrayList;\n"
        + "import java.util.List;\n" + "public class X {\n"
        + "  public static void main(String[] args) {\n" + "    Runnable r = () -> {\n"
        + "      List<SourceKey> keys = new ArrayList<>();\n" + "\n"
        + "      associate(\"Test\", keys.toArray(new SourceKey[keys.size()]));\n" + "    };\n"
        + "    r.run();\n" + "  }\n"
        + "  private static void associate(String o, SourceKey... keys) {\n"
        + "   System.out.println(o);\n" + "   System.out.println(keys.length);\n" + "  }\n"
        + "  public class SourceKey {\n" + "    public SourceKey(Object source, Object key) {\n"
        + "    }\n" + "  }\n" + "}\n" }, "Test\n0");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=427744, [1.8][compiler][regression] Issue with
  // boxing compatibility in poly conditional
  public void test427744() {
    this.runConformTest(new String[] { "X.java", "public class X {   \n"
        + "    public static void main(String argv[]) {\n"
        + "        int i = ((I) (x) -> { return 999; }).foo(true ? 0 : (Comparable) null);\n"
        + "        System.out.println(i);\n" + "    }\n" + "    interface I {\n"
        + "        int foo (Comparable arg); \n" + "        default int foo (Object arg) { \n"
        + "            return 0;\n" + "        }\n" + "    }\n" + "}\n" }, "999");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=427962, [1.8][compiler]
  // Stream#toArray(String[]::new) not inferred without help
  public void test427962() {
    this.runConformTest(new String[] { "X.java", "import java.util.function.Function;\n"
        + "import java.util.function.IntFunction;\n" + "import java.util.stream.Stream;\n"
        + "import java.util.stream.IntStream;\n" + "public class X {\n"
        + "  static <A, B> Stream<B> objmap(Function<A, B> p1, A[] p2) {return Stream.of(p2).map(p1);}\n" + "  static <B> Stream<B> intmap(IntFunction<B> p1, int[] p2) {return IntStream.of(p2).mapToObj(p1);}\n" + "  public static void main(String[] args) {\n" + "    Integer[] p12 = {1, 2, 3};\n" + "    int[] p22 = {1, 2, 3};\n" + "    //works\n" + "    String[] a11 = objmap(String::valueOf, p12).<String> toArray(String[]::new);\n" + "    String[] a21 = intmap(String::valueOf, p22).<String> toArray(String[]::new);\n" + "    //does not work\n" + "    String[] a12 = objmap(String::valueOf, p12).toArray(String[]::new);\n" + "    String[] a22 = intmap(String::valueOf, p22).toArray(String[]::new);\n" + "  }\n" + "}\n" }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=428112, [1.8][compiler] ClassCastException in
  // ReferenceExpression.generateCode
  public void test428112() {
    this.runConformTest(new String[] { "X.java", "import java.util.Arrays;\n"
        + "import java.util.Locale;\n" + "import java.util.stream.Collectors;\n"
        + "import java.util.stream.Stream;\n" + "public class X {\n"
        + " public static void main(String[] args) {\n"
        + "   System.out.println(Locale.lookup(Stream.of( \"de\", \"*-CH\" ).map(Locale.LanguageRange::new).collect(Collectors.toList()), \n" + "                                   Arrays.asList(Locale.getAvailableLocales())));\n" + " }\n" + "}\n" }, "de");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=428003, [1.8][compiler] Incorrect error on lambda
  // expression when preceded by another explicit lambda expression
  public void test428003() { // extracted small test
    this.runConformTest(new String[] { "X.java", "import java.util.Arrays;\n" + "public class X {\n"
        + "    public static void main(String[] args) {\n"
        + "        Arrays.sort(args, (String x, String y) -> x.length() - y.length());\n"
        + "        Arrays.sort(args, (x, y) -> Integer.compare(x.length(), y.length()));\n"
        + "    }\n" + "}\n" }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=428003, [1.8][compiler] Incorrect error on lambda
  // expression when preceded by another explicit lambda expression
  public void test428003a() { // full test case
    this.runConformTest(new String[] { "X.java", "import java.util.Arrays;\n" + "public class X {\n"
        + "    public static void main(String[] args) {\n"
        + "        String[] words = {\"java\", \"interface\", \"lambda\", \"expression\" };\n"
        + "        Arrays.sort(words, (String word1, String word2) -> {\n"
        + "                    if (word1.length() < word2.length())\n"
        + "                        return -1;\n"
        + "                    else if (word1.length() > word2.length())\n"
        + "                        return 1;\n" + "                    else\n"
        + "                        return 0;\n" + "                  });\n"
        + "        for (String word : words)\n" + "            System.out.println(word);\n"
        + "        words = new String [] {\"java\", \"interface\", \"lambda\", \"expression\" };\n" + "        Arrays.sort(words, (word1, word2) -> Integer.compare(word1.length(), word2.length()));\n" + "        for (String word : words)\n" + "            System.out.println(word);\n" + "        words = new String [] {\"java\", \"interface\", \"lambda\", \"expression\" };\n" + "        Arrays.sort(words, (String word1, String word2) -> Integer.compare(word1.length(), word2.length()));\n" + "        for (String word : words)\n" + "            System.out.println(word);\n" + "      }\n" + "  }\n" }, "java\n" + "lambda\n" + "interface\n" + "expression\n" + "java\n" + "lambda\n" + "interface\n" + "expression\n" + "java\n" + "lambda\n" + "interface\n" + "expression");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=428261, [1.8][compiler] Incorrect error: No
  // enclosing instance of the type X is accessible in scope
  public void test428261() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X foo(int a);\n" + "}\n"
        + "public class X {\n" + " public static void main(String[] args) {\n"
        + "   String s = \"Blah\";\n" + "   class Local extends X {\n" + "     Local(int a) {\n"
        + "       System.out.println(a);\n" + "       System.out.println(s);\n" + "     }\n"
        + "   }\n" + "   I i = Local::new; // Incorrect error here.\n" + "       i.foo(10);\n"
        + " }\n" + "}\n" }, "10\n" + "Blah");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=428261, [1.8][compiler] Incorrect error: No
  // enclosing instance of the type X is accessible in scope
  public void test428261a() {
    this.runConformTest(new String[] { "X.java", "interface I {\n" + " X foo(int a);\n" + "}\n"
        + "public class X {\n" + " void goo() {\n" + "   class Local extends X {\n"
        + "     Local(int a) {\n" + "       System.out.println(a);\n" + "     }\n" + "   }\n"
        + "   I i = Local::new;\n" + "       i.foo(10);\n" + " }\n"
        + "   public static void main(String [] args) {\n" + "        new X().goo();\n" + "   }\n"
        + "}\n" }, "10");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=429112, [1.8][compiler] Exception when compiling
  // Serializable array constructor reference
  public void test429112() {
    this.runConformTest(new String[] { "ArrayConstructorReference.java",
        "import java.io.Serializable;\n" + "import java.util.function.IntFunction;\n"
        + "public class ArrayConstructorReference {\n"
        + "  interface IF extends IntFunction<Object>, Serializable {}\n"
        + "  public static void main(String[] args) {\n" + "    IF factory=String[][][]::new;\n"
        + "    Object o = factory.apply(10);\n" + "    System.out.println(o.getClass());\n"
        + "    String [][][] sa = (String [][][]) o;\n" + "    System.out.println(sa.length);\n"
        + "  }\n" + "}\n" }, "class [[[Ljava.lang.String;\n" + "10");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=429112, [1.8][compiler] Exception when compiling
  // Serializable array constructor reference
  public void test429112a() {
    this.runConformTest(new String[] { "X.java", "import java.io.Serializable;\n" + "\n"
        + "interface IntFun<Object> {\n" + "  Object apply(int x);\n" + "}\n" + "\n"
        + "public class X {\n" + "  interface IF extends IntFunction<Object>, Serializable {\n"
        + "  }\n" + "  public static void main(String[] args) {\n"
        + "    IF factory = IntFun[][][]::new;\n" + "    Object o = factory.apply(10);\n"
        + "    System.out.println(o.getClass());\n" + "    IntFun[][][] sa = (IntFun[][][]) o;\n"
        + "    System.out.println(sa.length);\n" + "  }\n" + "}" },
        "class [[[Ljava.util.function.IntFunction;\n" + "10");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=429112, [1.8][compiler] Exception when compiling
  // Serializable array constructor reference
  public void test429112b() {
    this.runConformTest(new String[] { "ArrayConstructorReference.java",
        "import java.io.Serializable;\n" + "import java.util.function.IntFunction;\n"
        + "public class ArrayConstructorReference {\n"
        + "  interface IF extends IntFunction<Object>, Serializable {}\n"
        + "  public static void main(String[] args) {\n"
        + "    IF factory=java.util.function.IntFunction[]::new;\n"
        + "    Object o = factory.apply(10);\n" + "    System.out.println(o.getClass());\n"
        + "    java.util.function.IntFunction[] sa = (java.util.function.IntFunction[]) o;\n"
        + "    System.out.println(sa.length);\n" + "  }\n" + "}\n" },
        "class [Ljava.util.function.IntFunction;\n" + "10");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=429112, [1.8][compiler] Exception when compiling
  // Serializable array constructor reference
  public void test429112c() {
    this.runConformTest(new String[] { "ArrayConstructorReference.java",
        "import java.io.Serializable;\n" + "import java.util.function.IntFunction;\n"
        + "public class ArrayConstructorReference {\n"
        + "  interface IF extends IntFunction<Object>, Serializable {}\n"
        + "  public static void main(String[] args) {\n" + "    IF factory=String[]::new;\n"
        + "    Object o = factory.apply(10);\n" + "    System.out.println(o.getClass());\n"
        + "    String [] sa = (String []) o;\n" + "    System.out.println(sa.length);\n" + "  }\n"
        + "}\n" }, "class [Ljava.lang.String;\n" + "10");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=429763, [1.8][compiler] Incompatible type
  // specified for lambda expression's parameter
  public void test429763() {
    this.runConformTest(new String[] { "X.java", "import java.util.function.Function;\n"
        + "public class X {\n" + " public static void main(String[] args) {\n" + "       try {\n"
        + "       final int i = new Test<Integer>().test((Byte b) -> (int) b);\n"
        + "       } catch (NullPointerException e) {\n"
        + "            System.out.println(\"NPE\");\n" + "       }\n" + " }\n"
        + " static class Test<R> {\n" + "   <T> R test(Function<T,R> f) {\n" + "     return null;\n"
        + "   }\n" + " }\n" + "}\n" }, "NPE");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=429763, [1.8][compiler] Incompatible type
  // specified for lambda expression's parameter
  public void test429763a() {
    this.runConformTest(new String[] { "X.java", "import java.util.function.Function;\n"
        + "public class X {\n" + " public static void main(String[] args) {\n"
        + "   // does not compile\n" + "   new Test<Integer>().test((Byte b) -> (int) b);\n"
        + " }\n" + " static class Test<R> {\n" + "   <T> void test(Function<T,R> f) {\n" + "   }\n"
        + " }\n" + "}\n" }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=429759, [1.8][compiler] Lambda expression's
  // signature matching error
  public void test429759() {
    this.runConformTest(new String[] { "X.java", "import java.util.function.Function;\n"
        + "import java.util.function.Supplier;\n" + "public class X {\n"
        + " public static void main(String[] args) {\n"
        + "   final int i = new Test<Integer>().test(\"\", (String s) -> 1);\n" + " }\n"
        + " static class Test<R> {\n" + "   <T> R test(T t, Supplier<R> s) {\n"
        + "     return s.get();\n" + "   }\n" + "   <T> R test(T t, Function<T, R> f) {\n"
        + "     return f.apply(t);\n" + "   }\n" + " }\n" + "}\n" }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=429948, Unhandled event loop exception is thrown
  // when a lambda expression is nested
  public void test429948() {
    this.runConformTest(new String[] { "X.java", "import java.util.function.Supplier;\n"
        + "public class X {\n" + " public static void main(String[] args) {\n"
        + "   execute(() -> {\n" + "     executeInner(() -> {\n" + "     });\n"
        + "     return null;\n" + "   });\n" + "   System.out.println(\"done\");\n" + " }\n"
        + " static <R> R execute(Supplier<R> supplier) {\n" + "   return null;\n" + " }\n"
        + " static void executeInner(Runnable callback) {\n" + " }\n" + "}\n" }, "done");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=429969, [1.8][compiler] Possible RuntimeException
  // in Lambda tangles ECJ
  public void test429969() {
    this.runConformTest(new String[] { "X.java", "import java.util.Arrays;\n"
        + "import java.util.Optional;\n" + "public class X {\n"
        + "    public static void main(String[] args) {\n"
        + "        final String s = Arrays.asList(\"done\").stream().reduce(null, (s1,s2) -> {\n"
        + "                // THE FOLLOWING LINE CAUSES THE PROBLEM\n"
        + "                require(s1 != null || s2 != null, \"both strings are null\");\n"
        + "                    return (s1 != null) ? s1 : s2;\n"
        + "            }, (s1,s2) -> (s1 != null) ? s1 : s2);\n" + " \n"
        + "        System.out.println(s);\n" + "    }\n"
        + "    static void require(boolean condition, String msg) throws RuntimeException {\n"
        + "        if (!condition) {\n" + "            throw new RuntimeException(msg);\n"
        + "        }\n" + "    }\n" + "}\n" }, "done");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=430015, [1.8] NPE trying to disassemble classfile
  // with lambda method and MethodParameters
  public void test430015() {
    this.runConformTest(new String[] { "X.java", "import java.lang.reflect.Method;\n"
        + "import java.lang.reflect.Parameter;\n" + "import java.util.Arrays;\n"
        + "import java.util.function.IntConsumer;\n" + "public class X {\n"
        + "    IntConsumer xx(int a) {\n" + "        return i -> { };\n" + "    }\n"
        + "    public static void main(String[] args) {\n"
        + "        Method[] methods = X.class.getDeclaredMethods();\n"
        + "        for (Method method : methods) {\n"
        + "         if (method.getName().contains(\"lambda\")) {\n"
        + "             Parameter[] parameters = method.getParameters();\n"
        + "           System.out.println(Arrays.asList(parameters));\n" + "         }\n"
        + "        }\n" + "    }\n" + "}\n" }, "[int arg0]");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=430040, [1.8] [compiler] Type Type mismatch:
  // cannot convert from Junk13.ExpressionHelper<Object> to Junk13.ExpressionHelper<Object>
  public void test430040() {
    this.runConformTest(new String[] { "X.java", "public class X {\n"
        + "    public static void main(String [] args) {\n"
        + "        System.out.println(\"OK\");\n" + "    }\n" + "    class Observable<T> {}\n"
        + "    class ObservableValue<T> {}\n" + "    interface InvalidationListener {\n"
        + "        public void invalidated(Observable observable);\n" + "    }\n"
        + "    public interface ChangeListener<T> {\n"
        + "        void changed(ObservableValue<? extends T> observable, T oldValue, T newValue);\n" + "    }\n" + "    static class ExpressionHelper<T> {}\n" + "    public static <T> ExpressionHelper<T> addListener(ExpressionHelper<T> helper, ObservableValue<T> observable, InvalidationListener listener) {\n" + "        return helper;\n" + "    }\n" + "    public static <T> ExpressionHelper<T> addListener(ExpressionHelper<T> helper, ObservableValue<T> observable, ChangeListener<? super T> listener) {\n" + "        return helper;\n" + "    }\n" + "    private ExpressionHelper<Object> helper;\n" + "    public void junk() {\n" + "        helper = (ExpressionHelper<Object>) addListener(helper, null, (Observable o) -> {throw new RuntimeException();});\n" + "        helper = addListener(helper, null, (Observable o) -> {throw new RuntimeException();});\n" + "    }\n" + "}\n" }, "OK");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=430043, [1.8][compiler] Cannot infer type
  // arguments for Junk14<>
  public void test430043() {
    this.runConformTest(new String[] { "X.java", "import java.io.File;\n"
        + "import java.io.IOException;\n" + "import java.io.StringReader;\n"
        + "import java.nio.file.Files;\n" + "import java.text.MessageFormat;\n"
        + "import java.util.*;\n" + "import java.util.function.Function;\n"
        + "import java.util.jar.Attributes;\n" + "import java.util.jar.JarFile;\n"
        + "import java.util.jar.Manifest;\n" + "public class X<T>  {\n"
        + "    public X(String name, String description, String id, Class<T> valueType, String[] fallbackIDs, Function<Map<String, ? super Object>, T> defaultValueFunction, boolean requiresUserSetting, Function<String, T> stringConverter) {\n" + "    }\n" + "    public static final X<String> NAME  =\n" + "            new X<>(\n" + "                    null,\n" + "                    null,\n" + "                    null,\n" + "                    String.class,\n" + "                    null,\n" + "                    params -> {throw new IllegalArgumentException(\"junk14\");},\n" + "                    true,\n" + "                    s -> s\n" + "            );\n" + "     public static void main(String [] args) {\n" + "         System.out.println(\"OK\");\n" + "     }\n" + "}\n" }, "OK");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=430035, [1.8][compiler][codegen] Bridge methods
  // are not generated for lambdas/method references
  public void test430035() {
    this.runConformTest(new String[] { "X.java", "import java.util.function.Consumer;\n"
        + "public class X {\n" + "    interface StringConsumer extends Consumer<String> {\n"
        + "        void accept(String t);\n" + "    }\n"
        + "    public static void main(String... x) {\n"
        + "      StringConsumer c = s->System.out.println(\"m(\"+s+')');\n"
        + "      c.accept(\"direct call\");\n" + "      Consumer<String> c4b=c;\n"
        + "      c4b.accept(\"bridge method\");\n" + "    }\n" + "}\n" },
        "m(direct call)\n" + "m(bridge method)");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=430035, [1.8][compiler][codegen] Bridge methods
  // are not generated for lambdas/method references
  public void test430035a() { // test reference expressions requiring bridges.
    this.runConformTest(new String[] { "X.java", "import java.util.function.Consumer;\n"
        + "public class X {\n" + "    interface StringConsumer extends Consumer<String> {\n"
        + "        void accept(String t);\n" + "    }\n"
        + "    static void m(String s) { System.out.println(\"m(\"+s+\")\"); } \n"
        + "    public static void main(String... x) {\n" + "      StringConsumer c = X::m;\n"
        + "      c.accept(\"direct call\");\n" + "      Consumer<String> c4b=c;\n"
        + "      c4b.accept(\"bridge method\");\n" + "    }\n" + "}\n" },
        "m(direct call)\n" + "m(bridge method)");
  }

  /**
   * Won't fix unless encountered in the wild.
   */
  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=430035, [1.8][compiler][codegen] Bridge methods
  // are not generated for lambdas/method references
  // public void test430035b() {
  // this.runConformTest(new String[] { "X.java", "interface I<T> {\n" + " void foo(T t);\n" + "}\n"
  // + "interface J<T> {\n" + " void foo(T t);\n" + "}\n"
  // + "interface K extends I<String>, J<String> {\n" + "}\n" + "public class X {\n"
  // + "    public static void main(String... x) {\n"
  // + "      K k = s -> System.out.println(\"m(\"+s+')');\n" + "      k.foo(\"direct call\");\n"
  // + "      J<String> j = k;\n" + "      j.foo(\"bridge method\");\n"
  // + "      I<String> i = k;\n" + "      i.foo(\"bridge method\");\n" + "    }\n" + "}\n" },
  // "m(direct call)\n" + "m(bridge method)\n" + "m(bridge method)");
  // }
  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=430035, [1.8][compiler][codegen] Bridge methods
  // are not generated for lambdas/method references
  public void test430035c() {
    this.runConformTest(new String[] { "X.java", "interface I<T> {\n"
        + " void foo(String t, T u);\n" + "}\n" + "interface J<T> {\n"
        + " void foo(T t, String u);\n" + "}\n" + "interface K extends I<String>, J<String> {\n"
        + " void foo(String t, String u);\n" + "}\n" + "public class X {\n"
        + "    public static void main(String... x) {\n"
        + "      K k = (s, u) -> System.out.println(\"m(\"+ s + u + ')');\n"
        + "      k.foo(\"direct\", \" call\");\n" + "      J<String> j = k;\n"
        + "      j.foo(\"bridge\",  \" method(j)\");\n" + "      I<String> i = k;\n"
        + "      i.foo(\"bridge\",  \" method(i)\");\n" + "    }\n" + "}\n" },
        "m(direct call)\n" + "m(bridge method(j))\n" + "m(bridge method(i))");
  }

  /**
   * Won't fix unless encountered in the wild.
   */
  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=430035, [1.8][compiler][codegen] Bridge methods
  // are not generated for lambdas/method references
  // public void test430035d() { // 8b131 complains of ambiguity.
  // this.runConformTest(new String[] { "X.java", "interface I<T> {\n"
  // + " void foo(String t, T u);\n" + "}\n" + "interface J<T> {\n"
  // + " void foo(T t, String u);\n" + "}\n" + "interface K extends I<String>, J<String> {\n"
  // + "}\n" + "public class X {\n" + "    public static void main(String... x) {\n"
  // + "      K k = (s, u) -> System.out.println(\"m(\"+ s + u + ')');\n"
  // + "      k.foo(\"direct\", \" call\");\n" + "      J<String> j = k;\n"
  // + "      j.foo(\"bridge\",  \" method(j)\");\n" + "      I<String> i = k;\n"
  // + "      i.foo(\"bridge\",  \" method(i)\");\n" + "    }\n" + "}\n" },
  // "m(direct call)\n" + "m(bridge method(j))\n" + "m(bridge method(i))");
  // }
  /**
   * Won't fix unless encountered in the wild.
   */
  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=430035, [1.8][compiler][codegen] Bridge methods
  // are not generated for lambdas/method references
  // public void test430035e() { // 8b131 complains of ambiguity in call.
  // this.runConformTest(new String[] { "X.java", "interface I<T> {\n"
  // + " Object foo(String t, T u);\n" + "}\n" + "interface J<T> {\n"
  // + " String foo(T t, String u);\n" + "}\n" + "interface K extends I<String>, J<String> {\n"
  // + "}\n" + "public class X {\n" + "    public static void main(String... x) {\n"
  // + "      K k = (s, u) -> s + u;\n"
  // + "      System.out.println(k.foo(\"direct\", \" call\"));\n" + "      J<String> j = k;\n"
  // + "      System.out.println(j.foo(\"bridge\",  \" method(j)\"));\n"
  // + "      I<String> i = k;\n"
  // + "      System.out.println(i.foo(\"bridge\",  \" method(i)\"));\n" + "    }\n" + "}\n" },
  // "direct call\n" + "bridge method(j)\n" + "bridge method(i)");
  // }
  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=430035, [1.8][compiler][codegen] Bridge methods
  // are not generated for lambdas/method references
  public void test430035f() { // ensure co-variant return emits a bridge request.
    this.runConformTest(new String[] { "X.java", "interface I<T> {\n"
        + " Object foo(String t, String u);\n" + "}\n" + "interface J<T> {\n"
        + " String foo(String t, String u);\n" + "}\n"
        + "interface K extends I<String>, J<String> {\n" + "}\n" + "public class X {\n"
        + "    public static void main(String... x) {\n" + "      K k = (s, u) -> s + u;\n"
        + "      System.out.println(k.foo(\"direct\", \" call\"));\n" + "      J<String> j = k;\n"
        + "      System.out.println(j.foo(\"bridge\",  \" method(j)\"));\n"
        + "      I<String> i = k;\n"
        + "      System.out.println(i.foo(\"bridge\",  \" method(i)\"));\n" + "    }\n" + "}\n" },
        "direct call\n" + "bridge method(j)\n" + "bridge method(i)");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=430241, [1.8][compiler] Raw return type results
  // in incorrect covariant return bridge request to LambdaMetaFactory
  public void test430241() { // ensure raw return type variant does not emit a bridge request.
    this.runConformTest(new String[] { "X.java", "interface K extends I, J {\n" + "}\n"
        + "interface I {\n" + "    Comparable<Integer> foo();\n" + "}\n" + "interface J {\n"
        + "    Comparable foo();\n" + "}\n" + "public class X {\n"
        + " public static void main(String[] args) {\n" + "   K k = () -> null;\n"
        + "   System.out.println(k.foo());\n" + " }\n" + "}\n" }, "null");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=430310, [1.8][compiler] Functional interface
  // incorrectly rejected as not being.
  public void test430310() {
    this.runConformTest(new String[] { "X.java", "interface Func1<T1, R> {\n"
        + "        R apply(T1 v1);\n" + "        void other();\n" + "}\n"
        + "@FunctionalInterface // spurious error: F1<T, R> is not a functional interface\n"
        + "public interface X<T1, R> extends Func1<T1, R> {\n" + " default void other() {}\n"
        + "   public static void main(String [] args) {\n" + "       System.out.println(\"OK\");\n"
        + "   }\n" + "}\n" }, "OK");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=430310, [1.8][compiler] Functional interface
  // incorrectly rejected as not being.
  public void test430310a() {
    this.runConformTest(new String[] { "X.java", "@FunctionalInterface\n"
        + "public interface X<T1, T2, R> {\n" + "    R apply(T1 v1, T2 v2);\n"
        + "    default void other() {}\n" + "    public static void main(String[] args) {\n"
        + "        System.out.println(\"OK\");\n" + "    }\n" + "}\n" }, "OK");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=430310, [1.8][compiler] Functional interface
  // incorrectly rejected as not being.
  public void test430310b() {
    this.runConformTest(new String[] { "X.java", "interface I1 {\n" + " int foo(String s);\n"
        + "}\n" + "@FunctionalInterface\n" + "interface A1 extends I1 {\n" + " @Override\n"
        + " default int foo(String s) {\n" + "   return -1;\n" + " }\n"
        + " int foo(java.io.Serializable s);\n" + "}\n" + "public class X {\n"
        + " public static void main(String[] args) {\n" + "   System.out.println(\"OK\");\n"
        + " }\n" + "}\n" }, "OK");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=430310, [1.8][compiler] Functional interface
  // incorrectly rejected as not being.
  public void test430310c() {
    this.runConformTest(new String[] { "X.java", "interface I2 {\n" + " int foo(String s);\n"
        + "}\n" + "@FunctionalInterface\n" + "interface A2 extends I2 {\n" + " @Override\n"
        + " default int foo(String s) {\n" + "   return -1;\n" + " }\n"
        + " int bar(java.io.Serializable s);\n" + "}\n" + "public class X {\n"
        + " public static void main(String[] args) {\n" + "   System.out.println(\"OK\");\n"
        + " }\n" + "}\n" }, "OK");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=432619, [1.8] Bogus error from method reference:
  // "should be accessed in a static way"
  public void test432619() throws Exception {
    this.runConformTest(new String[] { "X.java", "import java.util.function.BiConsumer;\n"
        + "public interface X<E extends Exception> {\n" + " static void foo() {\n"
        + "     BiConsumer<double[][], Double> biConsumer2 = Re2::accumulate;\n" + " }\n"
        + " static class Re2 {\n"
        + "     static void accumulate(double[][] container, Double value) {}\n" + " }\n"
        + "   public static void main(String [] args) {\n" + "       System.out.println(\"OK\");\n"
        + "   }\n" + "}\n" }, "OK");
  }

  /**
   * Library issue.
   */
  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=432619, [1.8] Bogus error from method reference:
  // "should be accessed in a static way"
  // public void test432619a() throws Exception {
  // this.runConformTest(new String[] { "StreamInterface.java", "import java.util.Map;\n"
  // + "import java.util.stream.Collector;\n"
  // + "public interface StreamInterface<E extends Exception> {\n"
  // + " static class DoubleCo {\n" + "   private static class Re2 {\n"
  // + "     static <K, E extends Exception> Map<K, double[]> internalToMapToList2() {\n"
  // + "       Collector<Double, double[][], double[][]> toContainer1 = Collector.of(\n"
  // +
  // "       //The method supply() from the type StreamInterface.DoubleCo.Re2 should be accessed in a static way\n"
  // + "         StreamInterface.DoubleCo.Re2::supply,\n" +
  // "         //The method accumulate(double[][], Double) from the type StreamInterface.DoubleCo.Re2 should be accessed in a static way\n"
  // + "         StreamInterface.DoubleCo.Re2::accumulate,\n" +
  // "         //The method combine(double[][], double[][]) from the type StreamInterface.DoubleCo.Re2 should be accessed in a static way\n"
  // + "         StreamInterface.DoubleCo.Re2::combine);\n" +
  // "       Collector<Double, double[][], double[][]> toContainer2 =\n" +
  // "       //All 3 from above:\n" +
  // "         Collector.of(DoubleCo.Re2::supply, DoubleCo.Re2::accumulate, DoubleCo.Re2::combine);\n"
  // + "       return null;\n" + "     }\n" + "     private static double[][] supply() {\n" +
  // "       return new double[64][];\n" + "     }\n" +
  // "     private static void accumulate(double[][] container, Double value) {}\n" +
  // "     private static double[][] combine(double[][] container, double[][] containerRight) {\n" +
  // "       return new double[container.length + containerRight.length][];\n" + "     }\n" +
  // "   }\n" + " }\n" + "     public static void main(String [] args) {\n" +
  // "         System.out.println(\"OK\");\n" + "     }\n" + "}\n" }, "OK");
  // }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=432682, [1.8][compiler] Type mismatch error with
  // lambda expression
  public void test432682() throws Exception {
    this.runConformTest(new String[] { "X.java", "import java.util.Optional;\n"
        + "public class X {\n" + " public static void main(String[] args) {\n"
        + "   Optional<String> userName = Optional.of(\"sa\");\n"
        + "   Optional<String> password = Optional.of(\"sa\");\n"
        + "   boolean isValid = userName.flatMap(u -> {\n" + "     return password.map(p -> {\n"
        + "       return u.equals(\"sa\") && p.equals(\"sa\");\n" + "     });\n"
        + "   }).orElse(false);\n" + "   System.out.println(isValid);\n" + " }\n" + "}\n" },
        "true");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=432520, compiler "duplicate method" bug with
  // lamdas and generic interfaces
  public void test432520() throws Exception {
    this.runConformTest(new String[] { "X.java", "public class X {\n"
        + " public static void withProvider(Provider<String> provider) { }\n"
        + " public static void main(String [] args) {\n" + "   withProvider(() -> \"user\");\n"
        + " }\n" + "}\n" + "interface ParentProvider<T> {\n" + " T get();\n" + "}\n"
        + "// if you remove the extends clause everything works fine\n"
        + "interface Provider<T> extends ParentProvider<T> {\n" + " T get();\n" + "}\n" }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=432625, [1.8] VerifyError with lambdas and
  // wildcards
  public void test432625() throws Exception {
    this.runConformTest(new String[] { "X.java", "import java.util.stream.Stream;\n"
        + "public class X {\n" + "    public static void main(String[] args) {\n"
        + "        Stream<?> stream = Stream.of(\"A\");\n"
        + "        stream.map(x -> (String) x);\n" + "    }\n" + "}\n" }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=431190, [1.8] VerifyError when using a method
  // reference
  public void test431190() throws Exception {
    this.runConformTest(new String[] { "Java8VerifyError.java", "public class Java8VerifyError {\n"
        + "    public static class Foo {\n" + "        public Object get() {\n"
        + "            return new Object();\n" + "        }\n" + "    }\n"
        + "    @FunctionalInterface\n" + "    public static interface Provider<T> {\n"
        + "        public T get();\n" + "    }\n" + "    public static void main(String[] args) {\n"
        + "        Provider<Foo> f = () -> new Foo();\n"
        + "        Provider<Provider<Object>> meta = () -> f.get()::get;\n" + "    }\n" + "}\n" },
        "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=431514 [1.8] Incorrect compilation error in
  // lambda expression
  public void test431514() {
    this.runConformTest(new String[] { "X.java", "class X {\n" + " void fun1(int x) {\n"
        + "   class Local {\n" + "     FI test= () -> {\n" + "       try {\n"
        + "       } catch (Exception e) {\n" + "         int x;\n" + "       };\n" + "     };\n"
        + "   }\n" + " }\n" + "}\n" + "interface FI {\n" + " void foo();\n" + "}" });
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=431514 [1.8] Incorrect compilation error in
  // lambda expression
  public void test431514a() {
    this.runConformTest(new String[] { "X.java", "class X {\n" + " void fun1(int x) {\n"
        + "   class Local {\n" + "     class L1 { }\n" + "     int y;\n" + "     FI test= () -> {\n"
        + "       class L1 { } \n" + "       int y; \n" + "     };\n" + "   }\n" + " }\n" + " \n"
        + "}\n" + "interface FI {\n" + " void foo();\n" + "}" });
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=432531 [1.8] VerifyError with anonymous subclass
  // inside of lambda expression in the superclass constructor call
  public void test432531() {
    this.runConformTest(new String[] { "Y.java", "import java.util.function.Supplier;\n"
        + "class E {\n" + " E(Supplier<Object> factory) { }\n" + "}\n"
        + "public class Y extends E {\n" + " Y() {\n" + "   super(() -> new Object() {\n"
        + "   });\n" + " }\n" + " public static void main(String[] args) {\n" + "   new Y();\n"
        + " }\n" + "}" });
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=434297 [1.8] NPE in LamdaExpression.analyseCode
  // with lamda expression nested in a conditional expression
  public void test434297() {
    this.runConformTest(new String[] { "X.java", "import java.util.ArrayList;\n"
        + "import java.util.Collection;\n" + "import java.util.Collections;\n"
        + "import java.util.Comparator;\n" + "import java.util.List;\n" + "public class X {\n"
        + "  public static void main(String[] args) {\n" + "  }\n" + "  public void bla() {\n"
        + "    boolean b = Boolean.TRUE.booleanValue();\n"
        + "    List<String> c1 = new ArrayList<>();\n"
        + "    Collections.sort(c1, b ? null : new Bar(new ArrayList<>(),Comparator.nullsLast((a,e) -> {return 0;})));\n" + "  }\n" + "  private static class Bar implements Comparator<String>{\n" + "   public <T> Bar(Collection<T> col, Comparator<T> comp) { }\n" + " @Override\n" + " public int compare(String o1, String o2) {\n" + "   return 0;\n" + " }\n" + "  }\n" + "}" });
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=439515 [1.8] ECJ reports error at method
  // reference to overloaded instance method
  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=440643, Eclipse compiler doesn't like method
  // references with overloaded varargs method
  public void test439515() {
    this.runConformTest(new String[] { "X.java", "interface Fun<T, R> {\n" + " R apply(T arg);\n"
        + "}\n" + "public class X {\n" + " int size() {\n" + "   return -1;\n" + " }\n"
        + " int size(Object arg) {\n" + "   return 0;\n" + " }\n" + " int size(X arg) {\n"
        + "   return 1;\n" + " }\n" + " public static void main(String args[]) {\n"
        + "   Fun<X, Integer> f1 = X::size;\n" + "   System.out.println(f1.apply(new X()));\n"
        + " }\n" + "}\n" }, "-1");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=439515 [1.8] ECJ reports error at method
  // reference to overloaded instance method
  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=440643, Eclipse compiler doesn't like method
  // references with overloaded varargs method
  public void test439515a() {
    this.runConformTest(new String[] { "X.java", "interface Fun<T, R> {\n" + " R apply(T arg);\n"
        + "}\n" + "public class X {\n" + " static int size() {\n" + "   return -1;\n" + " }\n"
        + " static int size(Object arg) {\n" + "   return 0;\n" + " }\n"
        + " static int size(X arg) {\n" + "   return 1;\n" + " }\n"
        + " public static void main(String args[]) {\n" + "   Fun<X, Integer> f1 = X::size;\n"
        + "   System.out.println(f1.apply(new X()));\n" + " }\n" + "}\n" }, "1");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=438534 Java8 java.lang.Method.getGeneric* methods
  // fail with java.lang.reflect.GenericSignatureFormatError: Signature Parse error: Expected Field
  // Type Signature
  public void test438534() {
    this.runConformTest(new String[] { "ByteCodeTest.java", "import java.lang.reflect.Method;\n"
        + "import java.security.AccessController;\n"
        + "import java.security.PrivilegedAction;\n" + "import java.util.Collections;\n"
        + "import java.util.Comparator;\n" + "public class ByteCodeTest {\n"
        + "  public static class BrokenByteCode {\n" + "    public void hello() {\n"
        + "      Collections.sort(Collections.<String> emptyList(), Comparator.comparing((String data) -> data.length()));\n" + "    }\n" + "  }\n" + "  public static void main(String[] args) {\n" + "    for (Method method : AccessController.doPrivileged((PrivilegedAction<Method[]>) () -> BrokenByteCode.class.getDeclaredMethods())) {\n" + "      method.getGenericExceptionTypes();\n" + "      method.getGenericParameterTypes();\n" + "      method.getGenericReturnType();\n" + "    }\n" + "    System.out.println(\"SUCCESS\");\n" + "  }\n" + "}\n" }, "SUCCESS");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=440152
  // [codegen]"Missing code implementation in the compiler" on cascaded inner class references
  public void test440152() {
    this.runConformTest(new String[] { "Foo.java", "import java.util.function.Function;\n"
        + "interface Foo {void alpha(Bar pBar);}\n" + "class Bar {Object bravo() {return null;}}\n"
        + "class Test {\n" + "  Test(Function pFunction) {\n"
        + "    class Baz {public Baz(Object pObj) {pFunction.apply(pObj);}}\n"
        + "    delta(pBar -> charlie(new Baz(pBar.bravo())));\n" + "  }\n"
        + "  void charlie(Object pRemovals) {}\n" + "  void delta(Foo pListener) {}\n" + "}" });
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=440152
  // [codegen]"Missing code implementation in the compiler" on cascaded inner class references
  public void test440152a() {
    this.runConformTest(new String[] { "Foo.java", "import java.util.function.Function;\n"
        + "interface Foo {void alpha(Bar pBar);}\n" + "class Bar {Object bravo() {return null;}}\n"
        + "class Test {\n" + " Test(Function pFunction) {\n" + "     class Baz {\n"
        + "       public Baz(Object pObj) {\n" + "       }\n"
        + "       class NestedBaz extends Baz {\n" + "         NestedBaz(Object pObj) {\n"
        + "           super(pObj);\n" + "           pFunction.apply(pObj);\n" + "         }\n"
        + "       }\n" + "       }\n"
        + "     delta(pBar -> charlie(new Baz(pBar).new NestedBaz(pBar.bravo())));\n" + "   }\n"
        + "   void charlie(Object pRemovals) {}\n" + "   void delta(Foo pListener) {}\n" + "}\n" });
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=432110, [1.8][compiler] nested lambda type
  // incorrectly inferred vs javac
  public void test432110() {
    this.runConformTest(new String[] { "X.java", "import java.util.function.Function;\n"
        + "public interface X {\n" + "    default void test() {\n"
        + "        testee().flatMap(_warning_ -> {\n" + "            return result().map(s -> 0);\n"
        + "        });\n" + "    }\n" + "    Either<Integer, Integer> testee();\n"
        + "    Either<Integer, String> result();\n" + "    static interface Either<L, R> {\n"
        + "        <U> Either<L, U> flatMap(Function<? super R, Either<L, U>> mapper);\n"
        + "        <U> Either<L, U> map(Function<? super R, U> mapper);\n" + "    }\n"
        + "    public static void main(String [] args) {\n"
        + "        System.out.println(\"OK\");\n" + "    }\n" + "}\n", }, "OK");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=437781, [1.8][compiler] Eclipse accepts code
  // rejected by javac because of ambiguous method reference
  public void test437781() {
    this.runConformTest(new String[] { "X.java", "import java.util.function.Consumer;\n"
        + "import java.util.function.Function;\n" + "public class X {\n"
        + " public static void main(String[] args) {\n"
        + "   new X().visit( System.out::println );\n" + " }\n"
        + " public boolean visit(Function<Integer, Boolean> func) {\n"
        + "   System.out.println(\"Function\");\n" + "   return true;\n" + " }\n"
        + " public void visit(Consumer<Integer> func) {\n"
        + "   System.out.println(\"Consumer\");\n" + " } \n" + "}\n" }, "Consumer");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=443889, [1.8][compiler] Lambdas get compiled to
  // duplicate methods
  public void test443889() {
    this.runConformTest(new String[] { "X.java", "import java.util.function.BiConsumer;\n"
        + "import java.util.function.Consumer;\n" + "public class X {\n"
        + "    public interface CurryBiConsumer<T, U> extends BiConsumer<T, U> {\n"
        + "        default public CurryConsumer<U> curryFirst(T t) {\n"
        + "            return (u) -> accept(t, u);\n" + "        }\n"
        + "        default public CurryConsumer<T> currySecond(U u) {\n"
        + "            return (t) -> accept(t, u);\n" + "        }\n" + "    }\n"
        + "    public interface CurryConsumer<T> extends Consumer<T> {\n"
        + "        default public Runnable curry(T t) {\n" + "            return () -> accept(t);\n"
        + "        }\n" + "    }\n" + "    static void execute(Runnable r) {\n"
        + "        System.out.println(\"BEFORE\");\n" + "        r.run();\n"
        + "        System.out.println(\"AFTER\");\n" + "    }\n"
        + "    static void display(String str, int count) {\n"
        + "        System.out.println(\"DISP: \" + str + \" \" + count);\n" + "    }\n"
        + "    public static void main(String[] args) {\n"
        + "        CurryBiConsumer<String, Integer> bc = X::display;\n"
        + "        execute(bc.curryFirst(\"Salomon\").curry(42));\n" + "    }\n" + "}\n" },
        "BEFORE\n" + "DISP: Salomon 42\n" + "AFTER");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=441907, [1.8][compiler] Eclipse 4.4.x compiler
  // generics bugs with streams and lambdas
  public void test441907() {
    this.runConformTest(new String[] { "X.java", "import java.util.*;\n"
        + "import java.util.function.Predicate;\n" + "import java.util.stream.Stream;\n"
        + "public class X {\n" + "  public static class FooBar<V> {\n" + "  }\n"
        + "  public interface FooBarred {\n"
        + "    public <V> boolean hasFooBar(final FooBar<V> fooBar);\n" + "  }\n"
        + "  public interface Widget extends FooBarred {\n" + "  }\n"
        + "  public static void test() {\n" + "    Set<FooBar<?>> foobars = new HashSet<>();\n"
        + "    Set<Widget> widgets = new HashSet<>();\n" + "    Stream<X.FooBar<?>> s = null;\n"
        + "    FooBarred fb = null;\n" + "    fb.hasFooBar((FooBar<?>) null);\n"
        + "    boolean anyWidgetHasFooBar = widgets.stream().anyMatch(\n"
        + "        widget -> foobars.stream().anyMatch(widget::hasFooBar)\n" + "        );\n"
        + "  }\n" + "}\n" }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=444773, [1.8][compiler] NullPointerException in
  // LambdaExpression.analyseCode
  public void test444773() {
    this.runConformTest(new String[] { "X.java", "import java.util.ArrayList;\n"
        + "import java.util.List;\n" + "import java.util.Optional;\n" + " \n"
        + "public class X {\n" + "  static class Container {\n"
        + "    final private String s;\n" + "    public Container(String s) { this.s = s; }\n"
        + "  }\n" + " \n" + "  public static void main(String[] args) {\n"
        + "    final List<Container> list = new ArrayList<>();\n"
        + "    final Optional<String> optStr = Optional.of(\"foo\");\n"
        + "    list.add(new Container(optStr.orElseThrow(() -> new IllegalStateException()))); // Error here\n" + " \n" + "    // This will work:\n" + "    final String s = optStr.orElseThrow(IllegalStateException::new);\n" + "    list.add(new Container(s));  \n" + "  }\n" + "}\n" }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=444772, [1.8][compiler] NullPointerException in
  // ReferenceExpression.shouldGenerateImplicitLambda
  public void test444772() {
    this.runConformTest(new String[] { "X.java", "import java.util.ArrayList;\n"
        + "import java.util.List;\n" + "import java.util.Optional;\n" + " \n"
        + "public class X {\n" + "  static class Container {\n"
        + "    final private String s;\n" + "    public Container(String s) { this.s = s; }\n"
        + "  }\n" + " \n" + "  public static void main(String[] args) {\n"
        + "    final List<Container> list = new ArrayList<>();\n"
        + "    final Optional<String> optStr = Optional.of(\"foo\");\n"
        + "    list.add(new Container(optStr.orElseThrow(IllegalStateException::new))); // Error here\n" + " \n" + "    // This will work:\n" + "    final String s = optStr.orElseThrow(IllegalStateException::new);\n" + "    list.add(new Container(s));  \n" + "  }\n" + "}\n" }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=444803, [1.8][compiler] Exception in thread
  // "main" java.lang.VerifyError: Bad local variable type
  public void test444803() {
    this.runConformTest(new String[] { "X.java", "import java.util.ArrayList;\n"
        + "import java.util.Collection;\n" + "import java.util.List;\n" + "public class X {\n"
        + "    X abc = null;\n" + "    public static void main(String[] args) {\n"
        + "        new X();\n" + "    }\n" + "    private void doSth() {\n"
        + "        final List<String> l = new ArrayList<>();\n" + "        try {\n"
        + "            System.out.println(\"ok\");\n" +
        // "            Runnable r = () -> abc.terminateInstances(abc.withInstanceIds(l));\n" +
        "        } finally {\n"
        + "            Runnable r = () -> abc.terminateInstances(abc.withInstanceIds(l));\n"
        + "        }\n" + "    }\n" + "    public void terminateInstances(X abc) {\n" + "    }\n"
        + "    public X withInstanceIds(Collection<String> arg0) {\n" + "     return null;\n"
        + "    }\n" + "}\n" + "interface FI {\n" + " public void foo(Collection<String> arg0);\n"
        + "}\n" }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=444785, [1.8] Error in JDT Core during reconcile
  public void test444785() {
    this.runConformTest(new String[] { "X.java", "import java.io.Serializable;\n"
        + "import java.util.function.Function;\n" + "public interface X {\n"
        + " @FunctionalInterface\n"
        + " static interface Function1<T1, R> extends Function<T1, R>, Serializable {\n"
        + "   @Override\n" + "   R apply(T1 t1);\n" + " }\n" + " @FunctionalInterface\n"
        + " static interface Function6<T1, T2, T3, T4, T5, T6, R> extends Serializable {\n"
        + "   R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6);\n"
        + "   default Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, R>>>>>> curried() {\n" + "     return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> apply(t1, t2, t3, t4, t5, t6);\n" + "   }\n" + "   default Function1<Tuple6<T1, T2, T3, T4, T5, T6>, R> tupled() {\n" + "     return t -> apply(t._1, t._2, t._3, t._4, t._5, t._6);\n" + "   }\n" + " }\n" + " static final class Tuple6<T1, T2, T3, T4, T5, T6> {\n" + "   public final T1 _1;\n" + "   public final T2 _2;\n" + "   public final T3 _3;\n" + "   public final T4 _4;\n" + "   public final T5 _5;\n" + "   public final T6 _6;\n" + "   public Tuple6(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {\n" + "     this._1 = t1;\n" + "     this._2 = t2;\n" + "     this._3 = t3;\n" + "     this._4 = t4;\n" + "     this._5 = t5;\n" + "     this._6 = t6;\n" + "   }\n" + " }\n" + "}\n" }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=447119, [1.8][compiler] method references lost
  // generic type information (4.4 -> 4.4.1 regression)
  public void test447119() {
    this.runConformTest(
        new String[] { "X.java",
            "import java.lang.reflect.Method;\n"
                + "import java.lang.reflect.Parameter;\n" + "import java.util.Arrays;\n"
                + "import java.util.function.Function;\n" + "import java.util.List;\n"
                + "public class X {\n"
                + "    private static List<String> foo(List<String> x){return x;}\n"
                + "    public static void main(String[] args) {\n"
                + "        Function<List<String>,List<String>> f = i -> { return i; };\n"
                + "        Method[] methods = X.class.getDeclaredMethods();\n"
                + "        for (Method m : methods) {\n"
                + "         if (m.getName().contains(\"lambda\")) {\n"
                + "           System.out.println(\"- \" + m.getGenericReturnType() + \" \" + m.getName() + \"(\" + Arrays.asList(m.getGenericParameterTypes()) + \")\");\n"
                + "         }\n" + "        }\n" + "    }\n" + "}\n" },
        "- interface java.util.List lambda$0([interface java.util.List])");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=447119, [1.8][compiler] method references lost
  // generic type information (4.4 -> 4.4.1 regression)
  public void test447119a() {
    this.runConformTest(
        new String[] { "X.java",
            "import java.lang.reflect.Method;\n"
                + "import java.lang.reflect.Parameter;\n" + "import java.util.Arrays;\n"
                + "import java.util.function.Function;\n" + "import java.util.List;\n"
                + "public class X {\n"
                + "    private static List<String> foo(List<String> x){return x;}\n"
                + "    public static void main(String[] args) {\n"
                + "        Function<List<String>,List<String>> f = X::foo;\n"
                + "        Method[] methods = X.class.getDeclaredMethods();\n"
                + "        for (Method m : methods) {\n"
                + "         if (m.getName().contains(\"lambda\")) {\n"
                + "           System.out.println(\"- \" + m.getGenericReturnType() + \" \" + m.getName() + \"(\" + Arrays.asList(m.getGenericParameterTypes()) + \")\");\n"
                + "         }\n" + "        }\n" + "    }\n" + "}\n" }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=447119, [1.8][compiler] method references lost
  // generic type information (4.4 -> 4.4.1 regression)
  public void test447119b() {
    this.runConformTest(new String[] { "X.java", "import java.lang.reflect.Method;\n"
        + "import java.lang.reflect.Parameter;\n" + "import java.util.Arrays;\n"
        + "import java.util.function.Function;\n" + "import java.util.List;\n"
        + "import java.io.Serializable;" + "public class X {\n"
        + "    private static interface SerializableFunction<A, R> extends Function<A, R>, Serializable { }" + "    private static List<String> foo(List<String> x){return x;}\n" + "    public static void main(String[] args) {\n" + "        SerializableFunction<List<String>, List<String>> f = i -> { return i; };\n" + "        Method[] methods = X.class.getDeclaredMethods();\n" + "        for (Method m : methods) {\n" + "         if (m.getName().contains(\"lambda\")) {\n" + "           System.out.println(\"- \" + m.getGenericReturnType() + \" \" + m.getName() + \"(\" + Arrays.asList(m.getGenericParameterTypes()) + \")\");\n" + "         }\n" + "        }\n" + "    }\n" + "}\n" }, "- interface java.util.List lambda$0([interface java.util.List])");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=447119, [1.8][compiler] method references lost
  // generic type information (4.4 -> 4.4.1 regression)
  public void test447119c() {
    this.runConformTest(new String[] { "X.java", "import java.lang.reflect.Method;\n"
        + "import java.lang.reflect.Parameter;\n" + "import java.util.Arrays;\n"
        + "import java.util.function.Function;\n" + "import java.util.List;\n"
        + "import java.io.Serializable;" + "public class X {\n"
        + "    private static interface SerializableFunction<A, R> extends Function<A, R>, Serializable { }" + "    private static List<String> foo(List<String> x){return x;}\n" + "    public static void main(String[] args) {\n" + "        SerializableFunction<List<String>, List<String>> f = X::foo;\n" + "        Method[] methods = X.class.getDeclaredMethods();\n" + "        for (Method m : methods) {\n" + "         if (m.getName().contains(\"lambda\")) {\n" + "           System.out.println(\"- \" + m.getGenericReturnType() + \" \" + m.getName() + \"(\" + Arrays.asList(m.getGenericParameterTypes()) + \")\");\n" + "         }\n" + "        }\n" + "    }\n" + "}\n" }, "- java.util.List<java.lang.String> lambda$0([java.util.List<java.lang.String>])");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=447119, [1.8][compiler] method references lost
  // generic type information (4.4 -> 4.4.1 regression)
  public void test447119e() {
    this.runConformTest(
        new String[] { "X.java",
            "import java.lang.reflect.Method;\n"
                + "import java.lang.reflect.Parameter;\n" + "import java.util.Arrays;\n"
                + "import java.util.function.Function;\n" + "import java.util.List;\n"
                + "public class X implements java.io.Serializable {\n"
                + "    private static List<String> foo(List<String> x){return x;}\n"
                + "    public static void main(String[] args) {\n"
                + "        Function<List<String>,List<String>> f = X::foo;\n"
                + "        Method[] methods = X.class.getDeclaredMethods();\n"
                + "        for (Method m : methods) {\n"
                + "         if (m.getName().contains(\"lambda\")) {\n"
                + "           System.out.println(\"- \" + m.getGenericReturnType() + \" \" + m.getName() + \"(\" + Arrays.asList(m.getGenericParameterTypes()) + \")\");\n"
                + "         }\n" + "        }\n" + "    }\n" + "}\n" }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=432605, [1.8] Incorrect error
  // "The type ArrayList<T> does not define add(ArrayList<T>, Object) that is applicable here"
  public void test432605() {
    this.runConformTest(new String[] { "X.java", "import java.util.ArrayList;\n"
        + "import java.util.HashMap;\n" + "import java.util.function.Function;\n"
        + "import java.util.function.Supplier;\n" + "import java.util.stream.Collector;\n"
        + "import java.util.stream.Collectors;\n" + "import java.util.stream.Stream;\n"
        + "public class X {\n"
        + "static <T, E extends Exception, K, L, M> M terminalAsMapToList(\n"
        + "    Function<? super T, ? extends K> classifier,\n"
        + "    Function<HashMap<K, L>, M> intoMap,\n"
        + "    Function<ArrayList<T>, L> intoList,\n" + "    Supplier<Stream<T>> supplier,\n"
        + "    Class<E> classOfE) throws E {\n" + "   return terminalAsCollected(\n"
        + "     classOfE,\n" + "     Collectors.collectingAndThen(\n"
        + "       Collectors.groupingBy(\n" + "         classifier,\n"
        + "         HashMap<K, L>::new,\n" + "         Collectors.collectingAndThen(\n"
        + "           // The type ArrayList<T> does not define add(ArrayList<T>, Object) that is applicable here\n" + "           // from ArrayList<T>::add:\n" + "           Collector.of(ArrayList<T>::new, ArrayList<T>::add, (ArrayList<T> left, ArrayList<T> right) -> { \n" + "             left.addAll(right);\n" + "             return left;\n" + "           }),\n" + "           intoList)),\n" + "       intoMap),\n" + "     supplier);\n" + "  }\n" + " static <E extends Exception, T, M> M terminalAsCollected(\n" + "    Class<E> class1,\n" + "    Collector<T, ?, M> collector,\n" + "    Supplier<Stream<T>> supplier) throws E {\n" + "   try(Stream<T> s = supplier.get()) {\n" + "     return s.collect(collector);\n" + "   } catch(RuntimeException e) {\n" + "     throw unwrapCause(class1, e);\n" + "   }\n" + "  }\n" + " static <E extends Exception> E unwrapCause(Class<E> classOfE, RuntimeException e) throws E {\n" + "   Throwable cause = e.getCause();\n" + "   if(classOfE.isInstance(cause) == false) {\n" + "     throw e;\n" + "   }\n" + "   throw classOfE.cast(cause);\n" + "}\n" + "}\n" }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=432605, [1.8] Incorrect error
  // "The type ArrayList<T> does not define add(ArrayList<T>, Object) that is applicable here"
  public void testreduced432605() {
    this.runConformTest(new String[] { "X.java", "import java.util.ArrayList;\n"
        + "import java.util.HashMap;\n" + "import java.util.function.Function;\n"
        + "import java.util.stream.Collector;\n" + "import java.util.stream.Collectors;\n"
        + "public class X {\n" + "    static <T, K, L, M> void foo() {\n"
        + " Collector<T, ?, M> cat = \n" + "            Collectors.collectingAndThen(\n"
        + "   Collectors.groupingBy((Function<? super T, ? extends K>) null, \n"
        + "       HashMap<K, L>::new, \n" + "       (Collector<T, ArrayList<T>, L>) null), \n"
        + "       (Function<HashMap<K, L>, M>) null);\n" + " }\n" + "}\n" }, "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=448802, [1.8][compiler] Poly invocations
  // interleaved by a impertinent lambda may need some more changes,
  public void test448802() throws Exception {
    this.runConformTest(new String[] { "X.java", "import java.util.Optional;\n"
        + "public class X {\n" + " public static void main(String[] args) {\n"
        + "   Optional<String> userName = Optional.of(\"sa\");\n"
        + "   Optional<String> password = Optional.of(\"sa\");\n"
        + "   boolean isValid = userName.flatMap((String u) -> {\n"
        + "     return password.map((String p) -> {\n"
        + "       return u.equals(\"sa\") && p.equals(\"sa\");\n" + "     });\n"
        + "   }).orElse(false);\n" + "   System.out.println(isValid);\n" + " }\n" + "}\n" },
        "true");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=445949, Lambda parameter not shadowing in nested
  // scope producing non-existent compilation error
  public void test445949() {
    this.runConformTest(new String[] { "X.java", "import java.util.function.Consumer;\n"
        + "public class X {\n" + " void methodInFirstLevel(int y) {\n" + "   class Second {\n"
        + "     int t = y;\n" + "     Consumer<Integer> myConsumer1 = (z) -> {\n"
        + "       System.out.println(\"z = \" + z);\n"
        + "       System.out.println(\"y = \" + y);\n"
        + "       System.out.println(\"t = \" + t);\n" + "     };\n"
        + "     Consumer<Integer> myConsumer2 = (y) -> {\n"
        + "       System.out.println(\"y = \" + y);\n"
        + "       System.out.println(\"t = \" + t);\n" + "     };\n" + "     void foo( int y) {\n"
        + "       System.out.println(\"y = \" + y);\n" + "     }\n" + "     class Third {\n"
        + "       Consumer<Integer> myConsumer3 = (y) -> {\n"
        + "         System.out.println(\"y = \" + y);\n" + "       };\n" + "     }\n"
        + "     void bar(int y) {\n" + "       new Third().myConsumer3.accept(y);\n" + "     }\n"
        + "     }\n" + "   new Second().myConsumer1.accept(10);\n"
        + "   new Second().myConsumer2.accept(20);\n" + "   new Second().foo(30);\n"
        + "   new Second().bar(40);\n" + "   \n" + " }\n" + " void foo() {\n"
        + "     Consumer<Integer> myConsumer2 = (y) -> {\n" + "   class Inner {\n"
        + "     Consumer<Integer> myConsumer4 = (y) -> { \n" + "   class InnerMost {\n"
        + "   Consumer<Integer> myConsumer3 = (y /*error without fix*/) -> {};\n" + "   }\n"
        + "     };\n" + "   }\n" + "   new Inner().myConsumer4.accept(10);\n" + " };\n" + " }\n"
        + " public static void main(String[] args) {\n" + "   new X().methodInFirstLevel(5);\n"
        + "   new X().foo();\n" + " }\n" + "}\n" },
        "z = 10\ny = 5\nt = 5\ny = 20\nt = 5\ny = 30\ny = 40");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=456395, can't compile the Java8 code
  public void test456395() {
    this.runConformTest(new String[] { "Test.java", "import java.io.*;\n" + "import java.util.*;\n"
        + "import java.util.stream.*;\n" + "import static java.util.stream.Collectors.*;\n"
        + "public class Test {\n"
        + "   public static void main(String[] args) throws IOException {\n"
        + "      Stream<Locale> locales = Stream.of(Locale.getAvailableLocales());\n"
        + "      locales = Stream.of(Locale.getAvailableLocales());\n"
        + "      Map<String, Set<String>> countryToLanguages = locales.collect(\n"
        + "         groupingBy(Locale::getDisplayCountry, \n"
        + "            mapping(Locale::getDisplayLanguage,\n" + "               toSet())));\n"
        + "   }\n" + "}\n" });
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=459305
  public void test459305() {
    this.runConformTest(new String[] { "X.java", "import java.io.Serializable;\n"
        + "import java.util.function.BiConsumer;\n" + "import java.util.function.Consumer;\n"
        + "public class X {\n" + "   public static void main(String[] args) {\n"
        + "      foo(arg1 -> bar(X::baz));\n" + "   }\n"
        + "   private static <A1> void foo(Consumer<A1> c) { c.accept(null); }\n"
        + "   private static void baz(String s1, String s2) { System.out.println(s1 + \"::\" + s2); }\n" + "   private static void bar(VoidMethodRef2<String, String> mr2) { mr2.accept(\"one\", \"two\"); }\n" + "   private static interface VoidMethodRef2<A1, A2> extends BiConsumer<A1, A2>, Serializable {}\n" + "}\n" }, "one::two");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=467825 Missing code implementation in the
  // compiler
  public void test467825() {
    this.runConformTest(new String[] { "Main.java", "import java.util.function.Function;\n"
        + "public class Main {\n" + "    public Function<String, String> f(int x) {\n"
        + "     class A {\n" + "       void g() {\n" + "             System.out.println(x);\n"
        + "       }\n" + "     }\n" + "        return s -> {\n" + "         A a = new A();\n"
        + "            return s;\n" + "        };\n" + "    }\n" + "}\n" });
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=467825 Missing code implementation in the
  // compiler
  public void test467825a() {
    this.runConformTest(new String[] { "Test.java", "import java.util.function.Function;\n"
        + "interface Foo {void alpha(Bar pBar);}\n" + "class Bar {Object bravo() {return null;}}\n"
        + "class Test {\n" + " Foo foo(Function pFunction) {\n" + "     class Baz {\n"
        + "       public Baz(Object pObj) {\n" + "       }\n"
        + "       class NestedBaz extends Baz {\n" + "         NestedBaz(Object pObj) {\n"
        + "           super(pObj);\n" + "           pFunction.apply(pObj);\n" + "         }\n"
        + "       }\n" + "     }\n" + "     return pBar -> {\n"
        + "         Object o = new Baz(pBar).new NestedBaz(pBar.bravo());\n" + "       };\n"
        + "   }\n" + "   void charlie(Object pRemovals) {}\n" + "   void delta(Foo pListener) {}\n"
        + "}\n" });
  }
  // public static void main(String args[]) {
  // try {
  // LambdaExpressionTest let = new LambdaExpressionTest("Test");
  // Class c = LambdaExpressionTest.class;
  // Method[] m = c.getDeclaredMethods();
  // for (int i = 0; i < m.length; i++) {
  // if (m[i].getName().substring(0, 4).equals("test")) {
  // m[i].invoke(let);
  // }
  // }
  // } catch (Throwable e) {
  // System.err.println(e);
  // }
  // }
}
