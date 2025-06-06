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

package org.apache.harmony.tests.java.text;

import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.Format;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import junit.framework.TestCase;

public class Support_Format extends TestCase {

  protected String text;

  public Support_Format(String p1) {
    super(p1);
  }

  protected void t_FormatWithField(int count, Format format, Object object,
                                   String text, Format.Field field, int begin, int end) {
    StringBuffer buffer = new StringBuffer();
    FieldPosition pos = new FieldPosition(field);
    format.format(object, buffer, pos);

    // System.out.println(buffer);
    // System.out.println(pos);

    if (text == null) {
      assertEquals("Test " + count + ": incorrect formatted text", this.text, buffer.toString());
    } else {
      assertEquals(text, buffer.toString());
    }

    if (begin != pos.getBeginIndex() || end != pos.getEndIndex()) {
      assertEquals(field + " " + begin + ".." + end,
                   pos.getFieldAttribute() + " " + pos.getBeginIndex() + ".." + pos.getEndIndex());
    }
  }

  protected void t_Format(int count, Object object, Format format, Vector<FieldContainer> expectedResults) {
    Vector<FieldContainer> results = findFields(format.formatToCharacterIterator(object));
    assertEquals("size mismatch\n" +
                 format.format(object) + "\n" +
                 "expectedResults=" + expectedResults + "\n" +
                 "        results=" + results, expectedResults.size(), results.size());
    for (int i = 0; i < results.size(); ++i) {
      if (!results.contains(expectedResults.get(i))) {
        fail("didn't find expected result " + expectedResults.get(i) + "\n" +
            "expectedResults=" + expectedResults + "\n" +
            "        results=" + results);
      }
    }
  }

  /**
   * finds attributes with regards to char index in this
   * AttributedCharacterIterator, and puts them in a vector
   *
   * @param iterator
   * @return a vector, each entry in this vector are of type FieldContainer,
   *       which stores start and end indexes and an attribute this range has
   */
  protected static Vector<FieldContainer> findFields(AttributedCharacterIterator iterator) {
    Vector<FieldContainer> result = new Vector<FieldContainer>();
    while (iterator.getIndex() != iterator.getEndIndex()) {
      int start = iterator.getRunStart();
      int end = iterator.getRunLimit();

      Iterator<Attribute> it = iterator.getAttributes().keySet().iterator();
      while (it.hasNext()) {
        AttributedCharacterIterator.Attribute attribute = it.next();
        Object value = iterator.getAttribute(attribute);
        result.add(new FieldContainer(start, end, attribute, value));
        // System.out.println(start + " " + end + ": " + attribute + ",
        // " + value );
        // System.out.println("v.add(new FieldContainer(" + start +"," +
        // end +"," + attribute+ "," + value+ "));");
      }
      iterator.setIndex(end);
    }
    return result;
  }

  static class FieldContainer {
    final int start;
    final int end;
    final Attribute attribute;
    final Object value;

    // called from Support_DecimalFormat and Support_SimpleDateFormat tests
    public FieldContainer(int start, int end, Attribute attribute) {
      this(start, end, attribute, attribute);
    }

    // called from Support_MessageFormat tests
    public FieldContainer(int start, int end, Attribute attribute, int value) {
      this(start, end, attribute, new Integer(value));
    }

    // called from Support_MessageFormat tests
    public FieldContainer(int start, int end, Attribute attribute, Object value) {
      this.start = start;
      this.end = end;
      this.attribute = attribute;
      this.value = value;
    }

    @Override public boolean equals(Object obj) {
      if (!(obj instanceof FieldContainer)) {
        return false;
      }

      FieldContainer fc = (FieldContainer) obj;
      return (start == fc.start && end == fc.end && attribute == fc.attribute && value.equals(fc.value));
    }

    @Override public String toString() {
      return "FC[" + start + ".." + end + ",attribute=" + attribute + ",value=" + value + "]";
    }
  }
}
