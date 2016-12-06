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
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.HeaderMap;
import com.google.devtools.j2objc.util.Mappings;
import com.google.devtools.j2objc.util.PackageInfoLookup;
import com.google.devtools.j2objc.util.PackagePrefixes;
import com.google.devtools.j2objc.util.Parser;
import com.google.devtools.j2objc.util.SourceVersion;
import com.google.devtools.j2objc.util.Version;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jdt.core.JavaCore;

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
  private List<String> processorPathEntries = Lists.newArrayList();
  private File outputDirectory = new File(".");
  private OutputLanguageOption language = OutputLanguageOption.OBJECTIVE_C;
  private MemoryManagementOption memoryManagementOption = null;
  private boolean emitLineDirectives = false;
  private boolean warningsAsErrors = false;
  private boolean deprecatedDeclarations = false;
  private HeaderMap headerMap = new HeaderMap();
  private boolean stripGwtIncompatible = false;
  private boolean segmentedHeaders = true;
  private String fileEncoding = System.getProperty("file.encoding", "UTF-8");
  private boolean jsniWarnings = true;
  private boolean buildClosure = false;
  private boolean stripReflection = false;
  private boolean extractUnsequencedModifications = true;
  private boolean docCommentsEnabled = false;
  private boolean staticAccessorMethods = false;
  private int batchTranslateMaximum = -1;
  private String processors = null;
  private boolean disallowInheritedConstructors = false;
  private boolean swiftFriendly = false;
  private boolean nullability = false;
  private EnumSet<LintOption> lintOptions = EnumSet.noneOf(LintOption.class);
  private TimingLevel timingLevel = TimingLevel.NONE;
  private boolean dumpAST = false;
  private String lintArgument = null;

  // TODO(tball): remove after front-end conversion is complete.
  private FrontEnd javaFrontEnd = FrontEnd.defaultFrontEnd();

  private Mappings mappings = new Mappings();
  private PackageInfoLookup packageInfoLookup = new PackageInfoLookup();
  private PackagePrefixes packagePrefixes = new PackagePrefixes(packageInfoLookup);

  // The default source version number if not passed with -source is determined from the system
  // properties of the running java version after parsing the argument list.
  private SourceVersion sourceVersion = null;

  private static File proGuardUsageFile = null;

  private static String fileHeader;
  private static final String FILE_HEADER_KEY = "file-header";
  private static String usageMessage;
  private static String helpMessage;
  private static String xhelpMessage;
  private static final String USAGE_MSG_KEY = "usage-message";
  private static final String HELP_MSG_KEY = "help-message";
  private static final String X_HELP_MSG_KEY = "x-help-message";
  private static final String XBOOTCLASSPATH = "-Xbootclasspath:";
  private static String bootclasspath = System.getProperty("sun.boot.class.path");
  private static final String BATCH_PROCESSING_MAX_FLAG = "--batch-translate-max=";
  private static final String TIMING_INFO_ARG = "--timing-info";
  private static final String ENV_FRONT_END_FLAG = "J2OBJC_FRONT_END";

  // TODO(tball): remove obsolete flags once projects stop using them.
  private static final Set<String> obsoleteFlags = Sets.newHashSet(
    "--disallow-inherited-constructors",
    "--final-methods-as-functions",
    "--no-final-methods-functions",
    "--hide-private-members",
    "--no-hide-private-members",
    "--segmented-headers",
    "-q",
    "--quiet",
    "-Xforce-incomplete-java8"
  );

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
    xhelpMessage = properties.getProperty(X_HELP_MSG_KEY);
    Preconditions.checkNotNull(xhelpMessage);

    Logger rootLogger = Logger.getLogger("");
    for (Handler handler : rootLogger.getHandlers()) {
      handler.setLevel(Level.ALL);
    }
  }

  /**
   * Types of memory management to be used by translated code.
   */
  public static enum MemoryManagementOption { REFERENCE_COUNTING, ARC }

  /**
   * What languages can be generated.
   */
  public static enum OutputLanguageOption {
    OBJECTIVE_C(".m"),
    OBJECTIVE_CPLUSPLUS(".mm");

    private final String suffix;

    OutputLanguageOption(String suffix) {
      this.suffix = suffix;
    }

    public String suffix() {
      return suffix;
    }
  }

  // TODO(tball): remove after front-end conversion is complete.
  private static enum FrontEnd {
    JDT, JAVAC;

    static FrontEnd defaultFrontEnd() {
      String envFlag = System.getenv(ENV_FRONT_END_FLAG);
      if (envFlag != null) {
        if (envFlag.equalsIgnoreCase(JAVAC.name())) {
          return JAVAC;
        } else if (!envFlag.equalsIgnoreCase(JDT.name())) {
          ErrorUtil.error("Invalid front end environment flag: " + envFlag);
        }
      }
      return JDT;
    }
  }

  /**
   * Xlint options and their associated JDT parser warnings.
   */
  public static enum LintOption {
    CAST(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK),
    DEPRECATION(JavaCore.COMPILER_PB_DEPRECATION),
    DEP_ANN(JavaCore.COMPILER_PB_MISSING_DEPRECATED_ANNOTATION),
    EMPTY(JavaCore.COMPILER_PB_EMPTY_STATEMENT),
    FALLTHROUGH(JavaCore.COMPILER_PB_FALLTHROUGH_CASE),
    FINALLY(JavaCore.COMPILER_PB_FINALLY_BLOCK_NOT_COMPLETING),
    RAWTYPES(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE),
    SERIAL(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION),
    STATIC(JavaCore.COMPILER_PB_STATIC_ACCESS_RECEIVER),
    UNCHECKED(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION),
    VARARGS(JavaCore.COMPILER_PB_VARARGS_ARGUMENT_NEED_CAST),

    // Default JDT warnings that don't have javac equivalents. These are included since
    // all unspecified warnings are turned off in JdtParser.
    ASSERT_IDENTIFIER(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER),
    CHAR_CONCAT(JavaCore.COMPILER_PB_CHAR_ARRAY_IN_STRING_CONCATENATION),
    COMPARE_IDENTICAL(JavaCore.COMPILER_PB_COMPARING_IDENTICAL),
    DEAD_CODE(JavaCore.COMPILER_PB_DEAD_CODE),
    DISCOURAGED(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE),
    ENUM_IDENTIFIER(JavaCore.COMPILER_PB_ENUM_IDENTIFIER),
    FINAL_BOUND(JavaCore.COMPILER_PB_FINAL_PARAMETER_BOUND),
    FORBIDDEN(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE),
    INCOMPLETE_ENUM_SWITCH(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH),
    INTERFACE_ANNOTATON(JavaCore.COMPILER_PB_ANNOTATION_SUPER_INTERFACE),
    INTERFACE_NON_INHERITED(JavaCore.COMPILER_PB_INCOMPATIBLE_NON_INHERITED_INTERFACE_METHOD),
    MASKED_CATCH(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK),
    METHOD_WITH_CONSTRUCTOR_NAME(JavaCore.COMPILER_PB_METHOD_WITH_CONSTRUCTOR_NAME),
    NO_EFFECT_ASSIGN(JavaCore.COMPILER_PB_NO_EFFECT_ASSIGNMENT),
    NULL_REFERENCE(JavaCore.COMPILER_PB_NULL_REFERENCE),
    NULL_UNCHECKED_CONVERSION(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION),
    PARAMTER_ANNOTATION_DROPPED(JavaCore.COMPILER_PB_NONNULL_PARAMETER_ANNOTATION_DROPPED),
    PKG_DEFAULT_METHOD(JavaCore.COMPILER_PB_OVERRIDING_PACKAGE_DEFAULT_METHOD),
    REDUNDANT_NULL_ANNOTATION(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION),
    RESOURCE_LEAK(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE),
    TYPE_HIDING(JavaCore.COMPILER_PB_TYPE_PARAMETER_HIDING),
    UNUSED_IMPORT(JavaCore.COMPILER_PB_UNUSED_IMPORT),
    UNUSED_LABEL(JavaCore.COMPILER_PB_UNUSED_LABEL),
    UNUSED_LOCAL(JavaCore.COMPILER_PB_UNUSED_LOCAL),
    UNUSED_PRIVATE(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER),
    UNUSED_TYPE_ARGS(JavaCore.COMPILER_PB_UNUSED_TYPE_ARGUMENTS_FOR_METHOD_INVOCATION),
    WARNING_TOKEN(JavaCore.COMPILER_PB_UNHANDLED_WARNING_TOKEN);

    private final String jdtFlag;

    private LintOption(String jdtFlag) {
      this.jdtFlag = jdtFlag;
    }

    public String jdtFlag() {
      return jdtFlag;
    }

    static LintOption parseName(String name) {
      if (name.startsWith("-")) {
        name = name.substring(1);
      }
      for (LintOption option : values()) {
        if (option.name().toLowerCase().equals(name)) {
          return option;
        }
      }
      return null;
    }

    static EnumSet<LintOption> parse(String flag) {
      if (flag.equals("-Xlint") || flag.equals("-Xlint:all")) {
        return EnumSet.allOf(LintOption.class);
      }
      if (flag.equals("-Xlint:none")) {
        return EnumSet.noneOf(LintOption.class);
      }
      if (!flag.startsWith("-Xlint:")) {
        ErrorUtil.error("invalid flag: " + flag);
      }
      String flagList = flag.substring("-Xlint:".length());
      String[] flags =
          flagList.contains(",") ? flagList.split(",") : new String[] { flagList };
      boolean hasMinusOption = false;
      for (String f : flags) {
        if (f.startsWith("-")) {
          hasMinusOption = true;
          break;
        }
      }
      EnumSet<LintOption> result =
          hasMinusOption ? EnumSet.allOf(LintOption.class) : EnumSet.noneOf(LintOption.class);
      for (String f : flags) {
        if (f.equals("all")) {
          result.addAll(EnumSet.allOf(LintOption.class));
          continue;
        }
        if (f.equals("none")) {
          result.clear();
          continue;
        }
        LintOption option = parseName(f);
        if (option == null) {
          ErrorUtil.error("invalid flag: " + flag);
        }
        if (f.startsWith("-")) {
          result.remove(option);
        } else {
          result.add(option);
        }
      }
      return result;
    }
  }

  /**
   * What timing information should be printed, if any.
   */
  public enum TimingLevel {
    // Don't print any timing information.
    NONE,

    // Print the total execution time at the end.
    TOTAL,

    // Print all timing information.
    ALL
  }

  /**
   * Set all log handlers in this package with a common level.
   */
  private static void setLogLevel(Level level) {
    Logger.getLogger("com.google.devtools.j2objc").setLevel(level);
  }

  public static boolean isVerbose() {
    return Logger.getLogger("com.google.devtools.j2objc").getLevel().equals(Level.FINEST);
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
    setLogLevel(Level.WARNING);

    mappings.addJreMappings();

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
      } else if (arg.equals("-processorpath")) {
        if (++nArg == args.length) {
          usage("-processorpath requires an argument");
        }
        processorPathEntries.addAll(getPathArgument(args[nArg]));
      } else if (arg.equals("-d")) {
        if (++nArg == args.length) {
          usage("-d requires an argument");
        }
        outputDirectory = new File(args[nArg]);
      } else if (arg.equals("--mapping")) {
        if (++nArg == args.length) {
          usage("--mapping requires an argument");
        }
        mappings.addMappingsFiles(args[nArg].split(","));
      } else if (arg.equals("--header-mapping")) {
        if (++nArg == args.length) {
          usage("--header-mapping requires an argument");
        }
        headerMap.setMappingFiles(args[nArg]);
      } else if (arg.equals("--output-header-mapping")) {
        if (++nArg == args.length) {
          usage("--output-header-mapping requires an argument");
        }
        headerMap.setOutputMappingFile(new File(args[nArg]));
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
        packagePrefixes.addPrefixesFile(args[nArg]);
      } else if (arg.equals("-x")) {
        if (++nArg == args.length) {
          usage("-x requires an argument");
        }
        String s = args[nArg];
        if (s.equals("objective-c")) {
          language = OutputLanguageOption.OBJECTIVE_C;
        } else if (s.equals("objective-c++")) {
          language = OutputLanguageOption.OBJECTIVE_CPLUSPLUS;
        } else {
          usage("unsupported language: " + s);
        }
      } else if (arg.equals("--ignore-missing-imports")) {
        ErrorUtil.error("--ignore-missing-imports is no longer supported");
      } else if (arg.equals("-use-reference-counting")) {
        checkMemoryManagementOption(MemoryManagementOption.REFERENCE_COUNTING);
      } else if (arg.equals("--no-package-directories")) {
        headerMap.setOutputStyle(HeaderMap.OutputStyleOption.NONE);
      } else if (arg.equals("--preserve-full-paths")) {
        headerMap.setOutputStyle(HeaderMap.OutputStyleOption.SOURCE);
      } else if (arg.equals("-XcombineJars")) {
        headerMap.setCombineJars();
      } else if (arg.equals("-XincludeGeneratedSources")) {
        headerMap.setIncludeGeneratedSources();
      } else if (arg.equals("-use-arc")) {
        checkMemoryManagementOption(MemoryManagementOption.ARC);
      } else if (arg.equals("-g")) {
        emitLineDirectives = true;
      } else if (arg.equals("-g:none")) {
        emitLineDirectives = false;
      } else if (arg.equals("-Werror")) {
        warningsAsErrors = true;
      } else if (arg.equals("--generate-deprecated")) {
        deprecatedDeclarations = true;
      } else if (arg.equals("-l") || arg.equals("--list")) {
        setLogLevel(Level.INFO);
      } else if (arg.equals("-t") || arg.equals(TIMING_INFO_ARG)) {
        timingLevel = TimingLevel.ALL;
      } else if (arg.startsWith(TIMING_INFO_ARG + ':')) {
        String timingArg = arg.substring(TIMING_INFO_ARG.length() + 1);
        try {
          timingLevel = TimingLevel.valueOf(timingArg.toUpperCase());
        } catch (IllegalArgumentException e) {
          usage("invalid --timing-info argument");
        }
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
      } else if (arg.equals("--no-segmented-headers")) {
        segmentedHeaders = false;
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
      } else if (arg.equals("--static-accessor-methods")) {
        staticAccessorMethods = true;
      } else if (arg.equals("--swift-friendly")) {
        swiftFriendly = true;
      } else if (arg.equals("-processor")) {
        if (++nArg == args.length) {
          usage("-processor requires an argument");
        }
        processors = args[nArg];
      } else if (arg.equals("--allow-inherited-constructors")) {
        disallowInheritedConstructors = false;
      } else if (arg.equals("--nullability")) {
        nullability = true;
      } else if (arg.startsWith("-Xlint")) {
        lintArgument = arg;
        lintOptions = LintOption.parse(arg);
      } else if (arg.equals("-Xuse-jdt")) {
        javaFrontEnd = FrontEnd.JDT;
      } else if (arg.equals("-Xuse-javac")) {
        javaFrontEnd = FrontEnd.JAVAC;
      } else if (arg.equals("-Xdump-ast")) {
        dumpAST = true;
      } else if (arg.equals("-version")) {
        version();
      } else if (arg.startsWith("-h") || arg.equals("--help")) {
        help(false);
      } else if (arg.equals("-X")) {
        xhelp();
      }  else if (arg.equals("-source")) {
        if (++nArg == args.length) {
          usage("-source requires an argument");
        }
        // Handle aliasing of version numbers as supported by javac.
        try {
          sourceVersion = SourceVersion.parse(args[nArg]);
        } catch (IllegalArgumentException e) {
          usage("invalid source release: " + args[nArg]);
        }
      } else if (arg.equals("-target")) {
        // Dummy out passed target argument, since we don't care about target.
        if (++nArg == args.length) {
          usage("-target requires an argument");
        }
        // ignore
      } else if (obsoleteFlags.contains(arg)) {
        // also ignore
      } else if (arg.startsWith("-")) {
        usage("invalid flag: " + arg);
      } else {
        break;
      }
      ++nArg;
    }

    if (headerMap.useSourceDirectories() && buildClosure) {
      ErrorUtil.error(
          "--build-closure is not supported with -XcombineJars or --preserve-full-paths or "
          + "-XincludeGeneratedSources");
    }

    if (memoryManagementOption == null) {
      memoryManagementOption = MemoryManagementOption.REFERENCE_COUNTING;
    }

    if (swiftFriendly) {
      staticAccessorMethods = true;
      nullability = true;
    }

    // Pull source version from system properties if it is not passed with -source flag.
    if (sourceVersion == null) {
      sourceVersion = SourceVersion.parse(System.getProperty("java.version").substring(0, 3));
    }

    // Java 6 had a 1G max heap limit, removed in Java 7.
    if (batchTranslateMaximum == -1) {  // Not set by flag.
      batchTranslateMaximum = SourceVersion.java7Minimum(sourceVersion) ? 300 : 0;
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
  private void addPrefixOption(String arg) {
    int i = arg.indexOf('=');
    if (i < 1) {
      usage("invalid prefix format");
    }
    packagePrefixes.addPrefix(arg.substring(0, i), arg.substring(i + 1));
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

  public static void xhelp() {
    System.err.println(xhelpMessage);
    System.exit(0);
  }

  public static void version() {
    System.err.println("j2objc " + Version.jarVersion(Options.class));
    System.exit(0);
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

  public static List<String> getProcessorPathEntries() {
    return instance.processorPathEntries;
  }

  public static File getOutputDirectory() {
    return instance.outputDirectory;
  }

  public static OutputLanguageOption getLanguage() {
    return instance.language;
  }

  @VisibleForTesting
  public static void setOutputLanguage(OutputLanguageOption language) {
    instance.language = language;
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

  public static HeaderMap getHeaderMap() {
    return instance.headerMap;
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

  public static void setProGuardUsageFile(File newProGuardUsageFile) {
    proGuardUsageFile = newProGuardUsageFile;
  }

  public static File getProGuardUsageFile() {
    return proGuardUsageFile;
  }

  public static List<String> getBootClasspath() {
    return getPathArgument(bootclasspath);
  }

  public static Mappings getMappings() {
    return instance.mappings;
  }

  public static PackageInfoLookup getPackageInfoLookup() {
    return instance.packageInfoLookup;
  }

  public static PackagePrefixes getPackagePrefixes() {
    return instance.packagePrefixes;
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
  public static void setSegmentedHeaders(boolean b) {
    instance.segmentedHeaders = b;
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

  public static SourceVersion getSourceVersion(){
    return instance.sourceVersion;
  }

  @VisibleForTesting
  public static void setSourceVersion(SourceVersion version) {
    instance.sourceVersion = version;
  }

  public static boolean isJava8Translator() {
    return SourceVersion.java8Minimum(instance.sourceVersion);
  }

  public static boolean staticAccessorMethods() {
    return instance.staticAccessorMethods;
  }

  @VisibleForTesting
  public static void setStaticAccessorMethods(boolean b) {
    instance.staticAccessorMethods = b;
  }

  public static String getProcessors() {
    return instance.processors;
  }

  @VisibleForTesting
  public static void setProcessors(String processors) {
    instance.processors = processors;
  }

  public static boolean disallowInheritedConstructors() {
    return instance.disallowInheritedConstructors;
  }

  @VisibleForTesting
  public static void setDisallowInheritedConstructors(boolean b) {
    instance.disallowInheritedConstructors = b;
  }

  public static boolean swiftFriendly() {
    return instance.swiftFriendly;
  }

  @VisibleForTesting
  public static void setSwiftFriendly(boolean b) {
    instance.swiftFriendly = b;
    instance.staticAccessorMethods = b;
    instance.nullability = b;
  }

  public static boolean nullability() {
    return instance.nullability;
  }

  @VisibleForTesting
  public static void setNullability(boolean b) {
    instance.nullability = b;
  }

  public static EnumSet<LintOption> lintOptions() {
    return instance.lintOptions;
  }

  public static String lintArgument() {
    return instance.lintArgument;
  }

  public static TimingLevel timingLevel() {
    return instance.timingLevel;
  }

  public static boolean dumpAST() {
    return instance.dumpAST;
  }

  // TODO(tball): remove after front-end conversion is complete.
  public static Parser newParser() {
    if (instance.javaFrontEnd == FrontEnd.JDT) {
        return new com.google.devtools.j2objc.jdt.JdtParser();
    }
    return new com.google.devtools.j2objc.javac.JavacParser();
  }

  // TODO(kstanger): remove after front-end conversion is complete.
  public static boolean isJDT() {
    return instance.javaFrontEnd == FrontEnd.JDT;
  }
}
