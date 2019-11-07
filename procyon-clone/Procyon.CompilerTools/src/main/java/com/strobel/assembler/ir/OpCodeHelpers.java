/*
 * OpCodeHelpers.java
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

package com.strobel.assembler.ir;

public final class OpCodeHelpers {
    public static boolean isLocalStore(final OpCode code) {
        if (code == null) {
            return false;
        }

        switch (code) {
            case ISTORE:
            case LSTORE:
            case FSTORE:
            case DSTORE:
            case ASTORE:
            case ISTORE_0:
            case ISTORE_1:
            case ISTORE_2:
            case ISTORE_3:
            case LSTORE_0:
            case LSTORE_1:
            case LSTORE_2:
            case LSTORE_3:
            case FSTORE_0:
            case FSTORE_1:
            case FSTORE_2:
            case FSTORE_3:
            case DSTORE_0:
            case DSTORE_1:
            case DSTORE_2:
            case DSTORE_3:
            case ASTORE_0:
            case ASTORE_1:
            case ASTORE_2:
            case ASTORE_3:
                return true;

            case ISTORE_W:
            case LSTORE_W:
            case FSTORE_W:
            case DSTORE_W:
            case ASTORE_W:
                return true;

            default:
                return false;
        }
    }

    public static boolean isLocalLoad(final OpCode code) {
        if (code == null) {
            return false;
        }

        switch (code) {
            case ILOAD:
            case LLOAD:
            case FLOAD:
            case DLOAD:
            case ALOAD:
            case ILOAD_0:
            case ILOAD_1:
            case ILOAD_2:
            case ILOAD_3:
            case LLOAD_0:
            case LLOAD_1:
            case LLOAD_2:
            case LLOAD_3:
            case FLOAD_0:
            case FLOAD_1:
            case FLOAD_2:
            case FLOAD_3:
            case DLOAD_0:
            case DLOAD_1:
            case DLOAD_2:
            case DLOAD_3:
            case ALOAD_0:
            case ALOAD_1:
            case ALOAD_2:
            case ALOAD_3:
                return true;

            case ILOAD_W:
            case LLOAD_W:
            case FLOAD_W:
            case DLOAD_W:
            case ALOAD_W:
                return true;

            default:
                return false;
        }
    }
    
    public static int getLoadStoreMacroArgumentIndex(final OpCode code) {
        if (code == null) {
            return -1;
        }

        switch (code) {
            case ILOAD_0:
            case LLOAD_0:
            case FLOAD_0:
            case DLOAD_0:
            case ALOAD_0:
            case ISTORE_0:
            case LSTORE_0:
            case FSTORE_0:
            case DSTORE_0:
            case ASTORE_0:
                return 0;

            case ILOAD_1:
            case LLOAD_1:
            case FLOAD_1:
            case DLOAD_1:
            case ALOAD_1:
            case ISTORE_1:
            case LSTORE_1:
            case FSTORE_1:
            case DSTORE_1:
            case ASTORE_1:
                return 1;

            case ILOAD_2:
            case LLOAD_2:
            case FLOAD_2:
            case DLOAD_2:
            case ALOAD_2:
            case ISTORE_2:
            case LSTORE_2:
            case FSTORE_2:
            case DSTORE_2:
            case ASTORE_2:
                return 2;

            case ILOAD_3:
            case LLOAD_3:
            case FLOAD_3:
            case DLOAD_3:
            case ALOAD_3:
            case ISTORE_3:
            case LSTORE_3:
            case FSTORE_3:
            case DSTORE_3:
            case ASTORE_3:
                return 3;

            default:
                return -1;
        }

    }
}
