/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libcore.java.lang.reflect.annotations;

import junit.framework.TestCase;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.AnnotationA;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.AnnotationB;

/* J2ObjC removed.
import dalvik.system.VMRuntime; */

/**
 * Tests for the behavior of Annotation instances at runtime.
 */
public class AnnotationsTest extends TestCase {

    enum Breakfast { WAFFLES, PANCAKES }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface HasDefaultsAnnotation {
        byte a() default 5;
        short b() default 6;
        int c() default 7;
        long d() default 8;
        float e() default 9.0f;
        double f() default 10.0;
        char g() default 'k';
        boolean h() default true;
        Breakfast i() default Breakfast.WAFFLES;
        AnnotationA j() default @AnnotationA();
        String k() default "maple";
        Class l() default AnnotationB.class;
        int[] m() default { 1, 2, 3 };
        Breakfast[] n() default { Breakfast.WAFFLES, Breakfast.PANCAKES };
        Breakfast o();
        int p();
    }

    public void testAnnotationDefaults() throws Exception {
        assertEquals((byte) 5, defaultValue("a"));
        assertEquals((short) 6, defaultValue("b"));
        assertEquals(7, defaultValue("c"));
        assertEquals(8L, defaultValue("d"));
        assertEquals(9.0f, defaultValue("e"));
        assertEquals(10.0, defaultValue("f"));
        assertEquals('k', defaultValue("g"));
        assertEquals(true, defaultValue("h"));
        assertEquals(Breakfast.WAFFLES, defaultValue("i"));
        assertEquals("@" + AnnotationA.class.getName() + "()", defaultValue("j").toString());
        assertEquals("maple", defaultValue("k"));
        assertEquals(AnnotationB.class, defaultValue("l"));
        assertEquals("[1, 2, 3]", Arrays.toString((int[]) defaultValue("m")));
        assertEquals("[WAFFLES, PANCAKES]", Arrays.toString((Breakfast[]) defaultValue("n")));
        assertEquals(null, defaultValue("o"));
        assertEquals(null, defaultValue("p"));
    }

    private static Object defaultValue(String name) throws NoSuchMethodException {
        return HasDefaultsAnnotation.class.getMethod(name).getDefaultValue();
    }

    @Retention(RetentionPolicy.CLASS)
    public @interface ClassRetentionAnnotation {}

    @Retention(RetentionPolicy.RUNTIME)
    public @interface RuntimeRetentionAnnotation {}

    @Retention(RetentionPolicy.SOURCE)
    public @interface SourceRetentionAnnotation {}

    @ClassRetentionAnnotation @RuntimeRetentionAnnotation @SourceRetentionAnnotation
    public static class RetentionAnnotations {}

    /* J2ObjC removed.
    public void testRetentionPolicy() {
        // b/29500035
        int savedTargetSdkVersion = VMRuntime.getRuntime().getTargetSdkVersion();
        try {
            // Test N and later behavior
            VMRuntime.getRuntime().setTargetSdkVersion(24);
            Annotation classRetentionAnnotation =
                RetentionAnnotations.class.getAnnotation(ClassRetentionAnnotation.class);
            assertNull(classRetentionAnnotation);

            // Test pre-N behavior
            VMRuntime.getRuntime().setTargetSdkVersion(23);
            classRetentionAnnotation =
                RetentionAnnotations.class.getAnnotation(ClassRetentionAnnotation.class);
            assertNotNull(classRetentionAnnotation);
        } finally {
            VMRuntime.getRuntime().setTargetSdkVersion(savedTargetSdkVersion);
        }
        assertNotNull(RetentionAnnotations.class.getAnnotation(RuntimeRetentionAnnotation.class));
        assertNull(RetentionAnnotations.class.getAnnotation(SourceRetentionAnnotation.class));
    } */
}
