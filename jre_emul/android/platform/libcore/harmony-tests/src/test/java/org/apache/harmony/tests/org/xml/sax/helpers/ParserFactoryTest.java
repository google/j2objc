/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tests.org.xml.sax.helpers;

import junit.framework.TestCase;

import org.xml.sax.helpers.ParserFactory;

@SuppressWarnings("deprecation")
public class ParserFactoryTest extends TestCase {

    @Override protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMakeParser() throws ClassNotFoundException,
            IllegalAccessException, InstantiationException {

        System.clearProperty("org.xml.sax.parser");

        // Property not set at all
        try {
            ParserFactory.makeParser();
            fail("expected NullPointerException was not thrown");
        } catch (NullPointerException e) {
            // Expected
        }

        // Unknown class
        System.setProperty("org.xml.sax.parser", "foo.bar.SAXParser");

        try {
            ParserFactory.makeParser();
            fail("expected ClassNotFoundException was not thrown");
        } catch (ClassNotFoundException e) {
            // Expected
        }

// j2objc: all Objective C classes are accessible.
//        // Non-accessible class
//        System.setProperty("org.xml.sax.parser",
//                "org.apache.harmony.tests.org.xml.sax.support.NoAccessParser");
//
//        try {
//            ParserFactory.makeParser();
//            fail("expected IllegalAccessException was not thrown");
//        } catch (IllegalAccessException e) {
//            // Expected
//        }

// j2objc: b/65549211
//        // Non-instantiable class
//        System.setProperty("org.xml.sax.parser",
//                "org.apache.harmony.tests.org.xml.sax.support.NoInstanceParser");
//
//        try {
//            ParserFactory.makeParser();
//            fail("expected InstantiationException was not thrown");
//        } catch (InstantiationException e) {
//            // Expected
//        }

        // Non-Parser class
        System.setProperty("org.xml.sax.parser",
                "org.apache.harmony.tests.org.xml.sax.support.NoSubclassParser");

        try {
            ParserFactory.makeParser();
            fail("expected ClassCastException was not thrown");
        } catch (ClassCastException e) {
            // Expected
        }

        // Good one, finally
        System.setProperty("org.xml.sax.parser",
                "org.apache.harmony.tests.org.xml.sax.support.DoNothingParser");

        ParserFactory.makeParser();

    }

    public void testMakeParserString() throws ClassNotFoundException,
            IllegalAccessException, InstantiationException {
        // No class
        try {
            ParserFactory.makeParser(null);
            fail("expected NullPointerException was not thrown");
        } catch (NullPointerException e) {
            // Expected
        }

        // Unknown class
        try {
            ParserFactory.makeParser("foo.bar.SAXParser");
            fail("expected ClassNotFoundException was not thrown");
        } catch (ClassNotFoundException e) {
            // Expected
        }

// j2objc: all Objective C classes are accessible.
//        // Non-accessible class
//        try {
//            ParserFactory.makeParser(
//                    "org.apache.harmony.tests.org.xml.sax.support.NoAccessParser");
//            fail("expected IllegalAccessException was not thrown");
//        } catch (IllegalAccessException e) {
//            // Expected
//        }

// j2objc: b/65549211
//        // Non-instantiable class
//        try {
//            ParserFactory.makeParser(
//                    "org.apache.harmony.tests.org.xml.sax.support.NoInstanceParser");
//            fail("expected InstantiationException was not thrown");
//        } catch (InstantiationException e) {
//            // Expected
//        }

        // Non-Parser class
        try {
            ParserFactory.makeParser(
                    "org.apache.harmony.tests.org.xml.sax.support.NoSubclassParser");
            fail("expected ClassCastException was not thrown");
        } catch (ClassCastException e) {
            // Expected
        }

        // Good one, finally
        ParserFactory.makeParser(
                "org.apache.harmony.tests.org.xml.sax.support.DoNothingParser");
    }

}
