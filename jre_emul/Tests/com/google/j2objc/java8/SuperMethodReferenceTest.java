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

interface Outputter {
  public String s();
}

interface Get<T> {
  T g();
}

interface Func<T, R> {
  R f(T t);
}

class X {
  public String f() {
    return "Foo";
  }

  protected String b() {
    return "Bar";
  }

  public String fooGet(Outputter o) {
    return "Foo" + o.s();
  }
}

class XX extends X {
  public String b() {
    return "Baz";
  }

  public Outputter foo() {
    return super::f;
  }

  public Outputter bar() {
    return super::b;
  }

  public Func<Outputter, String> superFooBar() {
    return super::fooGet;
  }

  public void t() {
    super.b();
  }
}

class XXX extends XX {
  public Object fooBaz() {
    Outputter o = super::b;
    Get<Func> f = super::superFooBar;
    return f.g().f(o);
  }
}

/**
 * Command-line tests for creation references.
 *
 * @author Seth Kirby
 */
public class SuperMethodReferenceTest extends TestCase {
  public SuperMethodReferenceTest() { }

  public void testBasicReferences() throws Exception {
    XX xx = new XX();
    Outputter c = xx.foo();
    Outputter c2 = xx.bar();
    assertEquals("Foo", c.s());
    assertEquals("Bar", c2.s());
  }

  public void testNestedReferences() throws Exception {
    XXX xxx = new XXX();
    assertEquals("FooBaz", xxx.fooBaz());
  }
}
