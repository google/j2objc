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

import junit.framework.TestCase;

import java.lang.reflect.*;

/**
 * Tests that <clinit> execution.
 *
 * @author Daehoon Zee
 */
public class ClassInitializeTest extends TestCase {

  static class Foo1 {
    static final Foo1 instance = Singleton.instance;

    static class Singleton {
      static final Foo1 instance = new Foo1();
    }
  }

  static class Foo2 {
    static boolean innerClassAutoLoaded = InnerClass.loaded;

    static class Check {
      static boolean innerClassInitialized;
    }

    static class InnerClass {
      static boolean loaded;
      static {
        Check.innerClassInitialized = loaded = true;
      }
    }
  }

  static class Foo3 {
    static boolean innerClassLoaded = false;
    static class InnerClass {
      static String NOT_FINAL_CONSTANT_STRING = ".";
      static final String FINAL_CONSTANT_STRING = ".";
      static {
        innerClassLoaded = true;
      }
    }
  }

  public void testInitializeSequence() throws Exception {
    Class c = Foo1.Singleton.class;
    assertTrue (Foo1.instance != null);
  }

  public void testGetMetaclassInformation() throws Exception {
    // Foo2.<clinit> should not be called
    Foo2.class.getDeclaredClasses();
    assertFalse(Foo2.Check.innerClassInitialized);

    // Foo2.<clinit> should not be called
    Field[] fields = Foo2.class.getDeclaredFields();

    for (Field f : fields) {
      if (Modifier.isStatic(f.getModifiers())) {
        assertFalse(Foo2.Check.innerClassInitialized);
        Object s = f.get(null);
        assertTrue(Foo2.Check.innerClassInitialized);
        break;
      }
    }
  }

  public void testStringConstantAccess() throws Exception {
    System.out.print(Foo3.InnerClass.FINAL_CONSTANT_STRING);
    assertFalse(Foo3.innerClassLoaded);
    System.out.print(Foo3.InnerClass.NOT_FINAL_CONSTANT_STRING);
    assertTrue(Foo3.innerClassLoaded);
  }
}
