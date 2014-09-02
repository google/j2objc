/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.google.devtools.j2objc.util.ErrorUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The set of tool properties, initialized by the command-line arguments.
 * This class was extracted from the main class, to make it easier for
 * other classes to access options.
 *
 * @author Tom Ball
 */
public class Options {

  private static List<String> sourcePathEntries = Lists.newArrayList(".");
  private static List<String> classPathEntries = Lists.newArrayList(".");
  private static List<String> pluginPathEntries = Lists.newArrayList();
  private static String pluginOptionString = "";
  private static List<Plugin> plugins = new ArrayList<Plugin>();
  private static File outputDirectory = new File(".");
  private static boolean usePackageDirectories = true;
  private static String implementationSuffix = ".m";
  private static boolean ignoreMissingImports = false;
  private static MemoryManagementOption memoryManagementOption = null;
  private static boolean emitLineDirectives = false;
  private static boolean warningsAsErrors = false;
  private static boolean deprecatedDeclarations = false;
  private static Map<String, String> classMappings = Maps.newLinkedHashMap();
  private static Map<String, String> methodMappings = Maps.newLinkedHashMap();
  private static boolean memoryDebug = false;
  private static boolean generateNativeStubs = false;
  private static boolean stripGwtIncompatible = false;
  private static boolean segmentedHeaders = false;
  private static String fileEncoding = System.getProperty("file.encoding", "UTF-8");
  private static boolean jsniWarnings = true;
  private static boolean buildClosure = false;
  private static boolean stripReflection = false;
  private static boolean extractUnsequencedModifications = false;
  private static boolean docCommentsEnabled = false;
  private static boolean finalMethodsAsFunctions = false;
  // TODO(tball): change default to true once clients had a chance to update their builds.
  private static boolean hidePrivateMembers = false;
  private static int batchTranslateMaximum = 0;

  private static File proGuardUsageFile = null;

  private static final String JRE_MAPPINGS_FILE = "JRE.mappings";
  private static final List<String> mappingFiles = Lists.newArrayList(JRE_MAPPINGS_FILE);

  private static String fileHeader;
  private static final String FILE_HEADER_KEY = "file-header";
  private static String usageMessage;
  private static String helpMessage;
  private static final String USAGE_MSG_KEY = "usage-message";
  private static final String HELP_MSG_KEY = "help-message";
  private static String temporaryDirectory;
  private static final String XBOOTCLASSPATH = "-Xbootclasspath:";
  private static String bootclasspath = System.getProperty("sun.boot.class.path");
  private static Map<String, String> packagePrefixes = Maps.newHashMap();
  private static final String BATCH_PROCESSING_MAX_FLAG = "--batch-translate-max=";

  static {
    // Load string resources.
    URL propertiesUrl = Resources.getResource(J2ObjC.class, "J2ObjC.properties");
    Properties properties = new Properties();
    try {
      properties.load(propertiesUrl.openStream());
    } catch (IOException e) {
      System.err.println("unable to access tool properties: " + e);
      System.exit(1);
    }
    fileHeader = properties.getProperty(FILE_HEADER_KEY);
    Preconditions.checkNotNull(fileHeader);
    usageMessage = properties.getProperty(USAGE_MSG_KEY);
    Preconditions.checkNotNull(usageMessage);
    helpMessage = properties.getProperty(HELP_MSG_KEY);
    Preconditions.checkNotNull(helpMessage);
  }

  public static enum MemoryManagementOption { REFERENCE_COUNTING, GC, ARC }
  private static final MemoryManagementOption DEFAULT_MEMORY_MANAGEMENT_OPTION =
      MemoryManagementOption.REFERENCE_COUNTING;

  /**
   * Set all log handlers in this package with a common level.
   */
  private static void setLogLevel(Level level) {
    Logger.getLogger("com.google.devtools.j2objc").setLevel(level);
  }

