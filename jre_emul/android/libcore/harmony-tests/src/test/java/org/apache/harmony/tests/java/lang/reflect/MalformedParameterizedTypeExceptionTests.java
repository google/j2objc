package org.apache.harmony.tests.java.lang.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.Modifier;

public class MalformedParameterizedTypeExceptionTests  extends junit.framework.TestCase {

    /**
     * java.lang.reflect.MalformedParameterizedTypeException#MalformedParameterizedTypeException()
     */
    public void test_Constructor() throws Exception {
        Constructor<MalformedParameterizedTypeException> ctor = MalformedParameterizedTypeException.class
                .getDeclaredConstructor();
        assertNotNull("Parameterless constructor does not exist.", ctor);
        assertTrue("Constructor is not protected", Modifier.isPublic(ctor
                .getModifiers()));
        assertNotNull(ctor.newInstance());
    }

}
