/*
 * BindingFlags.java
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

package com.strobel.reflection;

import com.strobel.core.ArrayUtilities;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author Mike Strobel
 */
public enum BindingFlags {
    Default(0x0),
    IgnoreCase(0x1),
    DeclaredOnly(0x2),
    Instance(0x4),
    Static(0x8),
    Public(0x10),
    NonPublic(0x20),
    FlattenHierarchy(0x40),
    InvokeMethod(0x100),
    CreateInstance(0x200),
    GetField(0x400),
    SetField(0x800),
    ExactBinding(0x10000),
    SuppressChangeType(0x20000),
    OptionalParamBinding(0x40000),
    IgnoreReturn(0x1000000);

    public static final Set<BindingFlags> PublicStatic = Collections.unmodifiableSet(EnumSet.of(Public, Static));
    public static final Set<BindingFlags> PublicInstance = Collections.unmodifiableSet(EnumSet.of(Public, Instance));
    public static final Set<BindingFlags> PublicAll = Collections.unmodifiableSet(EnumSet.of(Public, Instance, Static));
    public static final Set<BindingFlags> NonPublicStatic = Collections.unmodifiableSet(EnumSet.of(NonPublic, Static));
    public static final Set<BindingFlags> NonPublicInstance = Collections.unmodifiableSet(EnumSet.of(NonPublic, Instance));
    public static final Set<BindingFlags> NonPublicAll = Collections.unmodifiableSet(EnumSet.of(NonPublic, Instance, Static));
    public static final Set<BindingFlags> All = Collections.unmodifiableSet(EnumSet.of(Public, NonPublic, Instance, Static));
    public static final Set<BindingFlags> AllStatic = Collections.unmodifiableSet(EnumSet.of(Public, NonPublic, Static));
    public static final Set<BindingFlags> AllInstance = Collections.unmodifiableSet(EnumSet.of(Public, NonPublic, Instance));

    public static final Set<BindingFlags> PublicStaticDeclared = Collections.unmodifiableSet(EnumSet.of(DeclaredOnly, Public, Static));
    public static final Set<BindingFlags> PublicInstanceDeclared = Collections.unmodifiableSet(EnumSet.of(DeclaredOnly, Public, Instance));
    public static final Set<BindingFlags> PublicAllDeclared = Collections.unmodifiableSet(EnumSet.of(DeclaredOnly, Public, Instance, Static));
    public static final Set<BindingFlags> NonPublicStaticDeclared = Collections.unmodifiableSet(EnumSet.of(DeclaredOnly, NonPublic, Static));
    public static final Set<BindingFlags> NonPublicInstanceDeclared = Collections.unmodifiableSet(EnumSet.of(DeclaredOnly, NonPublic, Instance));
    public static final Set<BindingFlags> NonPublicAllDeclared = Collections.unmodifiableSet(EnumSet.of(DeclaredOnly, NonPublic, Instance, Static));
    public static final Set<BindingFlags> AllDeclared = Collections.unmodifiableSet(EnumSet.of(DeclaredOnly, Public, NonPublic, Instance, Static));
    public static final Set<BindingFlags> AllStaticDeclared = Collections.unmodifiableSet(EnumSet.of(DeclaredOnly, Public, NonPublic, Static));
    public static final Set<BindingFlags> AllInstanceDeclared = Collections.unmodifiableSet(EnumSet.of(DeclaredOnly, Public, NonPublic, Instance));
    
    public static final Set<BindingFlags> PublicStaticExact = Collections.unmodifiableSet(EnumSet.of(ExactBinding, Public, Static));
    public static final Set<BindingFlags> PublicInstanceExact = Collections.unmodifiableSet(EnumSet.of(ExactBinding, Public, Instance));
    public static final Set<BindingFlags> PublicAllExact = Collections.unmodifiableSet(EnumSet.of(ExactBinding, Public, Instance, Static));
    public static final Set<BindingFlags> NonPublicStaticExact = Collections.unmodifiableSet(EnumSet.of(ExactBinding, NonPublic, Static));
    public static final Set<BindingFlags> NonPublicInstanceExact = Collections.unmodifiableSet(EnumSet.of(ExactBinding, NonPublic, Instance));
    public static final Set<BindingFlags> NonPublicAllExact = Collections.unmodifiableSet(EnumSet.of(ExactBinding, NonPublic, Instance, Static));
    public static final Set<BindingFlags> AllExact = Collections.unmodifiableSet(EnumSet.of(ExactBinding, Public, NonPublic, Instance, Static));
    public static final Set<BindingFlags> AllStaticExact = Collections.unmodifiableSet(EnumSet.of(ExactBinding, Public, NonPublic, Static));
    public static final Set<BindingFlags> AllInstanceExact = Collections.unmodifiableSet(EnumSet.of(ExactBinding, Public, NonPublic, Instance));

