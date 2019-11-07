/*
 * Collection.java
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

package com.strobel.assembler;

import com.strobel.core.IFreezable;
import com.strobel.core.VerifyArgument;

import java.util.AbstractList;
import java.util.ArrayList;

/**
 * @author Mike Strobel
 */
public class Collection<E> extends AbstractList<E> implements IFreezable {
    private final ArrayList<E> _items;
    private boolean _isFrozen;

    public Collection() {
        _items = new ArrayList<>();
    }

    @Override
    public final int size() {
        return _items.size();
    }

    @Override
    public final E get(final int index) {
        return _items.get(index);
    }

    @Override
    public final boolean add(final E e) {
        verifyNotFrozen();
        add(size(), e);
        return true;
    }

    @Override
    public final E set(final int index, final E element) {
        verifyNotFrozen();
        VerifyArgument.notNull(element, "element");
        beforeSet(index, element);
        return _items.set(index, element);
    }

    @Override
    public void add(final int index, final E element) {
        verifyNotFrozen();
        VerifyArgument.notNull(element, "element");
        addCore(index, element);
    }

    protected final void addCore(final int index, final E element) {
        final boolean append = index == size();
        _items.add(index, element);
        afterAdd(index, element, append);
    }

    @Override
    public final E remove(final int index) {
        verifyNotFrozen();
        final E e = _items.remove(index);
        if (e != null) {
            afterRemove(index, e);
        }
        return e;
    }

    @Override
    public final void clear() {
        verifyNotFrozen();
        beforeClear();
        _items.clear();
    }

    @Override
    public final boolean remove(final Object o) {
        verifyNotFrozen();

        @SuppressWarnings("SuspiciousMethodCalls")
        final int index = _items.indexOf(o);

        return index >= 0 &&
               remove(index) != null;
    }

    protected void afterAdd(final int index, final E e, final boolean appended) {}

    protected void beforeSet(final int index, final E e) {}

    protected void afterRemove(final int index, final E e) {}

    protected void beforeClear() {}

    // <editor-fold defaultstate="collapsed" desc="IFreezable Implementation">

    @Override
    public boolean canFreeze() {
        return !isFrozen();
    }

    @Override
    public final boolean isFrozen() {
        return _isFrozen;
    }

    @Override
    public final void freeze() {
        freeze(true);
    }

    public final void freeze(final boolean freezeContents) {
        if (!canFreeze()) {
            throw new IllegalStateException(
                "Collection cannot be frozen.  Be sure to check canFreeze() before calling " +
                "freeze(), or use the tryFreeze() method instead."
            );
        }

        freezeCore(freezeContents);

        _isFrozen = true;
    }

    protected void freezeCore(final boolean freezeContents) {
        if (freezeContents) {
            for (final E item : _items) {
                if (item instanceof IFreezable) {
                    ((IFreezable) item).freezeIfUnfrozen();
                }
            }
        }
    }

    protected final void verifyNotFrozen() {
        if (isFrozen()) {
            throw new IllegalStateException("Frozen collections cannot be modified.");
        }
    }

    protected final void verifyFrozen() {
        if (!isFrozen()) {
            throw new IllegalStateException(
                "Collection must be frozen before performing this operation."
            );
        }
    }

    @Override
    public final boolean tryFreeze() {
        if (!canFreeze()) {
            return false;
        }

        try {
            freeze();
            return true;
        }
        catch (final Throwable t) {
            return false;
        }
    }

    @Override
    public final void freezeIfUnfrozen() throws IllegalStateException {
        if (isFrozen()) {
            return;
        }
        freeze();
    }

    // </editor-fold>
}
