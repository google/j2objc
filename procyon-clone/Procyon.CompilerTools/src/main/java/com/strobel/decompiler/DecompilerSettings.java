/*
 * DecompilerSettings.java
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

import com.strobel.assembler.metadata.ITypeLoader;
import com.strobel.decompiler.languages.BytecodeOutputOptions;
import com.strobel.decompiler.languages.Language;
import com.strobel.decompiler.languages.Languages;
import com.strobel.decompiler.languages.java.JavaFormattingOptions;

public class DecompilerSettings {
    private ITypeLoader _typeLoader;
    private boolean _includeLineNumbersInBytecode = true;
    private boolean _showSyntheticMembers;
    private boolean _alwaysGenerateExceptionVariableForCatchBlocks = true;
    private boolean _forceFullyQualifiedReferences;
    private boolean _forceExplicitImports;
    private boolean _forceExplicitTypeArguments;
    private boolean _flattenSwitchBlocks;
    private boolean _excludeNestedTypes;
    private boolean _retainRedundantCasts;
    private boolean _retainPointlessSwitches;
    private boolean _isUnicodeOutputEnabled;
    private boolean _includeErrorDiagnostics = true;
    private boolean _mergeVariables;
    private boolean _disableForEachTransforms;
    private JavaFormattingOptions _javaFormattingOptions;
    private BytecodeOutputOptions _bytecodeOutputOptions;
    private Language _language;
    private String _outputFileHeaderText;
    private String _outputDirectory;
    private boolean _showDebugLineNumbers;
    private boolean _simplifyMemberReferences;

    public DecompilerSettings() {
    }

    public final boolean getExcludeNestedTypes() {
        return _excludeNestedTypes;
    }

    public final void setExcludeNestedTypes(final boolean excludeNestedTypes) {
        _excludeNestedTypes = excludeNestedTypes;
    }

    public final boolean getFlattenSwitchBlocks() {
        return _flattenSwitchBlocks;
    }

    public final void setFlattenSwitchBlocks(final boolean flattenSwitchBlocks) {
        _flattenSwitchBlocks = flattenSwitchBlocks;
    }

    public final boolean getForceExplicitImports() {
        return _forceExplicitImports;
    }

    public final void setForceExplicitImports(final boolean forceExplicitImports) {
        _forceExplicitImports = forceExplicitImports;
    }

    public final boolean getForceExplicitTypeArguments() {
        return _forceExplicitTypeArguments;
    }

    public final void setForceExplicitTypeArguments(final boolean forceExplicitTypeArguments) {
        _forceExplicitTypeArguments = forceExplicitTypeArguments;
    }

    public final String getOutputFileHeaderText() {
        return _outputFileHeaderText;
    }

    public final void setOutputFileHeaderText(final String outputFileHeaderText) {
        _outputFileHeaderText = outputFileHeaderText;
    }

    public final ITypeLoader getTypeLoader() {
        return _typeLoader;
    }

    public final void setTypeLoader(final ITypeLoader typeLoader) {
        _typeLoader = typeLoader;
    }

    public final Language getLanguage() {
        return _language != null ? _language : Languages.java();
    }

    public final void setLanguage(final Language language) {
        _language = language;
    }

    public final boolean getShowSyntheticMembers() {
        return _showSyntheticMembers;
    }

    public final void setShowSyntheticMembers(final boolean showSyntheticMembers) {
        _showSyntheticMembers = showSyntheticMembers;
    }

    public final JavaFormattingOptions getJavaFormattingOptions() {
        return _javaFormattingOptions;
    }

    public final void setJavaFormattingOptions(final JavaFormattingOptions javaFormattingOptions) {
        _javaFormattingOptions = javaFormattingOptions;
    }

    public final BytecodeOutputOptions getBytecodeOutputOptions() { return _bytecodeOutputOptions; }

    public final void setBytecodeOutputOptions(final BytecodeOutputOptions bytecodeOutputOptions) { _bytecodeOutputOptions = bytecodeOutputOptions; }

    public final boolean getAlwaysGenerateExceptionVariableForCatchBlocks() {
        return _alwaysGenerateExceptionVariableForCatchBlocks;
    }

    public final void setAlwaysGenerateExceptionVariableForCatchBlocks(final boolean value) {
        _alwaysGenerateExceptionVariableForCatchBlocks = value;
    }

    public final String getOutputDirectory() {
        return _outputDirectory;
    }

    public final void setOutputDirectory(final String outputDirectory) {
        _outputDirectory = outputDirectory;
    }

    public final boolean getRetainRedundantCasts() {
        return _retainRedundantCasts;
    }

    public final void setRetainRedundantCasts(final boolean retainRedundantCasts) {
        _retainRedundantCasts = retainRedundantCasts;
    }

    public final boolean getIncludeErrorDiagnostics() {
        return _includeErrorDiagnostics;
    }

    public final void setIncludeErrorDiagnostics(final boolean value) {
        _includeErrorDiagnostics = value;
    }

    public final boolean getIncludeLineNumbersInBytecode() {
        return _includeLineNumbersInBytecode;
    }

    public final void setIncludeLineNumbersInBytecode(final boolean value) {
        _includeLineNumbersInBytecode = value;
    }

    public final boolean getRetainPointlessSwitches() {
        return _retainPointlessSwitches;
    }

    public final void setRetainPointlessSwitches(final boolean retainPointlessSwitches) {
        _retainPointlessSwitches = retainPointlessSwitches;
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

    public final void setShowDebugLineNumbers(final boolean showDebugLineNumbers) {
        _showDebugLineNumbers = showDebugLineNumbers;
    }
    
    public final boolean getShowDebugLineNumbers() {
        return _showDebugLineNumbers;
    }

    public final boolean getSimplifyMemberReferences() {
        return _simplifyMemberReferences;
    }

    public final void setSimplifyMemberReferences(final boolean simplifyMemberReferences) {
        _simplifyMemberReferences = simplifyMemberReferences;
    }

    public final boolean getDisableForEachTransforms() {
        return _disableForEachTransforms;
    }

    public final void setDisableForEachTransforms(final boolean disableForEachTransforms) {
        _disableForEachTransforms = disableForEachTransforms;
    }

    public boolean getForceFullyQualifiedReferences() {
        return _forceFullyQualifiedReferences;
    }

    public void setForceFullyQualifiedReferences(final boolean forceFullyQualifiedReferences) {
        _forceFullyQualifiedReferences = forceFullyQualifiedReferences;
    }

    public static DecompilerSettings javaDefaults() {
        final DecompilerSettings settings = new DecompilerSettings();
        settings.setJavaFormattingOptions(JavaFormattingOptions.createDefault());
        return settings;
    }
}
