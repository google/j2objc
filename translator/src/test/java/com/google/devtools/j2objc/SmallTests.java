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

import com.google.devtools.j2objc.ast.LambdaExpressionTest;
import com.google.devtools.j2objc.ast.MethodReferenceTest;
import com.google.devtools.j2objc.ast.TreeUtilTest;
import com.google.devtools.j2objc.gen.ArrayAccessTest;
import com.google.devtools.j2objc.gen.ArrayCreationTest;
import com.google.devtools.j2objc.gen.JavadocGeneratorTest;
import com.google.devtools.j2objc.gen.LineDirectivesTest;
import com.google.devtools.j2objc.gen.LiteralGeneratorTest;
import com.google.devtools.j2objc.gen.ObjectiveCHeaderGeneratorTest;
import com.google.devtools.j2objc.gen.ObjectiveCImplementationGeneratorTest;
import com.google.devtools.j2objc.gen.ObjectiveCSegmentedHeaderGeneratorTest;
import com.google.devtools.j2objc.gen.ObjectiveCSourceFileGeneratorTest;
import com.google.devtools.j2objc.gen.PrimitiveArrayTest;
import com.google.devtools.j2objc.gen.SignatureGeneratorTest;
import com.google.devtools.j2objc.gen.StatementGeneratorTest;
import com.google.devtools.j2objc.gen.TypeDeclarationGeneratorTest;
import com.google.devtools.j2objc.gen.TypeImplementationGeneratorTest;
import com.google.devtools.j2objc.jdt.TreeConverterTest;
import com.google.devtools.j2objc.pipeline.J2ObjCIncompatibleStripperTest;
import com.google.devtools.j2objc.pipeline.TranslationProcessorTest;
import com.google.devtools.j2objc.translate.AbstractMethodRewriterTest;
import com.google.devtools.j2objc.translate.AnnotationRewriterTest;
import com.google.devtools.j2objc.translate.AnonymousClassConverterTest;
import com.google.devtools.j2objc.translate.ArrayRewriterTest;
import com.google.devtools.j2objc.translate.AutoboxerTest;
import com.google.devtools.j2objc.translate.CastResolverTest;
import com.google.devtools.j2objc.translate.ComplexExpressionExtractorTest;
import com.google.devtools.j2objc.translate.ConstantBranchPrunerTest;
import com.google.devtools.j2objc.translate.DeadCodeEliminatorTest;
import com.google.devtools.j2objc.translate.DefaultMethodsTest;
import com.google.devtools.j2objc.translate.DestructorGeneratorTest;
import com.google.devtools.j2objc.translate.EnhancedForRewriterTest;
import com.google.devtools.j2objc.translate.EnumRewriterTest;
import com.google.devtools.j2objc.translate.FunctionizerTest;
import com.google.devtools.j2objc.translate.GwtConverterTest;
import com.google.devtools.j2objc.translate.InitializationNormalizerTest;
import com.google.devtools.j2objc.translate.InnerClassExtractorTest;
import com.google.devtools.j2objc.translate.JavaCloneWriterTest;
import com.google.devtools.j2objc.translate.JavaToIOSMethodTranslatorTest;
import com.google.devtools.j2objc.translate.MetadataWriterTest;
import com.google.devtools.j2objc.translate.NilCheckResolverTest;
import com.google.devtools.j2objc.translate.OcniExtractorTest;
import com.google.devtools.j2objc.translate.OperatorRewriterTest;
import com.google.devtools.j2objc.translate.OuterReferenceFixerTest;
import com.google.devtools.j2objc.translate.OuterReferenceResolverTest;
import com.google.devtools.j2objc.translate.PrivateDeclarationResolverTest;
import com.google.devtools.j2objc.translate.RewriterTest;
import com.google.devtools.j2objc.translate.StaticVarRewriterTest;
import com.google.devtools.j2objc.translate.SuperMethodInvocationRewriterTest;
import com.google.devtools.j2objc.translate.SwitchRewriterTest;
import com.google.devtools.j2objc.translate.TypeUseAnnotationTest;
import com.google.devtools.j2objc.translate.UnsequencedExpressionRewriterTest;
import com.google.devtools.j2objc.translate.VarargsRewriterTest;
import com.google.devtools.j2objc.translate.VariableRenamerTest;
import com.google.devtools.j2objc.types.CompoundTypeTest;
import com.google.devtools.j2objc.types.HeaderImportCollectorTest;
import com.google.devtools.j2objc.types.ImplementationImportCollectorTest;
import com.google.devtools.j2objc.util.BindingUtilTest;
import com.google.devtools.j2objc.util.DeadCodeMapTest;
import com.google.devtools.j2objc.util.ElementUtilTest;
import com.google.devtools.j2objc.util.ErrorUtilTest;
import com.google.devtools.j2objc.util.FileUtilTest;
import com.google.devtools.j2objc.util.NameTableTest;
import com.google.devtools.j2objc.util.PackagePrefixesTest;
import com.google.devtools.j2objc.util.ProGuardUsageParserTest;
import com.google.devtools.j2objc.util.TranslationUtilTest;
import com.google.devtools.j2objc.util.UnicodeUtilsTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Returns a suite of all small tests in this package.
 */
