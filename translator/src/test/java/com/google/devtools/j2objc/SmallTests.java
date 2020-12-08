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

import com.google.devtools.j2objc.ast.AnnotationTest;
import com.google.devtools.j2objc.ast.InfixExpressionTest;
import com.google.devtools.j2objc.ast.LambdaExpressionTest;
import com.google.devtools.j2objc.ast.MethodReferenceTest;
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
import com.google.devtools.j2objc.javac.ClassFileConverterTest;
import com.google.devtools.j2objc.javac.JavacParserTest;
import com.google.devtools.j2objc.javac.JavacTreeConverterTest;
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
import com.google.devtools.j2objc.translate.ExternalAnnotationInjectorTest;
import com.google.devtools.j2objc.translate.FunctionizerTest;
import com.google.devtools.j2objc.translate.GwtConverterTest;
import com.google.devtools.j2objc.translate.InitializationNormalizerTest;
import com.google.devtools.j2objc.translate.InnerClassExtractorTest;
import com.google.devtools.j2objc.translate.JavaCloneWriterTest;
import com.google.devtools.j2objc.translate.JavaToIOSMethodTranslatorTest;
import com.google.devtools.j2objc.translate.LambdaTypeElementAdderTest;
import com.google.devtools.j2objc.translate.LogSiteInjectorTest;
import com.google.devtools.j2objc.translate.MetadataWriterTest;
import com.google.devtools.j2objc.translate.NilCheckResolverTest;
import com.google.devtools.j2objc.translate.NumberMethodRewriterTest;
import com.google.devtools.j2objc.translate.OcniExtractorTest;
import com.google.devtools.j2objc.translate.OperatorRewriterTest;
import com.google.devtools.j2objc.translate.OuterReferenceFixerTest;
import com.google.devtools.j2objc.translate.OuterReferenceResolverTest;
import com.google.devtools.j2objc.translate.PackageInfoRewriterTest;
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
import com.google.devtools.j2objc.util.ClassFileTest;
import com.google.devtools.j2objc.util.CodeReferenceMapTest;
import com.google.devtools.j2objc.util.ElementUtilTest;
import com.google.devtools.j2objc.util.ErrorUtilTest;
import com.google.devtools.j2objc.util.FileUtilTest;
import com.google.devtools.j2objc.util.NameTableTest;
import com.google.devtools.j2objc.util.PackageInfoLookupTest;
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

  private static final Class<?>[] smallTestClasses =
      new Class<?>[] {
        AbstractMethodRewriterTest.class,
        AnnotationRewriterTest.class,
        AnnotationTest.class,
        AnonymousClassConverterTest.class,
        ArrayAccessTest.class,
        ArrayCreationTest.class,
        ArrayRewriterTest.class,
        AutoboxerTest.class,
        CastResolverTest.class,
        ClassFileConverterTest.class,
        ClassFileTest.class,
        CodeReferenceMapTest.class,
        ComplexExpressionExtractorTest.class,
        CompoundTypeTest.class,
        ConstantBranchPrunerTest.class,
        DeadCodeEliminatorTest.class,
        DefaultMethodsTest.class,
        DestructorGeneratorTest.class,
        ElementUtilTest.class,
        EnhancedForRewriterTest.class,
        EnumRewriterTest.class,
        ErrorUtilTest.class,
        ExternalAnnotationInjectorTest.class,
        FileUtilTest.class,
        FunctionizerTest.class,
        GwtConverterTest.class,
        HeaderImportCollectorTest.class,
        ImplementationImportCollectorTest.class,
        InfixExpressionTest.class,
        InitializationNormalizerTest.class,
        InnerClassExtractorTest.class,
        J2ObjCIncompatibleStripperTest.class,
        J2ObjCTest.class,
        JavaCloneWriterTest.class,
        JavacParserTest.class,
        JavacTreeConverterTest.class,
        JavadocGeneratorTest.class,
        JavaToIOSMethodTranslatorTest.class,
        LambdaExpressionTest.class,
        LambdaTypeElementAdderTest.class,
        LineDirectivesTest.class,
        LiteralGeneratorTest.class,
        LogSiteInjectorTest.class,
        MetadataWriterTest.class,
        MethodReferenceTest.class,
        NameTableTest.class,
        NilCheckResolverTest.class,
        NumberMethodRewriterTest.class,
        ObjectiveCHeaderGeneratorTest.class,
        ObjectiveCImplementationGeneratorTest.class,
        ObjectiveCSegmentedHeaderGeneratorTest.class,
        ObjectiveCSourceFileGeneratorTest.class,
        OcniExtractorTest.class,
        OperatorRewriterTest.class,
        OptionsTest.class,
        OuterReferenceFixerTest.class,
        OuterReferenceResolverTest.class,
        PackageInfoLookupTest.class,
        PackageInfoRewriterTest.class,
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
        TypeDeclarationGeneratorTest.class,
        TypeImplementationGeneratorTest.class,
        TypeUseAnnotationTest.class,
        TranslationProcessorTest.class,
        TranslationUtilTest.class,
        UnicodeUtilsTest.class,
        UnsequencedExpressionRewriterTest.class,
        VarargsRewriterTest.class,
        VariableRenamerTest.class,
      };

  public static Test suite() {
    return new TestSuite(smallTestClasses);
  }
}
