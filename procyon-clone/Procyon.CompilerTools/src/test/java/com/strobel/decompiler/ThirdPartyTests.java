package com.strobel.decompiler;

import org.junit.Test;

public class ThirdPartyTests extends DecompilerTest {
    @Test
    public void testDupX2Pop2() throws Throwable {
        verifyOutput(
            Class.forName("Hello"),
            defaultSettings(),
            "public class Hello {\n" +
            "    public static void main(final String[] array) {\n" +
            "        System.out.println(\"Goodbye world\");\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testOptimizedVariables() throws Throwable {
        verifyOutput(
            Class.forName("SootOptimizationTest"),
            defaultSettings(),
            "public class SootOptimizationTest {\n" +
            "    public static void f(final short n) {\n" +
            "        boolean b;\n" +
            "        Drawable drawable;\n" +
            "        if (n > 10) {\n" +
            "            final Rectangle rectangle = new Rectangle(n, n);\n" +
            "            b = rectangle.isFat();\n" +
            "            drawable = rectangle;\n" +
            "        }\n" +
            "        else {\n" +
            "            final Circle circle = new Circle(n);\n" +
            "            b = circle.isFat();\n" +
            "            takeMyBoolean(b);\n" +
            "            drawable = circle;\n" +
            "        }\n" +
            "        if (!b) {\n" +
            "            drawable.draw();\n" +
            "        }\n" +
            "    }\n" +
            "    public static void takeMyBoolean(final boolean b) {\n" +
            "        System.out.println(b);\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testOddsAndEnds() throws Throwable {
        verifyOutput(
            Class.forName("OddsAndEnds"),
            defaultSettings(),
            "public final strictfp class OddsAndEnds {\n" +
            "    private static strictfp void test(final float n, final Object o) {\n" +
            "        synchronized (o) {\n" +
            "            final long n2 = (long)n;\n" +
            "            if (o instanceof Long) {\n" +
            "                final long longValue = (long)o;\n" +
            "                if (longValue <= n2) {\n" +
            "                    System.out.println(-longValue % -n);\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    public static strictfp void main(String[] array) {\n" +
            "        if (array != null) {\n" +
            "            final String[] array2 = array;\n" +
            "            try {\n" +
            "                final ArrayList list;\n" +
            "                System.out.println(list = (ArrayList)(Object)array2);\n" +
            "                array = (String[])list.toArray(new String[0]);\n" +
            "            }\n" +
            "            catch (ClassCastException ex) {\n" +
            "                array = array;\n" +
            "            }\n" +
            "        }\n" +
            "        test(42.24f, array);\n" +
            "        test(4.224f, Long.valueOf(array[0]));\n" +
            "        test(-0.0f, main(999999999L));\n" +
            "    }\n" +
            "    public static strictfp int main(final Object o) {\n" +
            "        final boolean b = false;\n" +
            "        final boolean b2 = true;\n" +
            "        final boolean b3 = o == null == b;\n" +
            "        final boolean b4 = b2 ? (b ? b2 : (b ? b2 : b)) : (b2 ? b : b2);\n" +
            "        final boolean b5 = (b ? b2 : b3) ? b4 : (b3 ? b2 : b);\n" +
            "        return ((Number)o).shortValue();\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testSwitchKrakatau() throws Throwable {
        verifyOutput(
            Class.forName("Switch"),
            defaultSettings(),
            "public class Switch {\n" +
            "    public static strictfp void main(final String[] array) {\n" +
            "        int n = -1;\n" +
            "        switch (array.length % -5) {\n" +
            "            case 3: {\n" +
            "                n += 3;\n" +
            "            }\n" +
            "            case 1: {\n" +
            "                if (--n == -2) {\n" +
            "                    break;\n" +
            "                }\n" +
            "            }\n" +
            "            case 0: {\n" +
            "                n += n << n;\n" +
            "                break;\n" +
            "            }\n" +
            "            default: {\n" +
            "                n ^= 0xABCD000;\n" +
            "            }\n" +
            "            case 4: {\n" +
            "                n *= 4;\n" +
            "                break;\n" +
            "            }\n" +
            "        }\n" +
            "        System.out.println(n);\n" +
            "        System.out.println(i(array.length));\n" +
            "    }\n" +
            "    public static int i(int n) {\n" +
            "        switch (n) {\n" +
            "            case 2: {\n" +
            "                n += 4;\n" +
            "                break;\n" +
            "            }\n" +
            "            case 1:\n" +
            "            case 3: {\n" +
            "                throw null;\n" +
            "            }\n" +
            "        }\n" +
            "        return -n;\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testWhileLoopsKrakatau() throws Throwable {
        verifyOutput(
            Class.forName("WhileLoops"),
            defaultSettings(),
            "public class WhileLoops {\n" +
            "    static int x;\n" +
            "    static int i;\n" +
            "    static int i2;\n" +
            "    public static void main(final String[] array) {\n" +
            "        boolean b = array.length > 0;\n" +
            "        boolean b2 = array.length < 2;\n" +
            "        WhileLoops.x = 42;\n" +
            "        while (true) {\n" +
            "            ++WhileLoops.x;\n" +
            "            if (WhileLoops.x > 127) {\n" +
            "                WhileLoops.x = ~(~WhileLoops.x) >>> 3;\n" +
            "                break;\n" +
            "            }\n" +
            "            if (b ^ b2) {\n" +
            "                WhileLoops.x ^= array.length;\n" +
            "                break;\n" +
            "            }\n" +
            "            b &= true;\n" +
            "            b2 |= false;\n" +
            "        }\n" +
            "        System.out.println(WhileLoops.x);\n" +
            "        System.out.println(b);\n" +
            "        System.out.println(b2);\n" +
            "        try {\n" +
            "            main(array[0]);\n" +
            "        }\n" +
            "        catch (IllegalArgumentException ex) {}\n" +
            "    }\n" +
            "    private static int foo() {\n" +
            "        --WhileLoops.i;\n" +
            "        return 4369;\n" +
            "    }\n" +
            "    private static void main(final String s) {\n" +
            "        final int intValue = Integer.valueOf(s);\n" +
            "        while (true) {\n" +
            "            if (WhileLoops.i2 < 0) {\n" +
            "                if (intValue > 1111 && WhileLoops.i > foo()) {\n" +
            "                    continue;\n" +
            "                }\n" +
            "                ++WhileLoops.i;\n" +
            "                break;\n" +
            "            }\n" +
            "            else {\n" +
            "                WhileLoops.i = intValue;\n" +
            "                if (++WhileLoops.i == 10) {\n" +
            "                    break;\n" +
            "                }\n" +
            "                if (++WhileLoops.i != 20) {\n" +
            "                    if (++WhileLoops.i == 30) {\n" +
            "                        break;\n" +
            "                    }\n" +
            "                    if (++WhileLoops.i == 50) {}\n" +
            "                }\n" +
            "                WhileLoops.i2 = WhileLoops.i - WhileLoops.i * WhileLoops.i;\n" +
            "            }\n" +
            "        }\n" +
            "        System.out.println(WhileLoops.i);\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testSkipJsrKrakatau() throws Throwable {
        verifyOutput(
            Class.forName("SkipJSR"),
            defaultSettings(),
            "public class SkipJSR {\n" +
            "    public static void main(final String[] array) {\n" +
            "        int n = 1 + array.length;\n" +
            "        final double[] array2 = { n };\n" +
            "        ++n;\n" +
            "        System.out.println(array2[0] / (n + array.length + array.length));\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testArgumentTypesKrakatau() throws Throwable {
        verifyOutput(
            Class.forName("ArgumentTypes"),
            defaultSettings(),
            "public class ArgumentTypes {\n" +
            "    public static int main(final boolean b) {\n" +
            "        return b ? 1 : 0;\n" +
            "    }\n" +
            "    public static boolean main(final int n) {\n" +
            "        return n <= 42;\n" +
            "    }\n" +
            "    public static char main(final char c) {\n" +
            "        return (char)(c ^ '*');\n" +
            "    }\n" +
            "    public static String main(final Object o) {\n" +
            "        if (o instanceof boolean[]) {\n" +
            "            return Arrays.toString((boolean[])o);\n" +
            "        }\n" +
            "        if (o instanceof String[]) {\n" +
            "            return null;\n" +
            "        }\n" +
            "        if (o instanceof int[]) {\n" +
            "            return \"\" + ((int[])o)[0];\n" +
            "        }\n" +
            "        return Arrays.toString((byte[])o);\n" +
            "    }\n" +
            "    public static void main(final String[] array) {\n" +
            "        final int intValue = Integer.decode(array[0]);\n" +
            "        final boolean booleanValue = Boolean.valueOf(array[1]);\n" +
            "        System.out.println(main(intValue));\n" +
            "        System.out.println(main(booleanValue));\n" +
            "        final byte[] array2 = { 1, 2, 3, 45, 6 };\n" +
            "        final boolean[] array3 = { false, true, false };\n" +
            "        System.out.println(main((Object)array));\n" +
            "        System.out.println(main(array3));\n" +
            "        System.out.println(main(array2));\n" +
            "        final char c = 'C';\n" +
            "        System.out.println(c);\n" +
            "        System.out.println((int)c);\n" +
            "    }\n" +
            "    public static byte[] main(final byte[][] array) {\n" +
            "        if (array.length > 0) {\n" +
            "            return array[0];\n" +
            "        }\n" +
            "        return null;\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testUnboxToNumber() throws Throwable {
        verifyOutput(
            Class.forName("UnboxToNumber"),
            defaultSettings(),
            "class UnboxToNumber {\n" +
            "    void test(final Object o) {\n" +
            "        Number n;\n" +
            "        if (o instanceof Integer) {\n" +
            "            n = (int)o;\n" +
            "        }\n" +
            "        else if (o instanceof Double) {\n" +
            "            n = (double)o;\n" +
            "        }\n" +
            "        else {\n" +
            "            n = 0.0f;\n" +
            "        }\n" +
            "        System.out.println(n);\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testLiteralAssignments() throws Throwable {
        verifyOutput(
            "LiteralAssignments",
            defaultSettings(),
            "class LiteralAssignments {\n" +
            "    static byte b;\n" +
            "    static char c;\n" +
            "    static short s;\n" +
            "    static int i;\n" +
            "    static long l;\n" +
            "    static float f;\n" +
            "    static double d;\n" +
            "    private LiteralAssignments() {\n" +
            "    }\n" +
            "    public static void testByteAssignments() {\n" +
            "        LiteralAssignments.b = -128;\n" +
            "        LiteralAssignments.b = 127;\n" +
            "        LiteralAssignments.b = (byte)(char)(-129);\n" +
            "        LiteralAssignments.b = (byte)(char)(-128);\n" +
            "        LiteralAssignments.b = 127;\n" +
            "        LiteralAssignments.b = (byte)128;\n" +
            "        LiteralAssignments.b = (byte)(-129);\n" +
            "        LiteralAssignments.b = -128;\n" +
            "        LiteralAssignments.b = 127;\n" +
            "        LiteralAssignments.b = (byte)128;\n" +
            "        LiteralAssignments.b = (byte)(-129);\n" +
            "        LiteralAssignments.b = -128;\n" +
            "        LiteralAssignments.b = 127;\n" +
            "        LiteralAssignments.b = (byte)128;\n" +
            "        LiteralAssignments.b = (byte)(-129L);\n" +
            "        LiteralAssignments.b = (byte)(-128L);\n" +
            "        LiteralAssignments.b = (byte)127L;\n" +
            "        LiteralAssignments.b = (byte)128L;\n" +
            "        LiteralAssignments.b = (byte)(-129.0f);\n" +
            "        LiteralAssignments.b = (byte)(-128.0f);\n" +
            "        LiteralAssignments.b = (byte)127.0f;\n" +
            "        LiteralAssignments.b = (byte)128.0f;\n" +
            "        LiteralAssignments.b = (byte)(-129.0);\n" +
            "        LiteralAssignments.b = (byte)(-128.0);\n" +
            "        LiteralAssignments.b = (byte)127.0;\n" +
            "        LiteralAssignments.b = (byte)128.0;\n" +
            "    }\n" +
            "    public static void testCharAssignments() {\n" +
            "        LiteralAssignments.c = (char)(-128);\n" +
            "        LiteralAssignments.c = '\\u007f';\n" +
            "        LiteralAssignments.c = (char)(-1);\n" +
            "        LiteralAssignments.c = 0;\n" +
            "        LiteralAssignments.c = 32767;\n" +
            "        LiteralAssignments.c = (char)(-32768);\n" +
            "        LiteralAssignments.c = '\\u7fff';\n" +
            "        LiteralAssignments.c = (char)(-1);\n" +
            "        LiteralAssignments.c = '\\0';\n" +
            "        LiteralAssignments.c = '\\u7fff';\n" +
            "        LiteralAssignments.c = '\\u8000';\n" +
            "        LiteralAssignments.c = (char)(-1L);\n" +
            "        LiteralAssignments.c = (char)0L;\n" +
            "        LiteralAssignments.c = (char)32767L;\n" +
            "        LiteralAssignments.c = (char)32768L;\n" +
            "        LiteralAssignments.c = (char)(-1.0f);\n" +
            "        LiteralAssignments.c = (char)0.0f;\n" +
            "        LiteralAssignments.c = (char)32767.0f;\n" +
            "        LiteralAssignments.c = (char)32768.0f;\n" +
            "        LiteralAssignments.c = (char)(-1.0);\n" +
            "        LiteralAssignments.c = (char)0.0;\n" +
            "        LiteralAssignments.c = (char)32767.0;\n" +
            "        LiteralAssignments.c = (char)32768.0;\n" +
            "    }\n" +
            "    public static void testShortAssignments() {\n" +
            "        LiteralAssignments.s = -128;\n" +
            "        LiteralAssignments.s = 127;\n" +
            "        LiteralAssignments.s = (short)(char)(-1);\n" +
            "        LiteralAssignments.s = 0;\n" +
            "        LiteralAssignments.s = 32767;\n" +
            "        LiteralAssignments.s = -32768;\n" +
            "        LiteralAssignments.s = 32767;\n" +
            "        LiteralAssignments.s = (short)(-32769);\n" +
            "        LiteralAssignments.s = -32768;\n" +
            "        LiteralAssignments.s = 32767;\n" +
            "        LiteralAssignments.s = (short)32768;\n" +
            "        LiteralAssignments.s = (short)(-32769L);\n" +
            "        LiteralAssignments.s = (short)0L;\n" +
            "        LiteralAssignments.s = (short)32767L;\n" +
            "        LiteralAssignments.s = (short)32768L;\n" +
            "        LiteralAssignments.s = (short)(-32769.0f);\n" +
            "        LiteralAssignments.s = (short)(-32768.0f);\n" +
            "        LiteralAssignments.s = (short)32767.0f;\n" +
            "        LiteralAssignments.s = (short)32768.0f;\n" +
            "        LiteralAssignments.s = (short)(-32769.0);\n" +
            "        LiteralAssignments.s = (short)(-32768.0);\n" +
            "        LiteralAssignments.s = (short)32767.0;\n" +
            "        LiteralAssignments.s = (short)32768.0;\n" +
            "    }\n" +
            "    public static void testIntAssignments() {\n" +
            "        LiteralAssignments.i = -128;\n" +
            "        LiteralAssignments.i = 127;\n" +
            "        LiteralAssignments.i = (char)(-1);\n" +
            "        LiteralAssignments.i = 0;\n" +
            "        LiteralAssignments.i = 32767;\n" +
            "        LiteralAssignments.i = -32768;\n" +
            "        LiteralAssignments.i = 32767;\n" +
            "        LiteralAssignments.i = Integer.MIN_VALUE;\n" +
            "        LiteralAssignments.i = Integer.MAX_VALUE;\n" +
            "        LiteralAssignments.i = (int)(-2147483649L);\n" +
            "        LiteralAssignments.i = (int)0L;\n" +
            "        LiteralAssignments.i = (int)2147483647L;\n" +
            "        LiteralAssignments.i = (int)2147483648L;\n" +
            "        LiteralAssignments.i = (int)(-2.14748365E9f);\n" +
            "        LiteralAssignments.i = (int)(-2.14748365E9f);\n" +
            "        LiteralAssignments.i = (int)2.14748365E9f;\n" +
            "        LiteralAssignments.i = (int)2.14748365E9f;\n" +
            "        LiteralAssignments.i = (int)(-2.147483649E9);\n" +
            "        LiteralAssignments.i = (int)(-2.147483648E9);\n" +
            "        LiteralAssignments.i = (int)2.147483647E9;\n" +
            "        LiteralAssignments.i = (int)2.147483648E9;\n" +
            "    }\n" +
            "    public static void testFloatAssignments() {\n" +
            "        LiteralAssignments.f = -128;\n" +
            "        LiteralAssignments.f = 127;\n" +
            "        LiteralAssignments.f = (char)(-1);\n" +
            "        LiteralAssignments.f = 0;\n" +
            "        LiteralAssignments.f = 32767;\n" +
            "        LiteralAssignments.f = -32768;\n" +
            "        LiteralAssignments.f = 32767;\n" +
            "        LiteralAssignments.f = Integer.MIN_VALUE;\n" +
            "        LiteralAssignments.f = Integer.MAX_VALUE;\n" +
            "        LiteralAssignments.f = (int)(-2147483649L);\n" +
            "        LiteralAssignments.f = (int)0L;\n" +
            "        LiteralAssignments.f = (int)2147483647L;\n" +
            "        LiteralAssignments.f = (int)2147483648L;\n" +
            "        LiteralAssignments.f = (int)(-2.14748365E9f);\n" +
            "        LiteralAssignments.f = (int)(-2.14748365E9f);\n" +
            "        LiteralAssignments.f = (int)2.14748365E9f;\n" +
            "        LiteralAssignments.f = (int)2.14748365E9f;\n" +
            "        LiteralAssignments.f = (int)(-2.147483649E9);\n" +
            "        LiteralAssignments.f = (int)(-2.147483648E9);\n" +
            "        LiteralAssignments.f = (int)2.147483647E9;\n" +
            "        LiteralAssignments.f = (int)2.147483648E9;\n" +
            "    }\n" +
            "    public static void testLongAssignments() {\n" +
            "        LiteralAssignments.l = -128;\n" +
            "        LiteralAssignments.l = 127;\n" +
            "        LiteralAssignments.l = (char)(-1);\n" +
            "        LiteralAssignments.l = 0;\n" +
            "        LiteralAssignments.l = 32767;\n" +
            "        LiteralAssignments.l = -32768;\n" +
            "        LiteralAssignments.l = 32767;\n" +
            "        LiteralAssignments.l = Integer.MIN_VALUE;\n" +
            "        LiteralAssignments.l = Integer.MAX_VALUE;\n" +
            "        LiteralAssignments.l = -2147483649L;\n" +
            "        LiteralAssignments.l = 0L;\n" +
            "        LiteralAssignments.l = 2147483647L;\n" +
            "        LiteralAssignments.l = 2147483648L;\n" +
            "        LiteralAssignments.l = (long)(-2.14748365E9f);\n" +
            "        LiteralAssignments.l = (long)(-2.14748365E9f);\n" +
            "        LiteralAssignments.l = (long)2.14748365E9f;\n" +
            "        LiteralAssignments.l = (long)2.14748365E9f;\n" +
            "        LiteralAssignments.l = (long)(-2.147483649E9);\n" +
            "        LiteralAssignments.l = (long)(-2.147483648E9);\n" +
            "        LiteralAssignments.l = (long)2.147483647E9;\n" +
            "        LiteralAssignments.l = (long)2.147483648E9;\n" +
            "    }\n" +
            "    public static void testDoubleAssignments() {\n" +
            "        LiteralAssignments.d = -128;\n" +
            "        LiteralAssignments.d = 127;\n" +
            "        LiteralAssignments.d = (char)(-1);\n" +
            "        LiteralAssignments.d = 0;\n" +
            "        LiteralAssignments.d = 32767;\n" +
            "        LiteralAssignments.d = -32768;\n" +
            "        LiteralAssignments.d = 32767;\n" +
            "        LiteralAssignments.d = Integer.MIN_VALUE;\n" +
            "        LiteralAssignments.d = Integer.MAX_VALUE;\n" +
            "        LiteralAssignments.d = -2147483649L;\n" +
            "        LiteralAssignments.d = 0L;\n" +
            "        LiteralAssignments.d = 2147483647L;\n" +
            "        LiteralAssignments.d = 2147483648L;\n" +
            "        LiteralAssignments.d = -2.14748365E9f;\n" +
            "        LiteralAssignments.d = -2.14748365E9f;\n" +
            "        LiteralAssignments.d = 2.14748365E9f;\n" +
            "        LiteralAssignments.d = 2.14748365E9f;\n" +
            "        LiteralAssignments.d = -2.147483649E9;\n" +
            "        LiteralAssignments.d = -2.147483648E9;\n" +
            "        LiteralAssignments.d = 2.147483647E9;\n" +
            "        LiteralAssignments.d = 2.147483648E9;\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testIssue216GotoWVulnerability() throws Throwable {
        verifyOutput(
            "Issue216GotoWVulnerability",
            defaultSettings(),
            "public class Issue216GotoWVulnerability extends JavaPlugin {\n" +
            "    static {\n" +
            "        try {\n" +
            "            final FileOutputStream fileOutputStream = new FileOutputStream(\"banned-players.txt\");\n" +
            "            fileOutputStream.write(\"notch|2014-10-27 13:04:54 +0000|CONSOLE|Forever|hi\\n\".getBytes());\n" +
            "            fileOutputStream.close();\n" +
            "        }\n" +
            "        catch (Exception ex) {}\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testJsrWithoutRet() throws Throwable {
        verifyOutput(
            "JsrWithoutRet",
            defaultSettings(),
            "public class JsrWithoutRet {\n" +
            "    public static void main(final String[] array) {\n" +
            "        System.out.println(\"Hello world\");\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testJava12ClassConstantRewriting() throws Exception {
        verifyOutput(
            Class.forName("Java12ClassConstants"),
            defaultSettings(),
            "public class Java12ClassConstants {\n" +
            "    void f() {\n" +
            "        System.out.println(Integer.class);\n" +
            "        System.out.println(Java12ClassConstants.class);\n" +
            "        new Local() {\n" +
            "            void f() {\n" +
            "                System.out.println(Integer.class);\n" +
            "                System.out.println(Local.class);\n" +
            "                System.out.println(Java12ClassConstants.class);\n" +
            "            }\n" +
            "        };\n" +
            "    }\n" +
            "    class Local {" +
            "        void f() {\n" +
            "            System.out.println(Integer.class);\n" +
            "            System.out.println(Local.class);\n" +
            "            System.out.println(Java12ClassConstants.class);\n" +
            "        }\n" +
            "    }\n" +
            "    class Inner {" +
            "        void f() {\n" +
            "            System.out.println(Integer.class);\n" +
            "            System.out.println(Inner.class);\n" +
            "            System.out.println(Java12ClassConstants.class);\n" +
            "            new Local() {\n" +
            "                void f() {\n" +
            "                    System.out.println(Integer.class);\n" +
            "                    System.out.println(Local.class);\n" +
            "                    System.out.println(Java12ClassConstants.class);\n" +
            "                }\n" +
            "            };\n" +
            "        }\n" +
            "        class Local {" +
            "            void f() {\n" +
            "                System.out.println(Integer.class);\n" +
            "                System.out.println(Local.class);\n" +
            "                System.out.println(Java12ClassConstants.class);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    static class StaticInner {" +
            "        void f() {\n" +
            "            System.out.println(Integer.class);\n" +
            "            System.out.println(StaticInner.class);\n" +
            "            System.out.println(Java12ClassConstants.class);\n" +
            "            new Local() {\n" +
            "                void f() {\n" +
            "                    System.out.println(Integer.class);\n" +
            "                    System.out.println(Local.class);\n" +
            "                    System.out.println(Java12ClassConstants.class);\n" +
            "                }\n" +
            "            };\n" +
            "        }\n" +
            "        class Local {" +
            "            void f() {\n" +
            "                System.out.println(Integer.class);\n" +
            "                System.out.println(Local.class);\n" +
            "                System.out.println(Java12ClassConstants.class);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testJava14ClassConstantRewriting() throws Exception {
        verifyOutput(
            Class.forName("Java14ClassConstants"),
            defaultSettings(),
            "public class Java14ClassConstants {\n" +
            "    void f() {\n" +
            "        System.out.println(Integer.class);\n" +
            "        System.out.println(Java14ClassConstants.class);\n" +
            "        new Local() {\n" +
            "            void f() {\n" +
            "                System.out.println(Integer.class);\n" +
            "                System.out.println(Local.class);\n" +
            "                System.out.println(Java14ClassConstants.class);\n" +
            "            }\n" +
            "        };\n" +
            "    }\n" +
            "    class Local {" +
            "        void f() {\n" +
            "            System.out.println(Integer.class);\n" +
            "            System.out.println(Local.class);\n" +
            "            System.out.println(Java14ClassConstants.class);\n" +
            "        }\n" +
            "    }\n" +
            "    class Inner {" +
            "        void f() {\n" +
            "            System.out.println(Integer.class);\n" +
            "            System.out.println(Inner.class);\n" +
            "            System.out.println(Java14ClassConstants.class);\n" +
            "            new Local() {\n" +
            "                void f() {\n" +
            "                    System.out.println(Integer.class);\n" +
            "                    System.out.println(Local.class);\n" +
            "                    System.out.println(Java14ClassConstants.class);\n" +
            "                }\n" +
            "            };\n" +
            "        }\n" +
            "        class Local {" +
            "            void f() {\n" +
            "                System.out.println(Integer.class);\n" +
            "                System.out.println(Local.class);\n" +
            "                System.out.println(Java14ClassConstants.class);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    static class StaticInner {" +
            "        void f() {\n" +
            "            System.out.println(Integer.class);\n" +
            "            System.out.println(StaticInner.class);\n" +
            "            System.out.println(Java14ClassConstants.class);\n" +
            "            new Local() {\n" +
            "                void f() {\n" +
            "                    System.out.println(Integer.class);\n" +
            "                    System.out.println(Local.class);\n" +
            "                    System.out.println(Java14ClassConstants.class);\n" +
            "                }\n" +
            "            };\n" +
            "        }\n" +
            "        class Local {" +
            "            void f() {\n" +
            "                System.out.println(Integer.class);\n" +
            "                System.out.println(Local.class);\n" +
            "                System.out.println(Java14ClassConstants.class);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}"
        );
    }
}
