// Copyright 2013 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.j2objc.testing;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.j2objc.annotations.AutoreleasePool;
import com.google.j2objc.annotations.WeakOuter;

import junit.framework.Test;
import junit.runner.Version;

import org.junit.internal.TextListener;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runners.JUnit4;
import org.junit.runners.Suite;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

/*-[
#include <objc/runtime.h>
]-*/

/**
 * Runs JUnit test classes.
 *
 * Provides a main() function that runs all JUnit tests linked into the executable.
 * The main() function accepts no arguments since Pulse unit tests are not designed to accept
 * arguments. Instead the code expects a file called "JUnitTestRunner.properties" to be include
 * as a resource.
 *
 * Any classes derived from {@link Test} (JUnit 3) or {@link Suite} (JUnit 4) are considered
 * JUnit tests. This behavior can be changed by overriding {@link #isJUnitTestClass},
 * {@link #isJUnit3TestClass} or {@link #isJUnit4TestClass}.
 *
 * @author iroth@google.com (Ian Roth)
 */
public class JUnitTestRunner {

  private static final String PROPERTIES_FILE_NAME = "JUnitTestRunner.properties";

  /**
   * Specifies the output format for tests.
   */
  public enum OutputFormat {
    JUNIT,            // JUnit style output.
    GTM_UNIT_TESTING  // Google Toolkit for Mac unit test output format.
  }

  /**
   * Specifies the sort order for tests.
   */
  public enum SortOrder {
    ALPHABETICAL,  // Sorted alphabetically
    RANDOM         // Sorted randomly (differs with each run)
  }

  /**
   * Specifies whether a pattern includes or excludes test classes.
   */
  public enum TestInclusion {
    INCLUDE,  // Includes test classes matching the pattern
    EXCLUDE   // Excludes test classes matching the pattern
  }

  private final PrintStream out;
  private final Set<String> includePatterns = Sets.newHashSet();
  private final Set<String> excludePatterns = Sets.newHashSet();
  private final Map<String, String> nameMappings = Maps.newHashMap();
  private final Map<String, String> randomNames = Maps.newHashMap();
  private final Random random = new Random(System.currentTimeMillis());
  private OutputFormat outputFormat = OutputFormat.JUNIT;
  private SortOrder sortOrder = SortOrder.ALPHABETICAL;

  public JUnitTestRunner() {
    this(System.out);
  }

  public JUnitTestRunner(PrintStream out) {
    this.out = out;
  }

  public static int main(String[] args) {
    // Create JUnit test runner.
    JUnitTestRunner runner = new JUnitTestRunner();
    runner.loadPropertiesFromResource(PROPERTIES_FILE_NAME);
    return runner.run();
  }

  /**
   * Runs the test classes given in {@param classes}.
   * @returns Zero if all tests pass, non-zero otherwise.
   */
  public static int run(Class[] classes, RunListener listener) {
    JUnitCore junitCore = new JUnitCore();
    junitCore.addListener(listener);
    boolean hasError = false;
    for (@AutoreleasePool Class c : classes) {
      Result result = junitCore.run(c);
      hasError = hasError || !result.wasSuccessful();
    }
    return hasError ? 1 : 0;
  }

  /**
   * Runs the test classes that match settings in {@link #PROPERTIES_FILE_NAME}.
   * @returns Zero if all tests pass, non-zero otherwise.
   */
  public int run() {
    Set<Class> classesSet = getTestClasses();
    Class[] classes = classesSet.toArray(new Class[classesSet.size()]);
    sortClasses(classes, sortOrder);
    RunListener listener = newRunListener(outputFormat);
    return run(classes, listener);
  }

  /**
   * Returns a new {@link RunListener} instance for the given {@param outputFormat}.
   */
  public RunListener newRunListener(OutputFormat outputFormat) {
    switch (outputFormat) {
      case JUNIT:
        out.println("JUnit version " + Version.id());
        return new TextListener(out);
      case GTM_UNIT_TESTING:
        return new GtmUnitTestingTextListener();
      default:
        throw new IllegalArgumentException("outputFormat");
    }
  }

