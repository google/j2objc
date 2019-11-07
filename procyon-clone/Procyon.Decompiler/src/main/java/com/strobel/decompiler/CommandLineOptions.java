/*
 * CommandLineOptions.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is based on Mono.Cecil from Jb Evain, Copyright (c) Jb Evain;
 * and ILSpy/ICSharpCode from SharpDevelop, Copyright (c) AlphaSierraPapa.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.decompiler;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

public class CommandLineOptions {
    @Parameter(description = "<type names or class/jar files>")
    private final List<String> _inputs = new ArrayList<>();

    @Parameter(
        names = { "-?", "--help" },
        help = true,
        description = "Display this usage information and exit.")
    private boolean _printUsage;

    @Parameter(
        names = { "-mv", "--merge-variables" },
        description = "Attempt to merge as many variables as possible.  This may lead to fewer declarations, " +
                      "but at the expense of inlining and useful naming.  This feature is experimental and " +
                      "may be removed or become the standard behavior in future releases.")
    private boolean _mergeVariables;

    @Parameter(
        names = { "-ei", "--explicit-imports" },
        description = "[DEPRECATED] Explicit imports are now enabled by default.  " +
                      "This option will be removed in a future release.")
    private boolean _forceExplicitImports;

    @Parameter(
        names = { "-ci", "--collapse-imports" },
        description = "Collapse multiple imports from the same package into a single wildcard import.")
    private boolean _collapseImports;

    @Parameter(
        names = { "-eta", "--explicit-type-arguments" },
        description = "Always print type arguments to generic methods.")
    private boolean _forceExplicitTypeArguments;

    @Parameter(
        names = { "-ec", "--retain-explicit-casts" },
        description = "Do not remove redundant explicit casts.")
    private boolean _retainRedundantCasts;

    @Parameter(
        names = { "-fsb", "--flatten-switch-blocks" },
        description = "Drop the braces statements around switch sections when possible.")
    private boolean _flattenSwitchBlocks;

    @Parameter(
        names = { "-ss", "--show-synthetic" },
        description = "Show synthetic (compiler-generated) members.")
    private boolean _showSyntheticMembers;

    @Parameter(
        names = { "-b", "--bytecode-ast" },
        description = "Output Bytecode AST instead of Java.")
    private boolean _bytecodeAst;

    @Parameter(
        names = { "-r", "--raw-bytecode" },
        description = "Output Raw Bytecode instead of Java (to control the level of detail, see: -cp, -lv, -ta, -v).")
    private boolean _rawBytecode;

    @Parameter(
        names = { "-cp", "--constant-pool" },
        description = "Includes the constant pool when displaying raw bytecode (unnecessary with -v).")
    private boolean _showConstantPool;

    @Parameter(
        names = { "-lv", "--local-variables" },
        description = "Includes the local variable tables when displaying raw bytecode (unnecessary with -v).")
    private boolean _showLocalVariableDetails;

    @Parameter(
        names = { "-ta", "--type-attributes" },
        description = "Includes type attributes when displaying raw bytecode (unnecessary with -v).")
    private boolean _showTypeAttributes;

    @Parameter(
        names = { "-v", "--verbose" },
        description = "Includes more detailed output depending on the output language (currently only supported for raw bytecode).")
    private boolean _verbose;

    @Parameter(
        names = { "-u", "--unoptimized" },
        description = "Show unoptimized code (only in combination with -b).")
    private boolean _unoptimized;

    @Parameter(
        names = { "-ent", "--exclude-nested" },
        description = "Exclude nested types when decompiling their enclosing types.")
    private boolean _excludeNestedTypes;

    @Parameter(
        names = { "-o", "--output-directory" },
        description = "Write decompiled results to specified directory instead of the console.")
    private String _outputDirectory;

    @Parameter(
        names = { "-jar", "--jar-file" },
        description = "[DEPRECATED] Decompile all classes in the specified jar file (disables -ent and -s).")
    private String _jarFile;

    @Parameter(
	       names = { "-ln", "--with-line-numbers" },
        description = "Include line numbers in raw bytecode mode; supports Java mode with -o only.")
    private boolean _includeLineNumbers;

    @Parameter(
        names = { "-sl", "--stretch-lines" },
        description = "Stretch Java lines to match original line numbers (only in combination with -o) [EXPERIMENTAL].")
    private boolean _stretchLines;
    
    @Parameter(
        names = { "-dl", "--debug-line-numbers" },
        description = "For debugging, show Java line numbers as inline comments (implies -ln; requires -o).")
    private boolean _showDebugLineNumbers;

    @Parameter(
        names = { "-ps", "--retain-pointless-switches" },
        description = "Do not lift the contents of switches having only a default label.")
    private boolean _retainPointlessSwitches;

    @Parameter(
        names = { "-ll", "--log-level" },
        description = "Set the level of log verbosity (0-3).  Level 0 disables logging.",
        arity = 1)
    private int _logLevel;

    @Parameter(
        names = { "-lc", "--light" },
        description = "Use a color scheme designed for consoles with light background colors.")
    private boolean _useLightColorScheme;

    @Parameter(
        names = { "--unicode" },
        description = "Enable Unicode output (printable non-ASCII characters will not be escaped).")
    private boolean _isUnicodeOutputEnabled;

    @Parameter(
        names = { "-eml", "--eager-method-loading" },
        description = "Enable eager loading of method bodies (may speed up decompilation of larger archives).")
    private boolean _isEagerMethodLoadingEnabled;

    @Parameter(
        names = { "-sm", "--simplify-member-references" },
        description = "Simplify type-qualified member references in Java output [EXPERIMENTAL].")
    private boolean _simplifyMemberReferences;


    @Parameter(
        names = { "-fq", "--force-qualified-references" },
        description = "Force fully qualified type and member references in Java output.")
    private boolean _forceFullyQualifiedReferences;

    @Parameter(
        names = { "--disable-foreach" },
        description = "Disable 'for each' loop transforms.")
    private boolean _disableForEachTransforms;

    @Parameter(
        names = { "--version" },
        description = "Display the decompiler version and exit.")
    private boolean _printVersion;

    @Parameter(
        names = { "--suppress-banner" },
        description = "Do not display the 'Decompiled by Procyon' banner in output.",
        hidden = true)
    private boolean _suppressBanner;

    public final List<String> getInputs() {
        return _inputs;
    }

    public final boolean isBytecodeAst() {
        return _bytecodeAst;
    }

    public final boolean isRawBytecode() {
        return _rawBytecode;
    }

    public final boolean isVerbose() {
        return _verbose;
    }

    public final boolean getShowConstantPool() {
        return _showConstantPool;
    }

    public final boolean getShowLocalVariableDetails() {
        return _showLocalVariableDetails;
    }

    public final boolean getShowTypeAttributes() {
        return _showTypeAttributes;
    }

    public final boolean getFlattenSwitchBlocks() {
        return _flattenSwitchBlocks;
    }

    public final boolean getExcludeNestedTypes() {
        return _excludeNestedTypes;
    }

    public final void setExcludeNestedTypes(final boolean excludeNestedTypes) {
        _excludeNestedTypes = excludeNestedTypes;
    }

    public final void setFlattenSwitchBlocks(final boolean flattenSwitchBlocks) {
        _flattenSwitchBlocks = flattenSwitchBlocks;
    }

    public final boolean getCollapseImports() {
        return _collapseImports;
    }

    public final void setCollapseImports(final boolean collapseImports) {
        _collapseImports = collapseImports;
    }

    public final boolean getForceExplicitTypeArguments() {
        return _forceExplicitTypeArguments;
    }

    public final void setForceExplicitTypeArguments(final boolean forceExplicitTypeArguments) {
        _forceExplicitTypeArguments = forceExplicitTypeArguments;
    }

    public boolean getRetainRedundantCasts() {
        return _retainRedundantCasts;
    }

    public void setRetainRedundantCasts(final boolean retainRedundantCasts) {
        _retainRedundantCasts = retainRedundantCasts;
    }

    public final void setRawBytecode(final boolean rawBytecode) {
        _rawBytecode = rawBytecode;
    }

    public final void setBytecodeAst(final boolean bytecodeAst) {
        _bytecodeAst = bytecodeAst;
    }

    public final boolean isUnoptimized() {
        return _unoptimized;
    }

    public final void setUnoptimized(final boolean unoptimized) {
        _unoptimized = unoptimized;
    }

    public final boolean getShowSyntheticMembers() {
        return _showSyntheticMembers;
    }

    public final void setShowSyntheticMembers(final boolean showSyntheticMembers) {
        _showSyntheticMembers = showSyntheticMembers;
    }

    public final boolean getPrintUsage() {
        return _printUsage;
    }

    public final void setPrintUsage(final boolean printUsage) {
        _printUsage = printUsage;
    }

    public final String getOutputDirectory() {
        return _outputDirectory;
    }

    public final void setOutputDirectory(final String outputDirectory) {
        _outputDirectory = outputDirectory;
    }

    public final String getJarFile() {
        return _jarFile;
    }

    public final void setJarFile(final String jarFile) {
        _jarFile = jarFile;
    }

    public final boolean getIncludeLineNumbers() {
        return _includeLineNumbers;
    }

    public final void setIncludeLineNumbers(final boolean includeLineNumbers) {
        _includeLineNumbers = includeLineNumbers;
    }

    public final boolean getStretchLines() {
        return _stretchLines;
    }

    public final void setStretchLines(final boolean stretchLines) {
        _stretchLines = stretchLines;
    }

    public final boolean getShowDebugLineNumbers() {
        return _showDebugLineNumbers;
    }

    public final void setShowDebugLineNumbers(final boolean showDebugLineNumbers) {
        _showDebugLineNumbers = showDebugLineNumbers;
    }

    public final boolean getRetainPointlessSwitches() {
        return _retainPointlessSwitches;
    }

    public final void setRetainPointlessSwitches(final boolean retainPointlessSwitches) {
        _retainPointlessSwitches = retainPointlessSwitches;
    }

    public final int getLogLevel() {
        return _logLevel;
    }

    public final void setLogLevel(final int logLevel) {
        _logLevel = logLevel;
    }

    public final boolean getUseLightColorScheme() {
        return _useLightColorScheme;
    }

    public final void setUseLightColorScheme(final boolean useLightColorScheme) {
        _useLightColorScheme = useLightColorScheme;
    }

    public final boolean isUnicodeOutputEnabled() {
        return _isUnicodeOutputEnabled;
    }

    public final void setUnicodeOutputEnabled(final boolean unicodeOutputEnabled) {
        _isUnicodeOutputEnabled = unicodeOutputEnabled;
    }

    public final boolean getMergeVariables() {
        return _mergeVariables;
    }

    public final void setMergeVariables(final boolean mergeVariables) {
        _mergeVariables = mergeVariables;
    }

    public final boolean isEagerMethodLoadingEnabled() {
        return _isEagerMethodLoadingEnabled;
    }

    public final void setEagerMethodLoadingEnabled(final boolean isEagerMethodLoadingEnabled) {
        _isEagerMethodLoadingEnabled = isEagerMethodLoadingEnabled;
    }

    public final boolean getSimplifyMemberReferences() {
        return _simplifyMemberReferences;
    }

    public final void setSimplifyMemberReferences(final boolean simplifyMemberReferences) {
        _simplifyMemberReferences = simplifyMemberReferences;
    }

    public boolean getForceFullyQualifiedReferences() {
        return _forceFullyQualifiedReferences;
    }

    public void setForceFullyQualifiedReferences(final boolean forceFullyQualifiedReferences) {
        _forceFullyQualifiedReferences = forceFullyQualifiedReferences;
    }

    public final boolean getDisableForEachTransforms() {
        return _disableForEachTransforms;
    }

    public final void setDisableForEachTransforms(final boolean disableForEachTransforms) {
        _disableForEachTransforms = disableForEachTransforms;
    }

    public final boolean getPrintVersion() {
        return _printVersion;
    }

    public final void setPrintVersion(final boolean printVersion) {
        _printVersion = printVersion;
    }

    public final boolean getSuppressBanner() {
        return _suppressBanner;
    }

    public final void setSuppressBanner(final boolean suppressBanner) {
        _suppressBanner = suppressBanner;
    }
}
