package com.strobel.assembler.metadata;

import com.strobel.compilerservices.RuntimeHelpers;
import org.junit.Test;

import static com.strobel.assembler.metadata.MetadataHelper.isAssignableFrom;
import static com.strobel.assembler.metadata.ConversionType.*;
import static com.strobel.core.CollectionUtilities.single;
import static java.lang.String.format;
import static org.junit.Assert.*;

@SuppressWarnings("UnusedDeclaration")
public class MetadataHelperTests {
    static {
        RuntimeHelpers.ensureClassInitialized(MetadataSystem.class);
    }

    // @formatter:off
    private static final boolean[][] IS_ASSIGNABLE_BIT_SET = {
       // bool   byte   short  char   int    long   float  double
        { true,  false, false, false, false, false, false, false },  // bool
        { false, true,  true,  false, true,  true,  true,  true  },  // byte
        { false, false, true,  false, true,  true,  true,  true  },  // short
        { false, false, false, true,  true,  true,  true,  true  },  // char
        { false, false, false, false, true,  true,  true,  true  },  // int
        { false, false, false, false, false, true,  true,  true  },  // long
        { false, false, false, false, false, false, true,  true  },  // float
        { false, false, false, false, false, false, false, true  },  // double
    };

    private static final ConversionType[][] CONVERSIONS = {
       // bool      byte      short     char      int       long      float           double
        { IDENTITY, NONE,     NONE,     NONE,     NONE,     NONE,     NONE,           NONE           },  // bool
        { NONE,     IDENTITY, IMPLICIT, EXPLICIT, IMPLICIT, IMPLICIT, IMPLICIT,       IMPLICIT       },  // byte
        { NONE,     EXPLICIT, IDENTITY, EXPLICIT, IMPLICIT, IMPLICIT, IMPLICIT,       IMPLICIT       },  // short
        { NONE,     EXPLICIT, EXPLICIT, IDENTITY, IMPLICIT, IMPLICIT, IMPLICIT,       IMPLICIT       },  // char
        { NONE,     EXPLICIT, EXPLICIT, EXPLICIT, IDENTITY, IMPLICIT, IMPLICIT_LOSSY, IMPLICIT       },  // int
        { NONE,     EXPLICIT, EXPLICIT, EXPLICIT, EXPLICIT, IDENTITY, IMPLICIT_LOSSY, IMPLICIT_LOSSY },  // long
        { NONE,     EXPLICIT, EXPLICIT, EXPLICIT, EXPLICIT, EXPLICIT, IDENTITY,       IMPLICIT       },  // float
        { NONE,     EXPLICIT, EXPLICIT, EXPLICIT, EXPLICIT, EXPLICIT, EXPLICIT,       IDENTITY       },  // double
    };
    // @formatter:on

    private static TypeReference string() {
        return MetadataSystem.instance().lookupTypeCore("java/lang/String");
    }

    private static TypeReference charSequence() {
        return MetadataSystem.instance().lookupTypeCore("java/lang/CharSequence");
    }

    private static TypeReference integer() {
        return MetadataSystem.instance().lookupTypeCore("java/lang/Integer");
    }

    private static TypeReference list() {
        return MetadataSystem.instance().lookupTypeCore("java/util/List");
    }

    private static TypeReference arrayList() {
        return MetadataSystem.instance().lookupTypeCore("java/util/ArrayList");
    }

    private static TypeReference iterable() {
        return MetadataSystem.instance().lookupTypeCore("java/lang/Iterable");
    }

    private static void assertSameType(final TypeReference expected, final TypeReference actual) {
        if (MetadataHelper.isSameType(expected, actual, true)) {
            return;
        }
        fail(
            format(
                "Type comparison failed!%nExpected: %s%n  Actual: %s",
                expected != null ? expected.getSignature() : null,
                actual != null ? actual.getSignature() : null
            )
        );
    }

