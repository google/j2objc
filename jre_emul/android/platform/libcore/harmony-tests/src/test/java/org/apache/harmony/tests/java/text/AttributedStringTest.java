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
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

public class AttributedStringTest extends junit.framework.TestCase {

	/**
	 * @tests java.text.AttributedString#AttributedString(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
		String test = "Test string";
		AttributedString attrString = new AttributedString(test);
		AttributedCharacterIterator it = attrString.getIterator();
		StringBuffer buf = new StringBuffer();
		buf.append(it.first());
		char ch;
		while ((ch = it.next()) != CharacterIterator.DONE)
			buf.append(ch);
		assertTrue("Wrong string: " + buf, buf.toString().equals(test));
	}

	/**
	 * @tests java.text.AttributedString#AttributedString(AttributedCharacterIterator)
	 */
	public void test_ConstructorLAttributedCharacterIterator() {
		//Regression for HARMONY-1354
		assertNotNull(new AttributedString(new testAttributedCharacterIterator()));
	}
	/**
	 * @tests java.text.AttributedString#AttributedString(AttributedCharacterIterator, int, int)
	 */
	public void test_ConstructorLAttributedCharacterIteratorII() {
		//Regression for HARMONY-1355
		assertNotNull(new AttributedString(new testAttributedCharacterIterator(), 0, 0));
	}

	private class testAttributedCharacterIterator implements AttributedCharacterIterator {
	    public Set getAllAttributeKeys() {
	        return null;
	    }
	    public Object getAttribute(AttributedCharacterIterator.Attribute p) {
	        return null;
	    }
	    public Map getAttributes() {
	        return null;
	    }
	    public int getRunLimit(Set p) {
	        return 0;
	    }
	    public int getRunLimit(AttributedCharacterIterator.Attribute p) {
	        return 0;
	    }
	    public int getRunLimit() {
	        return 0;
	    }
	    public int getRunStart(Set p) {
	        return 0;
	    }
	    public int getRunStart(AttributedCharacterIterator.Attribute p) {
	        return 0;
	    }
	    public int getRunStart() {
	        return 0;
	    }
	    public Object clone() {
	        return null;
	    }
	    public int getIndex() {
	        return 0;
	    }
	    public int getEndIndex() {
	        return 0;
	    }
	    public int getBeginIndex() {
	        return 0;
	    }
	    public char setIndex(int p) {
	        return 'a';
	    }
	    public char previous() {
	        return 'a';
	    }
	    public char next() {
	        return 'a';
	    }
	    public char current() {
	        return 'a';
	    }
	    public char last() {
	        return 'a';
	    }
	    public char first() {
	        return 'a';
	    }
	}

    public void test_addAttributeLjava_text_AttributedCharacterIterator$AttributeLjava_lang_ObjectII() {
        AttributedString as = new AttributedString("test");
        as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, "a", 2,
                3);
        AttributedCharacterIterator it = as.getIterator();
        assertEquals("non-null value limit", 2, it
                .getRunLimit(AttributedCharacterIterator.Attribute.LANGUAGE));

        as = new AttributedString("test");
        as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, null,
                2, 3);
        it = as.getIterator();
        assertEquals("null value limit", 4, it
                .getRunLimit(AttributedCharacterIterator.Attribute.LANGUAGE));

        try {
            as = new AttributedString("test");
            as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE,
                    null, -1, 3);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        // regression for Harmony-1244
        as = new AttributedString("123", new WeakHashMap());
        try {
            as.addAttribute(null, new TreeSet(), 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            as.addAttribute(null, new TreeSet(), -1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        } 
    }
    
    /**
     * @tests java.text.AttributedString.addAttribute(AttributedCharacterIterator, Object)
     */
    public void test_addAttributeLjava_text_AttributedCharacterIterator$AttributeLjava_lang_Object() {
        //regression for Harmony-1244
        AttributedString as = new AttributedString("123", new WeakHashMap());
        try {
            as.addAttribute(null, new TreeSet());
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            as.addAttribute(null, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
}