  /**
   * Load the options from a command-line, returning the arguments that were
   * not option-related (usually files).  If help is requested or an error is
   * detected, the appropriate status method is invoked and the app terminates.
   * @throws IOException
   */
  public static String[] load(String[] args) throws IOException {
    setLogLevel(Level.INFO);

    // Create a temporary directory as the sourcepath's first entry, so that
    // modified sources will take precedence over regular files.
    sourcePathEntries = Lists.newArrayList();

    int nArg = 0;
    String[] noFiles = new String[0];
    while (nArg < args.length) {
      String arg = args[nArg];
      if (arg.isEmpty()) {
        ++nArg;
        continue;
      }
      if (arg.equals("-classpath")) {
        if (++nArg == args.length) {
          return noFiles;
        }
        classPathEntries = getPathArgument(args[nArg]);
      } else if (arg.equals("-sourcepath")) {
        if (++nArg == args.length) {
          usage("-sourcepath requires an argument");
        }
        sourcePathEntries.addAll(getPathArgument(args[nArg]));
      } else if (arg.equals("-pluginpath")) {
        if (++nArg == args.length) {
          usage("-pluginpath requires an argument");
        }
        pluginPathEntries = getPathArgument(args[nArg]);
      } else if (arg.equals("-pluginoptions")) {
        if (++nArg == args.length){
          usage("-pluginoptions requires an argument");
        }
        pluginOptionString = args[nArg];
      } else if (arg.equals("-d")) {
        if (++nArg == args.length) {
          usage("-d requires an argument");
        }
        outputDirectory = new File(args[nArg]);
      } else if (arg.equals("--mapping")) {
        if (++nArg == args.length) {
          usage("--mapping requires an argument");
        }
        mappingFiles.add(args[nArg]);
      } else if (arg.equals("--dead-code-report")) {
        if (++nArg == args.length) {
          usage("--dead-code-report requires an argument");
        }
        proGuardUsageFile = new File(args[nArg]);
      } else if (arg.equals("--prefix")) {
        if (++nArg == args.length) {
          usage("--prefix requires an argument");
        }
        addPrefixOption(args[nArg]);
      } else if (arg.equals("--prefixes")) {
        if (++nArg == args.length) {
          usage("--prefixes requires an argument");
        }
        addPrefixesFile(args[nArg]);
      } else if (arg.equals("-x")) {
        if (++nArg == args.length) {
          usage("-x requires an argument");
        }
        String s = args[nArg];
        if (s.equals("objective-c")) {
          implementationSuffix = ".m";
        } else if (s.equals("objective-c++")) {
          implementationSuffix = ".mm";
        } else {
          usage("unsupported language: " + s);
        }
      } else if (arg.equals("--ignore-missing-imports")) {
        ignoreMissingImports = true;
      } else if (arg.equals("-use-reference-counting")) {
        checkMemoryManagementOption(MemoryManagementOption.REFERENCE_COUNTING);
      } else if (arg.equals("--no-package-directories")) {
        usePackageDirectories = false;
      } else if (arg.equals("-use-gc")) {
        checkMemoryManagementOption(MemoryManagementOption.GC);
      } else if (arg.equals("-use-arc")) {
        checkMemoryManagementOption(MemoryManagementOption.ARC);
      } else if (arg.equals("-g")) {
        emitLineDirectives = true;
      } else if (arg.equals("-Werror")) {
        warningsAsErrors = true;
      } else if (arg.equals("--generate-deprecated")) {
        deprecatedDeclarations = true;
      } else if (arg.equals("-q") || arg.equals("--quiet")) {
        setLogLevel(Level.WARNING);
      } else if (arg.equals("-t") || arg.equals("--timing-info")) {
        setLogLevel(Level.FINE);
      } else if (arg.equals("-v") || arg.equals("--verbose")) {
        setLogLevel(Level.FINEST);
      } else if (arg.startsWith(XBOOTCLASSPATH)) {
        bootclasspath = arg.substring(XBOOTCLASSPATH.length());
      } else if (arg.equals("-Xno-jsni-delimiters")) {
        // TODO(tball): remove flag when all client builds stop using it.
      } else if (arg.equals("--mem-debug")) {
        memoryDebug = true;
      } else if (arg.equals("--generate-native-stubs")) {
        generateNativeStubs = true;
      } else if (arg.equals("-Xno-jsni-warnings")) {
        jsniWarnings = false;
      } else if (arg.equals("-encoding")) {
        if (++nArg == args.length) {
          usage("-encoding requires an argument");
        }
        fileEncoding = args[nArg];
        try {
          // Verify encoding has a supported charset.
          Charset.forName(fileEncoding);
        } catch (UnsupportedCharsetException e) {
          ErrorUtil.warning(e.getMessage());
        }
      } else if (arg.equals("--strip-gwt-incompatible")) {
        stripGwtIncompatible = true;
      } else if (arg.equals("--strip-reflection")) {
        stripReflection = true;
      } else if (arg.equals("--segmented-headers")) {
        segmentedHeaders = true;
      } else if (arg.equals("--build-closure")) {
        buildClosure = true;
      } else if (arg.equals("--extract-unsequenced")) {
        extractUnsequencedModifications = true;
      } else if (arg.equals("--doc-comments")) {
        docCommentsEnabled = true;
      } else if (arg.startsWith(BATCH_PROCESSING_MAX_FLAG)) {
        batchTranslateMaximum =
            Integer.parseInt(arg.substring(BATCH_PROCESSING_MAX_FLAG.length()));
      } else if (arg.equals("--final-methods-as-functions")) {
        finalMethodsAsFunctions = true;
      } else if (arg.equals("--hide-private-members")) {
        hidePrivateMembers = true;
      } else if (arg.equals("--no-hide-private-members")) {
        hidePrivateMembers = false;
      } else if (arg.startsWith("-h") || arg.equals("--help")) {
        help(false);
      } else if (arg.startsWith("-")) {
        usage("invalid flag: " + arg);
      } else {
        break;
      }
      ++nArg;
    }

    if (memoryManagementOption == null) {
      memoryManagementOption = MemoryManagementOption.REFERENCE_COUNTING;
    }

    int nFiles = args.length - nArg;
    String[] files = new String[nFiles];
    for (int i = 0; i < nFiles; i++) {
      String path = args[i + nArg];
      if (path.endsWith(".jar")) {
        appendSourcePath(path);
      }
      files[i] = path;
    }
    return files;
  }

