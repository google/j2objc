/*
 * AstNodeCollection.java
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

import com.strobel.annotations.NotNull;
import com.strobel.core.CollectionUtilities;
import com.strobel.core.Predicate;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.Pattern;
import com.strobel.decompiler.patterns.Role;
import com.strobel.util.ContractUtils;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class AstNodeCollection<T extends AstNode> extends AbstractCollection<T> {
    private final AstNode _node;
    private final Role<T> _role;

    public AstNodeCollection(final AstNode node, final Role<T> role) {
        _node = VerifyArgument.notNull(node, "node");
        _role = VerifyArgument.notNull(role, "role");
    }

    @Override
    public int size() {
        int count = 0;

        for (AstNode current = _node.getFirstChild(); current != null; current = current.getNextSibling()) {
            if (current.getRole() == _role) {
                count++;
            }
        }

        return count;
    }

    @Override
    public boolean isEmpty() {
        for (AstNode current = _node.getFirstChild(); current != null; current = current.getNextSibling()) {
            if (current.getRole() == _role) {
                return false;
            }
        }

        return true;
    }

    public boolean hasSingleElement() {
        boolean hasElement = false;

        for (AstNode current = _node.getFirstChild(); current != null; current = current.getNextSibling()) {
            if (current.getRole() == _role) {
                if (hasElement) {
                    return false;
                }

                hasElement = true;
            }
        }

        return hasElement;
    }

    @Override
    public boolean contains(final Object o) {
        return o instanceof AstNode &&
               ((AstNode) o).getParent() == _node &&
               ((AstNode) o).getRole() == _role;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            AstNode position = _node.getFirstChild();
            T next;

            @SuppressWarnings("unchecked")
            private T selectNext() {
                if (next != null) {
                    return next;
                }

                for (; position != null; position = position.getNextSibling()) {
                    if (position.getRole() == _role) {
                        next = (T) position;
                        position = position.getNextSibling();
                        return next;
                    }
                }

                return null;
            }

            @Override
            public boolean hasNext() {
                return selectNext() != null;
            }

            @Override
            public T next() {
                final T next = selectNext();

                if (next == null) {
                    throw new NoSuchElementException();
                }

                this.next = null;
                return next;
            }

            @Override
            public void remove() {
                throw ContractUtils.unsupported();
            }
        };
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <T1> T1[] toArray(@NotNull final T1[] a) {
        int index = 0;
        T1[] destination = a;

        for (final T child : this) {
            if (index >= destination.length) {
                destination = Arrays.copyOf(destination, size());
            }
            destination[index++] = (T1) child;
        }

        return destination;
    }

    @Override
    public boolean add(final T t) {
        _node.addChild(t, _role);
        return true;
    }

    @Override
    public boolean remove(final Object o) {
        if (contains(o)) {
            ((AstNode) o).remove();
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        for (final T item : this) {
            item.remove();
        }
    }

    public void moveTo(final Collection<T> destination) {
        VerifyArgument.notNull(destination, "destination");

        for (final T node : this) {
            node.remove();
            destination.add(node);
        }
    }

    public T firstOrNullObject() {
        return firstOrNullObject(null);
    }

    public T firstOrNullObject(final Predicate<T> predicate) {
        for (final T item : this) {
            if (predicate == null || predicate.test(item)) {
                return item;
            }
        }
        return _role.getNullObject();
    }

    public T lastOrNullObject() {
        return lastOrNullObject(null);
    }

    public T lastOrNullObject(final Predicate<T> predicate) {
        T result = _role.getNullObject();

        for (final T item : this) {
            if (predicate == null || predicate.test(item)) {
                result = item;
            }
        }

        return result;
    }

    public void acceptVisitor(final IAstVisitor<? super T, ?> visitor) {
        AstNode next;

        for (AstNode current = _node.getFirstChild(); current != null; current = next) {
            assert current.getParent() == _node;
            next = current.getNextSibling();

            if (current.getRole() == _role) {
                current.acceptVisitor(visitor, null);
            }
        }
    }

    public final boolean matches(final AstNodeCollection<T> other, final Match match) {
        return Pattern.matchesCollection(
            _role,
            _node.getFirstChild(),
            VerifyArgument.notNull(other, "other")._node.getFirstChild(),
            VerifyArgument.notNull(match, "match")
        );
    }

    @Override
    public int hashCode() {
        return _node.hashCode() ^ _role.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof AstNodeCollection<?>) {
            final AstNodeCollection<?> other = (AstNodeCollection) obj;

            return other._node == _node &&
                   other._role == _role;
        }

        return false;
    }

    public final void replaceWith(final Iterable<T> nodes) {
        final List<T> nodeList = nodes != null ? CollectionUtilities.toList(nodes) : null;

        clear();

        if (nodeList == null) {
            return;
        }

        for (final T node : nodeList) {
            add(node);
        }
    }

    public final void insertAfter(final T existingItem, final T newItem) {
        _node.insertChildAfter(existingItem, newItem, _role);
    }

    public final void insertBefore(final T existingItem, final T newItem) {
        _node.insertChildBefore(existingItem, newItem, _role);
    }
}
