/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.tests.java.util;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ResourceBundleTest extends junit.framework.TestCase {

    public void test_getCandidateLocales() throws Exception {
        ResourceBundle.Control c = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT);
        assertEquals("[en_US, en, ]", c.getCandidateLocales("base", Locale.US).toString());
        assertEquals("[de_CH, de, ]", c.getCandidateLocales("base", new Locale("de", "CH")).toString());
    }

    /**
     * java.util.ResourceBundle#getBundle(java.lang.String,
     *        java.util.Locale)
     */
    public void test_getBundleLjava_lang_StringLjava_util_Locale() {
        ResourceBundle bundle;
        String name = "tests.support.Support_TestResource";
        Locale defLocale = Locale.getDefault();

        Locale.setDefault(new Locale("en", "US"));
        bundle = ResourceBundle.getBundle(name, new Locale("fr", "FR", "VAR"));
        assertEquals("Wrong bundle fr_FR_VAR", "frFRVARValue4", bundle.getString("parent4"));

        bundle = ResourceBundle.getBundle(name, new Locale("fr", "FR", "v1"));
        assertEquals("Wrong bundle fr_FR_v1", "frFRValue4", bundle.getString("parent4"));

        bundle = ResourceBundle.getBundle(name, new Locale("fr", "US", "VAR"));
        assertEquals("Wrong bundle fr_US_var", "frValue4", bundle.getString("parent4"));

        bundle = ResourceBundle.getBundle(name, new Locale("de", "FR", "VAR"));
        assertEquals("Wrong bundle de_FR_var", "enUSValue4", bundle.getString("parent4"));

        Locale.setDefault(new Locale("fr", "FR", "VAR"));
        bundle = ResourceBundle.getBundle(name, new Locale("de", "FR", "v1"));
        assertEquals("Wrong bundle de_FR_var 2", "frFRVARValue4", bundle.getString("parent4"));

        Locale.setDefault(new Locale("de", "US"));
        bundle = ResourceBundle.getBundle(name, new Locale("de", "FR", "var"));
        assertEquals("Wrong bundle de_FR_var 2", "parentValue4", bundle.getString("parent4")
                );

        try {
            ResourceBundle.getBundle(null, Locale.US);
            fail("NullPointerException expected");
        } catch (NullPointerException ee) {
            //expected
        }
        try {
            ResourceBundle.getBundle("blah", (Locale) null);
            fail("NullPointerException expected");
        } catch (NullPointerException ee) {
            //expected
        }


        try {
            ResourceBundle.getBundle("", new Locale("xx", "yy"));
            fail("MissingResourceException expected");
        } catch (MissingResourceException ee) {
            //expected
        }
    }

