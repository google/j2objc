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

package com.google.j2objc.java8;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

interface UnaryOperator<T> {
  public T apply(T t);
}

interface UOToUO<T> {
  UnaryOperator<T> apply(UOToUO<T> x);
}

/**
 * Command-line tests for Lambda support.
 *
 * @author Seth Kirby
 */
public class LambdaTest extends TestCase {

  public LambdaTest() {}

  public void testBasicReflection() {
    assertEquals(false, ((Lambdas.One) (x) -> 1).equals((Lambdas.One) (x) -> 1));
    Lambdas.One f = x -> 1;
    Lambdas.One g = f;
    assertEquals(f, g);
    assertEquals(f.toString(), "" + g);
    String lambdaClassName = f.getClass().getName();
    assertEquals(true, lambdaClassName.contains("LambdaTest"));
    assertEquals(true, lambdaClassName.contains("ambda$"));
  }

  String outerCall() {
    return "Foo";
  }

  private String privateOuterCall() {
    return "Bar";
  }

  static class Foo {
    String outer() {
      return "Foo";
    }

    class Bar {
      String findMe() {
        return "Bar";
      }

      Lambdas.Zero fooS = () -> outer();
      Lambdas.Zero barS = () -> findMe();
    }

    Bar getBar() {
      return new Bar();
    }
  }

  public void testOuterMethodCalls() {
    assertEquals("Foo", Lambdas.get(() -> outerCall()).apply());
    assertEquals("Bar", Lambdas.get(() -> privateOuterCall()).apply());
    Foo foo = new Foo();
    assertEquals("Foo", Lambdas.get(() -> foo.outer()).apply());
    assertEquals("Bar", Lambdas.get(() -> foo.getBar().barS.apply()).apply());
  }

  Integer outerX = 0;
  int outerY = 0;

  public void testOuterVarCapture() {
    Lambdas.Zero s = () -> outerX;
    Lambdas.Zero s2 = () -> outerY;
    assertEquals((Integer) 0, s.apply());
    assertEquals((Integer) 0, s2.apply());
    outerX += 42;
    outerY += 42;
    assertEquals((Integer) 42, s.apply());
    assertEquals((Integer) 42, s2.apply());
    outerX++;
    outerY++;
    assertEquals((Integer) 43, s.apply());
    assertEquals((Integer) 43, s2.apply());
    outerX++;
    outerY++;
  }

  public void testFunctionArray() {
    List<Lambdas.One<Object, Integer>> fs = new ArrayList<>();
    int[] nums = { 3, 2, 1, 0 };
    for (int i : nums) {
      fs.add((x) -> i);
    }
    assertEquals((Integer) 0, fs.get(3).apply("5"));
    assertEquals((Integer) 1, fs.get(2).apply(new Object()));
    assertEquals((Integer) 2, fs.get(1).apply(new Object()));
    assertEquals((Integer) 3, fs.get(0).apply("4"));
  }

  public <T> UnaryOperator<T> yComb(UnaryOperator<UnaryOperator<T>> r) {
    return ((UOToUO<T>) f -> f.apply(f)).apply(f -> r.apply(x -> f.apply(f).apply(x)));
  }

  public void testYCombinator() {
    UnaryOperator<UnaryOperator<String>> a = (UnaryOperator<String> f) -> {
      return (x) -> {
        if (x.length() == 5) {
          return x;
        } else {
          return f.apply((char) (x.charAt(0) + 1) + x);
        }
      };
    };
    assertEquals("edcba", yComb(a).apply("a"));
    UnaryOperator<UnaryOperator<Integer>> fibonacci = (UnaryOperator<Integer> f) -> {
      return (Integer x) -> {
        if (x < 1) {
          return 0;
        } else if (x == 1) {
          return x;
        } else {
          return f.apply(x - 1) + f.apply(x - 2);
        }
      };
    };
    assertEquals((Integer) 55, yComb(fibonacci).apply(10));
  }

