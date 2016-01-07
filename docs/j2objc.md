---
title: j2objc man page
layout: docs
---

# j2objc

The **j2objc** tool translates specified Java source files into either Objective-C or
Objective-C++ sources for use in an iOS application.

    j2objc [ options ] file1.java ...

The following options are supported. For options that take a path, multiple directories and jar files are separated by a ':', like is done with the `java` and `javac` commands.

## Common options
<dl>
<dt>-sourcepath &lt;path&gt;</dt>
<dd>Specify where to find input source files.</dd>

<dt>-classpath &lt;path&gt;</dt>
<dd>Specify where to find user class files.</dd>

<dt>-d &lt;directory&gt;</dt>
<dd>Specify where to place generated Objective-C files.</dd>

<dt>-encoding &lt;encoding&gt;</dt>
<dd>Specify character encoding of source files (UTF-8 default).</dd>

<dt>-source &lt;release&gt;</dt>
<dd>Provide source compatibility with specified release.</dd>

<dt>-g</dt>
<dd>Generate debugging support.</dd>

<dt>-q, --quiet</dt>
<dd>Do not print status messages.</dd>

<dt>-v, --verbose</dt>
<dd>Output messages about what the translator is doing.</dd>

<dt>-Werror</dt>
<dd>Make all warnings into errors.</dd>

<dt>-h, --help</dt>
<dd>Print this message.</dd>
</dl>

## Translation options
<dl>
<dt>--batch-translate-max=&lt;n&gt;</dt>
<dd>The maximum number of source files that are translated together. Batching speeds up translation, but
requires more memory.</dd>

<dt>--build-closure</dt>
<dd>Translate dependent classes if they are out-of-date (like<code>javac</code>does).</dd>

<dt>--dead-code-report &lt;file&gt;</dt>
<dd>Specify a ProGuard usage report for dead code elimination.</dd>

<dt>--disallow-inherited-constructors</dt>
<dd>Issue compiler warnings when native code accesses inherited constructors.</dd>

<dt>--doc-comments</dt>
<dd>Translate Javadoc comments into Xcode-compatible comments.</dd>

<dt>--no-extract-unsequenced</dt>
<dd>Don't rewrite expressions that would produce unsequenced modification errors.</dd>

<dt>--generate-deprecated</dt>
<dd>Generate deprecated attributes for deprecated methods, classes and interfaces.</dd>

<dt>-J&lt;flag&gt;</dt>
<dd>Pass a Java &lt;flag&gt;, such as<code>-Xmx1G</code>, to the system runtime.</dd>

<dt>--mapping &lt;file&gt;</dt>
<dd>Add a method mapping file.</dd>

<dt>--no-class-methods</dt>
<dd>Don't emit class methods for static Java methods (static methods are 
always converted to functions).</dd>

<dt>--no-package-directories</dt>
<dd>Don't create directories for Java packages when generating files.</dd>

<dt>--no-segmented-headers</dt>
<dd>Generates headers with guards around each declared type. Useful for breaking import cycles.</dd>

<dt>--nullability</dt>
<dd>Converts Nullable and Nonnull annotations to Objective-C annotations.</dd>

<dt>--prefix &lt;package=prefix&gt;</dt>
<dd>Substitute a specified prefix for a package name.</dd>

<dt>--prefixes &lt;file&gt;</dt>
<dd>Specify a properties file with prefix definitions.</dd>

<dt>--preserve-full-paths</dt>
<dd>Generates output files with the same relative paths as the input files.</dd>

<dt>-processor &lt;class1&gt;[,&lt;class2&gt;...]</dt>
<dd>Names of the annotation processors to run; bypasses default discovery process.</dd>

<dt>-processorpath &lt;path&gt;</dt>
<dd>Specify where to find annotation processors.</dd>

<dt>--static-accessor-methods</dt>
<dd>Generates accessor methods for static variables and enum constants.</dd>

<dt>--strip-gwt-incompatible</dt>
<dd>Removes methods that are marked with a<code>GwtIncompatible</code>annotation, unless its value is known to be compatible.</dd>

<dt>--strip-reflection</dt>
<dd>Do not generate metadata needed for Java reflection (note: this will significantly reduce reflection support).</dd>

<dt>--swift-friendly</dt>
<dd>Generate code that facilitates Swift importing.</dd>

<dt>-t, --timing-info</dt>
<dd>Print time spent in translation steps.</dd>

<dt>-use-arc</dt>
<dd>Generate Objective-C code to support Automatic Reference Counting (ARC).</dd>

<dt>-use-reference-counting</dt>
<dd>Generate Objective-C code to support iOS manual
reference counting (default).</dd>

<dt>-version</dt>
<dd>Version information.</dd>

<dt>-x language</dt>
<dd>Specify what language to output.  Possible values
are objective-c (default) and objective-c++.</dd>

<dt>-X</dt>
<dd>Print help for nonstandard options.</dd>
</dl>

## Nonstandard options
<dl>
<dt>-Xbootclasspath:&lt;path&gt;</dt>
<dd>Boot path used by translation (not the tool itself).</dd>

<dt>-Xlint</dt>
<dd>Enable all warnings.</dd>

<dt>-Xlint:none</dt>
<dd>Disable all warnings not mandated by the Java Language Specification.</dd>

<dt>-Xlint:-<i>xxx</i></dt>
</dd>Disable warning xxx, where xxx is one of the warning names supported for -Xlint:xxx, below.</dd>

<dt>-Xlint:cast</dt>
<dd>Warn about unnecessary and redundant cast expressions.</dd>

<dt>-Xlint:deprecation</dt>
<dd>Warn about the use of deprecated items.</dd>

<dt>-Xlint:dep-ann</dt>
</dd>Warn about items that are documented with an @deprecated Javadoc comment,
but do not have a @Deprecated annotation.</dd>

<dt>-Xlint:empty</dt>
<dd>Warn about empty statements.</dd>

<dt>-Xlint:fallthrough</dt>
<dd>Check switch blocks for fall-through cases and provide a warning message for any that are found.</dd>

<dt>-Xlint:finally</dt>
<dd>Warn about finally clauses that cannot complete normally.</dd>

<dt>-Xlint:rawtypes</dt>
<dd>Warn about unchecked operations on raw types.</dd>

<dt>-Xlint:serial</dt>
<dd>Warn about missing serialVersionUID definitions on serializable classes.</dd>

<dt>-Xlint:static</dt>
<dd>Warn about serial methods called on instances.</dd>

<dt>-Xlint:unchecked</dt>
<dd>Give more detail for unchecked conversion warnings that are mandated by the Java Language Specification.</dd>

<dt>-Xlint:varargs</dt>
<dd>Warn about unsafe usages of variable arguments (varargs) methods, in particular, those that contain
non-reifiable arguments.</dd>

<dt>-Xno-jsni-warnings</dt>
<dd>Warn if JSNI (GWT) native code delimiters are used instead of OCNI delimiters.<dd>
</dl>

<h2>See Also</h2>

<dl><dd><a href="j2objcc.html">j2objcc</a></dd></dl>
