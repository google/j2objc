/*
 * PackageReference.java
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

package com.strobel.assembler.metadata;

import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;

import java.util.List;

public class PackageReference {
    public final static PackageReference GLOBAL = new PackageReference();

    private final PackageReference _parent;
    private final String _name;

    private String _fullName;

    private PackageReference() {
        _parent = null;
        _name = StringUtilities.EMPTY;
    }

    public PackageReference(final String name) {
        _parent = null;
        _name = VerifyArgument.notNull(name, "name");
    }

    public PackageReference(final PackageReference parent, final String name) {
        _parent = parent;
        _name = VerifyArgument.notNull(name, "name");
    }

    public final boolean isGlobal() {
        return _name.length() == 0;
    }

    public final String getName() {
        return _name;
    }

    public final String getFullName() {
        if (_fullName == null) {
            if (_parent == null || _parent.equals(GLOBAL)) {
                _fullName = getName();
            }
            else {
                _fullName = _parent.getFullName() + "." + getName();
            }
        }
        return _fullName;
    }

    public final PackageReference getParent() {
        return _parent;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof PackageReference) {
            final PackageReference that = (PackageReference) o;

            return _name.equals(that._name) &&
                   (_parent == null ? that._parent == null : _parent.equals(that._parent));
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = _parent != null ? _parent.hashCode() : 0;
        result = 31 * result + _name.hashCode();
        return result;
    }

    public static PackageReference parse(final String qualifiedName) {
        VerifyArgument.notNull(qualifiedName, "qualifiedName");

        final List<String> parts = StringUtilities.split(qualifiedName, '.', '/');

        if (parts.isEmpty()) {
            return GLOBAL;
        }

        PackageReference current = new PackageReference(parts.get(0));

        for (int i = 1; i < parts.size(); i++) {
            current = new PackageReference(current, parts.get(i));
        }

        return current;
    }
}
