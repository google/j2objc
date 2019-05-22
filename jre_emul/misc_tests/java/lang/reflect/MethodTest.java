/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang.reflect;

import junit.framework.TestCase;

import java.util.AbstractList;

/**
 * Miscellaneous tests for java.lang.reflect.Method.
 *
 * @author Tom Ball
 */
public class MethodTest extends TestCase {

  abstract static class MyList extends AbstractList<String> {
    public boolean add(String s) {
      return true;
    }
  }

  private static boolean isSynthetic(Method m) {
    return (m.getModifiers() & 0x1000) > 0;
  }

  public void testGenericMethodWithConcreteTypeArgument() throws Exception {
    for (Method m : MyList.class.getDeclaredMethods()) {
      if (m.getName().equals("add") && !isSynthetic(m)) {
        assertEquals("public boolean java.lang.reflect.MethodTest$MyList.add(java.lang.String)",
            m.toString());
        return;
      }
    }
    fail("add method not found");
  }
}
