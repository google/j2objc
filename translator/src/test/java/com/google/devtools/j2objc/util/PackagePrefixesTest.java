/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
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

package com.google.devtools.j2objc.util;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * Unit tests for {@link PackagePrefixes}.
 *
 * @author Tom Ball
 */
public class PackagePrefixesTest extends GenerationTest {

  public void testPackagePrefixesFile() throws IOException {
    String prefixes =
        "# Prefix mappings\n"
        + "java.lang: JL\n"
        + "foo.bar: FB\n";
    StringReader reader = new StringReader(prefixes);
    Properties properties = new Properties();
    properties.load(reader);
    PackagePrefixes prefixMap = Options.getPackagePrefixes();
    assertFalse(prefixMap.hasPrefix("java.lang"));
    assertFalse(prefixMap.hasPrefix("foo.bar"));
    prefixMap.addPrefixProperties(properties);
    assertEquals("JL", prefixMap.getPrefix("java.lang"));
    assertEquals("FB", prefixMap.getPrefix("foo.bar"));
  }

  /**
   * Regression test for http://code.google.com/p/j2objc/issues/detail?id=100.
   */
  public void testPackagePrefixesWithTrailingSpace() throws IOException {
    String prefixes =
        "# Prefix mappings\n"
        + "java.lang: JL\n"
        + "foo.bar: FB \n";  // Trailing space should be ignored.
    StringReader reader = new StringReader(prefixes);
    Properties properties = new Properties();
    properties.load(reader);
    PackagePrefixes prefixMap = Options.getPackagePrefixes();
    prefixMap.addPrefixProperties(properties);
    assertEquals("JL", prefixMap.getPrefix("java.lang"));
    assertEquals("FB", prefixMap.getPrefix("foo.bar"));
  }

  // Verify class name with prefix.
  public void testGetFullNameWithPrefix() {
    String source = "package foo.bar; public class SomeClass {}";
    Options.addPackagePrefix("foo.bar", "FB");
    CompilationUnit unit = translateType("SomeClass", source);
    NameTable nameTable = unit.getNameTable();
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    assertEquals("FBSomeClass", nameTable.getFullName(decl.getTypeBinding()));
  }

  // Verify inner class name with prefix.
  public void testGetFullNameWithInnerClassAndPrefix() {
    String source = "package foo.bar; public class SomeClass { static class Inner {}}";
    Options.addPackagePrefix("foo.bar", "FB");
    CompilationUnit unit = translateType("SomeClass", source);
    NameTable nameTable = unit.getNameTable();
    AbstractTypeDeclaration decl = unit.getTypes().get(1);
    assertEquals("FBSomeClass_Inner", nameTable.getFullName(decl.getTypeBinding()));
  }

  public void testPackageWildcards() throws IOException {
    String source = "package foo.bar; public class SomeClass {}";
    Options.addPackagePrefix("foo.*", "FB");
    CompilationUnit unit = translateType("SomeClass", source);
    NameTable nameTable = unit.getNameTable();
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    assertEquals("FBSomeClass", nameTable.getFullName(decl.getTypeBinding()));
  }

  public void testWildcardToRegex() throws IOException {
    // Verify normal package name only matches itself.
    String regex = PackagePrefixes.wildcardToRegex("com.google.j2objc");
    assertEquals("^com\\.google\\.j2objc$", regex);
    assertTrue("com.google.j2objc".matches(regex));
    assertFalse("com google j2objc".matches(regex)); // Would match if wildcard wasn't converted.
    assertFalse("com.google.j2objc.annotations".matches(regex));

    regex = PackagePrefixes.wildcardToRegex("foo.bar.*");
    assertEquals("^(foo\\.bar|foo\\.bar\\..*)$", regex);
    assertTrue("foo.bar".matches(regex));
    assertTrue("foo.bar.mumble".matches(regex));
    assertFalse("foo.bars".matches(regex));
  }
}
