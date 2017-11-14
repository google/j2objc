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

import com.test.Hello;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.Arrays;
import junit.framework.TestCase;

/**
 * Basic tests to verify serialization support.
 *
 * @author Tom Ball
 */
public class SerializationTest extends TestCase {

  private static final String TEST_FILE_NAME = "serialization-test.bin";

  static class Greeting implements Serializable {
    private String greeting;
    private String name;
    private transient int n;

    // A default constructor. This should not be called during deserialization.
    public Greeting() {
      n = 1;
    }

    public Greeting(String greeting, String name, int n) {
      this.greeting = greeting;
      this.name = name;
      this.n = n;
    }

    @Override
    public String toString() {
      return String.format("%s, %s!", greeting, name);
    }
  }

  static class SerializableClass implements Serializable {}
  static class NotSerializableClass {}

  @Override
  protected void tearDown() throws Exception {
    new File(TEST_FILE_NAME).delete();
    super.tearDown();
  }

  public void testSerialization() throws IOException, ClassNotFoundException {
    Greeting greeting = new Greeting("hello", "world", 42);
    assertEquals("hello, world!", greeting.toString());
    assertEquals(42, greeting.n);

    // Save the greeting to a file.
    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(TEST_FILE_NAME));
    out.writeObject(greeting);
    out.close();
    File binFile = new File(TEST_FILE_NAME);
    assertTrue(binFile.exists());

    // Read back the greeting.
    ObjectInputStream in = new ObjectInputStream(new FileInputStream(TEST_FILE_NAME));
    Greeting greeting2 = (Greeting) in.readObject();
    in.close();
    assertEquals("hello, world!", greeting.toString());
    assertEquals(0, greeting2.n);  // 0 because n is transient.
  }

  public void testArraySerialization() throws Exception {
    String[] names = new String[] { "tom", "dick", "harry" };
    assertTrue("array is not Serializable", names instanceof Serializable);
    assertTrue("array is not instance of Serializable", Serializable.class.isInstance(names));
    assertTrue("array cannot be assigned to Serializable",
        Serializable.class.isAssignableFrom(names.getClass()));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(out);
    oos.writeObject(names);
    oos.close();
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(in);
    String[] result = (String[]) ois.readObject();
    ois.close();
    assertTrue("arrays not equal", Arrays.equals(names, result));
  }

  // Regression test for https://github.com/google/j2objc/issues/496.
  @SuppressWarnings("resource")
  public void testSerializingObjectClass() throws Exception {
    File tmpFile = File.createTempFile("filea", "object");
    try {
      FileOutputStream fileOut = new FileOutputStream(tmpFile);
      new ObjectOutputStream(fileOut).writeObject(Object.class);
      FileInputStream fileIn = new FileInputStream(tmpFile);
      assertEquals(Object.class, new ObjectInputStream(fileIn).readObject());
    } finally {
      tmpFile.delete();
    }
  }

  // Regression test for https://github.com/google/j2objc/issues/496.
  @SuppressWarnings("resource")
  public void testSerializingSerializableClass() throws Exception {
    File tmpFile = File.createTempFile("fileb", "object");
    try {
      FileOutputStream fileOut = new FileOutputStream(tmpFile);
      new ObjectOutputStream(fileOut).writeObject(SerializableClass.class);
      FileInputStream fileIn = new FileInputStream(tmpFile);
      assertEquals(SerializableClass.class, new ObjectInputStream(fileIn).readObject());
    } finally {
      tmpFile.delete();
    }
  }

  // Regression test for https://github.com/google/j2objc/issues/496.
  @SuppressWarnings("resource")
  public void testSerializingNotSerializableClass() throws Exception {
    File tmpFile = File.createTempFile("filec", "object");
    try {
      FileOutputStream fileOut = new FileOutputStream(tmpFile);
      new ObjectOutputStream(fileOut).writeObject(NotSerializableClass.class);
      FileInputStream fileIn = new FileInputStream(tmpFile);
      assertEquals(NotSerializableClass.class, new ObjectInputStream(fileIn).readObject());
    } finally {
      tmpFile.delete();
    }
  }

  // Verify that the serialVersionUID values for arrays are the same as the JVM returns.
  public void testArraySerialVersionUIDs() throws Exception {
    ObjectStreamClass osc = ObjectStreamClass.lookupAny(new int[0].getClass());
    assertEquals(5600894804908749477L, osc.getSerialVersionUID());
    osc = ObjectStreamClass.lookupAny(new int[0][0].getClass());
    assertEquals(1727100010502261052L, osc.getSerialVersionUID());
    osc = ObjectStreamClass.lookupAny(new double[0].getClass());
    assertEquals(4514449696888150558L, osc.getSerialVersionUID());
    osc = ObjectStreamClass.lookupAny(new String[0].getClass());
    assertEquals(-5921575005990323385L, osc.getSerialVersionUID());
    osc = ObjectStreamClass.lookupAny(new Thread[0].getClass());
    assertEquals(-6192713741133905679L, osc.getSerialVersionUID());
  }

  // Verify classes with package prefixes can be serialized.
  public void testSerializationPkgPrefixes() throws IOException, ClassNotFoundException {
    Hello greeting = new Hello("hello", "world", 42);
    assertEquals("hello, world!", greeting.toString());
    assertEquals(42, greeting.getN());

    // Save the greeting to a file.
    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(TEST_FILE_NAME));
    out.writeObject(greeting);
    out.close();
    File binFile = new File(TEST_FILE_NAME);
    assertTrue(binFile.exists());

    // Read back the greeting.
    ObjectInputStream in = new ObjectInputStream(new FileInputStream(TEST_FILE_NAME));
    Hello greeting2 = (Hello) in.readObject();
    in.close();
    assertEquals("hello, world!", greeting.toString());
    assertEquals(0, greeting2.getN());  // 0 because n is transient.
    
    // Verify package prefix was used.
    assertEquals("CTHello", objectiveCClassName(greeting2));
  }
  
  private native String objectiveCClassName(Object obj) /*-[
    return NSStringFromClass([obj class]); 
  ]-*/;
}