//    /**
//     * java.util.ResourceBundle#getBundle(java.lang.String,
//     *        java.util.Locale, java.lang.ClassLoader)
//     */
//    @KnownFailure("It's not allowed to pass null as parent class loader to"
//            + " a new ClassLoader anymore. Maybe we need to change"
//            + " URLClassLoader to allow this? It's not specified.")
//    public void test_getBundleLjava_lang_StringLjava_util_LocaleLjava_lang_ClassLoader() {
//        String classPath = System.getProperty("java.class.path");
//        StringTokenizer tok = new StringTokenizer(classPath, File.pathSeparator);
//        Vector<URL> urlVec = new Vector<URL>();
//        String resPackage = Support_Resources.RESOURCE_PACKAGE;
//        try {
//            while (tok.hasMoreTokens()) {
//                String path = tok.nextToken();
//                String url;
//                if (new File(path).isDirectory())
//                    url = "file:" + path + resPackage + "subfolder/";
//                else
//                    url = "jar:file:" + path + "!" + resPackage + "subfolder/";
//                urlVec.addElement(new URL(url));
//            }
//        } catch (MalformedURLException e) {
//        }
//        URL[] urls = new URL[urlVec.size()];
//        for (int i = 0; i < urlVec.size(); i++)
//            urls[i] = urlVec.elementAt(i);
//        URLClassLoader loader = new URLClassLoader(urls, null);
//
//        String name = Support_Resources.RESOURCE_PACKAGE_NAME
//                + ".hyts_resource";
//        ResourceBundle bundle = ResourceBundle.getBundle(name, Locale
//                .getDefault());
//            assertEquals("Wrong value read", "parent", bundle.getString("property"));
//        bundle = ResourceBundle.getBundle(name, Locale.getDefault(), loader);
//        assertEquals("Wrong cached value",
//                "resource", bundle.getString("property"));
//
//        try {
//            ResourceBundle.getBundle(null, Locale.getDefault(), loader);
//            fail("NullPointerException expected");
//        } catch (NullPointerException ee) {
//            //expected
//        }
//
//        try {
//            ResourceBundle.getBundle(name, null, loader);
//            fail("NullPointerException expected");
//        } catch (NullPointerException ee) {
//            //expected
//        }
//
//        try {
//            ResourceBundle.getBundle(name, Locale.getDefault(), (ClassLoader) null);
//            fail("NullPointerException expected");
//        } catch (NullPointerException ee) {
//            //expected
//        }
//
//        try {
//            ResourceBundle.getBundle("", Locale.getDefault(), loader);
//            fail("MissingResourceException expected");
//        } catch (MissingResourceException ee) {
//            //expected
//        }
//
//        // Regression test for Harmony-3823
//        B bb = new B();
//        String s = bb.find("nonexistent");
//        s = bb.find("name");
//        assertEquals("Wrong property got", "Name", s);
//    }

    /**
     * java.util.ResourceBundle#getString(java.lang.String)
     */
    public void test_getStringLjava_lang_String() {
        ResourceBundle bundle;
        String name = "tests.support.Support_TestResource";
        Locale.setDefault(new Locale("en", "US"));
        bundle = ResourceBundle.getBundle(name, new Locale("fr", "FR", "VAR"));
        assertEquals("Wrong value parent4",
                "frFRVARValue4", bundle.getString("parent4"));
        assertEquals("Wrong value parent3",
                "frFRValue3", bundle.getString("parent3"));
        assertEquals("Wrong value parent2",
                "frValue2", bundle.getString("parent2"));
        assertEquals("Wrong value parent1",
                "parentValue1", bundle.getString("parent1"));
        assertEquals("Wrong value child3",
                "frFRVARChildValue3", bundle.getString("child3"));
        assertEquals("Wrong value child2",
                "frFRVARChildValue2", bundle.getString("child2"));
        assertEquals("Wrong value child1",
                "frFRVARChildValue1", bundle.getString("child1"));

        try {
            bundle.getString(null);
            fail("NullPointerException expected");
        } catch (NullPointerException ee) {
            //expected
        }

        try {
            bundle.getString("");
            fail("MissingResourceException expected");
        } catch (MissingResourceException ee) {
            //expected
        }

        try {
            bundle.getString("IntegerVal");
            fail("ClassCastException expected");
        } catch (ClassCastException ee) {
            //expected
        }
    }
    public void test_getBundle_getClassName() {
        // Regression test for Harmony-1759
        Locale locale = Locale.GERMAN;
        String nonExistentBundle = "Non-ExistentBundle";
        try {
            ResourceBundle.getBundle(nonExistentBundle, locale, this.getClass()
                    .getClassLoader());
            fail("MissingResourceException expected!");
        } catch (MissingResourceException e) {
            assertEquals(nonExistentBundle + "_" + locale, e.getClassName());
        }

        try {
            ResourceBundle.getBundle(nonExistentBundle, locale);
            fail("MissingResourceException expected!");
        } catch (MissingResourceException e) {
            assertEquals(nonExistentBundle + "_" + locale, e.getClassName());
        }

        locale = Locale.getDefault();
        try {
            ResourceBundle.getBundle(nonExistentBundle);
            fail("MissingResourceException expected!");
        } catch (MissingResourceException e) {
            assertEquals(nonExistentBundle + "_" + locale, e.getClassName());
        }
    }

    class Mock_ResourceBundle extends ResourceBundle {
        @Override
        public Enumeration<String> getKeys() {
            return null;
        }

        @Override
        protected Object handleGetObject(String key) {
            return null;
        }
    }

    public void test_constructor() {
        assertNotNull(new Mock_ResourceBundle());
    }

    public void test_getLocale() {
        ResourceBundle bundle;
        String name = "tests.support.Support_TestResource";
        Locale loc = Locale.getDefault();
        Locale.setDefault(new Locale("en", "US"));

        bundle = ResourceBundle.getBundle(name, new Locale("fr", "FR", "VAR"));
        assertEquals("fr_FR_VAR", bundle.getLocale().toString());

        bundle = ResourceBundle.getBundle(name, new Locale("fr", "FR", "v1"));
        assertEquals("fr_FR", bundle.getLocale().toString());

        bundle = ResourceBundle.getBundle(name, new Locale("fr", "US", "VAR"));
        assertEquals("fr", bundle.getLocale().toString());

        bundle = ResourceBundle.getBundle(name, new Locale("de", "FR", "VAR"));
        assertEquals("en_US", bundle.getLocale().toString());

        bundle = ResourceBundle.getBundle(name, new Locale("de", "FR", "v1"));
        assertEquals("en_US", bundle.getLocale().toString());

        bundle = ResourceBundle.getBundle(name, new Locale("de", "FR", "var"));
        assertEquals("en_US", bundle.getLocale().toString());

        Locale.setDefault(loc);
    }

    public void test_getObjectLjava_lang_String() {
        ResourceBundle bundle;
        String name = "tests.support.Support_TestResource";
        Locale.setDefault(new Locale("en", "US"));
        bundle = ResourceBundle.getBundle(name, new Locale("fr", "FR", "VAR"));
        assertEquals("Wrong value parent4",
                "frFRVARValue4", (String)bundle.getObject("parent4"));
        assertEquals("Wrong value parent3",
                "frFRValue3", (String)bundle.getObject("parent3"));
        assertEquals("Wrong value parent2",
                "frValue2", (String) bundle.getObject("parent2"));
        assertEquals("Wrong value parent1",
                "parentValue1", (String)bundle.getObject("parent1"));
        assertEquals("Wrong value child3",
                "frFRVARChildValue3", (String)bundle.getObject("child3"));
        assertEquals("Wrong value child2",
                "frFRVARChildValue2", (String) bundle.getObject("child2"));
        assertEquals("Wrong value child1",
                "frFRVARChildValue1", (String)bundle.getObject("child1"));
        assertEquals("Wrong value IntegerVal",
                1, bundle.getObject("IntegerVal"));

        try {
            bundle.getObject(null);
            fail("NullPointerException expected");
        } catch (NullPointerException ee) {
            //expected
        }

        try {
            bundle.getObject("");
            fail("MissingResourceException expected");
        } catch (MissingResourceException ee) {
            //expected
        }
    }

    public void test_getStringArrayLjava_lang_String() {
        ResourceBundle bundle;
        String name = "tests.support.Support_TestResource";
        Locale.setDefault(new Locale("en", "US"));
        bundle = ResourceBundle.getBundle(name, new Locale("fr", "FR", "VAR"));

        String[] array = bundle.getStringArray("StringArray");
        for(int i = 0; i < array.length; i++) {
            assertEquals("Str" + (i + 1), array[i]);
        }

        try {
            bundle.getStringArray(null);
            fail("NullPointerException expected");
        } catch (NullPointerException ee) {
            //expected
        }

        try {
            bundle.getStringArray("");
            fail("MissingResourceException expected");
        } catch (MissingResourceException ee) {
            //expected
        }

        try {
            bundle.getStringArray("IntegerVal");
            fail("ClassCastException expected");
        } catch (ClassCastException ee) {
            //expected
        }
    }

    public void test_getBundleLjava_lang_String() {
        ResourceBundle bundle;
        String name = "tests.support.Support_TestResource";
        Locale defLocale = Locale.getDefault();

        Locale.setDefault(new Locale("en", "US"));
        bundle = ResourceBundle.getBundle(name);
        assertEquals("enUSValue4", bundle.getString("parent4"));

        Locale.setDefault(new Locale("fr", "FR", "v1"));
        bundle = ResourceBundle.getBundle(name);
        assertEquals("Wrong bundle fr_FR_v1", "frFRValue4", bundle.getString("parent4"));

        Locale.setDefault(new Locale("fr", "US", "VAR"));
        bundle = ResourceBundle.getBundle(name);
        assertEquals("Wrong bundle fr_US_var", "frValue4", bundle.getString("parent4"));

        Locale.setDefault(new Locale("de", "FR", "VAR"));
        bundle = ResourceBundle.getBundle(name);
        assertEquals("Wrong bundle de_FR_var", "parentValue4", bundle.getString("parent4"));

        Locale.setDefault(new Locale("de", "FR", "v1"));
        bundle = ResourceBundle.getBundle(name);
        assertEquals("Wrong bundle de_FR_var 2", "parentValue4", bundle.getString("parent4"));

        Locale.setDefault(new Locale("de", "FR", "var"));
        bundle = ResourceBundle.getBundle(name);
        assertEquals("Wrong bundle de_FR_var 2", "parentValue4", bundle.getString("parent4"));

        try {
            ResourceBundle.getBundle(null);
            fail("NullPointerException expected");
        } catch (NullPointerException ee) {
            //expected
        }

        try {
            ResourceBundle.getBundle("");
            fail("MissingResourceException expected");
        } catch (MissingResourceException ee) {
            //expected
        }

        Locale.setDefault(defLocale);
    }

    // http://b/26879578
    public void testBundleWithUtf8Values() {
        ResourceBundle bundle = ResourceBundle.getBundle(
                "org.apache.harmony.tests.java.util.TestUtf8ResourceBundle");
        assertEquals("Страх мой удивительный UTF-8 синтаксического анализа",
                bundle.getString("key"));
    }
}
