/*
 * BytecodeFormattingOptions.java
 *
 * Copyright (c) 2015 Mike Strobel
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

package com.strobel.decompiler.languages;

public class BytecodeOutputOptions {
    public boolean showTypeHeader;
    public boolean showTypeAttributes;
    public boolean showConstantPool;

    public boolean showFieldFlags;
    public boolean showFieldAttributes;

    public boolean showMethodsFlags;
    public boolean showMethodAttributes;
    public boolean showMethodsStack;
    public boolean showLineNumbers;
    public boolean showLocalVariableTables;

    public static BytecodeOutputOptions createDefault() {
        final BytecodeOutputOptions options = new BytecodeOutputOptions();

        options.showTypeHeader = true;
        options.showTypeAttributes = false;
        options.showConstantPool = false;

        options.showFieldFlags = true;
        options.showFieldAttributes = true;

        options.showMethodsFlags = true;
        options.showMethodAttributes = true;
        options.showMethodsStack = false;
        options.showLineNumbers = false;
        options.showLocalVariableTables = false;

        return options;
    }

    public static BytecodeOutputOptions createVerbose() {
        final BytecodeOutputOptions options = new BytecodeOutputOptions();

        options.showTypeHeader = true;
        options.showTypeAttributes = true;
        options.showConstantPool = true;

        options.showFieldFlags = true;
        options.showFieldAttributes = true;

        options.showMethodsFlags = true;
        options.showMethodAttributes = true;
        options.showMethodsStack = true;
        options.showLineNumbers = true;
        options.showLocalVariableTables = true;

        return options;
    }
}