public class SmallTests {

  private static final Class<?>[] smallTestClasses = new Class[] {
    AbstractMethodRewriterTest.class,
    AnnotationRewriterTest.class,
    AnonymousClassConverterTest.class,
    ArrayAccessTest.class,
    ArrayCreationTest.class,
    ArrayRewriterTest.class,
    AutoboxerTest.class,
    BindingUtilTest.class,
    CastResolverTest.class,
    ComplexExpressionExtractorTest.class,
    ConstantBranchPrunerTest.class,
    DeadCodeEliminatorTest.class,
    DeadCodeMapTest.class,
    DestructorGeneratorTest.class,
    ElementUtilTest.class,
    EnhancedForRewriterTest.class,
    EnumRewriterTest.class,
    ErrorUtilTest.class,
    FileUtilTest.class,
    FunctionizerTest.class,
    GwtConverterTest.class,
    HeaderImportCollectorTest.class,
    ImplementationImportCollectorTest.class,
    InitializationNormalizerTest.class,
    InnerClassExtractorTest.class,
    J2ObjCIncompatibleStripperTest.class,
    J2ObjCTest.class,
    JavaCloneWriterTest.class,
    JavadocGeneratorTest.class,
    JavaToIOSMethodTranslatorTest.class,
    LineDirectivesTest.class,
    LiteralGeneratorTest.class,
    MetadataWriterTest.class,
    NameTableTest.class,
    NilCheckResolverTest.class,
    ObjectiveCHeaderGeneratorTest.class,
    ObjectiveCImplementationGeneratorTest.class,
    ObjectiveCSegmentedHeaderGeneratorTest.class,
    ObjectiveCSourceFileGeneratorTest.class,
    OcniExtractorTest.class,
    OperatorRewriterTest.class,
    OptionsTest.class,
    OuterReferenceFixerTest.class,
    OuterReferenceResolverTest.class,
    PackagePrefixesTest.class,
    PrimitiveArrayTest.class,
    PrivateDeclarationResolverTest.class,
    ProGuardUsageParserTest.class,
    RewriterTest.class,
    SignatureGeneratorTest.class,
    StatementGeneratorTest.class,
    StaticVarRewriterTest.class,
    SuperMethodInvocationRewriterTest.class,
    SwitchRewriterTest.class,
    TreeConverterTest.class,
    TypeDeclarationGeneratorTest.class,
    TypeImplementationGeneratorTest.class,
    TranslationProcessorTest.class,
    TranslationUtilTest.class,
    TreeUtilTest.class,
    UnicodeUtilsTest.class,
    UnsequencedExpressionRewriterTest.class,
    VarargsRewriterTest.class,
    VariableRenamerTest.class
  };

  public static Test suite() {
    TestSuite testSuite = new TestSuite(smallTestClasses);
    try {
      Class.forName("java.lang.invoke.LambdaMetafactory");

      // Running with Java 8 JRE, add test classes that depend on it.
      testSuite.addTestSuite(CompoundTypeTest.class);
      testSuite.addTestSuite(DefaultMethodsTest.class);
      testSuite.addTestSuite(LambdaExpressionTest.class);
      testSuite.addTestSuite(MethodReferenceTest.class);
      testSuite.addTestSuite(TypeUseAnnotationTest.class);
    } catch (ClassNotFoundException e) {
      // Running on pre-Java 8 JRE.
    }
    return testSuite;
  }
}
