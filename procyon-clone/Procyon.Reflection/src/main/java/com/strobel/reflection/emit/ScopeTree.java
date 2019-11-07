/*
 * ScopeTree.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.reflection.emit;

import java.util.Arrays;

/**
 * @author strobelm
 */
@SuppressWarnings("PackageVisibleField")
final class ScopeTree {
    ScopeTree() {
        _openScopeCount = 0;
        _count = 0;
    }

    /*
     * Find the current active lexical scope. For example, if we have
     * "Open Open Open Close", we will return 1 as the second BeginScope
     * is currently active.
     */
    int getCurrentActiveScopeIndex() {
        int closeCount = 0;
        int i = _count - 1;

        if (_count == 0) {
            return -1;
        }

        for (; closeCount > 0 || _scopeActions[i] == ScopeAction.Close; i--) {
            if (_scopeActions[i] == ScopeAction.Open) {
                closeCount--;
            }
            else {
                closeCount++;
            }
        }

        return i;
    }

    void addLocalSymInfoToCurrentScope(
        final String name,
        final byte[] signature,
        final int slot,
        final int startOffset,
        final int endOffset) {

        final int i = getCurrentActiveScopeIndex();

        if (_localSymInfos[i] == null) {
            _localSymInfos[i] = new LocalSymInfo();
        }

        _localSymInfos[i].addLocalSymInfo(name, signature, slot, startOffset, endOffset);
    }

    void addPackageImportToCurrentScope(final String packageName) {
        final int i = getCurrentActiveScopeIndex();

        if (_localSymInfos[i] == null) {
            _localSymInfos[i] = new LocalSymInfo();
        }

        _localSymInfos[i].addPackageImport(packageName);
    }

    void addScopeInfo(final ScopeAction action, final int offset) {
        if (action == ScopeAction.Close && _openScopeCount <= 0) {
            throw new IllegalStateException("Tried to close unmatched symbol scope.");
        }

        // make sure that arrays are large enough to hold addition info
        ensureCapacity();

        _scopeActions[_count] = action;
        _offsets[_count] = offset;
        _localSymInfos[_count] = null;
        _count++;
        
        if (action == ScopeAction.Open) {
            _openScopeCount++;
        }
        else {
            _openScopeCount--;
        }
    }

    /**
     * Helper to ensure arrays are large enough
     */
    void ensureCapacity() {
        if (_count == 0) {
            // First time. Allocate the arrays.
            _offsets = new int[InitialSize];
            _scopeActions = new ScopeAction[InitialSize];
            _localSymInfos = new LocalSymInfo[InitialSize];
        }
        else if (_count == _offsets.length) {
            // the arrays are full. Enlarge the arrays
            // It would probably be simpler to just use Lists here.
            final int newSize = _count * 2;
            _offsets = Arrays.copyOf(_offsets, newSize);
            _scopeActions = Arrays.copyOf(_scopeActions, newSize);
            _localSymInfos = Arrays.copyOf(_localSymInfos, newSize);
        }
    }

/*
    void emitScopeTree(final ISymbolWriter symWriter) {
        int i;
        for (i = 0; i < _count; i++) {
            if (_scopeActions[i] == ScopeAction.Open) {
                symWriter.openScope(_offsets[i]);
            }
            else {
                symWriter.closeScope(_offsets[i]);
            }
            if (_localSymInfos[i] != null) {
                _localSymInfos[i].emitLocalSymInfo(symWriter);
            }
        }
    }
*/

    int[]            _offsets;          // array of offsets
    ScopeAction[]    _scopeActions;     // array of scope actions
    int              _count;            // how many entries in the arrays are occupied
    int              _openScopeCount;   // keep track how many scopes are open
    LocalSymInfo[]   _localSymInfos;    // keep track debugging local information

    final static int InitialSize = 16;
}
