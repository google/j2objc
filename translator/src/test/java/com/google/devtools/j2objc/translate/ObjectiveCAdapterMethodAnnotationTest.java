/*
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

package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.GenerationTest;
import java.io.IOException;

public class ObjectiveCAdapterMethodAnnotationTest extends GenerationTest {

  @Override
  public void setUp() throws IOException {
    super.setUp();
  }

  public void testSelectorRequired() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; "
            + "public class NoSelector {"
            + "  @ObjectiveCAdapterMethod()"
            + "  public boolean getFalse() { "
            + "    return false; "
            + "  }"
            + "}",
        "NoSelector.java");
    @SuppressWarnings("unused")
    String testHeader = translateSourceFile("NoSelector", "NoSelector.h");
    assertError("ObjectiveCAdapterMethod must specify a selector.");
  }

  public void testSelectorNotRequired() throws IOException {
    addSourceFile(
        "import com.google.j2kt.annotations.Throws; "
            + "public class NoSelector {"
            + "  @Throws"
            + "  public boolean getFalse() { "
            + "    return false; "
            + "  }"
            + "}",
        "NoSelector.java");
    @SuppressWarnings("unused")
    String testHeader = translateSourceFile("NoSelector", "NoSelector.h");
    assertNoErrors();
  }

  public void testSelectorToArgMatch() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; "
            + "public class BadSelector {"
            + "  @ObjectiveCAdapterMethod(selector=\"doSomethingWith:\" )"
            + "  public boolean getFalse() { "
            + "    return false; "
            + "  }"
            + "}",
        "BadSelector.java");
    @SuppressWarnings("unused")
    String testHeader = translateSourceFile("BadSelector", "BadSelector.h");
    assertError("ObjectiveCAdapterMethod selector does not match the number of arguments.");
  }

  public void testAdaptationsRequired() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; "
            + "public class NoAdaptations {"
            + "  @ObjectiveCAdapterMethod(selector=\"isFalse\")"
            + "  public boolean getFalse() { "
            + "    return false; "
            + "  }"
            + "}",
        "NoAdaptations.java");
    @SuppressWarnings("unused")
    String testHeader = translateSourceFile("NoAdaptations", "NoAdaptations.h");
    assertError("ObjectiveCAdapterMethod must specify at least one adaptation.");
  }

  public void testAdaptationsNotRequired() throws IOException {
    addSourceFile(
        "import com.google.j2kt.annotations.Throws; "
            + "public class NoAdaptations {"
            + "  @Throws"
            + "  public boolean getFalse() { "
            + "    return false; "
            + "  }"
            + "}",
        "NoAdaptations.java");
    @SuppressWarnings("unused")
    String testHeader = translateSourceFile("NoAdaptations", "NoAdaptations.h");
    assertNoErrors();
  }

  public void testVarArgsUnsupported() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; "
            + "import com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; "
            + "public class VarargsAttempt {"
            + "  @ObjectiveCAdapterMethod(selector=\"allFalse:\", "
            + "                           adaptations={Adaptation.RETURN_NATIVE_BOOLS})"
            + "  public boolean allFalse(boolean... bools) { "
            + "    return false; "
            + "  }"
            + "}",
        "VarargsAttempt.java");
    @SuppressWarnings("unused")
    String testHeader = translateSourceFile("VarargsAttempt", "VarargsAttempt.h");
    assertError("ObjectiveCAdapterMethod does not support varargs.");
  }

  public void testAbstractClassesAndMethods() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; "
            + "import com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; "
            + "import java.util.List; "
            + "public abstract class A {"
            + "  @ObjectiveCAdapterMethod(selector=\"integerArray\", "
            + "                           adaptations={Adaptation.RETURN_LISTS_AS_NATIVE_ARRAYS})"
            + "  abstract List<Integer> getIntegerList();"
            + "}",
        "A.java");
    addSourceFile(
        "import java.util.ArrayList; "
            + "import java.util.List; "
            + "public class C extends A {"
            + "  @Override "
            + "  public List<Integer> getIntegerList() {"
            + "    return new ArrayList<Integer>(); "
            + "  }"
            + "}",
        "C.java");
    String testAHeader = translateSourceFile("A", "A.h");
    String testASource = getTranslatedFile("A.m");
    String testCHeader = translateSourceFile("C", "C.h");
    String testCSource = getTranslatedFile("C.m");

    assertNoWarnings();
    assertNoErrors();
    assertTranslation(testAHeader, "- (NSArray *)integerArray;");
    assertTranslatedLines(
        testASource,
        "- (NSArray *)integerArray {",
        "  return JREAdaptedArrayFromJavaList([self _getIntegerList]);",
        "}");
    assertNotInTranslation(testCHeader, "- (NSArray *)integerArray");
    assertNotInTranslation(testCSource, "- (NSArray *)integerArray");
    assertTranslatedLines(
        testCSource,
        "- (id<JavaUtilList>)_getIntegerList {",
        "  return create_JavaUtilArrayList_init();",
        "}");
  }

  public void testInterfaceMethods() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; "
            + "import com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; "
            + "import java.util.List; "
            + "public interface I {"
            + "  @ObjectiveCAdapterMethod(selector=\"integerArray\", "
            + "                           adaptations={Adaptation.RETURN_LISTS_AS_NATIVE_ARRAYS})"
            + "  List<Integer> getIntegerList();"
            + "}",
        "I.java");
    addSourceFile(
        "import java.util.ArrayList; "
            + "import java.util.List; "
            + "public class C implements I {"
            + "  public List<Integer> getIntegerList() {"
            + "    return new ArrayList<Integer>(); "
            + "  }"
            + "}",
        "C.java");
    String testIHeader = translateSourceFile("I", "I.h");
    String testCHeader = translateSourceFile("C", "C.h");
    String testCSource = getTranslatedFile("C.m");

    assertNoWarnings();
    assertNoErrors();
    assertTranslation(testIHeader, "- (id<JavaUtilList>)_getIntegerList;");
    assertNotInTranslation(testIHeader, "integerArray");
    assertTranslation(testCHeader, "- (NSArray *)integerArray;");
    assertTranslatedLines(
        testCSource,
        "- (NSArray *)integerArray {",
        "  return JREAdaptedArrayFromJavaList([self _getIntegerList]);",
        "}");
  }

  public void testNullAnnotations() throws IOException {
    options.setNullability(true);

    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCNativeProtocol; "
            + "import com.google.j2objc.annotations.ObjectiveCAdapterProtocol; "
            + "@ObjectiveCNativeProtocol(name=\"NativeProtocol\") "
            + "@ObjectiveCAdapterProtocol(\"NativeProtocol\") "
            + "public class A {}",
        "A.java");

    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; "
            + "import com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; "
            + "import java.util.ArrayList; "
            + "import java.util.List; "
            + "import javax.annotation.*; "
            + "public class NullMethods {"
            + "  @Nullable "
            + "  @ObjectiveCAdapterMethod(selector=\"rewrittenReturnObject:\", "
            + "                           adaptations={Adaptation.RETURN_LISTS_AS_NATIVE_ARRAYS})"
            + "  public List getRewrittenReturnObject(boolean flag) { "
            + "    return new ArrayList(); "
            + "  }"
            + "  @Nullable "
            + "  @ObjectiveCAdapterMethod(selector=\"returnObject:\", "
            + "                           adaptations={Adaptation.ACCEPT_NATIVE_BOOLS})"
            + "  public List getReturnObject(boolean flag) { "
            + "    return new ArrayList(); "
            + "  }"
            + "  @Nullable "
            + "  @ObjectiveCAdapterMethod(selector=\"returnAdapterProtocol\", "
            + "                           adaptations={Adaptation.RETURN_ADAPTER_PROTOCOLS})"
            + "  public A getAdapterProtocol() { "
            + "    return new A(); "
            + "  }"
            + "}",
        "NullMethods.java");
    String testHeader = translateSourceFile("NullMethods", "NullMethods.h");

    assertNoWarnings();
    assertNoErrors();

    assertTranslation(testHeader, "- (NSArray * _Nullable)rewrittenReturnObject:(bool)flag;");
    assertTranslation(testHeader, "- (id<JavaUtilList> _Nullable)returnObject:(bool)flag;");
    assertTranslation(testHeader, "- (id<NativeProtocol> _Nullable)returnAdapterProtocol;");
    // In the future when we support written object arguments add here to make sure their
    // nullability is translated.
  }

  public void testExceptionAsErrorSingleArgNaming() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; "
            + "import com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; "
            + "public class ErrorMethod {"
            + "  @ObjectiveCAdapterMethod(selector=\"doSomething\", "
            + "                           adaptations={Adaptation.EXCEPTIONS_AS_ERRORS})"
            + "  public void doSomething() { "
            + "  }"
            + "}",
        "ErrorMethod.java");

    @SuppressWarnings("unused")
    String testHeader = translateSourceFile("ErrorMethod", "ErrorMethod.h");
    @SuppressWarnings("unused")
    String testSource = getTranslatedFile("ErrorMethod.m");

    assertNoWarnings();
    assertError(
        "ObjectiveCAdapterMethod handling exceptions requires a \"AndReturnError:\" selector.");
  }

  public void testExceptionAsErrorMultiArgNaming() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; "
            + "import com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; "
            + "public class ErrorMethod {"
            + "  @ObjectiveCAdapterMethod(selector=\"doSomething\", "
            + "                           adaptations={Adaptation.EXCEPTIONS_AS_ERRORS})"
            + "  public void doSomething(int somenumber) { "
            + "  }"
            + "}",
        "ErrorMethod.java");

    @SuppressWarnings("unused")
    String testHeader = translateSourceFile("ErrorMethod", "ErrorMethod.h");
    @SuppressWarnings("unused")
    String testSource = getTranslatedFile("ErrorMethod.m");

    assertNoWarnings();
    assertError(
        "ObjectiveCAdapterMethod handling exceptions requires selector with a final \"error:\""
            + " argument.");
  }

  public void testExceptionAsErrorAnnotationReturnTypes() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; "
            + "import com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; "
            + "import java.util.ArrayList; "
            + "import java.util.List; "
            + "public class ExceptionsAsErrorMethods {"
            + "  @ObjectiveCAdapterMethod(selector=\"doSomethingAndReturnError:\", "
            + "                           adaptations={Adaptation.EXCEPTIONS_AS_ERRORS})"
            + "  public void doSomething() { "
            + "  }"
            + "  @ObjectiveCAdapterMethod(selector=\"isItTrueAndReturnError:\", "
            + "                           adaptations={Adaptation.EXCEPTIONS_AS_ERRORS})"
            + "  public boolean getTrue() { "
            + "    return true; "
            + "  }"
            + "  @ObjectiveCAdapterMethod(selector=\"someList:error:\", "
            + "                           adaptations={Adaptation.EXCEPTIONS_AS_ERRORS})"
            + "  public List getSomeList(int somenumber) { "
            + "    return new ArrayList(); "
            + "  }"
            + "  @ObjectiveCAdapterMethod(selector=\"someArrayList:error:\", "
            + "                           adaptations={Adaptation.EXCEPTIONS_AS_ERRORS})"
            + "  public ArrayList getSomeArrayList(int somenumber) { "
            + "    return new ArrayList(); "
            + "  }"
            + "  @ObjectiveCAdapterMethod(selector=\"someListAsNativeArray:error:\", "
            + "                           adaptations={Adaptation.EXCEPTIONS_AS_ERRORS, "
            + "                                        Adaptation.RETURN_LISTS_AS_NATIVE_ARRAYS})"
            + "  public List getSomeNativeArray(int somenumber) { "
            + "    return new ArrayList(); "
            + "  }"
            + "}",
        "ExceptionsAsErrorMethods.java");

    String testHeader =
        translateSourceFile("ExceptionsAsErrorMethods", "ExceptionsAsErrorMethods.h");
    String testSource = getTranslatedFile("ExceptionsAsErrorMethods.m");

    assertNoWarnings();
    assertNoErrors();

    assertTranslation(testHeader, "- (bool)doSomethingAndReturnError:(NSError **)error;");
    assertTranslatedLines(
        testSource,
        "- (bool)doSomethingAndReturnError:(NSError **)error {",
        "@try {",
        "  [self _doSomething]; ",
        "  return YES; ",
        "} @catch (NSException *e) {",
        "  if (error) { *error = JREErrorFromException(e); } ",
        "  return NO; ",
        "}");

    assertTranslation(testHeader, "- (bool)isItTrueAndReturnError:(NSError **)error;");
    assertTranslatedLines(
        testSource,
        "- (bool)isItTrueAndReturnError:(NSError **)error {",
        "@try {",
        "  return [self _getTrue];",
        "} @catch (NSException *e) {",
        "  if (error) { *error = JREErrorFromException(e); } ",
        "  return 0; ",
        "}");

    assertTranslatedLines(
        testHeader, "- (id<JavaUtilList>)someList:(int32_t)somenumber", "error:(NSError **)error;");
    assertTranslatedLines(
        testSource,
        "- (id<JavaUtilList>)someList:(int32_t)somenumber",
        "error:(NSError **)error {",
        "@try {",
        "  return [self _getSomeListWithInt:somenumber];",
        "} @catch (NSException *e) {",
        "  if (error) { *error = JREErrorFromException(e); } ",
        "  return nil; ",
        "}");

    assertTranslatedLines(
        testHeader,
        "- (JavaUtilArrayList *)someArrayList:(int32_t)somenumber",
        "error:(NSError **)error;");
    assertTranslatedLines(
        testSource,
        "- (JavaUtilArrayList *)someArrayList:(int32_t)somenumber",
        "error:(NSError **)error {",
        "@try {",
        "  return [self _getSomeArrayListWithInt:somenumber];",
        "} @catch (NSException *e) {",
        "  if (error) { *error = JREErrorFromException(e); } ",
        "  return nil; ",
        "}");

    assertTranslatedLines(
        testHeader,
        "- (NSArray *)someListAsNativeArray:(int32_t)somenumber",
        "error:(NSError **)error;");
    assertTranslatedLines(
        testSource,
        "- (NSArray *)someListAsNativeArray:(int32_t)somenumber",
        "error:(NSError **)error {",
        "@try {",
        "  return JREAdaptedArrayFromJavaList([self _getSomeNativeArrayWithInt:somenumber]);",
        "} @catch (NSException *e) {",
        "  if (error) { *error = JREErrorFromException(e); } ",
        "  return nil; ",
        "}");
  }

  public void testInappropriateBooleanReturnWarning() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; import"
            + " com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; public class"
            + " NoBoolMethod {  @ObjectiveCAdapterMethod(selector=\"getInt\","
            + " adaptations={Adaptation.RETURN_NATIVE_BOOLS})  public Integer getInt() {     return"
            + " 42;   }}",
        "NoBoolMethod.java");
    @SuppressWarnings("unused")
    String testHeader = translateSourceFile("NoBoolMethod", "NoBoolMethod.h");
    assertWarning(
        "ObjectiveCAdapterMethod native bool return type adaptation used on a method without a"
            + " boolean return.");
  }

  public void testInappropriateBooleanArgWarning() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; import"
            + " com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; public class"
            + " NoBoolMethod {  @ObjectiveCAdapterMethod(selector=\"doIt:\","
            + " adaptations={Adaptation.ACCEPT_NATIVE_BOOLS})  public void doThing(Integer value) {"
            + "   }}",
        "NoBoolMethod.java");
    @SuppressWarnings("unused")
    String testHeader = translateSourceFile("NoBoolMethod", "NoBoolMethod.h");
    assertWarning(
        "ObjectiveCAdapterMethod native boolean argument adaptation used on method without boolean"
            + " arguments.");
  }

  public void testBooleanAnnotation() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; import"
            + " com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; public class"
            + " BooleanMethods {  @ObjectiveCAdapterMethod(selector=\"isItTrue\","
            + " adaptations={Adaptation.RETURN_NATIVE_BOOLS})  public boolean getTrue() {    "
            + " return true;   }  @ObjectiveCAdapterMethod(selector=\"isSomethingElse\","
            + " adaptations={Adaptation.RETURN_NATIVE_BOOLS})  static public boolean"
            + " getSomethingElse() {     return false;   } "
            + " @ObjectiveCAdapterMethod(selector=\"makeIt:\","
            + " adaptations={Adaptation.ACCEPT_NATIVE_BOOLS})  public void setValue(boolean value)"
            + " {   }  @ObjectiveCAdapterMethod(selector=\"makeSomethingElse:\","
            + " adaptations={Adaptation.ACCEPT_NATIVE_BOOLS})  static public void"
            + " setOtherValue(boolean value) {   }}",
        "BooleanMethods.java");

    String testHeader = translateSourceFile("BooleanMethods", "BooleanMethods.h");
    String testSource = getTranslatedFile("BooleanMethods.m");

    assertNoWarnings();
    assertNoErrors();

    assertTranslation(testHeader, "- (bool)isItTrue;");
    assertTranslatedLines(
        testSource, "- (bool)isItTrue {", "  return [self _getTrue] ? YES : NO;", "}");

    assertTranslation(testHeader, "+ (bool)isSomethingElse;");
    assertTranslatedLines(
        testSource,
        "+ (bool)isSomethingElse {",
        "  return [BooleanMethods _getSomethingElse] ? YES : NO;",
        "}");

    assertTranslation(testHeader, "- (void)makeIt:(bool)value;");
    assertTranslatedLines(
        testSource, "- (void)makeIt:(bool)value {", "  [self _setValueWithBoolean:value];", "}");

    assertTranslation(testHeader, "+ (void)makeSomethingElse:(bool)value;");
    assertTranslatedLines(
        testSource,
        "+ (void)makeSomethingElse:(bool)value {",
        "  [BooleanMethods _setOtherValueWithBoolean:value];",
        "}");
  }

  public void testInappropriateEnumReturnWarning() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; import"
            + " com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; public class"
            + " NoEnumMethods {  @ObjectiveCAdapterMethod(selector=\"blue:\","
            + " adaptations={Adaptation.RETURN_NATIVE_ENUMS})  public String getBlue(String thing)"
            + " {     return thing;   }}",
        "NoEnumMethods.java");
    @SuppressWarnings("unused")
    String testHeader = translateSourceFile("NoEnumMethods", "NoEnumMethods.h");
    assertWarning(
        "ObjectiveCAdapterMethod native enum return type adaptation used on a method without an"
            + " enum return.");
  }

  public void testInappropriateEnumArgWarning() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; import"
            + " com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; public class"
            + " NoEnumMethods {  @ObjectiveCAdapterMethod(selector=\"blue:\","
            + " adaptations={Adaptation.ACCEPT_NATIVE_ENUMS})  public String getBlue(String thing)"
            + " {     return thing;   }}",
        "NoEnumMethods.java");
    @SuppressWarnings("unused")
    String testHeader = translateSourceFile("NoEnumMethods", "NoEnumMethods.h");
    assertWarning(
        "ObjectiveCAdapterMethod native enum argument adaptation used on method without enum"
            + " arguments.");
  }

  public void testEnumImportAndForwardDeclaration() throws IOException {
    addSourceFile("public enum Color { RED, WHITE, BLUE };", "Color.java");

    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; import"
            + " com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; public class"
            + " EnumMethods {  @ObjectiveCAdapterMethod(selector=\"blue\","
            + " adaptations={Adaptation.RETURN_NATIVE_ENUMS})  public Color getBlue() {     return"
            + " Color.BLUE;   }}",
        "EnumMethods.java");

    String testHeader = translateSourceFile("EnumMethods", "EnumMethods.h");
    String testSource = getTranslatedFile("EnumMethods.m");
    assertTranslation(testHeader, "enum Color_Enum : int32_t;");
    assertTranslation(testSource, "#include \"Color.h\"");
  }

  public void testEnumLocalForwardDeclaration() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; import"
            + " com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; public class"
            + " EnumMethods {  public enum SomeInnerEnum { ONE, TWO }  "
            + " @ObjectiveCAdapterMethod(selector=\"one\","
            + " adaptations={Adaptation.RETURN_NATIVE_ENUMS})  public SomeInnerEnum getOne() {    "
            + " return SomeInnerEnum.ONE;   }}",
        "EnumMethods.java");

    String testHeader = translateSourceFile("EnumMethods", "EnumMethods.h");
    assertTranslation(testHeader, "enum EnumMethods_SomeInnerEnum_Enum : int32_t;");
  }

  public void testEnumAnnotation() throws IOException {
    addSourceFile("public enum Color { RED, WHITE, BLUE };", "Color.java");

    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; import"
            + " com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; public class"
            + " EnumMethods {  @ObjectiveCAdapterMethod(selector=\"blue\","
            + " adaptations={Adaptation.RETURN_NATIVE_ENUMS})  public Color getBlue() {     return"
            + " Color.BLUE;   }  @ObjectiveCAdapterMethod(selector=\"red\","
            + " adaptations={Adaptation.RETURN_NATIVE_ENUMS})  static public Color getRed() {    "
            + " return Color.RED;   }  @ObjectiveCAdapterMethod(selector=\"isBlue:\","
            + " adaptations={Adaptation.ACCEPT_NATIVE_ENUMS})  public boolean isBlue(Color color) {"
            + "     if (color == Color.BLUE) {       return true;     } else {       return false; "
            + "    }  }  @ObjectiveCAdapterMethod(selector=\"isRed:\","
            + " adaptations={Adaptation.ACCEPT_NATIVE_ENUMS})  static public boolean isRed(Color"
            + " color) {     if (color == Color.RED) {       return true;     } else {       return"
            + " false;     }  }}",
        "EnumMethods.java");

    String testHeader = translateSourceFile("EnumMethods", "EnumMethods.h");
    String testSource = getTranslatedFile("EnumMethods.m");

    assertNoWarnings();
    assertNoErrors();

    assertTranslation(testHeader, "- (enum Color_Enum)blue;");
    assertTranslatedLines(
        testSource, "- (enum Color_Enum)blue {", "  return [[self _getBlue] toNSEnum];", "}");

    assertTranslation(testHeader, "+ (enum Color_Enum)red;");
    assertTranslatedLines(
        testSource, "+ (enum Color_Enum)red {", "  return [[EnumMethods _getRed] toNSEnum];", "}");

    assertTranslation(testHeader, "- (bool)isBlue:(enum Color_Enum)color;");
    assertTranslatedLines(
        testSource,
        "- (bool)isBlue:(enum Color_Enum)color {",
        "  return [self _isBlueWithColor:[Color fromNSEnum:color]];",
        "}");

    assertTranslation(testHeader, "+ (bool)isRed:(enum Color_Enum)color;");
    assertTranslatedLines(
        testSource,
        "+ (bool)isRed:(enum Color_Enum)color {",
        "  return [EnumMethods _isRedWithColor:[Color fromNSEnum:color]];",
        "}");
  }

  public void testInappropriateProtocolReturnWarning() throws IOException {
    addSourceFile("public class A {}", "A.java");
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; import"
            + " com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; public class B { "
            + " @ObjectiveCAdapterMethod(selector=\"getA\","
            + " adaptations={Adaptation.RETURN_ADAPTER_PROTOCOLS})  public A getAnA() {    return"
            + " new A();   }}",
        "B.java");
    @SuppressWarnings("unused")
    String testAHeader = translateSourceFile("A", "A.h");
    @SuppressWarnings("unused")
    String testASource = getTranslatedFile("A.m");
    @SuppressWarnings("unused")
    String testBHeader = translateSourceFile("B", "B.h");
    @SuppressWarnings("unused")
    String testBSource = getTranslatedFile("B.m");

    assertWarning(
        "ObjectiveCAdapterMethod protocol return type adaptation used on a return type not"
            + " annotated with ObjectiveCAdapterProtocol.");
  }

  public void testProtocolReturnAnnotation() throws IOException {
    options.setAsObjCGenericDecl(true);

    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCNativeProtocol; "
            + "import com.google.j2objc.annotations.ObjectiveCAdapterProtocol; "
            + "@ObjectiveCNativeProtocol(name=\"NativeProtocol\") "
            + "@ObjectiveCAdapterProtocol(\"NativeProtocol\") "
            + "public class A {}",
        "A.java");

    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; import"
            + " com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; import"
            + " java.util.ArrayList; import java.util.List; public class B {  "
            + " @ObjectiveCAdapterMethod(selector=\"getA\","
            + " adaptations={Adaptation.RETURN_ADAPTER_PROTOCOLS})   public A getAnA() {     return"
            + " new A();   }  @ObjectiveCAdapterMethod(selector=\"getMoreA\","
            + " adaptations={Adaptation.RETURN_ADAPTER_PROTOCOLS,"
            + " Adaptation.RETURN_LISTS_AS_NATIVE_ARRAYS})   public List<A> getABunchOfA() {    "
            + " return new ArrayList<A>();   } }",
        "B.java");

    @SuppressWarnings("unused")
    String testAHeader = translateSourceFile("A", "A.h");
    @SuppressWarnings("unused")
    String testASource = getTranslatedFile("A.m");
    @SuppressWarnings("unused")
    String testBHeader = translateSourceFile("B", "B.h");
    @SuppressWarnings("unused")
    String testBSource = getTranslatedFile("B.m");

    assertNoWarnings();
    assertNoErrors();

    assertTranslation(testBHeader, "- (id<NativeProtocol>)getA;");
    assertTranslation(testBHeader, "- (NSArray<id<NativeProtocol>> *)getMoreA;");

    // No need to test source, no cast is inserted (or needed).
  }

  public void testInappropriateListReturnWarning() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; import"
            + " com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; public class"
            + " NoListMethod {  @ObjectiveCAdapterMethod(selector=\"getInt\","
            + " adaptations={Adaptation.RETURN_LISTS_AS_NATIVE_ARRAYS})  public Integer getInt() { "
            + "    return 42;   }}",
        "NoListMethod.java");
    @SuppressWarnings("unused")
    String testHeader = translateSourceFile("NoListMethod", "NoListMethod.h");
    assertWarning(
        "ObjectiveCAdapterMethod array return type adaptation used on a non-list return type.");
  }

  public void testListReturnAnnotation() throws IOException {
    options.setAsObjCGenericDecl(true);

    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCAdapterMethod; import"
            + " com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation; import"
            + " java.util.ArrayList; import java.util.List; public class A {  "
            + " @ObjectiveCAdapterMethod(selector=\"plainList\","
            + " adaptations={Adaptation.RETURN_LISTS_AS_NATIVE_ARRAYS})   public List"
            + " getPlainList() {     return new ArrayList();   }  "
            + " @ObjectiveCAdapterMethod(selector=\"stringList\","
            + " adaptations={Adaptation.RETURN_LISTS_AS_NATIVE_ARRAYS})   public List<String>"
            + " getStringList() {     return new ArrayList<String>();   }  "
            + " @ObjectiveCAdapterMethod(selector=\"arrayList\","
            + " adaptations={Adaptation.RETURN_LISTS_AS_NATIVE_ARRAYS})   public ArrayList"
            + " getArrayList() {     return new ArrayList();   }  "
            + " @ObjectiveCAdapterMethod(selector=\"stringArrayList\","
            + " adaptations={Adaptation.RETURN_LISTS_AS_NATIVE_ARRAYS})   public ArrayList<String>"
            + " getStringArrayList() {     return new ArrayList<String>();   } }",
        "A.java");

    String testHeader = translateSourceFile("A", "A.h");
    String testSource = getTranslatedFile("A.m");

    assertNoWarnings();
    assertNoErrors();

    assertTranslation(testHeader, "- (NSArray *)plainList");
    assertTranslatedLines(
        testSource,
        "- (NSArray *)plainList {",
        "  return JREAdaptedArrayFromJavaList([self _getPlainList]);",
        "}");

    assertTranslation(testHeader, "- (NSArray<NSString *> *)stringList");
    assertTranslatedLines(
        testSource,
        "- (NSArray *)stringList {",
        "  return JREAdaptedArrayFromJavaList([self _getStringList]);",
        "}");

    assertTranslation(testHeader, "- (NSArray *)arrayList");
    assertTranslatedLines(
        testSource,
        "- (NSArray *)arrayList {",
        "  return JREAdaptedArrayFromJavaList([self _getArrayList]);",
        "}");

    assertTranslation(testHeader, "- (NSArray<NSString *> *)stringArrayList");
    assertTranslatedLines(
        testSource,
        "- (NSArray *)stringArrayList {",
        "  return JREAdaptedArrayFromJavaList([self _getStringArrayList]);",
        "}");
  }

  public void testThrowsAsErrorSingleArgNaming() throws IOException {
    addSourceFile(
        "import com.google.j2kt.annotations.Throws; "
            + "public class ErrorMethod {"
            + "  @Throws"
            + "  public void doSomething() { "
            + "  }"
            + "}",
        "ErrorMethod.java");

    String testHeader = translateSourceFile("ErrorMethod", "ErrorMethod.h");
    String testSource = getTranslatedFile("ErrorMethod.m");
    assertNoWarnings();
    assertNoErrors();

    assertTranslation(testHeader, "- (bool)doSomethingAndReturnError:(NSError **)error;");
    assertTranslation(testSource, "- (bool)doSomethingAndReturnError:(NSError **)error {");
  }

  public void testThrowsAsErrorMultiArgNaming() throws IOException {
    addSourceFile(
        "import com.google.j2kt.annotations.Throws; "
            + "public class ErrorMethod {"
            + "  @Throws"
            + "  public void doSomething(int somenumber) { "
            + "  }"
            + "}",
        "ErrorMethod.java");

    @SuppressWarnings("unused")
    String testHeader = translateSourceFile("ErrorMethod", "ErrorMethod.h");
    @SuppressWarnings("unused")
    String testSource = getTranslatedFile("ErrorMethod.m");

    assertNoWarnings();
    assertNoErrors();

    assertTranslatedLines(
        testHeader, "- (bool)doSomethingWithInt:(int32_t)somenumber", "error:(NSError **)error;");
    assertTranslatedLines(
        testSource,
        "- (bool)doSomethingWithInt:(int32_t)somenumber",
        "error:(NSError **)error {",
        "@try {",
        "[self doSomethingWithInt:somenumber];",
        "return YES;",
        "} @catch (NSException *e) {",
        "if (error) { *error = JREErrorFromException(e); }",
        "return NO;",
        "}",
        "}");
  }
}
