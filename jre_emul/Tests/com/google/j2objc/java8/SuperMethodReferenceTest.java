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

/**
 * Command-line tests for super method references.
 *
 * @author Seth Kirby
 */
public class SuperMethodReferenceTest extends TestCase {
  public SuperMethodReferenceTest() {
  }

  class X {
    public String f() {
      return "Foo";
    }

    protected String b() {
      return "Bar";
    }

    public String fooGet(Lambdas.Zero o) {
      return "Foo" + o.apply();
    }
  }

  class XX extends X {
    public String b() {
      return "Baz";
    }

    public Lambdas.Zero foo() {
      return super::f;
    }

    public Lambdas.Zero bar() {
      return super::b;
    }

    public Lambdas.One<Lambdas.Zero, String> superFooBar() {
      return super::fooGet;
    }

    public void t() {
      super.b();
    }

    String m(Object a1, Object... rest) {
      return "" + a1 + stringify(rest);
    }

    String stringify(Object... ls) {
      String out = " [ ";
      for (Object x : ls) {
        out += x;
        out += ' ';
      }
      return out + ']';
    }
  }

  class XXX extends XX {
    public Object fooBaz() {
      Lambdas.Zero o = super::b;
      Lambdas.Zero<Lambdas.One> f = super::superFooBar;
      return f.apply().apply(o);
    }

    public String superString() {
      Lambdas.Three f = super::m;
      Lambdas.Four f2 = super::m;
      return f.apply("10", "15", "20") + " : " + f2.apply("40", "41", "42", "43");
    }
  }

  public void testBasicReferences() throws Exception {
    XX xx = new XX();
    Lambdas.Zero c = xx.foo();
    Lambdas.Zero c2 = xx.bar();
    assertEquals("Foo", c.apply());
    assertEquals("Bar", c2.apply());
  }

  public void testNestedReferences() throws Exception {
    XXX xxx = new XXX();
    assertEquals("FooBaz", Lambdas.get(xxx::fooBaz).apply());
  }

  public void testVarargs() throws Exception {
    XXX xxx = new XXX();
    assertEquals("10 [ 15 20 ] : 40 [ 41 42 43 ]", Lambdas.get(xxx::superString).apply());
  }
}