    private static final Set<BindingFlags> PublicOnly = Collections.unmodifiableSet(EnumSet.of(Public));
    private static final Set<BindingFlags> NonPublicOnly = Collections.unmodifiableSet(EnumSet.of(NonPublic));
    private static final Set<BindingFlags> PublicDeclared = Collections.unmodifiableSet(EnumSet.of(Public, DeclaredOnly));
    private static final Set<BindingFlags> NonPublicDeclared = Collections.unmodifiableSet(EnumSet.of(NonPublic, DeclaredOnly));

    private static final Set<BindingFlags>[] SingleEntrySets;

    static {
        final BindingFlags[] values = values();

        @SuppressWarnings("unchecked")
        final Set<BindingFlags>[] singleEntrySets = (Set<BindingFlags>[])Array.newInstance(Set.class, values.length);

        for (int i = 0, n = values.length; i < n; i++) {
            singleEntrySets[i] = Collections.singleton(values[i]);
        }

        SingleEntrySets = singleEntrySets;
    }

    private final int _mask;

    public int getMask() {
        return _mask;
    }

    private BindingFlags(final int mask) {
        _mask = mask;
    }

    public static Set<BindingFlags> set(final BindingFlags... flags) {
        if (ArrayUtilities.isNullOrEmpty(flags)) {
            return Collections.emptySet();
        }

        final BindingFlags firstFlag = flags[0];

        switch (flags.length) {
            case 1:
                if (firstFlag == null) {
                    return Collections.emptySet();
                }
                return SingleEntrySets[firstFlag.ordinal()];

            case 2:
                if (firstFlag == null) {
                    final BindingFlags secondFlag = flags[1];
                    if (secondFlag == null) {
                        return Collections.emptySet();
                    }
                    return SingleEntrySets[secondFlag.ordinal()];
                }
                else if (firstFlag == Public) {
                    if (flags[1] == Instance) {
                        return PublicInstance;
                    }
                    if (flags[1] == Static) {
                        return PublicStatic;
                    }
                }
                else if (firstFlag == NonPublic) {
                    if (flags[1] == Instance) {
                        return NonPublicInstance;
                    }
                    if (flags[1] == Static) {
                        return NonPublicStatic;
                    }
                }
                else if (firstFlag == Static) {
                    if (flags[1] == Public) {
                        return PublicStatic;
                    }
                    if (flags[1] == NonPublic) {
                        return NonPublicStatic;
                    }
                }
                else if (firstFlag == Instance) {
                    if (flags[1] == Public) {
                        return PublicInstance;
                    }
                    if (flags[1] == NonPublic) {
                        return NonPublicInstance;
                    }
                }

                // if it's not a common case, fall through to default...

            default:

                BindingFlags singleValue = firstFlag;
                EnumSet<BindingFlags> newSet = null;

                for (int i = 1, n = flags.length; i < n; i++) {
                    final BindingFlags flag = flags[i];

                    if (flag == null || flag == singleValue) {
                        continue;
                    }

                    if (newSet != null) {
                        newSet.add(flag);
                        continue;
                    }

                    if (singleValue == null) {
                        singleValue = flag;
                        continue;
                    }

                    newSet = EnumSet.of(singleValue);
                    newSet.add(flag);
                }

                if (newSet != null) {
                    return Collections.unmodifiableSet(newSet);
                }

                if (singleValue != null) {
                    return SingleEntrySets[singleValue.ordinal()];
                }

                return Collections.emptySet();
        }
    }

    static Set<BindingFlags> fromMask(final int mask) {
        BindingFlags singleValue = null;
        EnumSet<BindingFlags> newSet = null;

        for (final BindingFlags value : values()) {
            if ((value._mask & mask) == 0) {
                continue;
            }

            if (newSet != null) {
                newSet.add(value);
                continue;
            }

            if (singleValue == null) {
                singleValue = value;
                continue;
            }

            newSet = EnumSet.of(singleValue);
            newSet.add(value);
        }

        if (newSet != null) {
            return Collections.unmodifiableSet(newSet);
        }

        if (singleValue != null) {
            return SingleEntrySets[singleValue.ordinal()];
        }

        return Collections.emptySet();
    }

    static Set<BindingFlags> fromMember(final MemberInfo member) {
        if (member instanceof Type) {
            return fromTypeModifiers(member.getModifiers());
        }
        return fromMethodModifiers(member.getModifiers());
    }

    private static Set<BindingFlags> fromMethodModifiers(final int modifiers) {
        if (Modifier.isPublic(modifiers)) {
            if (Modifier.isStatic(modifiers)) {
                return PublicStatic;
            }
            return PublicInstance;
        }

        if (Modifier.isStatic(modifiers)) {
            return NonPublicStatic;
        }

        return NonPublicInstance;
    }

    private static Set<BindingFlags> fromTypeModifiers(final int modifiers) {
        if (Modifier.isStatic(modifiers)) {
            if (Modifier.isPublic(modifiers)) {
                return PublicStatic;
            }
            return NonPublicStatic;
        }

        if (Modifier.isPublic(modifiers)) {
            return PublicOnly;
        }

        return NonPublicOnly;
    }
}
