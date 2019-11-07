/*
 * ClassFilePreprocessor.java
 *
 * Copyright (c) 2014 Mike Strobel
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

package com.strobel.assembler.metadata;

import com.strobel.annotations.NotNull;
import com.strobel.assembler.ir.Instruction;
import com.strobel.assembler.ir.OpCode;
import com.strobel.core.VerifyArgument;

public class DeobfuscationUtilities {
    public static void processType(@NotNull final TypeDefinition type) {
        VerifyArgument.notNull(type, "type");

        if (Flags.testAny(type.getFlags(), Flags.DEOBFUSCATED)) {
            return;
        }

        type.setFlags(type.getFlags() | Flags.DEOBFUSCATED);

        flagAnonymousEnumDefinitions(type);
    }

    private static void flagAnonymousEnumDefinitions(final TypeDefinition type) {
        if (!type.isEnum() || type.getDeclaringType() != null) {
            return;
        }

        final TypeReference baseType = type.getBaseType();

        if (!"java/lang/Enum".equals(baseType.getInternalName())) {
            final TypeDefinition resolvedBaseType = baseType.resolve();

            if (resolvedBaseType != null) {
                processType(resolvedBaseType);
            }
        }

        if (type.getDeclaringType() != null && type.isAnonymous()) {
            //
            // We were already updated when we processed our base type.
            //
            return;
        }

        for (final MethodDefinition method : type.getDeclaredMethods()) {
            if (!method.isTypeInitializer()) {
                continue;
            }

            final MethodBody body = method.getBody();

            if (body == null) {
                continue;
            }

            for (final Instruction p : body.getInstructions()) {
                if (p.getOpCode() != OpCode.NEW) {
                    continue;
                }

                final TypeReference instantiatedType = p.getOperand(0);
                final TypeDefinition instantiatedTypeResolved = instantiatedType != null ? instantiatedType.resolve() : null;

                if (instantiatedTypeResolved == null) {
                    continue;
                }

                if (instantiatedTypeResolved.isEnum() &&
                    type.isEquivalentTo(instantiatedTypeResolved.getBaseType())) {

                    instantiatedTypeResolved.setDeclaringType(type);
                    type.getDeclaredTypesInternal().add(instantiatedTypeResolved);

                    instantiatedTypeResolved.setFlags(instantiatedTypeResolved.getFlags() | Flags.ANONYMOUS);
                }
            }
        }
    }
}