  Lambdas.One<Integer, Integer> outerF = (x) -> x;

  public void testOuterFunctions() {
    assertEquals((Integer) 42, outerF.apply(42));
  }

  static Lambdas.One<Integer, Integer> staticF = (x) -> x;

  public void testStaticFunctions() {
    assertEquals((Integer) 42, staticF.apply(42));
  }

  public void testNestedLambdas() throws Exception {
    Lambdas.One<String, Lambdas.One<String, String>> f = (x) -> (y) -> x;
    assertEquals(f.apply("Foo").apply("Bar"), "Foo");
    Lambdas.One<Integer, Lambdas.One<Integer, Integer>> f2 = (x) -> (y) -> x;
    assertEquals(f2.apply(42).apply(43), Integer.valueOf(42));
    Lambdas.One<Integer, Lambdas.One<Integer, Integer>> f3 = (y) -> (x) -> x;
    assertEquals(f3.apply(43).apply(42), Integer.valueOf(42));
    Lambdas.One<String, Lambdas.One<String, Integer>> f4 = (x) -> (y) -> Integer.parseInt(
        x);
    assertEquals(f4.apply("42").apply("43"), Integer.valueOf(42));
    Lambdas.One<String, Lambdas.One<Integer, Integer>> f5 = (x) -> (y) -> Integer.parseInt(
        x);
    assertEquals(f5.apply("42").apply(43), Integer.valueOf(42));
    Callable<Callable> c2 = () -> () -> 42;
    assertEquals(c2.call().call(), 42);
    Lambdas.Zero<Lambdas.Zero> s2 = () -> () -> 42;
    assertEquals(s2.apply().apply(), 42);
  }

  // Tests outer reference resolution, and that inner fields are being correctly resolved for
  // lambdas with implicit blocks.
  public void testAdditionInLambda() {
    Lambdas.One<Integer, Integer> f = new Lambdas.One<Integer, Integer>() {
      @Override
      public Integer apply(Integer x) {
        return x + 20;
      }
    };
    assertEquals((Integer) 42, f.apply(22));
    Lambdas.One<Integer, Integer> g = x -> {
      return x + 20;
    };
    assertEquals((Integer) 42, g.apply(22));
    Lambdas.One<Integer, Integer> h = x -> x + 20;
    assertEquals((Integer) 42, h.apply(22));
  }

  public void testBasicAnonymousClass() {
    Lambdas.One<Integer, Integer> h = new Lambdas.One<Integer, Integer>() {
      @Override
      public Integer apply(Integer x) {
        return x;
      }
    };
    assertEquals((Integer) 42, h.apply(42));
  }

  @SuppressWarnings("unchecked")
  public void testBasicFunction() {
    Lambdas.One f = x -> x;
    assertEquals(42, f.apply(42));
    Lambdas.One<Integer, Integer> f2 = x -> x;
    assertEquals((Integer) 42, f2.apply(42));
    Lambdas.One f3 = x -> {
      int y = 42;
      return y;
    };
    assertEquals(42, f3.apply(null));
    Lambdas.Four<String, Double, Integer, Boolean, String> appendFour = (a, b, c, d) -> a + b + c
        + d;
    assertEquals("Foo4.214true", appendFour.apply("Foo", 4.2, 14, true));
  }

  public void testBasicSupplier() {
    Lambdas.Zero s = () -> 42;
    assertEquals(42, s.apply());
  }

  public void testBasicConsumer() {
    Object[] ls = new Object[1];
    Lambdas.VoidOne<Integer> c = (x) -> ls[0] = x;
    c.apply(42);
    assertEquals(42, ls[0]);
  }

  public void testBasicCallable() throws Exception {
    Callable c = () -> 42;
    assertEquals(42, c.call());
  }

  // Adapted from translator/src/test/java/com/google/devtools/j2objc/ast/LambdaExpressionTest.java
  interface NumberFunction<T extends Number, R> {
    R apply(T t);
  }

