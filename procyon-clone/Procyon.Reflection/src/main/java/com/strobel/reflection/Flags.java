/*
 * Flags.java
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

import com.strobel.util.ContractUtils;

import javax.lang.model.element.Modifier;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * @author strobelm
 */
@SuppressWarnings("PointlessBitwiseExpression")
public class Flags {

    private Flags() {
        throw ContractUtils.unreachable();
    }

    public static String toString(final long flags) {
        final StringBuilder buf = new StringBuilder();
        String sep = "";
        for (final Flag s : asFlagSet(flags)) {
            buf.append(sep);
            buf.append(s);
            sep = " ";
        }
        return buf.toString();
    }

    @SuppressWarnings("ConstantConditions")
    public static EnumSet<Flag> asFlagSet(final long mask) {
        final EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);

        if ((mask & PUBLIC) != 0) {
            flags.add(Flag.PUBLIC);
        }
        if ((mask & PRIVATE) != 0) {
            flags.add(Flag.PRIVATE);
        }
        if ((mask & PROTECTED) != 0) {
            flags.add(Flag.PROTECTED);
        }
        if ((mask & STATIC) != 0) {
            flags.add(Flag.STATIC);
        }
        if ((mask & FINAL) != 0) {
            flags.add(Flag.FINAL);
        }
        if ((mask & SYNCHRONIZED) != 0) {
            flags.add(Flag.SYNCHRONIZED);
        }
        if ((mask & VOLATILE) != 0) {
            flags.add(Flag.VOLATILE);
        }
        if ((mask & TRANSIENT) != 0) {
            flags.add(Flag.TRANSIENT);
        }
        if ((mask & NATIVE) != 0) {
            flags.add(Flag.NATIVE);
        }
        if ((mask & INTERFACE) != 0) {
            flags.add(Flag.INTERFACE);
        }
        if ((mask & ABSTRACT) != 0) {
            flags.add(Flag.ABSTRACT);
        }
        if ((mask & STRICTFP) != 0) {
            flags.add(Flag.STRICTFP);
        }
        if ((mask & BRIDGE) != 0) {
            flags.add(Flag.BRIDGE);
        }
        if ((mask & SYNTHETIC) != 0) {
            flags.add(Flag.SYNTHETIC);
        }
        if ((mask & DEPRECATED) != 0) {
            flags.add(Flag.DEPRECATED);
        }
        if ((mask & HASINIT) != 0) {
            flags.add(Flag.HASINIT);
        }
        if ((mask & ENUM) != 0) {
            flags.add(Flag.ENUM);
        }
        if ((mask & IPROXY) != 0) {
            flags.add(Flag.IPROXY);
        }
        if ((mask & NOOUTERTHIS) != 0) {
            flags.add(Flag.NOOUTERTHIS);
        }
        if ((mask & EXISTS) != 0) {
            flags.add(Flag.EXISTS);
        }
        if ((mask & COMPOUND) != 0) {
            flags.add(Flag.COMPOUND);
        }
        if ((mask & CLASS_SEEN) != 0) {
            flags.add(Flag.CLASS_SEEN);
        }
        if ((mask & SOURCE_SEEN) != 0) {
            flags.add(Flag.SOURCE_SEEN);
        }
        if ((mask & LOCKED) != 0) {
            flags.add(Flag.LOCKED);
        }
        if ((mask & UNATTRIBUTED) != 0) {
            flags.add(Flag.UNATTRIBUTED);
        }
        if ((mask & ANONCONSTR) != 0) {
            flags.add(Flag.ANONCONSTR);
        }
        if ((mask & ACYCLIC) != 0) {
            flags.add(Flag.ACYCLIC);
        }
        if ((mask & PARAMETER) != 0) {
            flags.add(Flag.PARAMETER);
        }
        if ((mask & VARARGS) != 0) {
            flags.add(Flag.VARARGS);
        }

