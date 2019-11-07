package com.strobel.decompiler;

import org.junit.Test;

public class PrimitiveTests extends DecompilerTest {
    @SuppressWarnings({ "NumericOverflow", "SuspiciousNameCombination", "LocalCanBeFinal" })
    private static final class A {
        static final float e = 2.7182818459f;
        static double d = 2.7182818459;

        static double w = 1.0 / 0.0;
        static double x = -1.0 / 0.0;
        static double y = -5e-324;
        static float z = 700649232162408535461864791644958065640130970938257885878534141944895541342930300743319094181060791015626E-150f;

        public static strictfp void test() {
            double t = (double) (1L << 53);
            double x = t * t;
            double y = (double) (-1L >>> 11);
            double z = x % y;

            System.out.println(z);
            System.out.println(z == 1.0);
            System.out.println(z * e);
            System.out.println(z * d);

            System.out.println(A.x);
            System.out.println(A.y);
            System.out.println(A.z);
            System.out.println((double) A.z);
        }
    }

    @Test
    public void testFloatingPointPrecision() throws Throwable {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private static final class A {\n" +
            "    static final float e = 2.7182817f;\n" +
            "    static double d;\n" +
            "    static double w;\n" +
            "    static double x;\n" +
            "    static double y;\n" +
            "    static float z;\n" +
            "    public static strictfp void test() {\n" +
            "        final double t = 9.007199254740992E15;\n" +
            "        final double x = t * t;\n" +
            "        final double y = 9.007199254740991E15;\n" +
            "        final double z = x % y;\n" +
            "        System.out.println(z);\n" +
            "        System.out.println(z == 1.0);\n" +
            "        System.out.println(z * 2.7182817459106445);\n" +
            "        System.out.println(z * A.d);\n" +
            "        System.out.println(A.x);\n" +
            "        System.out.println(A.y);\n" +
            "        System.out.println(A.z);\n" +
            "        System.out.println((double)A.z);\n" +
            "    }\n" +
            "    static {\n" +
            "        A.d = 2.7182818459;\n" +
            "        A.w = Double.POSITIVE_INFINITY;\n" +
            "        A.x = Double.NEGATIVE_INFINITY;\n" +
            "        A.y = -4.9E-324;\n" +
            "        A.z = Float.MIN_VALUE;\n" +
            "    }\n" +
            "}"
        );
    }
}
