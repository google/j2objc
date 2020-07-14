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
import java.text.AttributedCharacterIterator.Attribute;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import java.text.Annotation;

public class AttributedCharacterIteratorAttributeTest extends junit.framework.TestCase {
	
	/**
     * @tests java.text.AttributedCharacterIterator$Attribute()
     */
	public void test_constructor() {
		MyAttribute attribute = new MyAttribute("attribute");

		assertEquals("Attribute has wrong name", "attribute", attribute.getExposedName());
		
		attribute = new MyAttribute(null);
		assertEquals("Attribute has wrong name", null, attribute.getExposedName());
	}
	
	/**
	 * @tests java.text.AttributedCharacterIterator.Attribute#equals(Object)
	 */
	public void test_equals() {
		
		assertTrue(Attribute.LANGUAGE.equals(Attribute.LANGUAGE));
		
		assertFalse(Attribute.LANGUAGE.equals(Attribute.READING));
		
		MyAttribute attribute = new MyAttribute("test");
		
		assertTrue(attribute.equals(attribute));
		
		/* this implementation of equals should only return true 
		 * if the same objects */
		assertFalse(attribute.equals(new MyAttribute("test")));
		
		attribute = new MyAttribute(null);
		assertFalse(attribute.equals(new MyAttribute(null)));
	}
	
	 /**
     * @tests java.text.AttributedCharacterIterator$Attribute#readResolve()
     */
    public void test_readResolve() {
        // test for method java.lang.Object readResolve()

        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bytes);

            AttributedCharacterIterator.Attribute dattribute, dattribute2;
            MyAttribute attribute;

            // a regular instance of DateFormat.Field
            dattribute = AttributedCharacterIterator.Attribute.LANGUAGE;

            // a subclass instance with null name
            attribute = new MyAttribute(null);

            out.writeObject(dattribute);
            try {
                out.writeObject(attribute);
            } catch (NotSerializableException e) {
            }
            
            in = new ObjectInputStream(new ByteArrayInputStream(bytes
                    .toByteArray()));

            try {
                dattribute2 = (AttributedCharacterIterator.Attribute) in.readObject();
                assertSame("resolved incorrectly", dattribute, dattribute2);
            } catch (IllegalArgumentException e) {
                fail("Unexpected IllegalArgumentException: " + e);
            }

        } catch (IOException e) {
            fail("unexpected IOException" + e);
        } catch (ClassNotFoundException e) {
            fail("unexpected ClassNotFoundException" + e);
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
            }
        }
    }
	
    /**
     * @tests java.text.AttributedCharacterIterator$Attribute#LANGUAGE
     * java.text.AttributedCharacterIterator$Attribute#READING
     * java.text.AttributedCharacterIterator$Attribute#INPUT_METHOD_SEGMENT
     */
	public void test_fields() {
		
        // Just check that the fields are accessible as all
        // methods are protected
		Attribute language = Attribute.LANGUAGE;
		Attribute reading = Attribute.READING;
		Attribute inputMethodSegment = Attribute.INPUT_METHOD_SEGMENT;
	}
    
	protected void setUp() {
	}

	protected void tearDown() {
	}
	
	class MyAttribute extends AttributedCharacterIterator.Attribute {
		protected MyAttribute(String name) {
			super(name);
		}
		
		public Object getExposedName() {
			return this.getName();
		}
	}
}