    @Test
    public void testIsAssignableBetweenPrimitives() throws Throwable {
        final JvmType[] jvmTypes = JvmType.values();

        final TypeReference[] primitiveTypes = {
            BuiltinTypes.Boolean,
            BuiltinTypes.Byte,
            BuiltinTypes.Short,
            BuiltinTypes.Character,
            BuiltinTypes.Integer,
            BuiltinTypes.Long,
            BuiltinTypes.Float,
            BuiltinTypes.Double,
        };

        for (int i = 0, n = IS_ASSIGNABLE_BIT_SET.length; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (MetadataHelper.isAssignableFrom(primitiveTypes[i], primitiveTypes[j]) != IS_ASSIGNABLE_BIT_SET[j][i]) {
                    fail(format("%s (assignable from) %s = %s", primitiveTypes[i], primitiveTypes[j], IS_ASSIGNABLE_BIT_SET[j][i]));
                }
                if (MetadataHelper.getConversionType(primitiveTypes[i], primitiveTypes[j]) != CONVERSIONS[j][i]) {
                    fail(format("%s (conversion from) %s = %s", primitiveTypes[i], primitiveTypes[j], CONVERSIONS[j][i]));
                }
            }
        }
    }

    @Test
    public void testIsSameTypeWithSimpleGenerics() throws Throwable {
/*
        final TypeReference arrayList = arrayList();
        final TypeReference rawArrayList = new RawType(arrayList());
        final TypeReference genericArrayList = arrayList().makeGenericType(string());

        assertTrue(isSameType(rawArrayList, genericArrayList, false));
        assertTrue(isSameType(genericArrayList, rawArrayList, false));

        assertFalse(isSameType(rawArrayList, genericArrayList, true));
        assertFalse(isSameType(genericArrayList, rawArrayList, true));

        assertTrue(isSameType(arrayList, arrayList, false));
        assertTrue(isSameType(rawArrayList, rawArrayList, false));
        assertTrue(isSameType(genericArrayList, genericArrayList, false));
        assertTrue(isSameType(arrayList, arrayList, true));
        assertTrue(isSameType(rawArrayList, rawArrayList, true));
        assertTrue(isSameType(genericArrayList, genericArrayList, true));

        assertFalse(isSameType(arrayList, rawArrayList, false));
        assertFalse(isSameType(arrayList, genericArrayList, false));
        assertFalse(isSameType(rawArrayList, arrayList, false));
        assertFalse(isSameType(genericArrayList, arrayList, false));

        assertFalse(isSameType(arrayList, rawArrayList, true));
        assertFalse(isSameType(arrayList, genericArrayList, true));
        assertFalse(isSameType(rawArrayList, arrayList, true));
        assertFalse(isSameType(genericArrayList, arrayList, true));
*/
    }

    @Test
    public void testAsSuperWithSimpleGenerics() throws Throwable {
        final TypeReference arrayList = arrayList();
        final TypeReference rawArrayList = new RawType(arrayList());
        final TypeReference genericArrayList = arrayList().makeGenericType(string());

        final TypeReference iterable = iterable();
        final TypeReference rawIterable = new RawType(iterable());
        final TypeReference genericIterable = iterable().makeGenericType(string());
        final TypeReference wildIterable = iterable().makeGenericType(WildcardType.unbounded());

        final TypeReference t1 = MetadataHelper.asSuper(genericIterable, arrayList);
        final TypeReference t2 = MetadataHelper.asSuper(genericIterable, genericArrayList);
        final TypeReference t3 = MetadataHelper.asSuper(genericIterable, rawArrayList);
        final TypeReference t4 = MetadataHelper.asSuper(iterable, arrayList);
        final TypeReference t5 = MetadataHelper.asSuper(iterable, genericArrayList);
        final TypeReference t6 = MetadataHelper.asSuper(iterable, rawArrayList);
        final TypeReference t7 = MetadataHelper.asSuper(rawIterable, arrayList);
        final TypeReference t8 = MetadataHelper.asSuper(rawIterable, genericArrayList);
        final TypeReference t9 = MetadataHelper.asSuper(rawIterable, rawArrayList);

        assertSameType(iterable.makeGenericType(single(arrayList.getGenericParameters())), t1);
        assertSameType(genericIterable, t2);
        assertSameType(rawIterable, t3);
        assertSameType(iterable.makeGenericType(single(arrayList.getGenericParameters())), t4);
        assertSameType(genericIterable, t5);
        assertSameType(rawIterable, t6);
        assertSameType(iterable.makeGenericType(single(arrayList.getGenericParameters())), t7);
        assertSameType(genericIterable, t8);
        assertSameType(rawIterable, t9);
    }

    @Test
    public void testAsSuperWithWildcards() throws Throwable {
        final TypeReference arrayList = arrayList();
        final TypeReference rawArrayList = new RawType(arrayList());
        final TypeReference genericArrayList = arrayList().makeGenericType(string());
        final TypeReference wildArrayList = arrayList().makeGenericType(WildcardType.unbounded());

        final TypeReference iterable = iterable();
        final TypeReference rawIterable = new RawType(iterable());
        final TypeReference genericIterable = iterable().makeGenericType(string());
        final TypeReference wildIterable = iterable().makeGenericType(WildcardType.unbounded());
        final TypeReference iterableOfE = iterable.makeGenericType(arrayList.getGenericParameters());

        final TypeReference t1 = MetadataHelper.asSuper(rawIterable, wildIterable);
        final TypeReference t2 = MetadataHelper.asSuper(rawIterable, wildArrayList);
        final TypeReference t3 = MetadataHelper.asSuper(wildIterable, iterable);
        final TypeReference t4 = MetadataHelper.asSuper(wildIterable, rawIterable);
        final TypeReference t5 = MetadataHelper.asSuper(wildIterable, genericIterable);
        final TypeReference t6 = MetadataHelper.asSuper(wildIterable, arrayList);
        final TypeReference t7 = MetadataHelper.asSuper(wildIterable, rawArrayList);
        final TypeReference t8 = MetadataHelper.asSuper(wildIterable, genericArrayList);
        final TypeReference t9 = MetadataHelper.asSuper(wildIterable, wildArrayList);

        assertSameType(wildIterable, t1);
        assertSameType(wildIterable, t2);
        assertSameType(iterable, t3);
        assertSameType(rawIterable, t4);
        assertSameType(genericIterable, t5);
        assertSameType(iterableOfE, t6);
        assertSameType(rawIterable, t7);
        assertSameType(genericIterable, t8);
        assertSameType(wildIterable, t9);
    }

    @Test
    public void testIsAssignableWithWildcards() throws Throwable {
        final TypeReference arrayList = arrayList();
        final TypeReference rawArrayList = new RawType(arrayList());
        final TypeReference genericArrayList = arrayList().makeGenericType(string());
        final TypeReference wildArrayList = arrayList().makeGenericType(WildcardType.unbounded());

        final TypeReference iterable = iterable();
        final TypeReference rawIterable = new RawType(iterable());
        final TypeReference genericIterable = iterable().makeGenericType(string());
        final TypeReference wildIterable = iterable().makeGenericType(WildcardType.unbounded());

        final TypeReference t1 = MetadataHelper.asSuper(rawIterable, wildIterable);
        final TypeReference t2 = MetadataHelper.asSuper(rawIterable, wildArrayList);
        final TypeReference t3 = MetadataHelper.asSuper(wildIterable, iterable);
        final TypeReference t4 = MetadataHelper.asSuper(wildIterable, rawIterable);
        final TypeReference t5 = MetadataHelper.asSuper(wildIterable, genericIterable);
        final TypeReference t6 = MetadataHelper.asSuper(wildIterable, arrayList);
        final TypeReference t7 = MetadataHelper.asSuper(wildIterable, rawArrayList);
        final TypeReference t8 = MetadataHelper.asSuper(wildIterable, genericArrayList);
        final TypeReference t9 = MetadataHelper.asSuper(wildIterable, wildArrayList);

        assertTrue(isAssignableFrom(rawIterable, iterable));
        assertTrue(isAssignableFrom(rawIterable, rawIterable));
        assertTrue(isAssignableFrom(rawIterable, genericIterable));
        assertTrue(isAssignableFrom(rawIterable, wildIterable));
        assertTrue(isAssignableFrom(rawIterable, arrayList));
        assertTrue(isAssignableFrom(rawIterable, rawArrayList));
        assertTrue(isAssignableFrom(rawIterable, genericArrayList));
        assertTrue(isAssignableFrom(rawIterable, wildArrayList));

        assertTrue(isAssignableFrom(wildIterable, iterable));
        assertTrue(isAssignableFrom(wildIterable, rawIterable));
        assertTrue(isAssignableFrom(wildIterable, genericIterable));
        assertTrue(isAssignableFrom(wildIterable, wildIterable));
        assertTrue(isAssignableFrom(wildIterable, arrayList));
        assertTrue(isAssignableFrom(wildIterable, rawArrayList));
        assertTrue(isAssignableFrom(wildIterable, genericArrayList));
        assertTrue(isAssignableFrom(wildIterable, wildArrayList));
    }

    @Test
    public void testAsSubTypeWithSimpleGenerics() throws Throwable {
        final TypeReference arrayList = arrayList();
        final TypeReference rawArrayList = new RawType(arrayList());
        final TypeReference genericArrayList = arrayList().makeGenericType(string());

        final TypeReference iterable = iterable();
        final TypeReference rawIterable = new RawType(iterable());
        final TypeReference genericIterable = iterable().makeGenericType(string());

        final TypeReference t1 = MetadataHelper.asSubType(arrayList, genericIterable);
        final TypeReference t2 = MetadataHelper.asSubType(genericArrayList, genericIterable);
        final TypeReference t3 = MetadataHelper.asSubType(rawArrayList, genericIterable);
        final TypeReference t4 = MetadataHelper.asSubType(arrayList, iterable);
        final TypeReference t5 = MetadataHelper.asSubType(genericArrayList, iterable);
        final TypeReference t6 = MetadataHelper.asSubType(rawArrayList, iterable);
        final TypeReference t7 = MetadataHelper.asSubType(arrayList, rawIterable);
        final TypeReference t8 = MetadataHelper.asSubType(genericArrayList, rawIterable);
        final TypeReference t9 = MetadataHelper.asSubType(rawArrayList, rawIterable);

        assertSameType(genericArrayList, t1);
        assertSameType(genericArrayList, t2);
        assertSameType(genericArrayList, t3);
        assertSameType(arrayList.makeGenericType(single(iterable.getGenericParameters())), t4);
        assertSameType(genericArrayList, t5);
        assertSameType(arrayList.makeGenericType(single(iterable.getGenericParameters())), t6);
        assertSameType(rawArrayList, t7);
        assertSameType(genericArrayList, t8);
        assertSameType(rawArrayList, t9);
    }
}
