package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.GenerationTest;
import java.io.IOException;

/**
 * Unit tests for {@link LogSiteInjector} class.
 *
 * @author Tom Ball
 */
public class LogSiteInjectorTest extends GenerationTest {

  // Verify Logger.log(level, ...) statements are translated to
  // logp(level, sourceClass, sourceMethod, ...) methods.
  public void testLog() throws IOException {
    options.setInjectLogSites(true);
    String source =
        String.join(
            "\n",
            "package test;",
            "import java.util.logging.Logger;",
            "import java.util.logging.Level;",
            "public class Hello{",
            "  private static final Logger logger = Logger.getLogger(Hello.class.getName());",
            "  public static void f(Throwable t, String msg, Object arg, Object... args) {",
            "    logger.log(Level.INFO, msg);",
            "    logger.log(Level.INFO, msg, arg);",
            "    logger.log(Level.INFO, msg, args);",
            "    logger.log(Level.INFO, msg, t);",
            "  }",
            "}");
    String translation = translateSourceFile(source, "test.Hello", "test/Hello.m");

    // logger.log(level, msg): first invocation nil-tests logger.
    assertTranslation(
        translation,
        "[((JavaUtilLoggingLogger *) nil_chk(TestHello_logger)) "
            + "logpWithJavaUtilLoggingLevel:JreLoadStatic(JavaUtilLoggingLevel, INFO) "
            + "withNSString:@\"test.Hello\" "
            + "withNSString:@\"f\" withNSString:msg];");

    // logger.log(level, msg, arg)
    assertTranslation(
        translation,
        "[TestHello_logger "
            + "logpWithJavaUtilLoggingLevel:JreLoadStatic(JavaUtilLoggingLevel, INFO) "
            + "withNSString:@\"test.Hello\" "
            + "withNSString:@\"f\" "
            + "withNSString:msg withId:arg];");

    // logger.log(level, msg, varargs)
    assertTranslation(
        translation,
        "[TestHello_logger "
            + "logpWithJavaUtilLoggingLevel:JreLoadStatic(JavaUtilLoggingLevel, INFO) "
            + "withNSString:@\"test.Hello\" "
            + "withNSString:@\"f\" "
            + "withNSString:msg withNSObjectArray:args];");

    // logger.log(level, msg, throwable)
    assertTranslation(
        translation,
        "[TestHello_logger "
            + "logpWithJavaUtilLoggingLevel:JreLoadStatic(JavaUtilLoggingLevel, INFO) "
            + "withNSString:@\"test.Hello\" "
            + "withNSString:@\"f\" "
            + "withNSString:msg "
            + "withJavaLangThrowable:t];");
  }

  // Verify log.logRecord() isn't modified.
  public void testLogRecord() throws IOException {
    options.setInjectLogSites(true);
    String source =
        String.join(
            "\n",
            "package test;",
            "import java.util.logging.Logger;",
            "import java.util.logging.Level;",
            "import java.util.logging.LogRecord;",
            "public class Hello2 {",
            "  private static final Logger logger = Logger.getLogger(Hello2.class.getName());",
            "  public static void f(LogRecord r) {",
            "    logger.log(r);",
            "  }",
            "}");
    String translation = translateSourceFile(source, "test.Hello2", "test/Hello2.m");
    assertTranslation(
        translation,
        "[((JavaUtilLoggingLogger *) nil_chk(TestHello2_logger)) "
            + "logWithJavaUtilLoggingLogRecord:r];");
  }

  // Verify call site injection into Logger convenience methods, like log.info().
  public void testConvenienceMethods() throws IOException {
    options.setInjectLogSites(true);
    String source =
        String.join(
            "\n",
            "package test;"
                + "import java.util.logging.Logger;"
                + "public class Hello3 {"
                + "  private static final Logger logger=Logger.getLogger(Hello3.class.getName());"
                + "  public static void f(Throwable t, String msg, Object arg, Object... args) {"
                + "    logger.finest(msg);"
                + "    logger.fine(msg);"
                + "    logger.finer(msg);"
                + "    logger.severe(msg);"
                + "    logger.warning(msg);"
                + "    logger.config(msg);"
                + "    logger.info(msg);"
                + "  }"
                + "}");
    String translation = translateSourceFile(source, "test.Hello3", "test/Hello3.m");
    assertTranslatedLines(
        translation,
        "[((JavaUtilLoggingLogger *) nil_chk(TestHello3_logger)) "
            + "logpWithJavaUtilLoggingLevel:JreLoadStatic(JavaUtilLoggingLevel, FINEST) "
            + "withNSString:@\"test.Hello3\" withNSString:@\"f\" withNSString:msg];",
        "[TestHello3_logger "
            + "logpWithJavaUtilLoggingLevel:JreLoadStatic(JavaUtilLoggingLevel, FINE) "
            + "withNSString:@\"test.Hello3\" withNSString:@\"f\" withNSString:msg];",
        "[TestHello3_logger "
            + "logpWithJavaUtilLoggingLevel:JreLoadStatic(JavaUtilLoggingLevel, FINER) "
            + "withNSString:@\"test.Hello3\" withNSString:@\"f\" withNSString:msg];",
        "[TestHello3_logger "
            + "logpWithJavaUtilLoggingLevel:JreLoadStatic(JavaUtilLoggingLevel, SEVERE) "
            + "withNSString:@\"test.Hello3\" withNSString:@\"f\" withNSString:msg];",
        "[TestHello3_logger "
            + "logpWithJavaUtilLoggingLevel:JreLoadStatic(JavaUtilLoggingLevel, WARNING) "
            + "withNSString:@\"test.Hello3\" withNSString:@\"f\" withNSString:msg];",
        "[TestHello3_logger "
            + "logpWithJavaUtilLoggingLevel:JreLoadStatic(JavaUtilLoggingLevel, CONFIG) "
            + "withNSString:@\"test.Hello3\" withNSString:@\"f\" withNSString:msg];",
        "[TestHello3_logger "
            + "logpWithJavaUtilLoggingLevel:JreLoadStatic(JavaUtilLoggingLevel, INFO) "
            + "withNSString:@\"test.Hello3\" withNSString:@\"f\" withNSString:msg];");
  }

