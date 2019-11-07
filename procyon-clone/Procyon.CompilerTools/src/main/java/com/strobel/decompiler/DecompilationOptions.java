/*
 * DecompilationOptions.java
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

public class DecompilationOptions {
    private boolean _fullDecompilation = true;
    private DecompilerSettings _settings;

    public final boolean isFullDecompilation() {
        return _fullDecompilation;
    }

    public final void setFullDecompilation(final boolean fullDecompilation) {
        _fullDecompilation = fullDecompilation;
    }

    public final DecompilerSettings getSettings() {
        if (_settings == null) {
            _settings = new DecompilerSettings();
        }
        return _settings;
    }

    public final void setSettings(final DecompilerSettings settings) {
        _settings = settings;
    }
}
