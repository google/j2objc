---
layout: default
---

# j2objc

The **j2objc** tool translates specified Java source files into either Objective-C or
Objective-C++ sources for use in an iOS application.

    j2objc [ options ] file1.java ...

The following options are supported. For options that take a path, multiple directories and jar files are separated by a ':', like is done with the `java` and `javac` commands.

#### Common options
<dl>
<dt>-sourcepath &lt;path&gt;</dt>
<dd>Specify where to find input source files</dd>

<dt>-classpath &lt;path&gt;</dt>
<dd>Specify where to find user class files</dd>

<dt>-d &lt;directory&gt;</dt>
<dd>Specify where to place generated Objective-C files</dd>

<dt>-encoding &lt;encoding&gt;</dt>
<dd>Specify character encoding of source files (UTF-8 default)</dd>

<dt>-g</dt>
<dd>Generate debugging support</dd>

<dt>-q, --quiet</dt>
<dd>Do not print status messages</dd>

<dt>-v, --verbose</dt>
<dd>Output messages about what the translator is doing</dd>

<dt>-Werror</dt>
<dd>Make all warnings into errors</dd>

<dt>-h, --help</dt>
<dd>Print this message.</dd>
</dl>

#### Other options
<dl>
<dt>--batch-translate-max=&lt;n&gt;</dt>
<dd>The maximum number of source files that are translated together. Batching speeds up translation, but
requires more memory.</dd>

<dt>--build-closure</dt>
<dd>Translate dependent classes if they are out-of-date (like<code>javac</code>does).

<dt>--dead-code-report &lt;file&gt;</dt>
<dd>Specify a ProGuard usage report for dead code elimination.</dd>

<dt>--doc-comments</dt>
<dd>Translate Javadoc comments into Xcode-compatible comments.</dd>

<dt>--extract-unsequenced</dt>
<dd>Rewrite expressions that would produce unsequenced modification errors.</dd>

<dt>--generate-deprecated</dt>
<dd>Generate deprecated attributes for deprecated methods, classes and interfaces.</dd>

<dt>-J&lt;flag&gt;</dt>
<dd>Pass a Java <flag>, such as<code>-Xmx1G</code>, to the system runtime.

<dt>--mapping &lt;file&gt;</dt>
<dd>Add a method mapping file</dd>

<dt>--no-class-methods</dt>
<dd>Don't emit class methods for static Java methods (static methods are always converted to functions)</dd>

<dt>--no-final-methods-functions</dt>
<dd>Disable generating functions for final methods.</dd>

<dt>--no-hide-private-members</dt>
<dd>Includes private fields and methods in header file.</dd>

<dt>--no-package-directories</dt>
<dd>Don't create directories for Java packages when generating files.</dd>

<dt>--prefix &lt;package=prefix&gt;</dt>
<dd>Substitute a specified prefix for a package name</dd>

<dt>--prefixes &lt;file&gt;</dt>
<dd>Specify a properties file with prefix definitions</dd>

<dt>--strip-gwt-incompatible</dt>
<dd>Removes methods that are marked with a<code>GwtIncompatible</code>annotation, unless its value is known to be compatible.</dd>

<dt>--strip-reflection</dt>
<dd>Do not generate metadata needed for Java reflection (note: this will significantly reduce reflection support).</dd>

<dt>--segmented-headers</dt>
<dd>Generates headers with guards around each declared type. Useful for breaking import cycles.</dd>

<dt>-t, --timing-info</dt>
<dd>Print time spent in translation steps</dd>

<dt>-use-arc</dt>
<dd>Generate Objective-C code to support Automatic Reference Counting (ARC)</dd>

<dt>-use-reference-counting</dt>
<dd>Generate Objective-C code to support iOS manual
reference counting (default)</dd>

<dt>-x language</dt>
<dd>Specify what language to output.  Possible values
are objective-c (default) and objective-c++</dd>

<dt>-Xbootclasspath:&lt;path&gt;</dt>
<dd>Boot path used by translation (not the tool itself).</dd>
</dl>

<h3>See Also</h3>

<dl><dd><a href="j2objcc.html">j2objcc</a></dd></dl>
