/*
 * JavaFormattingOptions.java
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

package com.strobel.decompiler.languages.java;

@SuppressWarnings("PublicField")
public class JavaFormattingOptions {
    public boolean IndentNamespaceBody;
    public boolean IndentClassBody;
    public boolean IndentInterfaceBody;
    public boolean IndentEnumBody;
    public boolean IndentMethodBody;
    public boolean IndentBlocks;
    public boolean IndentSwitchBody;
    public boolean IndentCaseBody;
    public boolean IndentBreakStatements;
    public boolean AlignEmbeddedUsingStatements;
    public boolean AlignEmbeddedIfStatements;
    public BraceStyle AnonymousClassBraceStyle = BraceStyle.DoNotChange;
    public BraceStyle ClassBraceStyle = BraceStyle.DoNotChange;
    public BraceStyle InterfaceBraceStyle = BraceStyle.DoNotChange;
    public BraceStyle AnnotationBraceStyle = BraceStyle.DoNotChange;
    public BraceStyle EnumBraceStyle = BraceStyle.DoNotChange;
    public BraceStyle MethodBraceStyle = BraceStyle.DoNotChange;
    public BraceStyle InitializerBlockBraceStyle = BraceStyle.DoNotChange;
    public BraceStyle ConstructorBraceStyle = BraceStyle.DoNotChange;
    public BraceStyle EventBraceStyle = BraceStyle.DoNotChange;
    public BraceStyle EventAddBraceStyle = BraceStyle.DoNotChange;
    public BraceStyle EventRemoveBraceStyle = BraceStyle.DoNotChange;
    public BraceStyle StatementBraceStyle = BraceStyle.DoNotChange;
    public boolean AllowIfBlockInline;
    public BraceEnforcement IfElseBraceEnforcement = BraceEnforcement.DoNotChange;
    public BraceEnforcement ForBraceEnforcement = BraceEnforcement.DoNotChange;
    public BraceEnforcement ForEachBraceEnforcement = BraceEnforcement.DoNotChange;
    public BraceEnforcement WhileBraceEnforcement = BraceEnforcement.DoNotChange;
    public BraceEnforcement UsingBraceEnforcement = BraceEnforcement.DoNotChange;
    public BraceEnforcement FixedBraceEnforcement = BraceEnforcement.DoNotChange;
    public boolean PlaceElseOnNewLine;
    public boolean PlaceElseIfOnNewLine;
    public boolean PlaceCatchOnNewLine;
    public boolean PlaceFinallyOnNewLine;
    public boolean PlaceWhileOnNewLine;
    public boolean SpaceBeforeMethodDeclarationParentheses;
    public boolean SpaceBetweenEmptyMethodDeclarationParentheses;
    public boolean SpaceBeforeMethodDeclarationParameterComma;
    public boolean SpaceAfterMethodDeclarationParameterComma;
    public boolean SpaceWithinMethodDeclarationParentheses;
    public boolean SpaceBeforeMethodCallParentheses;
    public boolean SpaceBetweenEmptyMethodCallParentheses;
    public boolean SpaceBeforeMethodCallParameterComma;
    public boolean SpaceAfterMethodCallParameterComma;
    public boolean SpaceWithinMethodCallParentheses;
    public boolean SpaceBeforeFieldDeclarationComma;
    public boolean SpaceAfterFieldDeclarationComma;
    public boolean SpaceBeforeLocalVariableDeclarationComma;
    public boolean SpaceAfterLocalVariableDeclarationComma;
    public boolean SpaceBeforeConstructorDeclarationParentheses;
    public boolean SpaceBetweenEmptyConstructorDeclarationParentheses;
    public boolean SpaceBeforeConstructorDeclarationParameterComma;
    public boolean SpaceAfterConstructorDeclarationParameterComma;
    public boolean SpaceWithinConstructorDeclarationParentheses;
    public boolean SpaceWithinEnumDeclarationParentheses;
    public boolean SpaceBeforeIndexerDeclarationBracket;
    public boolean SpaceWithinIndexerDeclarationBracket;
    public boolean SpaceBeforeIndexerDeclarationParameterComma;
    public boolean SpaceAfterIndexerDeclarationParameterComma;
    public boolean SpaceBeforeDelegateDeclarationParentheses;
    public boolean SpaceBetweenEmptyDelegateDeclarationParentheses;
    public boolean SpaceBeforeDelegateDeclarationParameterComma;
    public boolean SpaceAfterDelegateDeclarationParameterComma;
    public boolean SpaceWithinDelegateDeclarationParentheses;
    public boolean SpaceBeforeNewParentheses;
    public boolean SpaceBeforeIfParentheses;
    public boolean SpaceBeforeWhileParentheses;
    public boolean SpaceBeforeForParentheses;
    public boolean SpaceBeforeForeachParentheses;
    public boolean SpaceBeforeCatchParentheses;
    public boolean SpaceBeforeSwitchParentheses;
    public boolean SpaceBeforeSynchronizedParentheses;
    public boolean SpaceBeforeUsingParentheses;
    public boolean SpaceAroundAssignment;
    public boolean SpaceAroundLogicalOperator;
    public boolean SpaceAroundEqualityOperator;
    public boolean SpaceAroundRelationalOperator;
    public boolean SpaceAroundBitwiseOperator;
    public boolean SpaceAroundAdditiveOperator;
    public boolean SpaceAroundMultiplicativeOperator;
    public boolean SpaceAroundShiftOperator;
    public boolean SpaceAroundNullCoalescingOperator;
    public boolean SpacesWithinParentheses;
    public boolean SpacesWithinIfParentheses;
    public boolean SpacesWithinWhileParentheses;
    public boolean SpacesWithinForParentheses;
    public boolean SpacesWithinForeachParentheses;
    public boolean SpacesWithinCatchParentheses;
    public boolean SpacesWithinSwitchParentheses;
    public boolean SpacesWithinSynchronizedParentheses;
    public boolean SpacesWithinUsingParentheses;
    public boolean SpacesWithinCastParentheses;
    public boolean SpacesWithinNewParentheses;
    public boolean SpacesBetweenEmptyNewParentheses;
    public boolean SpaceBeforeNewParameterComma;
    public boolean SpaceAfterNewParameterComma;
    public boolean SpaceBeforeConditionalOperatorCondition;
    public boolean SpaceAfterConditionalOperatorCondition;
    public boolean SpaceBeforeConditionalOperatorSeparator;
    public boolean SpaceAfterConditionalOperatorSeparator;
    public boolean SpacesWithinBrackets;
    public boolean SpacesBeforeBrackets;
    public boolean SpaceBeforeBracketComma;
    public boolean SpaceAfterBracketComma;
    public boolean SpaceBeforeForSemicolon;
    public boolean SpaceAfterForSemicolon;
    public boolean SpaceAfterTypecast;
    public boolean SpaceBeforeArrayDeclarationBrackets;
    public boolean SpaceInNamedArgumentAfterDoubleColon;
    public int BlankLinesAfterPackageDeclaration;
    public int BlankLinesAfterImports;
    public int BlankLinesBeforeFirstDeclaration;
    public int BlankLinesBetweenTypes;
    public int BlankLinesBetweenFields;
    public int BlankLinesBetweenEventFields;
    public int BlankLinesBetweenMembers;
    public boolean KeepCommentsAtFirstColumn;
    public Wrapping ArrayInitializerWrapping = Wrapping.DoNotWrap;
    public BraceStyle ArrayInitializerBraceStyle = BraceStyle.DoNotChange;

    public static JavaFormattingOptions createDefault() {
        final JavaFormattingOptions options = new JavaFormattingOptions();

        options.IndentNamespaceBody = true;
        options.IndentClassBody = true;
        options.IndentInterfaceBody = true;
        options.IndentEnumBody = true;
        options.IndentMethodBody = true;
        options.IndentBlocks = true;
        options.IndentSwitchBody = false;
        options.IndentCaseBody = true;
        options.IndentBreakStatements = true;
        options.ClassBraceStyle = BraceStyle.NextLine;
        options.AnonymousClassBraceStyle = BraceStyle.EndOfLine;
        options.InterfaceBraceStyle = BraceStyle.NextLine;
        options.AnnotationBraceStyle = BraceStyle.EndOfLine;
        options.EnumBraceStyle = BraceStyle.NextLine;
        options.MethodBraceStyle = BraceStyle.EndOfLine;
        options.ConstructorBraceStyle = BraceStyle.EndOfLine;

        options.EventBraceStyle = BraceStyle.EndOfLine;
        options.EventAddBraceStyle = BraceStyle.EndOfLine;
        options.EventRemoveBraceStyle = BraceStyle.EndOfLine;
        options.StatementBraceStyle = BraceStyle.EndOfLine;

        options.PlaceElseOnNewLine = false;
        options.PlaceCatchOnNewLine = false;
        options.PlaceFinallyOnNewLine = false;
        options.PlaceWhileOnNewLine = false;
        options.ArrayInitializerWrapping = Wrapping.WrapIfTooLong;
        options.ArrayInitializerBraceStyle = BraceStyle.EndOfLine;

        options.SpaceBeforeMethodCallParentheses = false;
        options.SpaceBeforeMethodDeclarationParentheses = false;
        options.SpaceBeforeConstructorDeclarationParentheses = false;
        options.SpaceBeforeDelegateDeclarationParentheses = false;
        options.SpaceAfterMethodCallParameterComma = true;
        options.SpaceAfterConstructorDeclarationParameterComma = true;

        options.SpaceBeforeNewParentheses = false;
        options.SpacesWithinNewParentheses = false;
        options.SpacesBetweenEmptyNewParentheses = false;
        options.SpaceBeforeNewParameterComma = false;
        options.SpaceAfterNewParameterComma = true;

        options.SpaceBeforeIfParentheses = true;
        options.SpaceBeforeWhileParentheses = true;
        options.SpaceBeforeForParentheses = true;
        options.SpaceBeforeForeachParentheses = true;
        options.SpaceBeforeCatchParentheses = true;
        options.SpaceBeforeSwitchParentheses = true;
        options.SpaceBeforeSynchronizedParentheses = true;
        options.SpaceBeforeUsingParentheses = true;
        options.SpaceAroundAssignment = true;
        options.SpaceAroundLogicalOperator = true;
        options.SpaceAroundEqualityOperator = true;
        options.SpaceAroundRelationalOperator = true;
        options.SpaceAroundBitwiseOperator = true;
        options.SpaceAroundAdditiveOperator = true;
        options.SpaceAroundMultiplicativeOperator = true;
        options.SpaceAroundShiftOperator = true;
        options.SpaceAroundNullCoalescingOperator = true;
        options.SpacesWithinParentheses = false;
        options.SpaceWithinMethodCallParentheses = false;
        options.SpaceWithinMethodDeclarationParentheses = false;
        options.SpacesWithinIfParentheses = false;
        options.SpacesWithinWhileParentheses = false;
        options.SpacesWithinForParentheses = false;
        options.SpacesWithinForeachParentheses = false;
        options.SpacesWithinCatchParentheses = false;
        options.SpacesWithinSwitchParentheses = false;
        options.SpacesWithinSynchronizedParentheses = false;
        options.SpacesWithinUsingParentheses = false;
        options.SpacesWithinCastParentheses = false;
        options.SpaceBeforeConditionalOperatorCondition = true;
        options.SpaceAfterConditionalOperatorCondition = true;
        options.SpaceBeforeConditionalOperatorSeparator = true;
        options.SpaceAfterConditionalOperatorSeparator = true;

        options.SpacesWithinBrackets = false;
        options.SpacesBeforeBrackets = true;
        options.SpaceBeforeBracketComma = false;
        options.SpaceAfterBracketComma = true;

        options.SpaceBeforeForSemicolon = false;
        options.SpaceAfterForSemicolon = true;
        options.SpaceAfterTypecast = false;

        options.AlignEmbeddedIfStatements = true;
        options.AlignEmbeddedUsingStatements = true;
        options.SpaceBeforeMethodDeclarationParameterComma = false;
        options.SpaceAfterMethodDeclarationParameterComma = true;
        options.SpaceBeforeFieldDeclarationComma = false;
        options.SpaceAfterFieldDeclarationComma = true;
        options.SpaceBeforeLocalVariableDeclarationComma = false;
        options.SpaceAfterLocalVariableDeclarationComma = true;

        options.SpaceBeforeIndexerDeclarationBracket = true;
        options.SpaceWithinIndexerDeclarationBracket = false;
        options.SpaceBeforeIndexerDeclarationParameterComma = false;
        options.SpaceInNamedArgumentAfterDoubleColon = true;

        options.SpaceAfterIndexerDeclarationParameterComma = true;

        options.BlankLinesAfterPackageDeclaration = 1;
        options.BlankLinesAfterPackageDeclaration = 0;
        options.BlankLinesAfterImports = 1;

        options.BlankLinesBeforeFirstDeclaration = 0;
        options.BlankLinesBetweenTypes = 1;
        options.BlankLinesBetweenFields = 0;
        options.BlankLinesBetweenEventFields = 0;
        options.BlankLinesBetweenMembers = 1;

        options.KeepCommentsAtFirstColumn = true;

        return options;
    }
}