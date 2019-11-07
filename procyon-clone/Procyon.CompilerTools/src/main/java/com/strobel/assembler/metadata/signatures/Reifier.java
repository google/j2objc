/*
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.strobel.assembler.metadata.signatures;

import com.strobel.assembler.metadata.BuiltinTypes;
import com.strobel.assembler.metadata.TypeReference;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Reifier implements TypeTreeVisitor<TypeReference> {
    private final static Logger LOG = Logger.getLogger(Reifier.class.getSimpleName());

    private final MetadataFactory factory;
    private TypeReference resultType;

    private Reifier(final MetadataFactory f) {
        factory = f;
    }

    public static Reifier make(final MetadataFactory f) {
        return new Reifier(f);
    }

    private MetadataFactory getFactory() {
        return factory;
    }

    private TypeReference[] reifyTypeArguments(final TypeArgument[] tas) {
        final TypeReference[] ts = new TypeReference[tas.length];
        for (int i = 0; i < tas.length; i++) {
            tas[i].accept(this);
            ts[i] = resultType;
            if (ts[i] == null) {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.warning("BAD TYPE ARGUMENTS: " + Arrays.toString(tas) + "; " + Arrays.toString(ts));
                }
                ts[i] = BuiltinTypes.Object;
            }
        }
        return ts;
    }

    public TypeReference getResult() {
        assert resultType != null;
        return resultType;
    }

    public void visitFormalTypeParameter(final FormalTypeParameter ftp) {
        final FieldTypeSignature[] bounds = ftp.getBounds();
        resultType = getFactory().makeTypeVariable(ftp.getName(), bounds);
    }

    public void visitClassTypeSignature(final ClassTypeSignature ct) {
        // This method examines the path name stored in ct, which has the form
        // n1.n2...nk<t_args>....
        // where n1 ... nk-1 might not exist OR
        // nk might not exist (but not both). It may be that k equals 1.
        // The idea is that nk is the simple class type name that has
        // any type parameters associated with it.
        //  We process this path in two phases.
        //  First, we scan until we reach nk (if it exists).
        //  If nk does not exist, this identifies a raw class n1 ... nk-1
        // which we can return.
        // if nk does exist, we begin the 2nd phase.
        // Here nk defines a parameterized type. Every further step nj (j > k)
        // down the path must also be represented as a parameterized type,
        // whose owner is the representation of the previous step in the path,
        // n{j-1}.

        // extract iterator on list of simple class type sigs
        final List<SimpleClassTypeSignature> scts = ct.getPath();
        assert (!scts.isEmpty());
        final Iterator<SimpleClassTypeSignature> iter = scts.iterator();
        SimpleClassTypeSignature sc = iter.next();
        final StringBuilder n = new StringBuilder(sc.getName());
        boolean dollar;

        // phase 1: iterate over simple class types until
        // we are either done or we hit one with non-empty type parameters
        while (iter.hasNext() && sc.getTypeArguments().length == 0) {
            sc = iter.next();
            dollar = sc.useDollar();
            n.append(dollar ? "$" : ".").append(sc.getName());
        }

        // Now, either sc is the last element of the list, or
        // it has type arguments (or both)
        assert (!(iter.hasNext()) || (sc.getTypeArguments().length > 0));
        // Create the raw type
        TypeReference c = getFactory().makeNamedType(n.toString());
        // if there are no type arguments
        if (sc.getTypeArguments().length == 0) {
            //we have surely reached the end of the path
            assert (!iter.hasNext());
            resultType = c; // the result is the raw type
        }
        else {
            assert (sc.getTypeArguments().length > 0);
            // otherwise, we have type arguments, so we create a parameterized
            // type, whose declaration is the raw type c, and whose owner is
            // the declaring class of c (if any). This latter fact is indicated
            // by passing null as the owner.
            // First, we reify the type arguments
            TypeReference[] pts = reifyTypeArguments(sc.getTypeArguments());

            TypeReference owner = getFactory().makeParameterizedType(c, null, pts);
            // phase 2: iterate over remaining simple class types
            while (iter.hasNext()) {
                sc = iter.next();
                dollar = sc.useDollar();
                n.append(dollar ? "$" : ".").append(sc.getName()); // build up raw class name
                c = getFactory().makeNamedType(n.toString()); // obtain raw class
                pts = reifyTypeArguments(sc.getTypeArguments());// reify params
                // Create a parameterized type, based on type args, raw type
                // and previous owner
                owner = getFactory().makeParameterizedType(c, owner, pts);
            }
            resultType = owner;
        }
    }

    public void visitArrayTypeSignature(final ArrayTypeSignature a) {
        // extract and reify component type
        a.getComponentType().accept(this);
        final TypeReference ct = resultType;
        assert ct != null;
        resultType = getFactory().makeArrayType(ct);
    }

    public void visitTypeVariableSignature(final TypeVariableSignature tv) {
        resultType = getFactory().findTypeVariable(tv.getName());

//        if (resultType == null) {
//            resultType = BuiltinTypes.Object;
//        }
    }

    public void visitWildcard(final Wildcard w) {
        resultType = getFactory().makeWildcard(w.getSuperBound(), w.getExtendsBound());
    }

    public void visitSimpleClassTypeSignature(final SimpleClassTypeSignature sct) {
        resultType = getFactory().makeNamedType(sct.getName());
    }

    public void visitBottomSignature(final BottomSignature b) {
        resultType = null;
    }

    public void visitByteSignature(final ByteSignature b) {
        resultType = getFactory().makeByte();
    }

    public void visitBooleanSignature(final BooleanSignature b) {
        resultType = getFactory().makeBoolean();
    }

    public void visitShortSignature(final ShortSignature s) {
        resultType = getFactory().makeShort();
    }

    public void visitCharSignature(final CharSignature c) {
        resultType = getFactory().makeChar();
    }

    public void visitIntSignature(final IntSignature i) {
        resultType = getFactory().makeInt();
    }

    public void visitLongSignature(final LongSignature l) {
        resultType = getFactory().makeLong();
    }

    public void visitFloatSignature(final FloatSignature f) {
        resultType = getFactory().makeFloat();
    }

    public void visitDoubleSignature(final DoubleSignature d) {
        resultType = getFactory().makeDouble();
    }

    public void visitVoidSignature(final VoidSignature v) {
        resultType = getFactory().makeVoid();
    }
}