  /**
   * Add prefix option, which has a format of "<package>=<prefix>".
   */
  private static void addPrefixOption(String arg) {
    int i = arg.indexOf('=');

    // Make sure key and value are at least 1 character.
    if (i < 1 || i >= arg.length() - 1) {
      usage("invalid prefix format");
    }
    String pkg = arg.substring(0, i);
    String prefix = arg.substring(i + 1);
    addPackagePrefix(pkg, prefix);
  }

  /**
   * Add a file map of packages to their respective prefixes, using the
   * Properties file format.
   */
  private static void addPrefixesFile(String filename) throws IOException {
    Properties props = new Properties();
    FileInputStream fis = new FileInputStream(filename);
    props.load(fis);
    fis.close();
    addPrefixProperties(props);
  }

  @VisibleForTesting
  static void addPrefixProperties(Properties props) {
    for (String pkg : props.stringPropertyNames()) {
      addPackagePrefix(pkg, props.getProperty(pkg).trim());
    }
  }

  /**
   * Check that the memory management option wasn't previously set to a
   * different value.  If okay, then set the option.
   */
  private static void checkMemoryManagementOption(MemoryManagementOption option) {
    if (memoryManagementOption != null &&
        memoryManagementOption != option) {
      usage("Multiple memory management options cannot be set.");
    }
    setMemoryManagementOption(option);
  }

  public static void usage(String invalidUseMsg) {
    System.err.println("j2objc: " + invalidUseMsg);
    System.err.println(usageMessage);
    System.exit(1);
  }

  public static void help(boolean errorExit) {
    System.err.println(helpMessage);
    // javac exits with 2, but any non-zero value works.
    System.exit(errorExit ? 2 : 0);
  }

