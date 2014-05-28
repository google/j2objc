/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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

package com.google.devtools.j2objc;

import com.google.devtools.j2objc.gen.ArrayAccessTest;
import com.google.devtools.j2objc.gen.ArrayCreationTest;
import com.google.devtools.j2objc.gen.HiddenFieldDetectorTest;
import com.google.devtools.j2objc.gen.LineDirectivesTest;
import com.google.devtools.j2objc.gen.ObjectiveCHeaderGeneratorTest;
import com.google.devtools.j2objc.gen.ObjectiveCImplementationGeneratorTest;
import com.google.devtools.j2objc.gen.ObjectiveCSegmentedHeaderGeneratorTest;
import com.google.devtools.j2objc.gen.ObjectiveCSourceFileGeneratorTest;
import com.google.devtools.j2objc.gen.PrimitiveArrayTest;
import com.google.devtools.j2objc.gen.StatementGeneratorTest;
import com.google.devtools.j2objc.translate.ASTFactoryTest;
import com.google.devtools.j2objc.translate.AnonymousClassConverterTest;
import com.google.devtools.j2objc.translate.ArrayRewriterTest;
import com.google.devtools.j2objc.translate.AutoboxerTest;
import com.google.devtools.j2objc.translate.ComplexExpressionExtractorTest;
import com.google.devtools.j2objc.translate.CopyAllFieldsWriterTest;
import com.google.devtools.j2objc.translate.DeadCodeEliminatorTest;
import com.google.devtools.j2objc.translate.DestructorGeneratorTest;
import com.google.devtools.j2objc.translate.EnhancedForRewriterTest;
import com.google.devtools.j2objc.translate.FunctionizerTest;
import com.google.devtools.j2objc.translate.GwtConverterTest;
import com.google.devtools.j2objc.translate.InitializationNormalizerTest;
import com.google.devtools.j2objc.translate.InnerClassExtractorTest;
import com.google.devtools.j2objc.translate.JavaToIOSMethodTranslatorTest;
import com.google.devtools.j2objc.translate.NilCheckResolverTest;
import com.google.devtools.j2objc.translate.OperatorRewriterTest;
import com.google.devtools.j2objc.translate.OuterReferenceFixerTest;
import com.google.devtools.j2objc.translate.OuterReferenceResolverTest;
import com.google.devtools.j2objc.translate.RewriterTest;
import com.google.devtools.j2objc.translate.StaticVarRewriterTest;
import com.google.devtools.j2objc.translate.TypeSorterTest;
import com.google.devtools.j2objc.translate.UnsequencedExpressionRewriterTest;
import com.google.devtools.j2objc.types.BindingMapBuilderTest;
import com.google.devtools.j2objc.types.HeaderImportCollectorTest;
import com.google.devtools.j2objc.types.ImplementationImportCollectorTest;
import com.google.devtools.j2objc.types.ModifiedTypeBindingTest;
import com.google.devtools.j2objc.types.RenamedTypeBindingTest;
import com.google.devtools.j2objc.util.BindingUtilTest;
import com.google.devtools.j2objc.util.DeadCodeMapTest;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitorTest;
import com.google.devtools.j2objc.util.NameTableTest;
import com.google.devtools.j2objc.util.ProGuardUsageParserTest;
import com.google.devtools.j2objc.util.UnicodeUtilsTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Returns a suite of all small tests in this package.
 *
 * @author Tom Ball
 */
public class SmallTests {

  private static final Class<?>[] smallTestClasses = new Class[] {
    ASTFactoryTest.class,
    AnonymousClassConverterTest.class,
    ArrayAccessTest.class,
    ArrayCreationTest.class,
    ArrayRewriterTest.class,
    AutoboxerTest.class,
    BindingMapBuilderTest.class,
    BindingUtilTest.class,
    ComplexExpressionExtractorTest.class,
    CopyAllFieldsWriterTest.class,
    DeadCodeEliminatorTest.class,
    DeadCodeMapTest.class,
    DestructorGeneratorTest.class,
    EnhancedForRewriterTest.class,
    ErrorReportingASTVisitorTest.class,
    FunctionizerTest.class,
    GwtConverterTest.class,
    HeaderImportCollectorTest.class,
    HiddenFieldDetectorTest.class,
    ImplementationImportCollectorTest.class,
    InitializationNormalizerTest.class,
    InnerClassExtractorTest.class,
    JavaToIOSMethodTranslatorTest.class,
    LineDirectivesTest.class,
    ModifiedTypeBindingTest.class,
    NameTableTest.class,
    NilCheckResolverTest.class,
    ObjectiveCHeaderGeneratorTest.class,
    ObjectiveCImplementationGeneratorTest.class,
    ObjectiveCSegmentedHeaderGeneratorTest.class,
    ObjectiveCSourceFileGeneratorTest.class,
    OperatorRewriterTest.class,
    OptionsTest.class,
    OuterReferenceFixerTest.class,
    OuterReferenceResolverTest.class,
    PrimitiveArrayTest.class,
    ProGuardUsageParserTest.class,
    RenamedTypeBindingTest.class,
    RewriterTest.class,
    StatementGeneratorTest.class,
    StaticVarRewriterTest.class,
    TypeSorterTest.class,
    UnicodeUtilsTest.class,
    UnsequencedExpressionRewriterTest.class
  };

  public static Test suite() {
    return new TestSuite(smallTestClasses);
  }
}
