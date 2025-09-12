/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.stubbing.defaultanswers;

import static org.mockito.internal.exceptions.Reporter.smartNullPointerException;
import static org.mockito.internal.util.ObjectMethodsGuru.isToStringMethod;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.mockito.Mockito;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.debugging.LocationFactory;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.invocation.Location;
import org.mockito.mock.MockCreationSettings;
import org.mockito.stubbing.Answer;

/**
 * Optional Answer that can be used with
 * {@link Mockito#mock(Class, Answer)}
 * <p>
 * This implementation can be helpful when working with legacy code. Un-stubbed
 * methods often return null. If your code uses the object returned by an
 * un-stubbed call, you get a NullPointerException. This implementation of
 * Answer returns SmartNulls instead of nulls.
 * SmartNull gives nicer exception message than NPE because it points out the
 * line where un-stubbed method was called. You just click on the stack trace.
 * <p>
 * ReturnsSmartNulls first tries to return ordinary return values (see
 * {@link ReturnsMoreEmptyValues}) then it tries to return SmartNull. If the
 * return type is not mockable (e.g. final) then ordinary null is returned.
 * <p>
 * ReturnsSmartNulls will be probably the default return values strategy in
 * Mockito 2.1.0
 */
public class ReturnsSmartNulls implements Answer<Object>, Serializable {

    private static final long serialVersionUID = 7618312406617949441L;

    private final Answer<Object> delegate = new ReturnsMoreEmptyValues();

    @Override
    public Object answer(final InvocationOnMock invocation) throws Throwable {
        Object defaultReturnValue = delegate.answer(invocation);

        if (defaultReturnValue != null) {
            return defaultReturnValue;
        }

        return RetrieveGenericsForDefaultAnswers.returnTypeForMockWithCorrectGenerics(
                invocation,
                new RetrieveGenericsForDefaultAnswers.AnswerCallback() {
                    @Override
                    public Object apply(Class<?> type) {
                        if (type == null) {
                            return null;
                        }

                        MockCreationSettings<?> mockSettings =
                                MockUtil.getMockSettings(invocation.getMock());
                        Answer<?> defaultAnswer =
                                new ThrowsSmartNullPointer(invocation, LocationFactory.create());

                        return Mockito.mock(
                                type,
                                new MockSettingsImpl<>()
                                        .defaultAnswer(defaultAnswer)
                                        .mockMaker(mockSettings.getMockMaker()));
                    }
                });
    }

    private static class ThrowsSmartNullPointer implements Answer {

        private final InvocationOnMock unstubbedInvocation;

        private final Location location;

        ThrowsSmartNullPointer(InvocationOnMock unstubbedInvocation, Location location) {
            this.unstubbedInvocation = unstubbedInvocation;
            this.location = location;
        }

        @Override
        public Object answer(InvocationOnMock currentInvocation) throws Throwable {
            if (isToStringMethod(currentInvocation.getMethod())) {
                return "SmartNull returned by this un-stubbed method call on a mock:\n"
                        + unstubbedInvocation;
            } else if (isMethodOf(
                    mockAccessClass(),
                    currentInvocation.getMock(),
                    currentInvocation.getMethod())) {
                /* The MockAccess methods should be called directly */
                return currentInvocation.callRealMethod();
            }

            throw smartNullPointerException(unstubbedInvocation.toString(), location);
        }

        private static boolean isMethodOf(Class<?> clazz, Object instance, Method method) {
            if (!clazz.isInstance(instance)) {
                return false;
            }

            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equalsIgnoreCase(method.getName())
                        && Arrays.equals(m.getParameterTypes(), method.getParameterTypes())) {
                    return true;
                }
            }

            return false;
        }
    }

    private static Class<?> mockAccessClass() {
        if (mockAccessClass != null) {
            return mockAccessClass;
        }
        try {
            mockAccessClass = Class.forName("org.mockito.internal.creation.bytebuddy.access.MockAccess");
        } catch (ClassNotFoundException runningUnderIos) {
            mockAccessClass = Void.class;
            /*
             * The result of mockAccessClass() is passed to isMethodOf(...). That will result in a
             * call to isMethodOf(Void.class, ...), which will always return false. That's the
             * result that we want: Since iOS runs don't contain MockAccess.class at all, there's no
             * way that the method being called could be a method on MockAccess.
             */
        }
        return mockAccessClass;
    }

    private static volatile Class<?> mockAccessClass;
}