  static class CaptureTest1 {
    int y;
    NumberFunction<Integer, Integer> f = x -> y + x.intValue();
  }

  static class CaptureTest2 {
    int y;
    class InnerCaptureTest {
      int y;
      NumberFunction<Integer, Integer> fOuter = x -> CaptureTest2.this.y + x.intValue();
      NumberFunction<Integer, Integer> fInner = x -> y + x.intValue();
    }
  }

  public void testImplicitCapture() {
    CaptureTest1 t1 = new CaptureTest1();
    assertEquals((Integer) 1, t1.f.apply(1));
    t1.y = 1;
    assertEquals((Integer) 3, t1.f.apply(2));

    CaptureTest2 t2 = new CaptureTest2();
    CaptureTest2.InnerCaptureTest t2i = t2.new InnerCaptureTest();
    t2.y = 10;
    t2i.y = 20;
    assertEquals((Integer) 9, t2i.fOuter.apply(-1));
    assertEquals((Integer) 19, t2i.fInner.apply(-1));
  }

  interface IntSupplier {
    int get();

    static int f(IntSupplier s) {
      return s.get();
    }
  }

  // x + y * 2
  private int getFromSimpleLambda(int x, int y) {
    return IntSupplier.f(() -> x + y) + y;
  }

  // x + y * 3
  private int getFromNestedLambdas(int x, int y) {
    return IntSupplier.f(() -> IntSupplier.f(() -> x + y) + y) + y;
  }

  // x + y * 3
  private int getFromAnonymousClassesAndLambda(int x, int y) {
    return IntSupplier.f(new IntSupplier() {
      @Override
      public int get() {
        return IntSupplier.f(() -> x + y) + y;
      }
    }) + y;
  }

  // x + y * 3
  private int getFromLambdaAndAnonymousClass(int x, int y) {
    return IntSupplier.f(() -> IntSupplier.f(new IntSupplier() {
      @Override
      public int get() {
        return x + y;
      }
    }) + y) + y;
  }

  // x + y * 5
  private int getFromNestedAnonymousClassesAndLambdas(int x, int y) {
    return IntSupplier.f(new IntSupplier() {
      @Override
      public int get() {
        return IntSupplier.f(() -> IntSupplier.f(new IntSupplier() {
          @Override
          public int get() {
            return IntSupplier.f(() -> x + y) + y;
          }
        }) + y) + y;
      }
    }) + y;
  }

  // x + y * 5
  private int getFromNestedLambdasAndAnonymousClasses(int x, int y) {
    return IntSupplier.f(() -> IntSupplier.f(new IntSupplier() {
      @Override
      public int get() {
        return IntSupplier.f(() -> IntSupplier.f(new IntSupplier() {
          @Override
          public int get() {
            return x + y;
          }
        }) + y) + y;
      }
    }) + y) + y;
  }

  // Putting everything together
  static class ComplexLambda {
    static final int P = 1;
    final int q = 2;
    int r = Integer.MIN_VALUE;

    public class Inner {
      static final int S = 4;
      final int t = 5;
      int u = Integer.MIN_VALUE;

      // Should be x + y * 6 + P + Q + R + S + T + U;
      public int get(int x, int y) {
        return IntSupplier.f(() -> IntSupplier.f(new IntSupplier() {
          @Override
          public int get() {
            return IntSupplier.f(() -> IntSupplier.f(new IntSupplier() {
              @Override
              public int get() {
                return IntSupplier.f(() -> x + y + P) + y + q;
              }
            }) + y + r) + y + S;
          }
        }) + y + t) + y + u;
      }
    }
  }

