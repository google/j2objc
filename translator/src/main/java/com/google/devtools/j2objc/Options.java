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

package com.google.devtools.j2objc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.devtools.j2objc.gen.GenerationUnit;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.ExternalAnnotations;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.HeaderMap;
import com.google.devtools.j2objc.util.Mappings;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.PackageInfoLookup;
import com.google.devtools.j2objc.util.PackagePrefixes;
import com.google.devtools.j2objc.util.SourceVersion;
import com.google.devtools.j2objc.util.Version;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * The set of tool properties, initialized by the command-line arguments.
 * This class was extracted from the main class, to make it easier for
 * other classes to access options.
 *
 * @author Tom Ball
 */
public class Options {

  private List<String> processorPathEntries = new ArrayList<>();
  private OutputLanguageOption language = OutputLanguageOption.OBJECTIVE_C;
  private MemoryManagementOption memoryManagementOption = null;
  private EmitLineDirectivesOption emitLineDirectives = EmitLineDirectivesOption.NONE;
  private boolean warningsAsErrors = false;
  private boolean deprecatedDeclarations = false;
  private HeaderMap headerMap = new HeaderMap();
  private boolean stripGwtIncompatible = false;
  private boolean segmentedHeaders = true;
  private boolean jsniWarnings = true;
  private boolean buildClosure = false;
  private EnumSet<MetadataSupport> includedMetadata =
      EnumSet.of(
          MetadataSupport.FULL, MetadataSupport.ENUM_CONSTANTS, MetadataSupport.NAME_MAPPING);
  private boolean emitWrapperMethods = true;
  private boolean extractUnsequencedModifications = true;
  private boolean docCommentsEnabled = false;
  private boolean staticAccessorMethods = true;
  private boolean classProperties = true;
  private String processors = null;
  private boolean disallowInheritedConstructors = true;
  private boolean nullability = true;
  private boolean defaultNonnull = false;
  private TimingLevel timingLevel = TimingLevel.NONE;
  private boolean dumpAST = false;
  private String lintArgument = null;
  private boolean reportJavadocWarnings = false;
  private boolean translateBootclasspath = false;
  private boolean translateClassfiles = false;
  private String annotationsJar = null;
  private CombinedOutput globalCombinedOutput = null;
  private String bootclasspath = null;
  private boolean emitKytheMappings = false;
  private boolean emitSourceHeaders = true;

  private Mappings mappings = new Mappings();
  private FileUtil fileUtil = new FileUtil();
  private PackageInfoLookup packageInfoLookup = new PackageInfoLookup(fileUtil);
  private PackagePrefixes packagePrefixes = new PackagePrefixes(packageInfoLookup);
  private final ExternalAnnotations externalAnnotations = new ExternalAnnotations();
  private final List<String> entryClasses = new ArrayList<>();

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
  private static final String TIMING_INFO_ARG = "--timing-info";

  private static final Pattern KNOWN_FILE_SUFFIX_PATTERN
      = Pattern.compile(".*\\.(java|class|jar|zip)");

  // TODO(tball): remove obsolete flags once projects stop using them.
  private static final Set<String> obsoleteFlags = Sets.newHashSet(
    "--batch-translate-max",
    "--disallow-inherited-constructors",
    "--extract-unsequenced",
    "--final-methods-as-functions",
    "--jsni-warnings",
    "--no-final-methods-functions",
    "--hide-private-members",
    "--no-hide-private-members",
    "--segmented-headers",
    "-q",
    "--quiet",
    "-Xforce-incomplete-java8"
  );
  private static final String BATCH_PROCESSING_MAX_FLAG = "--batch-translate-max=";

  /**
   * Types of memory management to be used by translated code.
   */
  public enum MemoryManagementOption { REFERENCE_COUNTING, ARC }

  /**
   * What languages can be generated.
   */
  public enum OutputLanguageOption {
    OBJECTIVE_C(".m", ".h"),
    OBJECTIVE_CPLUSPLUS(".mm", ".h"),

