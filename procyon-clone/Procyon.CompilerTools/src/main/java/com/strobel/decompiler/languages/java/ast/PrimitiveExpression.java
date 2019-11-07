/*
 * PrimitiveExpression.java
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

import com.strobel.core.Comparer;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.languages.TextLocation;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.Pattern;

public class PrimitiveExpression extends Expression {
    public final static Object ANY_VALUE = new Object();
    public final static String ANY_STRING = Pattern.ANY_STRING;

    private TextLocation _startLocation;
    private TextLocation _endLocation;

    private String _literalValue;
    private Object _value;

    public PrimitiveExpression( final int offset, final Object value) {
        super( offset);
        _value = value;
        _startLocation = TextLocation.EMPTY;
        _literalValue = StringUtilities.EMPTY;
    }

    public PrimitiveExpression( final int offset, final Object value, final String literalValue) {
        super( offset);
        _value = value;
        _startLocation = TextLocation.EMPTY;
        _literalValue = literalValue != null ? literalValue : StringUtilities.EMPTY;
    }

    public PrimitiveExpression( final int offset, final Object value, final TextLocation startLocation, final String literalValue) {
        super( offset);
        _value = value;
        _startLocation = startLocation;
        _literalValue = literalValue != null ? literalValue : StringUtilities.EMPTY;
    }

    @Override
    public TextLocation getStartLocation() {
        final TextLocation startLocation = _startLocation;
        return startLocation != null ? startLocation : TextLocation.EMPTY;
    }

    @Override
    public TextLocation getEndLocation() {
        if (_endLocation == null) {
            final TextLocation startLocation = getStartLocation();
            if (_literalValue == null) {
                return startLocation;
            }
            _endLocation = new TextLocation(_startLocation.line(), _startLocation.column() + _literalValue.length());
        }
        return _endLocation;
    }

    public final void setStartLocation(final TextLocation startLocation) {
        _startLocation = VerifyArgument.notNull(startLocation, "startLocation");
        _endLocation = null;
    }

    public final String getLiteralValue() {
        return _literalValue;
    }

    public final void setLiteralValue(final String literalValue) {
        verifyNotFrozen();
        _literalValue = literalValue;
        _endLocation = null;
    }

    public final Object getValue() {
        return _value;
    }

    public final void setValue(final Object value) {
        verifyNotFrozen();
        _value = value;
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitPrimitiveExpression(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof PrimitiveExpression) {
            final PrimitiveExpression otherPrimitive = (PrimitiveExpression) other;

            return !other.isNull() &&
                   (_value == ANY_VALUE ||
                    _value == ANY_STRING && otherPrimitive._value instanceof String ||
                    Comparer.equals(_value, otherPrimitive._value));
        }

        return false;
    }
}