  /**
   * Sorts the classes given in {@param classes} according to {@param sortOrder}.
   */
  public void sortClasses(Class[] classes, final SortOrder sortOrder) {
    Arrays.sort(classes, new Comparator<Class>() {
      public int compare(Class class1, Class class2) {
        String name1 = getSortKey(class1, sortOrder);
        String name2 = getSortKey(class2, sortOrder);
        return name1.compareTo(name2);
      }
    });
  }

  private String replaceAll(String value) {
    for (Map.Entry<String, String> entry : nameMappings.entrySet()) {
      String pattern = entry.getKey();
      String replacement = entry.getValue();
      value = value.replaceAll(pattern, replacement);
    }
    return value;
  }

  private String getSortKey(Class cls, SortOrder sortOrder) {
    String className = cls.getName();
    switch (sortOrder) {
      case ALPHABETICAL:
        return replaceAll(className);
      case RANDOM:
        String sortKey = randomNames.get(className);
        if (sortKey == null) {
          sortKey = Integer.toString(random.nextInt());
          randomNames.put(className, sortKey);
        }
        return sortKey;
      default:
        throw new IllegalArgumentException("sortOrder");
    }
  }

  /*-[
  // Returns true if |cls| conforms to the NSObject protocol.
  BOOL IsNSObjectClass(Class cls) {
    while (cls != nil) {
      if (class_conformsToProtocol(cls, @protocol(NSObject))) {
        return YES;
      }
      // class_conformsToProtocol() does not examine superclasses.
      cls = class_getSuperclass(cls);
    }
    return NO;
  }
  ]-*/

  /**
   * Returns the set of all loaded JUnit test classes.
   */
  private native Set<Class> getAllTestClasses() /*-[
    int classCount = objc_getClassList(NULL, 0);
    Class *classes = (Class *)malloc(classCount * sizeof(Class));
    objc_getClassList(classes, classCount);
    id<JavaUtilSet> result = [ComGoogleCommonCollectSets newHashSet];
    for (int i = 0; i < classCount; i++) {
      @try {
        Class cls = classes[i];
        if (IsNSObjectClass(cls)) {
          IOSClass *javaClass = IOSClass_fromClass(cls);
          if ([self isJUnitTestClassWithIOSClass:javaClass]) {
            [result addWithId:javaClass];
          }
        }
      }
      @catch (JavaLangThrowable *t) {
        // Ignore any exceptions thrown by class initialization.
      }
    }
    free(classes);
    return result;
  ]-*/;

  /**
   * @return true if {@param cls} is either a JUnit 3 or JUnit 4 test.
   */
  protected boolean isJUnitTestClass(Class cls) {
    return isJUnit3TestClass(cls) || isJUnit4TestClass(cls);
  }

  /**
   * @return true if {@param cls} derives from {@link Test} and is not part of the
   * {@link junit.framework} package.
   */
  protected boolean isJUnit3TestClass(Class cls) {
    if (Test.class.isAssignableFrom(cls)) {
      String packageName = getPackageName(cls);
      return !packageName.startsWith("junit.framework")
          && !packageName.startsWith("junit.extensions");
    }
    return false;
  }