        return flags;
    }

    /* Standard Java flags.
     */
    public static final int PUBLIC       = 1 << 0;
    public static final int PRIVATE      = 1 << 1;
    public static final int PROTECTED    = 1 << 2;
    public static final int STATIC       = 1 << 3;
    public static final int FINAL        = 1 << 4;
    public static final int SYNCHRONIZED = 1 << 5;
    public static final int VOLATILE     = 1 << 6;
    public static final int TRANSIENT    = 1 << 7;
    public static final int NATIVE       = 1 << 8;
    public static final int INTERFACE    = 1 << 9;
    public static final int ABSTRACT     = 1 << 10;
    public static final int STRICTFP     = 1 << 11;

    /* Flag that marks a symbol synthetic, added in classfile v49.0. */
    public static final int SYNTHETIC = 1 << 12;

    /**
     * Flag that marks attribute interfaces, added in classfile v49.0.
     */
    public static final int ANNOTATION = 1 << 13;

    /**
     * An enumeration type or an enumeration constant, added in
     * classfile v49.0.
     */
    public static final int ENUM = 1 << 14;

    public static final int StandardFlags = 0x0fff;
    public static final int ModifierFlags = StandardFlags & ~INTERFACE;

    // Because the following access flags are overloaded with other
    // bit positions, we translate them when reading and writing class
    // files into unique bits positions: ACC_SYNTHETIC <-> SYNTHETIC,
    // for example.
    public static final int ACC_SUPER     = 0x0020;
    public static final int ACC_BRIDGE    = 0x0040;
    public static final int ACC_VARARGS   = 0x0080;
    public static final int ACC_SYNTHETIC = 0x1000;

    /*****************************************
     * Internal compiler flags (no bits in the lower 16).
     *****************************************/

    /**
     * Flag is set if symbol is deprecated.
     */
    public static final int DEPRECATED = 1 << 17;

    /**
     * Flag is set for a variable symbol if the variable's definition
     * has an initializer part.
     */
    public static final int HASINIT = 1 << 18;

    /**
     * Flag is set for compiler-generated anonymous method symbols
     * that `own' an initializer block.
     */
    public static final int BLOCK = 1 << 20;

    /**
     * Flag is set for compiler-generated abstract methods that implement
     * an interface method (Miranda methods).
     */
    public static final int IPROXY = 1 << 21;

    /**
     * Flag is set for nested classes that do not access instance members
     * or `this' of an outer class and therefore don't need to be passed
     * a this$n reference.  This flag is currently set only for anonymous
     * classes in superclass constructor calls and only for pre 1.4 targets.
     * todo: use this flag for optimizing away this$n parameters in
     * other cases.
     */
    public static final int NOOUTERTHIS = 1 << 22;

    /**
     * Flag is set for package symbols if a package has a member or
     * directory and therefore exists.
     */
    public static final int EXISTS = 1 << 23;

    /**
     * Flag is set for compiler-generated compound classes
     * representing multiple variable bounds
     */
    public static final int COMPOUND = 1 << 24;

    /**
     * Flag is set for class symbols if a class file was found for this class.
     */
    public static final int CLASS_SEEN = 1 << 25;

    /**
     * Flag is set for class symbols if a source file was found for this
     * class.
     */
    public static final int SOURCE_SEEN = 1 << 26;

    /* State flags (are reset during compilation).
     */

    /**
     * Flag for class symbols is set and later re-set as a lock in
     * Enter to detect cycles in the superclass/superinterface
     * relations.  Similarly for constructor call cycle detection in
     * Attr.
     */
    public static final int LOCKED = 1 << 27;

    /**
     * Flag for class symbols is set and later re-set to indicate that a class
     * has been entered but has not yet been attributed.
     */
    public static final int UNATTRIBUTED = 1 << 28;

    /**
     * Flag for synthesized default constructors of anonymous classes.
     */
    public static final int ANONCONSTR = 1 << 29;

    /**
     * Flag for class symbols to indicate it has been checked and found
     * acyclic.
     */
    public static final int ACYCLIC = 1 << 30;

    /**
     * Flag that marks bridge methods.
     */
    public static final long BRIDGE = 1L << 31;

    /**
     * Flag that marks formal parameters.
     */
    public static final long PARAMETER = 1L << 33;

    /**
     * Flag that marks varargs methods.
     */
    public static final long VARARGS = 1L << 34;

    /**
     * Flag for annotation type symbols to indicate it has been
     * checked and found acyclic.
     */
    public static final long ACYCLIC_ANN = 1L << 35;

    /**
     * Flag that marks a generated default constructor.
     */
    public static final long GENERATEDCONSTR = 1L << 36;

    /**
     * Flag that marks a hypothetical method that need not really be
     * generated in the binary, but is present in the symbol table to
     * simplify checking for erasure clashes - also used for 292 poly sig methods.
     */
    public static final long HYPOTHETICAL = 1L << 37;

    /**
     * Flag that marks an internal proprietary class.
     */
    public static final long PROPRIETARY = 1L << 38;

    /**
     * Flag that marks a a multi-catch parameter
     */
    public static final long UNION = 1L << 39;

    /**
     * Flag that marks a special kind of bridge methods (the ones that
     * come from restricted supertype bounds)
     */
    public static final long OVERRIDE_BRIDGE = 1L << 40;

    /**
     * Flag that marks an 'effectively final' local variable
     */
    public static final long EFFECTIVELY_FINAL = 1L << 41;

    /**
     * Flag that marks non-override equivalent methods with the same signature
     */
    public static final long CLASH = 1L << 42;

    /**
     * Flag that marks either a default method or an interface containing default methods.
     */
    public static final long DEFAULT = 1L<<43;

    /**
     * Modifier masks.
     */
    public static final int
        AccessFlags          = PUBLIC | PROTECTED | PRIVATE,
        LocalClassFlags      = FINAL | ABSTRACT | STRICTFP | ENUM | SYNTHETIC,
        MemberClassFlags     = LocalClassFlags | INTERFACE | AccessFlags,
        ClassFlags           = LocalClassFlags | INTERFACE | PUBLIC | ANNOTATION,
        InterfaceVarFlags    = FINAL | STATIC | PUBLIC,
        VarFlags             = AccessFlags | FINAL | STATIC |
                               VOLATILE | TRANSIENT | ENUM,
        ConstructorFlags     = AccessFlags,
        InterfaceMethodFlags = ABSTRACT | PUBLIC,
        MethodFlags          = AccessFlags | ABSTRACT | STATIC | NATIVE |
                               SYNCHRONIZED | FINAL | STRICTFP;

    public static final long LocalVarFlags = FINAL | PARAMETER;

    public static Set<Modifier> asModifierSet(final long flags) {
        Set<Modifier> modifiers = modifierSets.get(flags);

        if (modifiers == null) {
            modifiers = java.util.EnumSet.noneOf(Modifier.class);

            if (0 != (flags & PUBLIC)) {
                modifiers.add(Modifier.PUBLIC);
            }
            if (0 != (flags & PROTECTED)) {
                modifiers.add(Modifier.PROTECTED);
            }
            if (0 != (flags & PRIVATE)) {
                modifiers.add(Modifier.PRIVATE);
            }
            if (0 != (flags & ABSTRACT)) {
                modifiers.add(Modifier.ABSTRACT);
            }
            if (0 != (flags & STATIC)) {
                modifiers.add(Modifier.STATIC);
            }
            if (0 != (flags & FINAL)) {
                modifiers.add(Modifier.FINAL);
            }
            if (0 != (flags & TRANSIENT)) {
                modifiers.add(Modifier.TRANSIENT);
            }
            if (0 != (flags & VOLATILE)) {
                modifiers.add(Modifier.VOLATILE);
            }
            if (0 != (flags & SYNCHRONIZED)) {
                modifiers.add(Modifier.SYNCHRONIZED);
            }
            if (0 != (flags & NATIVE)) {
                modifiers.add(Modifier.NATIVE);
            }
            if (0 != (flags & STRICTFP)) {
                modifiers.add(Modifier.STRICTFP);
            }

            modifiers = Collections.unmodifiableSet(modifiers);
            modifierSets.put(flags, modifiers);
        }

        return modifiers;
    }

    public static boolean testAny(final int value, final int flags) {
        return (value & flags) != 0;
    }

    public static boolean testAll(final int value, final int flags) {
        return (value & flags) == flags;
    }

    public static boolean testAny(final long value, final long flags) {
        return (value & flags) != 0;
    }

    public static boolean testAll(final long value, final long flags) {
        return (value & flags) == flags;
    }

    // Cache of modifier sets.
    private static final Map<Long, Set<Modifier>> modifierSets =
        new java.util.concurrent.ConcurrentHashMap<>(64);
    
    public static boolean isEnum(final MemberInfo symbol) {
        return (symbol.getModifiers() & ENUM) != 0;
    }

    public enum Flag {
        PUBLIC("public"),
        PRIVATE("private"),
        PROTECTED("protected"),
        STATIC("static"),
        FINAL("final"),
        SYNCHRONIZED("synchronized"),
        VOLATILE("volatile"),
        TRANSIENT("transient"),
        NATIVE("native"),
        INTERFACE("interface"),
        ABSTRACT("abstract"),
        DEFAULT("default"),
        STRICTFP("strictfp"),
        SUPER("super"),
        BRIDGE("bridge"),
        SYNTHETIC("synthetic"),
        DEPRECATED("deprecated"),
        HASINIT("hasinit"),
        ENUM("enum"),
        MANDATED("mandated"),
        IPROXY("iproxy"),
        NOOUTERTHIS("noouterthis"),
        EXISTS("exists"),
        COMPOUND("compound"),
        CLASS_SEEN("class_seen"),
        SOURCE_SEEN("source_seen"),
        LOCKED("locked"),
        UNATTRIBUTED("unattributed"),
        ANONCONSTR("anonconstr"),
        ACYCLIC("acyclic"),
        PARAMETER("parameter"),
        VARARGS("varargs"),
        PACKAGE("package");

        public final String name;

        Flag(final String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }
}