  public void testGoogleLogger() throws IOException {
    options.setInjectLogSites(true);
    String source = String.join("\n",
        "package test;",
        "import com.google.common.flogger.GoogleLogger;",
        "public class HelloFlogger {",
        "  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();",
        "  public static void f(String argument) {",
        "    logger.atInfo().log(\"Log message with formatted: %s\", argument);",
        "  }",
        "}");
    String translation = translateSourceFile(
        source, "test.HelloFlogger", "test/HelloFlogger.m");

    // Verify forEnclosingClass() is replaced with injected class name.
    assertTranslation(
        translation,
        "JreStrongAssign(&TestHelloFlogger_logger, "
            + "ComGoogleCommonFloggerGoogleLogger_forInjectedClassNameWithNSString_("
            + "@\"test.HelloFlogger\"));");

    // Verify logger.atInfo().log(...) is replaced with injected log site:
    // logger.atInfo().withInjectedLogSite("test/HelloFlogger", "f", 6, "HelloFlogger.java")
    //     .log(...);
    assertTranslation(
        translation,
        // All of these casts are also generated by javac.
        "[((id<ComGoogleCommonFloggerGoogleLogger_Api>) "
            + "nil_chk([((id<ComGoogleCommonFloggerGoogleLogger_Api>) "
            + "nil_chk([((ComGoogleCommonFloggerGoogleLogger *) "
            + "nil_chk(TestHelloFlogger_logger)) atInfo])) "
            + "withInjectedLogSiteWithNSString:@\"test.HelloFlogger\" "
            + "withNSString:@\"f\" withInt:6 withNSString:@\"HelloFlogger.java\"])) "
            + "logWithNSString:@\"Log message with formatted: %s\" withId:argument];");

  }

  // Verify custom log method isn't modified.
  public void testCustomLogger() throws IOException {
    options.setInjectLogSites(true);
    String source =
        String.join(
            "\n",
            "package test;"
                + "import java.util.logging.Logger;"
                + "public class Hello4 {"
                + "  static class MyLogger extends Logger {"
                + "    protected MyLogger(String name, String resourceBundleName) {"
                + "      super(name, resourceBundleName);"
                + "    }"
                + "    public void log() {}"
                + "  }"
                + "  public static void f(MyLogger l) {"
                + "    l.log();"
                + "  }"
                + "}");
    String translation = translateSourceFile(source, "test.Hello4", "test/Hello4.m");
    assertTranslation(translation, "[((TestHello4_MyLogger *) nil_chk(l)) log];");
  }

  public void testInnerClass() throws IOException {
    options.setInjectLogSites(true);
    String source =
        String.join(
            "\n",
            "package test;"
                + "import java.util.logging.Logger;"
                + "import java.util.logging.Level;"
                + "public class Hello5 {"
                + "  private static final Logger logger = Logger.getLogger(Hello5.class.getName());"
                + "  public static class Inner {"
                + "    public static void f(String msg) {"
                + "      logger.info(msg);"
                + "    }"
                + "  }"
                + "}");
    String translation = translateSourceFile(source, "test.Hello5", "test/Hello5.m");
    assertTranslation(
        translation,
        "[((JavaUtilLoggingLogger *) nil_chk(JreLoadStatic(TestHello5, logger))) "
            + "logpWithJavaUtilLoggingLevel:JreLoadStatic(JavaUtilLoggingLevel, INFO) "
            + "withNSString:@\"test.Hello5.Inner\" withNSString:@\"f\" withNSString:msg];");
  }
}