  /**
   * @return true if {@param cls} is {@link JUnit4} annotated.
   */
  protected boolean isJUnit4TestClass(Class cls) {
    // Need to find test classes, otherwise crashes with b/11790448.
    if (!cls.getName().endsWith("Test")) {
      return false;
    }
    // Check the annotations.
    Annotation annotation = cls.getAnnotation(RunWith.class);
    if (annotation != null) {
      RunWith runWith = (RunWith) annotation;
      Object value = runWith.value();
      if (value.equals(JUnit4.class) || value.equals(Suite.class)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the name of a class's package or "" for the default package
   * or (for Foundation classes) no package object.
   */
  private String getPackageName(Class cls) {
    Package pkg = cls.getPackage();
    return pkg != null ? pkg.getName() : "";
  }

  /**
   * Returns the set of test classes that match settings in {@link #PROPERTIES_FILE_NAME}.
   */
  private Set<Class> getTestClasses() {
    Set<Class> allTestClasses = getAllTestClasses();
    Set<Class> includedClasses = Sets.newHashSet();

    if (includePatterns.isEmpty()) {
      // Include all tests if no include patterns specified.
      includedClasses = allTestClasses;
    } else {
      // Search all tests for tests to include.
      for (Class testClass : allTestClasses) {
        for (String includePattern : includePatterns) {
          if (matchesPattern(testClass, includePattern)) {
            includedClasses.add(testClass);
            break;
          }
        }
      }
    }

    // Search included tests for tests to exclude.
    Iterator<Class> includedClassesIterator = includedClasses.iterator();
    while (includedClassesIterator.hasNext()) {
      Class testClass = includedClassesIterator.next();
      for (String excludePattern : excludePatterns) {
        if (matchesPattern(testClass, excludePattern)) {
          includedClassesIterator.remove();
          break;
        }
      }
    }

    return includedClasses;
  }

  private boolean matchesPattern(Class testClass, String pattern) {
    return testClass.getCanonicalName().contains(pattern);
  }

  private void loadProperties(InputStream stream) {
    Properties properties = new Properties();
    try {
      properties.load(stream);
    } catch (IOException e) {
      onError(e);
    }
    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    Set<String> propertyNames = properties.stringPropertyNames();
    for (String key : propertyNames) {
      String value = properties.getProperty(key);
      try {
        if (key.equals("outputFormat")) {
          outputFormat = OutputFormat.valueOf(value);
        } else if (key.equals("sortOrder")) {
          sortOrder = SortOrder.valueOf(value);
        } else if (value.equals(TestInclusion.INCLUDE.name())) {
          includePatterns.add(key);
        } else if (value.equals(TestInclusion.EXCLUDE.name())) {
          excludePatterns.add(key);
        } else {
          nameMappings.put(key, value);
        }
      } catch (IllegalArgumentException e) {
        onError(e);
      }
    }
  }

  private void loadPropertiesFromResource(String resourcePath) {
    try {
      InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
      if (stream != null) {
        loadProperties(stream);
      } else {
        throw new IOException(String.format("Resource not found: %s", resourcePath));
      }
    } catch (Exception e) {
      onError(e);
    }
  }

  private void onError(Exception e) {
    e.printStackTrace(out);
  }

  @WeakOuter
  private class GtmUnitTestingTextListener extends RunListener {

    private int numTests = 0;
    private int numFailures = 0;
    private final int numUnexpected = 0; // Never changes, but required in output.

    private Failure testFailure;
    private double testStartTime;

    @Override
    public void testRunFinished(Result result) throws Exception {
      out.printf("Executed %d tests, with %d failures (%d unexpected)\n", numTests, numFailures,
          numUnexpected);
    }

    @Override
    public void testStarted(Description description) throws Exception {
      numTests++;
      testFailure = null;
      testStartTime = System.currentTimeMillis();
      out.printf("Test Case '-[%s]' started.\n", parseDescription(description));
    }

    @Override
    public void testFinished(Description description) throws Exception {
      double testEndTime = System.currentTimeMillis();
      double elapsedSeconds = 0.001 * (testEndTime - testStartTime);
      String statusMessage = "passed";
      if (testFailure != null) {
        statusMessage = "failed";
        out.print(testFailure.getTrace());
      }
      out.printf("Test Case '-[%s]' %s (%.3f seconds).\n\n",
          parseDescription(description), statusMessage, elapsedSeconds);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
      testFailure = failure;
      numFailures++;
    }

    private String parseDescription(Description description) {
      String displayName = description.getDisplayName();
      int p1 = displayName.indexOf("(");
      int p2 = displayName.indexOf(")");
      if (p1 < 0 || p2 < 0 || p2 <= p1) {
        return displayName;
      }
      String methodName = displayName.substring(0, p1);
      String className = displayName.substring(p1 + 1, p2);
      return replaceAll(className) + " " + methodName;
    }
  }
}
