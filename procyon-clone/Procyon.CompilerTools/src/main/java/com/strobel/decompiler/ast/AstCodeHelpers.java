/*
 * AstCodeHelpers.java
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

package com.strobel.decompiler.ast;

public final class AstCodeHelpers {
    public static boolean isLocalStore(final AstCode code) {
        if (code == null) {
            return false;
        }

        switch (code) {
            case __IStore:
            case __LStore:
            case __FStore:
            case __DStore:
            case __AStore:
            case __IStore0:
            case __IStore1:
            case __IStore2:
            case __IStore3:
            case __LStore0:
            case __LStore1:
            case __LStore2:
            case __LStore3:
            case __FStore0:
            case __FStore1:
            case __FStore2:
            case __FStore3:
            case __DStore0:
            case __DStore1:
            case __DStore2:
            case __DStore3:
            case __AStore0:
            case __AStore1:
            case __AStore2:
            case __AStore3:
                return true;

            case __IStoreW:
            case __LStoreW:
            case __FStoreW:
            case __DStoreW:
            case __AStoreW:
                return true;

            case Store:
                return true;

            default:
                return false;
        }
    }

    public static boolean isLocalLoad(final AstCode code) {
        if (code == null) {
            return false;
        }

        switch (code) {
            case __ILoad:
            case __LLoad:
            case __FLoad:
            case __DLoad:
            case __ALoad:
            case __ILoad0:
            case __ILoad1:
            case __ILoad2:
            case __ILoad3:
            case __LLoad0:
            case __LLoad1:
            case __LLoad2:
            case __LLoad3:
            case __FLoad0:
            case __FLoad1:
            case __FLoad2:
            case __FLoad3:
            case __DLoad0:
            case __DLoad1:
            case __DLoad2:
            case __DLoad3:
            case __ALoad0:
            case __ALoad1:
            case __ALoad2:
            case __ALoad3:
                return true;

            case __ILoadW:
            case __LLoadW:
            case __FLoadW:
            case __DLoadW:
            case __ALoadW:
                return true;

            case Load:
                return true;

            default:
                return false;
        }
    }

    public static int getLoadStoreMacroArgumentIndex(final AstCode code) {
        if (code == null) {
            return -1;
        }

        switch (code) {
            case __ILoad0:
            case __LLoad0:
            case __FLoad0:
            case __DLoad0:
            case __ALoad0:
            case __IStore0:
            case __LStore0:
            case __FStore0:
            case __DStore0:
            case __AStore0:
                return 0;

            case __ILoad1:
            case __LLoad1:
            case __FLoad1:
            case __DLoad1:
            case __ALoad1:
            case __IStore1:
            case __LStore1:
            case __FStore1:
            case __DStore1:
            case __AStore1:
                return 1;

            case __ILoad2:
            case __LLoad2:
            case __FLoad2:
            case __DLoad2:
            case __ALoad2:
            case __IStore2:
            case __LStore2:
            case __FStore2:
            case __DStore2:
            case __AStore2:
                return 2;

            case __ILoad3:
            case __LLoad3:
            case __FLoad3:
            case __DLoad3:
            case __ALoad3:
            case __IStore3:
            case __LStore3:
            case __FStore3:
            case __DStore3:
            case __AStore3:
                return 3;

            default:
                return -1;
        }

    }
}
