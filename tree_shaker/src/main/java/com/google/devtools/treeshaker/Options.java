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

package com.google.devtools.treeshaker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.devtools.j2objc.util.SourceVersion;
import com.google.devtools.j2objc.util.Version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

class Options {

  private static final String XBOOTCLASSPATH = "-Xbootclasspath:";
  private static String usageMessage;
  private static String helpMessage;
  private static File publicRootSetFile = null;

  static {
    // Load string resources.
    URL propertiesUrl = Resources.getResource(TreeShaker.class, "TreeShaker.properties");
    Properties properties = new Properties();
    try {
      properties.load(propertiesUrl.openStream());
    } catch (IOException e) {
      System.err.println("unable to access tool properties: " + e);
      System.exit(1);
    }
    usageMessage = properties.getProperty("usage-message");
    Preconditions.checkNotNull(usageMessage);
    helpMessage = properties.getProperty("help-message");
    Preconditions.checkNotNull(helpMessage);
  }

  private String sourcepath;
  private String classpath;
  private String bootclasspath;
  private List<String> sourceFiles = Lists.newArrayList();
  private String fileEncoding = System.getProperty("file.encoding", "UTF-8");
  private boolean treatWarningsAsErrors = false;

  // The default source version number if not passed with -source is determined from the system
  // properties of the running java version after parsing the argument list.
  private SourceVersion sourceVersion = null;

  public List<String> getSourceFiles() {
    return sourceFiles;
  }

  public void setSourceFiles(List<String> files) {
    this.sourceFiles = files;
  }

  public String getSourcepath() {
    return sourcepath;
  }

  public String getClasspath() {
    return classpath;
  }

  public void setClasspath(String classpath) {
    this.classpath = classpath;
  }

  public String getBootclasspath() {
    return bootclasspath != null ? bootclasspath : System.getProperty("sun.boot.class.path");
  }

  public boolean treatWarningsAsErrors() {
    return treatWarningsAsErrors;
  }

  private void addManifest(String manifestFile) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(new File(manifestFile)));
    try {
      for (String line = in.readLine(); line != null; line = in.readLine()) {
        if (!Strings.isNullOrEmpty(line)) {
          sourceFiles.add(line.trim());
        }
      }
    } finally {
      in.close();
    }
  }

  public String fileEncoding() {
    return fileEncoding;
  }

  public SourceVersion sourceVersion() {
    if (sourceVersion == null) {
      sourceVersion = SourceVersion.defaultVersion();
    }
    return sourceVersion;
  }

  @VisibleForTesting
  void setSourceVersion(SourceVersion sv) {
      sourceVersion = sv;
  }

  public static void usage(String invalidUseMsg) {
    System.err.println("tree_shaker: " + invalidUseMsg);
    System.err.println(usageMessage);
    System.exit(1);
  }

  public static void help(boolean errorExit) {
    System.err.println(helpMessage);
    // javac exits with 2, but any non-zero value works.
    System.exit(errorExit ? 2 : 0);
  }

  public static void version() {
    System.err.println("tree_shaker " + Version.jarVersion(Options.class));
    System.exit(0);
  }

  public static Options parse(String[] args) throws IOException {
    Options options = new Options();

    int nArg = 0;
    while (nArg < args.length) {
      String arg = args[nArg];
      if (arg.equals("-sourcepath")) {
        if (++nArg == args.length) {
          usage("-sourcepath requires an argument");
        }
        options.sourcepath = args[nArg];
      } else if (arg.equals("-classpath")) {
        if (++nArg == args.length) {
          usage("-classpath requires an argument");
        }
        options.classpath = args[nArg];
      } else if (arg.equals("--sourcefilelist") || arg.equals("-s")) {
        if (++nArg == args.length) {
          usage("--sourcefilelist requires an argument");
        }
        options.addManifest(args[nArg]);
      } else if (arg.equals("--tree-shaker-roots")) {
        if (++nArg == args.length) {
          usage("--tree-shaker-roots");
        }
        publicRootSetFile = new File(args[nArg]);
      //TODO(malvania): Enable the bootclasspath option when we have a class file AST
      //                parser that can use class jars.
      } else if (arg.startsWith(XBOOTCLASSPATH)) {
        options.bootclasspath = arg.substring(XBOOTCLASSPATH.length());
      } else if (arg.equals("-encoding")) {
        if (++nArg == args.length) {
          usage("-encoding requires an argument");
        }
        options.fileEncoding = args[nArg];
      }  else if (arg.equals("-source")) {
        if (++nArg == args.length) {
          usage("-source requires an argument");
        }
        try {
          options.sourceVersion = SourceVersion.parse(args[nArg]);
        } catch (IllegalArgumentException e) {
          usage("invalid source release: " + args[nArg]);
        }
      } else if (arg.equals("-version")) {
        version();
      } else if (arg.equals("-Werror")) {
        options.treatWarningsAsErrors = true;
      } else if (arg.startsWith("-h") || arg.equals("--help")) {
        help(false);
      } else if (arg.startsWith("-")) {
        usage("invalid flag: " + arg);
      } else {
        break;
      }
      ++nArg;
    }

    while (nArg < args.length) {
      options.sourceFiles.add(args[nArg++]);
    }
    if (options.sourceFiles.isEmpty()) {
      usage("no source files");
    }

    return options;
  }

  public File getPublicRootSetFile() {
    return publicRootSetFile;
  }
}
