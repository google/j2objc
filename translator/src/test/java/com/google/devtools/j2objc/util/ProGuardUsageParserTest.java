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

import com.google.common.io.CharStreams;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Unit tests for the ProGuardUsageParser.
 *
 * @author Daniel Connelly
 */
public class ProGuardUsageParserTest extends TestCase {

  public void testParse_SkipHeader() throws IOException {
    String listing = "ProGuard, version 4.7\n" +
        "Reading program jar [/foo/bar/baz.jar\n" +
        "Reading library jar [/foo/bar/bah.jar]\n";
    DeadCodeMap report = ProGuardUsageParser.parse(CharStreams.newReaderSupplier(listing));
    assertTrue(report.isEmpty());
  }

  public void testParse_Class() throws IOException {
    String listing = "com.google.apps.docs.commands.AbstractCommand\n";
    DeadCodeMap dead = ProGuardUsageParser.parse(CharStreams.newReaderSupplier(listing));
    assertTrue(dead.isDeadClass("com.google.apps.docs.commands.AbstractCommand"));
  }

  public void testParse_Method_NoLineNumbers() throws IOException {
    String listing = "com.google.apps.docs.commands.Command:\n" +
        "    public abstract com.google.apps.docs.commands.Command " +
            "transform(com.google.apps.docs.commands.Command,boolean)\n";
    DeadCodeMap dead = ProGuardUsageParser.parse(CharStreams.newReaderSupplier(listing));
    assertTrue(dead.isDeadMethod(
        "com.google.apps.docs.commands.Command",
        "transform",
        "(Lcom/google/apps/docs/commands/Command;Z)Lcom/google/apps/docs/commands/Command;"));
  }

  public void testParse_Method_LineNumbers() throws IOException {
    String listing = "com.foo.Baz:\n    312:313:public com.foo.Bar baz()\n";
    DeadCodeMap dead = ProGuardUsageParser.parse(CharStreams.newReaderSupplier(listing));
    assertTrue(dead.isDeadMethod("com.foo.Baz", "baz", "()Lcom/foo/Bar;"));
  }

  public void testParse_Method_Signatures() throws IOException {
    String listing = "com.foo.Baz:\n" +
        "    public static void fooBar()\n" +
        "    int foo_bar_baz(java.lang.String,com.google.Bar[][][])\n" +
        "    private native com.google.Baz[] Hello(WORLD,int,byte[])\n" +
        "    java.lang.String trim()\n" +
        "    Constructor(int)\n";
    DeadCodeMap dead = ProGuardUsageParser.parse(CharStreams.newReaderSupplier(listing));
    assertTrue(dead.isDeadMethod("com.foo.Baz", "fooBar", "()V"));
    assertTrue(dead.isDeadMethod(
        "com.foo.Baz", "foo_bar_baz", "(Ljava/lang/String;[[[Lcom/google/Bar;)I"));
    assertTrue(dead.isDeadMethod("com.foo.Baz", "Hello", "(LWORLD;I[B)[Lcom/google/Baz;"));
    assertTrue(dead.isDeadMethod("com.foo.Baz", "trim", "()Ljava/lang/String;"));
    assertTrue(dead.isDeadMethod("com.foo.Baz", "Constructor", "(I)V"));
  }

  public void testParse_Fields_NoLineNumbers() throws IOException {
    String listing = "com.foo.Baz:\n    int FOO\n";
    DeadCodeMap dead = ProGuardUsageParser.parse(CharStreams.newReaderSupplier(listing));
    assertTrue(dead.isDeadField("com.foo.Baz", "FOO"));
  }

  public void testParse_Fields_LineNumbers() throws IOException {
    String listing = "com.foo.Baz:\n    28:29:int FOO\n";
    DeadCodeMap dead = ProGuardUsageParser.parse(CharStreams.newReaderSupplier(listing));
    assertTrue(dead.isDeadField("com.foo.Baz", "FOO"));
  }

  public void testParse_Method_MissingClass() {
    String listing = "    312:313:public com.google.common.base.Foo Bar()\n";
    try {
      ProGuardUsageParser.parse(CharStreams.newReaderSupplier(listing));
      fail("Parsing method with no attached class should throw IOException");
    } catch (IOException e) {
      // ok
    }
  }

}
