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
import com.google.devtools.j2objc.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

/**
 * The set of tool properties, initialized by the command-line arguments.
 * This class was extracted from the main class, to make it easier for
 * other classes to access options.
 *
 * @author Tom Ball
 */
public class Options {

  // Using instance fields instead of static fields makes it easier to reset
  // state for unit testing.
  private static Options instance = new Options();

  private List<String> sourcePathEntries = Lists.newArrayList(".");
  private List<String> classPathEntries = Lists.newArrayList(".");
  private File outputDirectory = new File(".");
  private OutputStyleOption outputStyle = OutputStyleOption.PACKAGE;
  private String implementationSuffix = ".m";
  private MemoryManagementOption memoryManagementOption = null;
  private boolean emitLineDirectives = false;
  private boolean warningsAsErrors = false;
  private boolean deprecatedDeclarations = false;
  // Keys are class names, values are header paths (with a .h).
  private Map<String, String> headerMappings = Maps.newLinkedHashMap();
  private File outputHeaderMappingFile = null;
  private Map<String, String> classMappings = Maps.newLinkedHashMap();
  private Map<String, String> methodMappings = Maps.newLinkedHashMap();
  private boolean stripGwtIncompatible = false;
  private boolean segmentedHeaders = false;
  private String fileEncoding = System.getProperty("file.encoding", "UTF-8");
  private boolean jsniWarnings = true;
  private boolean buildClosure = false;
  private boolean stripReflection = false;
  private boolean extractUnsequencedModifications = true;
  private boolean docCommentsEnabled = false;
  private boolean finalMethodsAsFunctions = true;
  private boolean removeClassMethods = false;
  private boolean hidePrivateMembers = true;
  private int batchTranslateMaximum = 0;
  private List<String> headerMappingFiles = null;
  private Map<String, String> packagePrefixes = Maps.newHashMap();

  private static File proGuardUsageFile = null;

  static final String DEFAULT_HEADER_MAPPING_FILE = "mappings.j2objc";
  // Null if not set (means we use the default). Can be empty also (means we use no mapping files).

  private static final String JRE_MAPPINGS_FILE = "JRE.mappings";

  private static String fileHeader;
  private static final String FILE_HEADER_KEY = "file-header";
  private static String usageMessage;
  private static String helpMessage;
  private static final String USAGE_MSG_KEY = "usage-message";
  private static final String HELP_MSG_KEY = "help-message";
  private static String temporaryDirectory;
  private static final String XBOOTCLASSPATH = "-Xbootclasspath:";
  private static String bootclasspath = System.getProperty("sun.boot.class.path");
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

  /**
   * Types of memory management to be used by translated code.
   */
  public static enum MemoryManagementOption { REFERENCE_COUNTING, ARC }

  /**
   * Types of output file generation. Output files are generated in
   * the specified output directory in an optional sub-directory.
   */
  public static enum OutputStyleOption {
    /** Use the class's package, like javac.*/
    PACKAGE,

    /** Use the relative directory of the input file. */
    SOURCE,

    /** Use the relative directory of the input file, even (especially) if it is a jar. */
    SOURCE_COMBINED,

    /** Don't use a relative directory. */
    NONE
  }
  public static final OutputStyleOption DEFAULT_OUTPUT_STYLE_OPTION =
      OutputStyleOption.PACKAGE;

  /**
   * Set all log handlers in this package with a common level.
   */
  private static void setLogLevel(Level level) {
    Logger.getLogger("com.google.devtools.j2objc").setLevel(level);
  }

  public static boolean isVerbose() {
    return Logger.getLogger("com.google.devtools.j2objc").getLevel() == Level.FINEST;
  }

  @VisibleForTesting
  public static void reset() {
    instance = new Options();
  }

  /**
   * Load the options from a command-line, returning the arguments that were
   * not option-related (usually files).  If help is requested or an error is
   * detected, the appropriate status method is invoked and the app terminates.
   * @throws IOException
   */
  public static String[] load(String[] args) throws IOException {
    return instance.loadInternal(args);
  }

