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

package com.google.devtools.j2objc.regression;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

/**
 * Basic framework for working with Eclipse regression tests. Eclipse has built a very large set of
 * user driven test data, which if we leverage for new features allows us to quickly identify areas
 * where we have bugs, features we are not supporting, and, if we look back from the tests to the
 * original bug descriptions, an idea of how users are using tested features. A good example of this
 * is intersection types, which are currently hard to track down examples for, but are covered well
 * in LambdaExpressionsTest.
 * <p>
 * For testing current features this will probably not be as useful, as it will be easy to get lost
 * fixing bug minutia rather than implementing used features, and the model of j2objc has been to
 * let our users be our bumpers, and point out which features they need, allowing us to only focus
 * on the used parts of the language, rather than trying to match the JLS directly for
 * completeness.
 * <p>
 * We can find Eclipse's repository of regression tests here:
 * <p>
 * https://github.com/eclipse/eclipse.jdt.core/tree/master/org.eclipse.jdt.core.tests.compiler/src/org/eclipse/jdt/core/tests/compiler/regression
 */
public abstract class AbstractRegressionTest extends GenerationTest {

  private static final String J2OBJCC_LOCATION =
      System.getProperty("j2objcc.path", "../dist/j2objcc");

  private String writeFileFromString(String filename, String content) {
    PrintWriter out;
    File file = new File(tempDir, filename);
    try {
      new File(file.getParent()).mkdirs();
      out = new PrintWriter(file, "UTF-8");
      out.print(content);
      out.close();
    } catch (IOException e) {
      fail("Unable to write file " + filename);
    }
    return file.getAbsolutePath();
  }

  private List<String> writeFiles(String[] ls) {
    List<String> fileArgs = Lists.newArrayListWithCapacity(ls.length / 2);
    for (int i = 0; i < ls.length; i += 2) {
      fileArgs.add(writeFileFromString(ls[i], ls[i + 1]));
    }
    return fileArgs;
  }

  /**
   * Takes a list of .java files and returns a list with the .java extension replaced with .m.
   */
  private static List<String> getImplementationFileList(List<String> fileArgs) {
    List<String> mFileArgs = Lists.newArrayListWithCapacity(fileArgs.size());
    for (String s : fileArgs) {
      String newString = s.substring(0, s.lastIndexOf('/'));
      newString += s.substring(s.lastIndexOf('/'), s.lastIndexOf('.')) + ".m";
      mFileArgs.add(newString);
    }
    return mFileArgs;
  }

  /**
   * Takes a .java file name and possibly path and returns the name of the represented class.
   */
  private static String className(String path) {
    return NameTable.camelCasePath(path.substring(0, path.lastIndexOf('.')));
  }

  private static String runCommand(String command) {
    Runtime rt = Runtime.getRuntime();
    Process process;
    try {
      process = rt.exec(command);
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      StringBuilder buffer = new StringBuilder();
      int c;
      while ((c = stdInput.read()) != -1) {
        buffer.append((char) c);
      }
      while ((c = stdError.read()) != -1) {
        buffer.append((char) c);
      }
      stdInput.close();
      stdError.close();
      return buffer.toString();
    } catch (IOException e) {
      return null;
    }
  }

  private static void regressionFail(String methodName, String[] ls, String res, String output) {
    StringBuilder buffer = new StringBuilder();
    buffer.append('\n' + methodName);
    buffer.append("\nExpected:\n");
    buffer.append(res);
    buffer.append("\nReturned:\n");
    if (output == null) {
      buffer.append("***Failed to compile***");
    } else {
      buffer.append(output);
    }
    buffer.append("\n\n");
    for (int i = 0; i < ls.length; i += 2) {
      buffer.append("FILE:\n");
      buffer.append(ls[i]);
      buffer.append('\n');
      buffer.append(ls[i + 1]);
      buffer.append('\n');
    }
    System.out.println("FAIL");
    System.out.println(buffer.toString());
    fail(buffer.toString());
  }

  private static void checkMatch(String methodName, String[] ls, String res, String output) {
    res = res.trim();
    if (output != null) {
      output = output.trim();
    }
    if (!res.equals(output)) {
      regressionFail(methodName, ls, res, output);
    }
  }

  void runConformTest(String[] ls) {
    runConformTest(ls, null);
  }

  void runConformTest(String[] ls, String res) {
    StackTraceElement[] st = Thread.currentThread().getStackTrace();
    String methodName = st[2].getMethodName();
    List<String> fileArgs = writeFiles(ls);

    J2ObjC.run(fileArgs, options);
    if (ErrorUtil.errorCount() > 0) {
      regressionFail(methodName, ls, res, Joiner.on(' ').join(ErrorUtil.getErrorMessages()));
    }
    if (J2OBJCC_LOCATION.isEmpty() || res == null || res.isEmpty()) {
      return;
    }
    List<String> mFileArgs = getImplementationFileList(fileArgs);
    String executable = tempDir + "/regressiontesting ";
    String command = J2OBJCC_LOCATION + " -g -I" + tempDir + " -ObjC -o " + executable
        + Joiner.on(' ').join(mFileArgs);
    String compileOutput = runCommand(command);
    if (compileOutput.contains("error: ")) {
      regressionFail(methodName, ls, res, compileOutput);
    }
    checkMatch(methodName, ls, res, runCommand(executable + className(ls[0])));
    runCommand("rm " + executable);
  }
}
