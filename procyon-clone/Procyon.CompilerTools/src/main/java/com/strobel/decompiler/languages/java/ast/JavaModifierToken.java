/*
 * JavaModifierToken.java
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

package com.strobel.decompiler.languages.java.ast;

import com.strobel.core.ArrayUtilities;
import com.strobel.decompiler.languages.TextLocation;
import com.strobel.decompiler.languages.java.JavaFormattingOptions;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;

import javax.lang.model.element.Modifier;
import java.util.List;

public class JavaModifierToken extends JavaTokenNode {
    private final static List<Modifier> ALL_MODIFIERS = ArrayUtilities.asUnmodifiableList(Modifier.values());

    public static List<Modifier> allModifiers() {
        return ALL_MODIFIERS;
    }

    private Modifier _modifier;

    public JavaModifierToken(final Modifier modifier) {
        this(TextLocation.EMPTY, modifier);
    }

    public JavaModifierToken(final TextLocation startLocation, final Modifier modifier) {
        super(startLocation);
        _modifier = modifier;
    }

    public final Modifier getModifier() {
        return _modifier;
    }

    public final void setModifier(final Modifier modifier) {
        verifyNotFrozen();
        _modifier = modifier;
    }
    
    public static String getModifierName(final Modifier modifier) {
        return String.valueOf(modifier);
    }

    @Override
    public String getText(final JavaFormattingOptions options) {
        return getModifierName(_modifier);
    }

    @Override
    protected int getTokenLength() {
        return getModifierName(_modifier).length();
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof JavaModifierToken &&
               ((JavaModifierToken) other)._modifier == _modifier;
    }
}
