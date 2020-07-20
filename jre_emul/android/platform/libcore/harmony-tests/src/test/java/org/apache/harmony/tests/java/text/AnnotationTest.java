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

import java.text.Annotation;

import junit.framework.TestCase;

public class AnnotationTest extends TestCase {

	public void testAnnotation() {
		assertNotNull(new Annotation(null));
		assertNotNull(new Annotation("value"));
	}

	public void testGetValue() {
		Annotation a = new Annotation(null);
		assertNull(a.getValue());
		a = new Annotation("value");
		assertEquals("value", a.getValue());
	}

	public void testToString() {
        Annotation ant = new Annotation("HelloWorld");
        assertEquals("toString error.",
                     "java.text.Annotation[value=HelloWorld]",ant.toString());
        assertNotNull(new Annotation(null).toString());
        assertNotNull(new Annotation("value").toString());
	}
}
