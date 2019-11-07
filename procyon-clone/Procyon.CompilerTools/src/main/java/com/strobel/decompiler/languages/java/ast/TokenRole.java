/*
 * TokenRole.java
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

import com.strobel.assembler.metadata.Flags;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.patterns.Role;

public final class TokenRole extends Role<JavaTokenNode> {
    public final static byte FLAG_KEYWORD = 0x01;
    public final static byte FLAG_OPERATOR = 0x02;
    public final static byte FLAG_DELIMITER = 0x04;

    private final String _token;
    private final int _length;
    private final byte _flags;

    public final String getToken() {
        return _token;
    }

    public final int getLength() {
        return _length;
    }

    public final boolean isKeyword() {
        return Flags.testAny(_flags, FLAG_KEYWORD);
    }

    public final boolean isOperator() {
        return Flags.testAny(_flags, FLAG_OPERATOR);
    }

    public final boolean isDelimiter() {
        return Flags.testAny(_flags, FLAG_DELIMITER);
    }

    public TokenRole(final String token) {
        this(token, 0);
    }

    public TokenRole(final String token, final int flags) {
        super(token, JavaTokenNode.class, JavaTokenNode.NULL);

        _token = VerifyArgument.notNull(token, "token");
        _length = token.length();
        _flags = (byte) (flags & 0xFF);
    }
}