  private static List<String> getPathArgument(String argument) {
    List<String> entries = Lists.newArrayList();
    for (String entry : Splitter.on(File.pathSeparatorChar).split(argument)) {
      if (new File(entry).exists()) {  // JDT fails with bad path entries.
        entries.add(entry);
      } else if (entry.startsWith("~/")) {
        // Expand bash/csh tildes, which don't get expanded by the shell
        // first if in the middle of a path string.
        String expanded = System.getProperty("user.home") + entry.substring(1);
        if (new File(expanded).exists()) {
          entries.add(expanded);
        }
      }
    }
    return entries;
  }

  public static boolean docCommentsEnabled() {
    return docCommentsEnabled;
  }

  @VisibleForTesting
  public static void setDocCommentsEnabled(boolean value) {
    docCommentsEnabled = value;
  }

  @VisibleForTesting
  public static void resetDocComments() {
    docCommentsEnabled = false;
  }

  public static List<String> getSourcePathEntries() {
    return sourcePathEntries;
  }

  public static void appendSourcePath(String entry) {
    sourcePathEntries.add(entry);
  }

  public static void insertSourcePath(int index, String entry) {
    sourcePathEntries.add(index, entry);
  }

  public static List<String> getClassPathEntries() {
    return classPathEntries;
  }

  public static String[] getPluginPathEntries() {
    return pluginPathEntries.toArray(new String[pluginPathEntries.size()]);
  }

  public static String getPluginOptionString() {
    return pluginOptionString;
  }

  public static List<Plugin> getPlugins() {
    return plugins;
  }

  public static File getOutputDirectory() {
    return outputDirectory;
  }

  public static boolean memoryDebug() {
    return memoryDebug;
  }

  public static void setMemoryDebug(boolean value) {
    memoryDebug = value;
  }

  public static boolean generateNativeStubs() {
    return generateNativeStubs;
  }

  public static void setGenerateNativeStubs(boolean value) {
    generateNativeStubs = value;
  }

  /**
   * If true, put output files in sub-directories defined by
   * package declaration (like javac does).
   */
  public static boolean usePackageDirectories() {
    return usePackageDirectories;
  }

  public static void setPackageDirectories(boolean b) {
    usePackageDirectories = b;
  }

  public static String getImplementationFileSuffix() {
    return implementationSuffix;
  }

  public static boolean ignoreMissingImports() {
    return ignoreMissingImports;
  }

  public static boolean useReferenceCounting() {
    return memoryManagementOption == MemoryManagementOption.REFERENCE_COUNTING;
  }

  public static boolean useGC() {
    return memoryManagementOption == MemoryManagementOption.GC;
  }

  public static boolean useARC() {
    return memoryManagementOption == MemoryManagementOption.ARC;
  }

  public static MemoryManagementOption getMemoryManagementOption() {
    return memoryManagementOption;
  }

  // Used by tests.
  public static void setMemoryManagementOption(MemoryManagementOption option) {
    memoryManagementOption = option;
  }

  public static void resetMemoryManagementOption() {
    memoryManagementOption = DEFAULT_MEMORY_MANAGEMENT_OPTION;
  }

  public static boolean emitLineDirectives() {
    return emitLineDirectives;
  }

  public static void setEmitLineDirectives(boolean b) {
    emitLineDirectives = b;
  }

  public static boolean treatWarningsAsErrors() {
    return warningsAsErrors;
  }

  @VisibleForTesting
  public static void enableDeprecatedDeclarations() {
    deprecatedDeclarations = true;
  }

  @VisibleForTesting
  public static void resetDeprecatedDeclarations() {
    deprecatedDeclarations = false;
  }

  public static boolean generateDeprecatedDeclarations() {
    return deprecatedDeclarations;
  }

  public static Map<String, String> getClassMappings() {
    return classMappings;
  }

  public static Map<String, String> getMethodMappings() {
    return methodMappings;
  }

  public static List<String> getMappingFiles() {
    return mappingFiles;
  }

  public static String getUsageMessage() {
    return usageMessage;
  }

  public static String getHelpMessage() {
    return helpMessage;
  }

  public static String getFileHeader() {
    return fileHeader;
  }