  public void testNestingLambdasAndAnonymousClasses() {
    // Each method is called twice to make sure that all lambdas are capturing ones.
    assertEquals(102, getFromSimpleLambda(100, 1));
    assertEquals(104, getFromSimpleLambda(100, 2));
    assertEquals(109, getFromNestedLambdas(100, 3));
    assertEquals(112, getFromNestedLambdas(100, 4));
    assertEquals(115, getFromAnonymousClassesAndLambda(100, 5));
    assertEquals(118, getFromAnonymousClassesAndLambda(100, 6));
    assertEquals(121, getFromLambdaAndAnonymousClass(100, 7));
    assertEquals(124, getFromLambdaAndAnonymousClass(100, 8));
    assertEquals(145, getFromNestedAnonymousClassesAndLambdas(100, 9));
    assertEquals(150, getFromNestedLambdasAndAnonymousClasses(100, 10));

    int z = 10;
    class Local {
      int w = 1;

      // x + y * 3 + w + z
      private int get(int x, int y) {
        return IntSupplier.f(new IntSupplier() {
          @Override
          public int get() {
            return IntSupplier.f(() -> x + y + z + w) + y;
          }
        }) + y;
      }
    }

    Local l = new Local();
    assertEquals(171, l.get(100, 20));
    assertEquals(186, l.get(100, 25));
    l.w = 500;
    assertEquals(670, l.get(100, 20));
    assertEquals(685, l.get(100, 25));

    ComplexLambda cl = new ComplexLambda();
    cl.r = 3;
    ComplexLambda.Inner inner = cl.new Inner();
    inner.u = 6;

    // Should be 1000 + 20 * 6 + sum(1...6)
    assertEquals(1141, inner.get(1000, 20));

    cl.r += 10000;
    inner.u += 10000;
    // Should be 1000 + 20 * 6 + sum(1...6) + 10000 + 10000;
    assertEquals(21141, inner.get(1000, 20));
  }

  interface IntSupplierSupplier {
    IntSupplier get();
  }

  public void testNestedLambdaCapturesOuterScope() {
    IntSupplier s = new IntSupplier() {
      int i = 1234;
      public int get() {
        IntSupplierSupplier ss = () -> () -> i;
        return ss.get().get();
      }
    };
    assertEquals(1234, s.get());
  }

  interface ImplicitCaptures {
    interface F {
      int f();

      default F g() {
        return () -> f();
      }
    }

    interface G extends F {
      default F g() {
        return () -> F.super.g().f();
      }
    }

    static class P {
      int x = 0;

      void setX(int x) {
        this.x = x;
      }

      int getX() {
        return x;
      }
    }

    static class Q extends P {
      public F getSuperGetX() {
        return () -> super.getX();
      }

      public F getFieldX() {
        return () -> x;
      }

      public F getThisFieldX() {
        return () -> this.x;
      }

      public F getThisGetX() {
        return () -> getX();
      }
    }
  }

  public void testCapturingLambdasFromQualifiedSuperInvocations() throws Exception {
    ImplicitCaptures.G g1 = new ImplicitCaptures.G() {

      @Override
      public int f() {
        return 10;
      }
    };

    ImplicitCaptures.G g2 = new ImplicitCaptures.G() {
      @Override
      public int f() {
        return 20;
      }
    };

    assertEquals(10, g1.g().f());

    // () -> F.super.g().f() must be a non-capturing lambda to return this value correctly.
    assertEquals(20, g2.g().f());
  }

  public void testImplicitLambdaCaptures() throws Exception {
    ImplicitCaptures.Q q1 = new ImplicitCaptures.Q() {
      @Override
      int getX() {
        return 20;
      }
    };

    ImplicitCaptures.Q q2 = new ImplicitCaptures.Q() {
      @Override
      int getX() {
        return 40;
      }
    };

    q1.setX(10);
    assertEquals(10, q1.getSuperGetX().f());
    assertEquals(10, q1.getFieldX().f());
    assertEquals(10, q1.getThisFieldX().f());
    assertEquals(20, q1.getThisGetX().f()); // From q1's overridden getX()

    q2.setX(30);
    assertEquals(30, q2.getSuperGetX().f());
    assertEquals(30, q2.getFieldX().f());
    assertEquals(30, q2.getThisFieldX().f());
    assertEquals(40, q2.getThisGetX().f()); // From q2's overridden getX()
  }
}
