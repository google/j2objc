/*
 * JavaPrimitiveCast.java
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

import com.strobel.assembler.metadata.JvmType;
import com.strobel.core.StringUtilities;

public final class JavaPrimitiveCast {
    public static Object cast(final JvmType targetType, final Object input) {
        switch (targetType) {
            case Boolean:
                if (input instanceof Boolean) {
                    return input;
                }
                if (input instanceof Number) {
                    if (input instanceof Float || input instanceof Double) {
                        break;
                    }
                    return ((Number) input).longValue() != 0L;
                }
                if (input instanceof Character) {
                    return (char) input != '\0';
                }
                if (input instanceof String) {
                    return StringUtilities.isTrue((String) input);
                }
                break;

            case Byte:
                if (input instanceof Number) {
                    return ((Number) input).byteValue();
                }
                if (input instanceof Character) {
                    return (byte)(char)input;
                }
                if (input instanceof String) {
                    return Byte.parseByte((String) input);
                }
                break;

            case Character:
                if (input instanceof Character) {
                    return input;
                }
                if (input instanceof Number) {
                    return (char)((Number) input).intValue();
                }
                if (input instanceof String) {
                    final String stringValue = (String) input;
                    return stringValue.length() == 0 ? '\0' : stringValue.charAt(0);
                }
                break;

            case Short:
                if (input instanceof Number) {
                    return ((Number) input).shortValue();
                }
                if (input instanceof Character) {
                    return (short)(char)input;
                }
                if (input instanceof String) {
                    return Short.parseShort((String) input);
                }
                break;

            case Integer:
                if (input instanceof Number) {
                    return ((Number) input).intValue();
                }
                if (input instanceof Boolean) {
                    return ((Boolean)input) ? 1 : 0;
                }
                if (input instanceof String) {
                    return Integer.parseInt((String) input);
                }
                if (input instanceof Character) {
                    return (int)(char)input;
                }
                break;

            case Long:
                if (input instanceof Number) {
                    return ((Number) input).longValue();
                }
                if (input instanceof Character) {
                    return (long)(char)input;
                }
                if (input instanceof String) {
                    return Long.parseLong((String) input);
                }
                break;

            case Float:
                if (input instanceof Number) {
                    return ((Number) input).floatValue();
                }
                if (input instanceof Character) {
                    return (float)(char)input;
                }
                if (input instanceof String) {
                    return Float.parseFloat((String) input);
                }
                break;

            case Double:
                if (input instanceof Number) {
                    return ((Number) input).doubleValue();
                }
                if (input instanceof Character) {
                    return (double)(char)input;
                }
                if (input instanceof String) {
                    return Double.parseDouble((String) input);
                }
                break;

            default:
                return input;
        }

        throw new ClassCastException();
    }
}