  private String[] loadInternal(String[] args) throws IOException {
    setLogLevel(Level.INFO);

    addJreMappings();

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
      } else if (arg.equals("-d")) {
        if (++nArg == args.length) {
          usage("-d requires an argument");
        }
        outputDirectory = new File(args[nArg]);
      } else if (arg.equals("--mapping")) {
        if (++nArg == args.length) {
          usage("--mapping requires an argument");
        }
        addMappingsFiles(args[nArg].split(","));
      } else if (arg.equals("--header-mapping")) {
        if (++nArg == args.length) {
          usage("--header-mapping requires an argument");
        }
        if (args[nArg].isEmpty()) {
          // For when user supplies an empty mapping files list. Otherwise the default will be used.
          headerMappingFiles = Collections.<String>emptyList();
        } else {
          headerMappingFiles = Lists.newArrayList(args[nArg].split(","));
        }
      } else if (arg.equals("--output-header-mapping")) {
        if (++nArg == args.length) {
          usage("--output-header-mapping requires an argument");
        }
        outputHeaderMappingFile = new File(args[nArg]);
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
        ErrorUtil.error("--ignore-missing-imports is no longer supported");
      } else if (arg.equals("-use-reference-counting")) {
        checkMemoryManagementOption(MemoryManagementOption.REFERENCE_COUNTING);
      } else if (arg.equals("--no-package-directories")) {
        outputStyle = OutputStyleOption.NONE;
      } else if (arg.equals("--preserve-full-paths")) {
        outputStyle = OutputStyleOption.SOURCE;
      } else if (arg.equals("-XcombineJars")) {
        outputStyle = OutputStyleOption.SOURCE_COMBINED;
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
      } else if (arg.equals("--no-extract-unsequenced")) {
        extractUnsequencedModifications = false;
      } else if (arg.equals("--doc-comments")) {
        docCommentsEnabled = true;
      } else if (arg.startsWith(BATCH_PROCESSING_MAX_FLAG)) {
        batchTranslateMaximum =
            Integer.parseInt(arg.substring(BATCH_PROCESSING_MAX_FLAG.length()));
      // TODO(tball): remove obsolete flag once projects stop using it.
      } else if (arg.equals("--final-methods-as-functions")) {
        finalMethodsAsFunctions = true;
      } else if (arg.equals("--no-final-methods-functions")) {
        finalMethodsAsFunctions = false;
      // TODO(kstanger): remove both "class-methods" flags once the behavior is standardized.
      } else if (arg.equals("--no-class-methods")) {
        removeClassMethods = true;
      } else if (arg.equals("--keep-class-methods")) {
        removeClassMethods = false;
      // TODO(tball): remove obsolete flag once projects stop using it.
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

    if (shouldMapHeaders() && buildClosure) {
      ErrorUtil.error(
          "--build-closure is not supported with -XcombineJars or --preserve-full-paths");
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

  private void addMappingsFiles(String[] filenames) throws IOException {
    for (String filename : filenames) {
      if (!filename.isEmpty()) {
        addMappingsProperties(FileUtil.loadProperties(filename));
      }
    }
  }

  private void addJreMappings() throws IOException {
    InputStream stream = J2ObjC.class.getResourceAsStream(JRE_MAPPINGS_FILE);
    addMappingsProperties(FileUtil.loadProperties(stream));
  }

  private void addMappingsProperties(Properties mappings) {
    Enumeration<?> keyIterator = mappings.propertyNames();
    while (keyIterator.hasMoreElements()) {
      String key = (String) keyIterator.nextElement();
      if (key.indexOf('(') > 0) {
        // All method mappings have parentheses characters, classes don't.
        String iosMethod = mappings.getProperty(key);
        methodMappings.put(key, iosMethod);
      } else {
        String iosClass = mappings.getProperty(key);
        classMappings.put(key, iosClass);
      }
    }
  }

  /**
   * Check that the memory management option wasn't previously set to a
   * different value.  If okay, then set the option.
   */
  private void checkMemoryManagementOption(MemoryManagementOption option) {
    if (memoryManagementOption != null && memoryManagementOption != option) {
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
    return instance.docCommentsEnabled;
  }

  @VisibleForTesting
  public static void setDocCommentsEnabled(boolean value) {
    instance.docCommentsEnabled = value;
  }

  public static List<String> getSourcePathEntries() {
    return instance.sourcePathEntries;
  }

  public static void appendSourcePath(String entry) {
    instance.sourcePathEntries.add(entry);
  }

  public static void insertSourcePath(int index, String entry) {
    instance.sourcePathEntries.add(index, entry);
  }

  public static List<String> getClassPathEntries() {
    return instance.classPathEntries;
  }

  public static File getOutputDirectory() {
    return instance.outputDirectory;
  }

  /**
   * If true, put output files in sub-directories defined by
   * package declaration (like javac does).
   */
  public static boolean usePackageDirectories() {
    return instance.outputStyle == OutputStyleOption.PACKAGE;
  }

  /**
   * If true, put output files in the same directories from
   * which the input files were read.
   */
  public static boolean useSourceDirectories() {
    return instance.outputStyle == OutputStyleOption.SOURCE
        || instance.outputStyle == OutputStyleOption.SOURCE_COMBINED;
  }

  public static boolean combineSourceJars() {
    return instance.outputStyle == OutputStyleOption.SOURCE_COMBINED;
  }

  @VisibleForTesting
  public static void setOutputStyle(OutputStyleOption style) {
    instance.outputStyle = style;
  }

  public static String getImplementationFileSuffix() {
    return instance.implementationSuffix;
  }

  public static boolean useReferenceCounting() {
    return instance.memoryManagementOption == MemoryManagementOption.REFERENCE_COUNTING;
  }

  public static boolean useARC() {
    return instance.memoryManagementOption == MemoryManagementOption.ARC;
  }

  public static MemoryManagementOption getMemoryManagementOption() {
    return instance.memoryManagementOption;
  }

  @VisibleForTesting
  public static void setMemoryManagementOption(MemoryManagementOption option) {
    instance.memoryManagementOption = option;
  }

  public static boolean emitLineDirectives() {
    return instance.emitLineDirectives;
  }

  public static void setEmitLineDirectives(boolean b) {
    instance.emitLineDirectives = b;
  }

  public static boolean treatWarningsAsErrors() {
    return instance.warningsAsErrors;
  }

  @VisibleForTesting
  public static void enableDeprecatedDeclarations() {
    instance.deprecatedDeclarations = true;
  }

  public static boolean generateDeprecatedDeclarations() {
    return instance.deprecatedDeclarations;
  }

  public static Map<String, String> getClassMappings() {
    return instance.classMappings;
  }

  public static Map<String, String> getMethodMappings() {
    return instance.methodMappings;
  }

  public static Map<String, String> getHeaderMappings() {
    return instance.headerMappings;
  }

  @Nullable
  public static List<String> getHeaderMappingFiles() {
    return instance.headerMappingFiles;
  }

  public static void setHeaderMappingFiles(List<String> headerMappingFiles) {
    instance.headerMappingFiles = headerMappingFiles;
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

  public static File getOutputHeaderMappingFile() {
    return instance.outputHeaderMappingFile;
  }

  @VisibleForTesting
  public static void setOutputHeaderMappingFile(File outputHeaderMappingFile) {
    instance.outputHeaderMappingFile = outputHeaderMappingFile;
  }

  public static List<String> getBootClasspath() {
    return getPathArgument(bootclasspath);
  }

  public static Map<String, String> getPackagePrefixes() {
    return instance.packagePrefixes;
  }

  public static String addPackagePrefix(String pkg, String prefix) {
    return instance.packagePrefixes.put(pkg, prefix);
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

  static void deleteDir(File dir) {
    for (File f : dir.listFiles()) {
      if (f.isDirectory()) {
        deleteDir(f);
      } else if (f.getName().endsWith(".java")) {
        // Only delete Java files, as other temporary files (like hsperfdata)
        // may also be in tmpdir.
        // TODO(kstanger): It doesn't make sense that hsperfdata would show up in our tempdir.
        // Consider deleting this method and using FileUtil#deleteTempDir() instead.
        f.delete();
      }
    }
    dir.delete();  // Will fail if other files in dir, which is fine.
  }

  public static String fileEncoding() {
    return instance.fileEncoding;
  }

  public static Charset getCharset() {
    return Charset.forName(instance.fileEncoding);
  }

  public static boolean stripGwtIncompatibleMethods() {
    return instance.stripGwtIncompatible;
  }

  @VisibleForTesting
  public static void setStripGwtIncompatibleMethods(boolean b) {
    instance.stripGwtIncompatible = b;
  }

  public static boolean generateSegmentedHeaders() {
    return instance.segmentedHeaders;
  }

  @VisibleForTesting
  public static void enableSegmentedHeaders() {
    instance.segmentedHeaders = true;
  }

  public static boolean jsniWarnings() {
    return instance.jsniWarnings;
  }

  public static void setJsniWarnings(boolean b) {
    instance.jsniWarnings = b;
  }

  public static boolean buildClosure() {
    return instance.buildClosure;
  }

  @VisibleForTesting
  public static void setBuildClosure(boolean b) {
    instance.buildClosure = b;
  }

  public static boolean stripReflection() {
    return instance.stripReflection;
  }

  @VisibleForTesting
  public static void setStripReflection(boolean b) {
    instance.stripReflection = b;
  }

  public static boolean extractUnsequencedModifications() {
    return instance.extractUnsequencedModifications;
  }

  @VisibleForTesting
  public static void enableExtractUnsequencedModifications() {
    instance.extractUnsequencedModifications = true;
  }

  public static int batchTranslateMaximum() {
    return instance.batchTranslateMaximum;
  }

  @VisibleForTesting
  public static void setBatchTranslateMaximum(int max) {
    instance.batchTranslateMaximum = max;
  }

  public static boolean finalMethodsAsFunctions() {
    return instance.finalMethodsAsFunctions;
  }

  @VisibleForTesting
  public static void setFinalMethodsAsFunctions(boolean b) {
    instance.finalMethodsAsFunctions = b;
  }

  public static boolean removeClassMethods() {
    return instance.removeClassMethods;
  }

  @VisibleForTesting
  public static void setRemoveClassMethods(boolean b) {
    instance.removeClassMethods = b;
  }

  public static boolean hidePrivateMembers() {
    return instance.hidePrivateMembers;
  }

  @VisibleForTesting
  public static void setHidePrivateMembers(boolean b) {
    instance.hidePrivateMembers = b;
  }

  public static boolean shouldMapHeaders() {
    return useSourceDirectories() || combineSourceJars();
  }
}
