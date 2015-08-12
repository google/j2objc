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
import com.google.common.io.Files;
import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.Options;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
 * on the used parts of the language, rather than trying to match the JLS directly for completeness.
 * <p>
 * We can find Eclipse's repository of regression tests here:
 * https://github.com/eclipse/eclipse.jdt.core/tree/master/org.eclipse.jdt.core.tests.compiler/src/org/eclipse/jdt/core/tests/compiler/regression
 * 
 * @author Seth Kirby
 */
public abstract class AbstractRegressionTest extends GenerationTest {
  String methodName = null;
  public static int testCount = 0;

  static final String j2objccLocation = System.getProperty("j2objcc.path", "j2objcc");
  String oldUserDir = null;
  
  @Override
  protected void setUp() throws IOException {
    tempDir = Files.createTempDir();
    tempDir.mkdirs();
    oldUserDir = System.setProperty("user.dir", tempDir.getAbsolutePath());
    Options.load(new String[] { "-d", tempDir.getAbsolutePath(), "-sourcepath",
        tempDir.getAbsolutePath(), "-q", "-encoding", "UTF-8",
        "-source", "8", // Treat as Java 8 source.
        "-Xforce-incomplete-java8" // Internal flag to force Java 8 support.
    });
    parser = GenerationTest.initializeParser(tempDir);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    System.setProperty("user.dir", oldUserDir);
  }

  public String writeFileFromString(String filename, String content) {
    PrintWriter out;
    File file = new File(tempDir, filename);
    try {
      new File(file.getParent()).mkdirs();
      out = new PrintWriter(file);
      out.print(content);
      out.close();
    } catch (FileNotFoundException e) {
      fail("Unable to write file " + filename);
    }
    return file.getAbsolutePath();
  }

  public List<String> writeFiles(String[] ls) {
    List<String> fileArgs = Lists.newArrayListWithCapacity(ls.length / 2);
    for (int i = 0; i < ls.length; i += 2) {
      fileArgs.add(writeFileFromString(ls[i], ls[i + 1]));
    }
    return fileArgs;
  }

  /**
   * Takes a list of .java files and returns a list with the .java extension replaced with .m.
   */
  public List<String> getImplementationFileList(List<String> fileArgs) {
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
  public String className(String path) {
    int pathIndex = path.lastIndexOf('/');
    int extensionIndex = path.lastIndexOf('.');
    return path.substring(Math.max(0, pathIndex), extensionIndex);
  }

  public String runCommand(String command) {
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
  public void regressionFail(String[] ls, String res, String output, List<String> mFileArgs) {
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
  
  public void checkMatch(String[] ls, String res, String output, List<String> mFileArgs) {
    res = res.trim();
    if (output != null) {
      output = output.trim();
    }
    if (!res.equals(output)) {
      regressionFail(ls, res, output, mFileArgs);
    }
  }

  public void runConformTest(String[] ls) {
    runConformTest(ls, null);
  }

  public void runConformTest(String[] ls, String res) {
    StackTraceElement[] st = Thread.currentThread().getStackTrace();
    methodName = st[2].getMethodName();
    List<String> fileArgs = writeFiles(ls);
    J2ObjC.run(fileArgs);
    List<String> mFileArgs = getImplementationFileList(fileArgs);
    String command = j2objccLocation + " -g -I. -ObjC -o " + tempDir + "/regressiontesting "
        + Joiner.on(' ').join(mFileArgs);
    String compileOutput = runCommand(command);
    if (compileOutput.indexOf("error: ") != -1) {
      regressionFail(ls, res, compileOutput, mFileArgs);
    }
    if (res != null) {
      checkMatch(ls, res, runCommand(tempDir + "/regressiontesting " + className(ls[0])),
          mFileArgs);
      runCommand("rm " + tempDir + "/regressiontesting");
    }
  }

  void runNegativeTest(String[] ls, String res) {
    fail("Negative tests not supported (or needed)");
  }
}
