/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package crossvmtest.java.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;

public class RecordComponentTest {

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
  public @interface CustomAnnotation {
    String value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.RECORD_COMPONENT})
  public @interface CustomAnnotation2 {

    CustomAnnotation[] customAnnotations();
  }

  record RecordInteger(
      @CustomAnnotation2(customAnnotations = {@CustomAnnotation("a")}) @CustomAnnotation("b")
          int x) {}

  record RecordString(String s) {}

  record GenericRecord<A, B extends AbstractMap<String, BigInteger>>(A a, B b, List<String> c) {}

  @Test
  public void testBasic() {
    assertTrue(RecordInteger.class.isRecord());
    RecordComponent[] components = RecordInteger.class.getRecordComponents();
    assertEquals(Arrays.deepToString(components), 1, components.length);
  }

  @Test
  public void testGetAccessor() throws Exception {
    RecordComponent[] components = RecordInteger.class.getRecordComponents();
    Method getter = components[0].getAccessor();

    RecordInteger a = new RecordInteger(9);
    assertEquals(9, getter.invoke(a));
  }

  @Test
  public void testGetAnnotation() throws Exception {
    RecordComponent[] components = RecordInteger.class.getRecordComponents();
    assertEquals(
        "b",
        RecordInteger.class.getDeclaredField("x").getAnnotation(CustomAnnotation.class).value());
    assertEquals("b", components[0].getAnnotation(CustomAnnotation.class).value());
  }

  @Test
  public void testGetRecordComponentAnnotation() throws Exception {
    RecordComponent[] components = RecordInteger.class.getRecordComponents();

    assertTrue(RecordInteger.class.isRecord());
    assertEquals(1, components.length);
    assertNull(RecordInteger.class.getDeclaredField("x").getAnnotation(CustomAnnotation2.class));
    CustomAnnotation2 customAnnotation2 = components[0].getAnnotation(CustomAnnotation2.class);
    assertNotNull(customAnnotation2);
    assertEquals(1, customAnnotation2.customAnnotations().length);
    assertEquals("a", customAnnotation2.customAnnotations()[0].value());
  }

  @Test
  public void testGetAnnotations() {
    RecordComponent[] components = RecordInteger.class.getRecordComponents();
    assertEquals(2, components[0].getAnnotations().length);
  }

  @Test
  public void testGetDeclaredAnnotations() {
    RecordComponent[] components = RecordInteger.class.getRecordComponents();
    assertEquals(2, components[0].getDeclaredAnnotations().length);
  }

  @Test
  public void testGetDeclaringRecord() {
    RecordComponent[] components = RecordInteger.class.getRecordComponents();
    assertEquals(RecordInteger.class, components[0].getDeclaringRecord());
  }

  @Test
  public void testGetName() {
    RecordComponent[] components = RecordInteger.class.getRecordComponents();
    assertEquals("x", components[0].getName());
  }

  @Test
  public void testGetType() {
    RecordComponent[] components = RecordInteger.class.getRecordComponents();
    assertEquals(int.class, components[0].getType());
  }

  @Test
  public void testToString() {
    RecordComponent[] components = RecordInteger.class.getRecordComponents();
    assertEquals("int x", components[0].toString());
  }

  @Test
  public void testGetGenericType() {
    RecordComponent[] components = RecordInteger.class.getRecordComponents();
    assertEquals(int.class, components[0].getGenericType());

    components = RecordString.class.getRecordComponents();
    assertEquals(String.class, components[0].getGenericType());

    components = GenericRecord.class.getRecordComponents();
    assertEquals(3, components.length);
    assertEquals("A", components[0].getGenericType().getTypeName());
    assertEquals("B", components[1].getGenericType().getTypeName());
    assertEquals("java.util.List<java.lang.String>", components[2].getGenericType().getTypeName());
  }

  /* J2ObjC does not support generic signatures.
  @Test
  public void testGetGenericSingature() {
      RecordComponent[] components = RecordInteger.class.getRecordComponents();
      assertNull(components[0].getGenericSignature());

      components = RecordString.class.getRecordComponents();
      assertNull(components[0].getGenericSignature());

      components = GenericRecord.class.getRecordComponents();
      assertEquals(3, components.length);
      assertEquals("TA;", components[0].getGenericSignature());
      assertEquals("TB;", components[1].getGenericSignature());
      assertEquals("Ljava/util/List<Ljava/lang/String;>;", components[2].getGenericSignature());
  }
  */
}
