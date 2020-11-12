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

package com.google.devtools.cyclefinder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.ExternalAnnotations;
import com.google.devtools.j2objc.util.SourceVersion;
import com.google.devtools.j2objc.util.Version;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

class Options {

  private static final String XBOOTCLASSPATH = "-Xbootclasspath:";
  private static String usageMessage;
  private static String helpMessage;

  static {
    // Load string resources.
    URL propertiesUrl = Resources.getResource(CycleFinder.class, "CycleFinder.properties");
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
  private final List<String> suppressListFiles = Lists.newArrayList();
  private final List<String> restrictToListFiles = Lists.newArrayList();
  private List<String> sourceFiles = Lists.newArrayList();
  private String fileEncoding = System.getProperty("file.encoding", "UTF-8");
  private boolean printReferenceGraph = false;
  private SourceVersion sourceVersion = null;
  private final ExternalAnnotations externalAnnotations = new ExternalAnnotations();

  // Flags that are directly forwarded to the javac parser.
  private static final ImmutableSet<String> PLATFORM_MODULE_SYSTEM_OPTIONS =
      ImmutableSet.of("--patch-module", "--system", "--add-reads");
  private final List<String> platformModuleSystemOptions = new ArrayList<>();

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

  public List<String> getSuppressListFiles() {
    return suppressListFiles;
  }

  public void addSuppressListFile(String fileName) {
    suppressListFiles.add(fileName);
  }

  public List<String> getRestrictToFiles() {
    return restrictToListFiles;
  }

  public void addRestrictToFile(String fileName) {
    restrictToListFiles.add(fileName);
  }

  private void addManifest(String manifestFile) throws IOException {
    try (BufferedReader in =
        Files.newReader(new File(manifestFile), Charset.forName(fileEncoding))) {
      for (String line = in.readLine(); line != null; line = in.readLine()) {
        if (!Strings.isNullOrEmpty(line)) {
          sourceFiles.add(line.trim());
        }
      }
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

  public boolean printReferenceGraph() {
    return printReferenceGraph;
  }

  @VisibleForTesting
  public void setPrintReferenceGraph() {
     printReferenceGraph = true;
  }

  public ExternalAnnotations externalAnnotations() {
    return externalAnnotations;
  }

  @VisibleForTesting
  public void addExternalAnnotationFile(String file) throws IOException {
    externalAnnotations.addExternalAnnotationFile(file);
  }

  public void addPlatformModuleSystemOptions(String... flags) {
    Collections.addAll(platformModuleSystemOptions, flags);
  }

  public List<String> getPlatformModuleSystemOptions() {
    return platformModuleSystemOptions;
  }

  public static void usage(String invalidUseMsg) {
    System.err.println("cycle_finder: " + invalidUseMsg);
    System.err.println(usageMessage);
    System.exit(1);
  }

  public static void help(boolean errorExit) {
    System.err.println(helpMessage);
    // javac exits with 2, but any non-zero value works.
    System.exit(errorExit ? 2 : 0);
  }

  public static void version() {
    System.err.println("cycle_finder " + Version.jarVersion(Options.class));
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
      } else if (arg.equals("--suppress-list")
          // Deprecated flag names.
          || arg.equals("--whitelist")
          || arg.equals("-w")) {
        if (++nArg == args.length) {
          usage("--suppress-list requires an argument");
        }
        options.suppressListFiles.add(args[nArg]);
      } else if (arg.equals("--restrict-to")
          // Deprecated flag name.
          || arg.equals("--blacklist")) {
        if (++nArg == args.length) {
          usage("--restrict-to requires an argument");
        }
        options.restrictToListFiles.add(args[nArg]);
      } else if (arg.equals("--sourcefilelist") || arg.equals("-s")) {
        if (++nArg == args.length) {
          usage("--sourcefilelist requires an argument");
        }
        options.addManifest(args[nArg]);
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
          SourceVersion maxVersion = SourceVersion.getMaxSupportedVersion();
          if (options.sourceVersion.version() > maxVersion.version()) {
            ErrorUtil.warning("Java " + options.sourceVersion.version() + " source version is not "
                + "supported, using Java " + maxVersion.version() + ".");
            options.sourceVersion = maxVersion;
          }
        } catch (IllegalArgumentException e) {
          usage("invalid source release: " + args[nArg]);
        }
      } else if (arg.equals("--print-reference-graph")) {
        options.printReferenceGraph = true;
      } else if (arg.equals("-external-annotation-file")) {
        if (++nArg == args.length) {
          usage(arg + " requires an argument");
        }
        options.addExternalAnnotationFile(args[nArg]);
      } else if (PLATFORM_MODULE_SYSTEM_OPTIONS.contains(arg)) {
        String option = arg;
        if (++nArg == args.length) {
          usage(option + " requires an argument");
        }
        options.addPlatformModuleSystemOptions(option, args[nArg]);
      } else if (arg.equals("-version")) {
        version();
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
}