  public static File getProGuardUsageFile() {
    return proGuardUsageFile;
  }

  public static List<String> getBootClasspath() {
    return getPathArgument(bootclasspath);
  }

  public static Map<String, String> getPackagePrefixes() {
    return packagePrefixes;
  }

  public static void addPackagePrefix(String pkg, String prefix) {
    packagePrefixes.put(pkg, prefix);
  }

  @VisibleForTesting
  public static void clearPackagePrefixes() {
    packagePrefixes.clear();
  }

  public static String getTemporaryDirectory() throws IOException {
    if (temporaryDirectory != null) {
      return temporaryDirectory;
    }
    File tmpfile = File.createTempFile("j2objc", Long.toString(System.nanoTime()));
    if (!tmpfile.delete()) {
      throw new IOException("Could not delete temp file: " + tmpfile.getAbsolutePath());
    }
    if (!tmpfile.mkdir()) {
      throw new IOException("Could not create temp directory: " + tmpfile.getAbsolutePath());
    }
    temporaryDirectory = tmpfile.getAbsolutePath();
    return temporaryDirectory;
  }

  // Called on exit.  This is done here rather than using File.deleteOnExit(),
  // so the package directories created by the dead-code-eliminator don't have
  // to be tracked.
  public static void deleteTemporaryDirectory() {
    if (temporaryDirectory != null) {
      deleteDir(new File(temporaryDirectory));
      temporaryDirectory = null;
    }
  }

  private static void deleteDir(File dir) {
    for (File f : dir.listFiles()) {
      if (f.isDirectory()) {
        deleteDir(f);
      } else if (f.getName().endsWith(".java")) {
        // Only delete Java files, as other temporary files (like hsperfdata)
        // may also be in tmpdir.
        f.delete();
      }
    }
    dir.delete();  // Will fail if other files in dir, which is fine.
  }

  public static String fileEncoding() {
    return fileEncoding;
  }

  public static Charset getCharset() {
    return Charset.forName(fileEncoding);
  }

  public static boolean stripGwtIncompatibleMethods() {
    return stripGwtIncompatible;
  }

  @VisibleForTesting
  public static void setStripGwtIncompatibleMethods(boolean b) {
    stripGwtIncompatible = b;
  }

  public static boolean generateSegmentedHeaders() {
    return segmentedHeaders;
  }

  @VisibleForTesting
  public static void enableSegmentedHeaders() {
    segmentedHeaders = true;
  }

  @VisibleForTesting
  public static void resetSegmentedHeaders() {
    segmentedHeaders = false;
  }

  public static boolean jsniWarnings() {
    return jsniWarnings;
  }

  public static void setJsniWarnings(boolean b) {
    jsniWarnings = b;
  }

  public static boolean buildClosure() {
    return buildClosure;
  }

  public static boolean stripReflection() {
    return stripReflection;
  }

  @VisibleForTesting
  public static void setStripReflection(boolean b) {
    stripReflection = b;
  }

  public static boolean extractUnsequencedModifications() {
    return extractUnsequencedModifications;
  }

  @VisibleForTesting
  public static void enableExtractUnsequencedModifications() {
    extractUnsequencedModifications = true;
  }

  @VisibleForTesting
  public static void resetExtractUnsequencedModifications() {
    extractUnsequencedModifications = false;
  }

  public static int batchTranslateMaximum() {
    return batchTranslateMaximum;
  }

  public static boolean finalMethodsAsFunctions() {
    return finalMethodsAsFunctions;
  }

  @VisibleForTesting
  public static void enableFinalMethodsAsFunctions() {
    finalMethodsAsFunctions = true;
  }

  @VisibleForTesting
  public static void resetFinalMethodsAsFunctions() {
    finalMethodsAsFunctions = false;
  }

  public static boolean hidePrivateMembers() {
    return hidePrivateMembers;
  }

  @VisibleForTesting
  public static void enableHidePrivateMembers() {
    hidePrivateMembers = true;
  }

  @VisibleForTesting
  public static void resetHidePrivateMembers() {
    hidePrivateMembers = false;
  }

}