    // Test-only language option, for A/B test comparisons.
    TEST_OBJECTIVE_C(".m2", ".h2");

    private final String suffix;
    private final String headerSuffix;

    OutputLanguageOption(String suffix, String headerSuffix) {
      this.suffix = suffix;
      this.headerSuffix = headerSuffix;
    }

    public String suffix() {
      return suffix;
    }

    public String headerSuffix() {
      return headerSuffix;
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
   * What reflection support should be generated, if any.
   */
  public enum MetadataSupport {
    // Generate metadata for enum constants.
    ENUM_CONSTANTS,

    // Generate name mapping.
    NAME_MAPPING,

    // Generate all metadata.
    FULL
  }

  /**
   * Different ways that #line debug directives can be emitted.
   */
  private enum EmitLineDirectivesOption {
    // Don't emit #line directives.
    NONE,

    // Emit #line directives using the unmodified source file path of the compilation unit; this may
    // be an absolute path or a relative path.
    NORMAL,

    // Emit #line directives using the source file path of the compilation unit converted to a
    // relative path, relative to the current working directory; if the file is not in a
    // subdirectory of the current working directory then the emitted path is the same as NORMAL.
    RELATIVE,
  }

  /**
   * Class that holds the information needed to generate combined output, so that all output goes to
   * a single, named .h/.m file set.
   */
  public static class CombinedOutput {

    private final String outputName;
    private final GenerationUnit combinedUnit;

    CombinedOutput(String outputName, Options options) {
      this.outputName = outputName;
      this.combinedUnit = GenerationUnit.newCombinedJarUnit(outputName, options);
    }

    public String globalCombinedOutputName() {
      return outputName;
    }

    public GenerationUnit globalGenerationUnit() {
      return combinedUnit;
    }
  }

  // Flags that are directly forwarded to the javac parser.
  private static final ImmutableSet<String> PLATFORM_MODULE_SYSTEM_OPTIONS =
      ImmutableSet.of("--patch-module", "--system", "--add-reads");
  private final List<String> platformModuleSystemOptions = new ArrayList<>();

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

  public CombinedOutput globalCombinedOutput() {
    return globalCombinedOutput;
  }

  public void setGlobalCombinedOutput(String outputName) {
    this.globalCombinedOutput = new CombinedOutput(outputName, this);
  }

  /**
   * Set all log handlers in this package with a common level.
   */
  private void setLogLevel(Level level) {
    Logger.getLogger("com.google.devtools.j2objc").setLevel(level);
  }

  public boolean isVerbose() {
    return Logger.getLogger("com.google.devtools.j2objc").getLevel().equals(Level.FINEST);
  }

  /**
   * Load the options from a command-line, returning the arguments that were
   * not option-related (usually files).  If help is requested or an error is
   * detected, the appropriate status method is invoked and the app terminates.
   * @throws IOException
   */
  public List<String> load(String[] args) throws IOException {
    setLogLevel(Level.WARNING);

    mappings.addJreMappings();

    // Create a temporary directory as the sourcepath's first entry, so that
    // modified sources will take precedence over regular files.
    fileUtil.setSourcePathEntries(new ArrayList<>());

    ArgProcessor processor = new ArgProcessor();
    processor.processArgs(args);
    postProcessArgs();

    return processor.sourceFiles;
  }

  private class ArgProcessor {

    private List<String> sourceFiles = new ArrayList<>();

    private void processArgs(String[] args) throws IOException {
      Iterator<String> iter = Arrays.asList(args).iterator();
      while (iter.hasNext()) {
        processArg(iter);
      }
    }

    private void processArgsFile(String filename) throws IOException {
      if (filename.isEmpty()) {
        usage("no @ file specified");
      }
      File f = new File(filename);
      String fileArgs = Files.asCharSource(f, fileUtil.getCharset()).read();
      // Simple split on any whitespace, quoted values aren't supported.
      processArgs(fileArgs.split("\\s+"));
    }

    private String getArgValue(Iterator<String> args, String arg) {
      if (!args.hasNext()) {
        usage(arg + " requires an argument");
      }
      return args.next();
    }

    private void processArg(Iterator<String> args) throws IOException {
      String arg = args.next();
      if (arg.isEmpty()) {
        return;
      } else if (arg.startsWith("@")) {
        processArgsFile(arg.substring(1));
      } else if (arg.equals("-classpath") || arg.equals("-cp")) {
        fileUtil.getClassPathEntries().addAll(getPathArgument(getArgValue(args, arg), true, true));
      } else if (arg.equals("-sourcepath")) {
        fileUtil.getSourcePathEntries()
            .addAll(getPathArgument(getArgValue(args, arg), false, false));
      } else if (arg.equals("-processorpath")) {
        processorPathEntries.addAll(getPathArgument(getArgValue(args, arg), true, false));
      } else if (arg.equals("-d")) {
        fileUtil.setOutputDirectory(new File(getArgValue(args, arg)));
      } else if (arg.equals("--mapping")) {
        mappings.addMappingsFiles(getArgValue(args, arg).split(","));
      } else if (arg.equals("--header-mapping")) {
        headerMap.setMappingFiles(getArgValue(args, arg));
      } else if (arg.equals("--output-header-mapping")) {
        headerMap.setOutputMappingFile(new File(getArgValue(args, arg)));
      } else if (arg.equals("--dead-code-report")) {
        proGuardUsageFile = new File(getArgValue(args, arg));
      } else if (arg.equals("--prefix")) {
        addPrefixOption(getArgValue(args, arg));
      } else if (arg.equals("--prefixes")) {
        packagePrefixes.addPrefixesFile(getArgValue(args, arg));
      } else if (arg.equals("-x")) {
        String s = getArgValue(args, arg);
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
      } else if (arg.equals("-XglobalCombinedOutput")) {
        setGlobalCombinedOutput(getArgValue(args, arg));
      } else if (arg.equals("-XincludeGeneratedSources")) {
        headerMap.setIncludeGeneratedSources();
      } else if (arg.equals("-Xpublic-hdrs")) {
        fileUtil.setHeaderOutputDirectory(new File(getArgValue(args, arg)));
      } else if (arg.equals("-use-arc")) {
        checkMemoryManagementOption(MemoryManagementOption.ARC);
      } else if (arg.equals("-g")) {
        emitLineDirectives = EmitLineDirectivesOption.NORMAL;
      } else if (arg.equals("-g:none")) {
        emitLineDirectives = EmitLineDirectivesOption.NONE;
      } else if (arg.equals("-g:relative")) {
        emitLineDirectives = EmitLineDirectivesOption.RELATIVE;
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
        try {
          fileUtil.setFileEncoding(getArgValue(args, arg));
        } catch (UnsupportedCharsetException e) {
          ErrorUtil.warning(e.getMessage());
        }
      } else if (arg.equals("--strip-gwt-incompatible")) {
        stripGwtIncompatible = true;
      } else if (arg.equals("--strip-reflection")) {
        includedMetadata = EnumSet.of(MetadataSupport.ENUM_CONSTANTS);
      } else if (arg.equals("-Xstrip-enum-constants")) {
        includedMetadata.remove(MetadataSupport.ENUM_CONSTANTS);
      } else if (arg.startsWith("--reflection:")) {
        includedMetadata.remove(MetadataSupport.FULL);
        String[] subArgs = arg.substring(arg.indexOf(':') + 1).split(",", -1);
        for (String subArg : subArgs) {
          switch (subArg) {
            case "all": {
              includedMetadata = EnumSet.allOf(MetadataSupport.class);
              break;
            }
            case "none": {
              includedMetadata = EnumSet.noneOf(MetadataSupport.class);
              break;
            }
            case "enum-constants": {
              includedMetadata.add(MetadataSupport.ENUM_CONSTANTS);
              break;
            }
            case "-enum-constants": {
              includedMetadata.remove(MetadataSupport.ENUM_CONSTANTS);
              break;
            }
            case "name-mapping": {
              includedMetadata.add(MetadataSupport.NAME_MAPPING);
              break;
            }
            case "-name-mapping": {
              includedMetadata.remove(MetadataSupport.NAME_MAPPING);
              break;
            }
            default: {
              usage("invalid --reflection argument: " + subArg);
            }
          }
        }
      } else if (arg.equals("--no-wrapper-methods")) {
        emitWrapperMethods = false;
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
      } else if (arg.equals("--doc-comment-warnings")) {
        reportJavadocWarnings = true;
      } else if (arg.equals("--static-accessor-methods")) {
        staticAccessorMethods = true;
      } else if (arg.equals("--class-properties")) {
        setClassProperties(true);
      } else if (arg.equals("--no-class-properties")) {
        setClassProperties(false);
      } else if (arg.equals("--swift-friendly")) {
        setSwiftFriendly(true);
      } else if (arg.equals("-processor")) {
        processors = getArgValue(args, arg);
      } else if (arg.equals("--allow-inherited-constructors")) {
        disallowInheritedConstructors = false;
      } else if (arg.equals("--nullability")) {
        nullability = true;
      } else if (arg.equals("--no-nullability")) {
        nullability = false;
      } else if (arg.equals("-Xdefault-nonnull")) {
        defaultNonnull = true;
      } else if (arg.startsWith("-Xlint")) {
        lintArgument = arg;
      } else if (arg.equals("-Xtranslate-bootclasspath")) {
        translateBootclasspath = true;
      } else if (arg.equals("-Xdump-ast")) {
        dumpAST = true;
      } else if (arg.equals("-Xtranslate-classfiles")) {
        translateClassfiles = true;
      } else if (arg.equals("-Xannotations-jar")) {
        annotationsJar = getArgValue(args, arg);
      } else if (arg.equals("-Xkythe-mapping")) {
        emitKytheMappings = true;
      } else if (arg.equals("-Xno-source-headers")) {
        emitSourceHeaders = false;
      } else if (arg.equals("-external-annotation-file")) {
        addExternalAnnotationFile(getArgValue(args, arg));
      } else if (arg.equals("--reserved-names")) {
        NameTable.addReservedNames(getArgValue(args, arg));
      } else if (arg.equals("-version")) {
        version();
      } else if (arg.startsWith("-h") || arg.equals("--help")) {
        help(false);
      } else if (arg.equals("-X")) {
        xhelp();
      }  else if (arg.equals("-source")) {
        String s = getArgValue(args, arg);
        // Handle aliasing of version numbers as supported by javac.
        try {
          sourceVersion = SourceVersion.parse(s);
        } catch (IllegalArgumentException e) {
          usage("invalid source release: " + s);
        }
      } else if (arg.equals("-target")) {
        // Dummy out passed target argument, since we don't care about target.
        getArgValue(args, arg);  // ignore
      } else if (PLATFORM_MODULE_SYSTEM_OPTIONS.contains(arg)) {
        addPlatformModuleSystemOptions(arg, getArgValue(args, arg));
      } else if (arg.startsWith(BATCH_PROCESSING_MAX_FLAG)) {
        // Ignore, batch processing isn't used with javac front-end.
      } else if (obsoleteFlags.contains(arg)) {
        // also ignore
      } else if (arg.startsWith("-")) {
        usage("invalid flag: " + arg);
      } else if (NameTable.isValidClassName(arg) && !hasKnownFileSuffix(arg)) {
        // TODO(tball): document entry classes when build is updated to Bazel.
        entryClasses.add(arg);
      } else {
        sourceFiles.add(arg);
      }
    }
  }

  private void postProcessArgs() {
    postProcessSourceVersion();
    // Fix up the classpath, adding the current dir if it is empty, as javac would.
    List<String> classPaths = fileUtil.getClassPathEntries();
    if (classPaths.isEmpty()) {
      classPaths.add(".");
    }
    // javac will search the classpath for sources if no -sourcepath is specified. So here we copy
    // the classpath entries to the sourcepath list.
    List<String> sourcePaths = fileUtil.getSourcePathEntries();
    if (sourcePaths.isEmpty()) {
      sourcePaths.addAll(classPaths);
    }
    if (annotationsJar != null) {
      classPaths.add(annotationsJar);
    }

    if (headerMap.useSourceDirectories() && buildClosure) {
      ErrorUtil.error(
          "--build-closure is not supported with -XcombineJars or --preserve-full-paths or "
          + "-XincludeGeneratedSources");
    }

    // Entry classes are only allowed with --build-closure flag.
    if (!entryClasses.isEmpty() && !buildClosure) {
      ErrorUtil.error("entry class names can only be specified with --build-closure flag");
    }

    if (memoryManagementOption == null) {
      memoryManagementOption = MemoryManagementOption.REFERENCE_COUNTING;
    }

    if (bootclasspath == null) {
      // Set jre_emul.jar as bootclasspath, if available. This ensures that source files
      // accessing JRE classes or methods not supported in the JRE emulation library are
      // reported during compilation, rather than with a more obscure link error.
      for (String path : Splitter.on(':').split(System.getProperty("java.class.path"))) {
        if (path.endsWith("jre_emul.jar")) {
          bootclasspath = path;
          break;
        }
      }
    }
    if (bootclasspath == null) {
      // Fall back to Java 8 and earlier property.
      bootclasspath = System.getProperty("sun.boot.class.path", "");
    }
  }

  private void postProcessSourceVersion() {
    if (sourceVersion == null) {
      sourceVersion = SourceVersion.defaultVersion();
    }
    SourceVersion maxVersion = SourceVersion.getMaxSupportedVersion();
    if (sourceVersion.version() > maxVersion.version()) {
      ErrorUtil.warning("Java " + sourceVersion.version() + " source version is not "
          + "supported, using Java " + maxVersion.version() + ".");
      sourceVersion = maxVersion;
    }
    if (sourceVersion.version() > 8) {
      // Allow the modularized JRE to read the J2ObjC annotations (they are in the unnamed module).
      addPlatformModuleSystemOptions("--add-reads", "java.base=ALL-UNNAMED");
    } else {
      platformModuleSystemOptions.clear();
    }
  }

  private boolean hasKnownFileSuffix(String s) {
    return KNOWN_FILE_SUFFIX_PATTERN.matcher(s).matches();
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

  private List<String> getPathArgument(String argument, boolean expandAarFiles,
      boolean expandWildcard) {
    List<String> entries = new ArrayList<>();
    for (String entry : Splitter.on(File.pathSeparatorChar).split(argument)) {
      if (entry.startsWith("~/")) {
        // Expand bash/csh tildes, which don't get expanded by the shell
        // first if in the middle of a path string.
        entry = System.getProperty("user.home") + entry.substring(1);
      }
      File f = new File(entry);
      if (f.getName().equals("*") && expandWildcard) {
        File parent = f.getParentFile() == null ? new File(".") : f.getParentFile();
        FileFilter jarFilter = file -> file.getName().endsWith(".jar");
        File[] files = parent.listFiles(jarFilter);
        if (files != null) {
          for (File jar : files) {
            entries.add(jar.toString());
          }
        }
        continue;
      }
      if (entry.endsWith(".aar") && expandAarFiles) {
        // Extract classes.jar from Android library AAR file.
        f = fileUtil().extractClassesJarFromAarFile(f);
      }
      if (f.exists()) {
        entries.add(f.toString());
      }
    }
    return entries;
  }

  public FileUtil fileUtil() {
    return fileUtil;
  }

  public boolean docCommentsEnabled() {
    return docCommentsEnabled;
  }

  @VisibleForTesting
  public void setDocCommentsEnabled(boolean value) {
    docCommentsEnabled = value;
  }

  public List<String> getProcessorPathEntries() {
    return processorPathEntries;
  }

  public OutputLanguageOption getLanguage() {
    return language;
  }

  @VisibleForTesting
  public void setOutputLanguage(OutputLanguageOption language) {
    this.language = language;
  }

  public boolean useReferenceCounting() {
    return memoryManagementOption == MemoryManagementOption.REFERENCE_COUNTING;
  }

  public boolean useARC() {
    return memoryManagementOption == MemoryManagementOption.ARC;
  }

  public MemoryManagementOption getMemoryManagementOption() {
    return memoryManagementOption;
  }

  @VisibleForTesting
  public void setMemoryManagementOption(MemoryManagementOption option) {
    memoryManagementOption = option;
  }

  public boolean emitLineDirectives() {
    return emitLineDirectives != EmitLineDirectivesOption.NONE;
  }

  @VisibleForTesting
  public void setEmitLineDirectives(boolean b) {
    emitLineDirectives = b ? EmitLineDirectivesOption.NORMAL : EmitLineDirectivesOption.NONE;
  }

  public boolean emitRelativeLineDirectives() {
    return emitLineDirectives == EmitLineDirectivesOption.RELATIVE;
  }

  @VisibleForTesting
  public void setEmitRelativeLineDirectives() {
    emitLineDirectives = EmitLineDirectivesOption.RELATIVE;
  }

  public boolean treatWarningsAsErrors() {
    return warningsAsErrors;
  }

  @VisibleForTesting
  public void enableDeprecatedDeclarations() {
    deprecatedDeclarations = true;
  }

  public boolean generateDeprecatedDeclarations() {
    return deprecatedDeclarations;
  }

  public HeaderMap getHeaderMap() {
    return headerMap;
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

  public List<String> getBootClasspath() {
    return getPathArgument(bootclasspath, false, false);
  }

  public Mappings getMappings() {
    return mappings;
  }

  public PackageInfoLookup getPackageInfoLookup() {
    return packageInfoLookup;
  }

  public PackagePrefixes getPackagePrefixes() {
    return packagePrefixes;
  }

  public boolean stripGwtIncompatibleMethods() {
    return stripGwtIncompatible;
  }

  @VisibleForTesting
  public void setStripGwtIncompatibleMethods(boolean b) {
    stripGwtIncompatible = b;
  }

  public boolean generateSegmentedHeaders() {
    return segmentedHeaders;
  }

  @VisibleForTesting
  public void setSegmentedHeaders(boolean b) {
    segmentedHeaders = b;
  }

  public boolean jsniWarnings() {
    return jsniWarnings;
  }

  public void setJsniWarnings(boolean b) {
    jsniWarnings = b;
  }

  public boolean buildClosure() {
    return buildClosure;
  }

  @VisibleForTesting
  public void setBuildClosure(boolean b) {
    buildClosure = b;
  }

  public boolean stripReflection() {
    return !includedMetadata.contains(MetadataSupport.FULL);
  }

  @VisibleForTesting
  public void setStripReflection(boolean b) {
    if (b) {
      includedMetadata.remove(MetadataSupport.FULL);
      includedMetadata.remove(MetadataSupport.NAME_MAPPING);
    } else {
      includedMetadata = EnumSet.allOf(MetadataSupport.class);
    }
  }

  public boolean stripEnumConstants() {
    return !includedMetadata.contains(MetadataSupport.ENUM_CONSTANTS);
  }

  @VisibleForTesting
  public void setStripEnumConstants(boolean b) {
    if (b) {
      includedMetadata.remove(MetadataSupport.ENUM_CONSTANTS);
    } else {
      includedMetadata.add(MetadataSupport.ENUM_CONSTANTS);
    }
  }

  public boolean stripNameMapping() {
    return !includedMetadata.contains(MetadataSupport.NAME_MAPPING);
  }

  @VisibleForTesting
  public void setStripNameMapping(boolean b) {
    if (b) {
      includedMetadata.remove(MetadataSupport.NAME_MAPPING);
    } else {
      includedMetadata.add(MetadataSupport.NAME_MAPPING);
    }
  }

  @VisibleForTesting
  public void setStripAllReflection() {
    includedMetadata = EnumSet.noneOf(MetadataSupport.class);
  }

  public boolean emitWrapperMethods() {
    return emitWrapperMethods;
  }

  @VisibleForTesting
  public void setEmitWrapperMethods(boolean b) {
    emitWrapperMethods = b;
  }

  public boolean extractUnsequencedModifications() {
    return extractUnsequencedModifications;
  }

  @VisibleForTesting
  public void enableExtractUnsequencedModifications() {
    extractUnsequencedModifications = true;
  }


  public SourceVersion getSourceVersion(){
    return sourceVersion;
  }

  @VisibleForTesting
  public void setSourceVersion(SourceVersion version) {
    sourceVersion = version;
    postProcessSourceVersion();
  }

  public boolean staticAccessorMethods() {
    return staticAccessorMethods;
  }

  @VisibleForTesting
  public void setStaticAccessorMethods(boolean b) {
    staticAccessorMethods = b;
  }

  public boolean classProperties() {
    return classProperties;
  }

  @VisibleForTesting
  public void setClassProperties(boolean b) {
    classProperties = b;
    staticAccessorMethods = b;
  }

  public String getProcessors() {
    return processors;
  }

  @VisibleForTesting
  public void setProcessors(String processors) {
    this.processors = processors;
  }

  public boolean disallowInheritedConstructors() {
    return disallowInheritedConstructors;
  }

  @VisibleForTesting
  public void setDisallowInheritedConstructors(boolean b) {
    disallowInheritedConstructors = b;
  }

  @VisibleForTesting
  public void setSwiftFriendly(boolean b) {
    setClassProperties(b);
    setNullability(b);
  }

  public boolean nullability() {
    return nullability;
  }

  @VisibleForTesting
  public void setNullability(boolean b) {
    nullability = b;
  }

  public boolean defaultNonnull() {
    return nullability && defaultNonnull;
  }

  @VisibleForTesting
  public void setDefaultNonnull(boolean b) {
    nullability = true;
    defaultNonnull = b;
  }

  public String lintArgument() {
    return lintArgument;
  }

  public TimingLevel timingLevel() {
    return timingLevel;
  }

  public boolean dumpAST() {
    return dumpAST;
  }

  public boolean reportJavadocWarnings() {
    return reportJavadocWarnings;
  }

  public boolean translateBootclasspathFiles() {
    return translateBootclasspath;
  }

  public boolean emitKytheMappings() {
    return emitKytheMappings;
  }

  @VisibleForTesting
  public void setEmitKytheMappings(boolean b) {
    emitKytheMappings = b;
  }

  public boolean emitSourceHeaders() {
    return emitSourceHeaders;
  }

  @VisibleForTesting
  public void setEmitSourceHeaders(boolean b) {
    emitSourceHeaders = b;
  }

  public ExternalAnnotations externalAnnotations() {
    return externalAnnotations;
  }

  @VisibleForTesting
  public void addExternalAnnotationFile(String file) throws IOException {
    externalAnnotations.addExternalAnnotationFile(file);
  }

  // Unreleased experimental project.
  public boolean translateClassfiles() {
    return translateClassfiles;
  }

  @VisibleForTesting
  public void setTranslateClassfiles(boolean b) {
    translateClassfiles = b;
  }

  public List<String> entryClasses() {
    return entryClasses;
  }

  public void addPlatformModuleSystemOptions(String... flags) {
    Collections.addAll(platformModuleSystemOptions, flags);
  }

  public List<String> getPlatformModuleSystemOptions() {
    return platformModuleSystemOptions;
  }
}
