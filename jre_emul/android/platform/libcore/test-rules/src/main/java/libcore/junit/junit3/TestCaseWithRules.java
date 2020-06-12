/*
 * Copyright (C) 2016 The Android Open Source Project
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
package libcore.junit.junit3;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import static org.junit.internal.runners.rules.RuleMemberValidator.RULE_VALIDATOR;

/**
 * A {@link TestCase} that supports the @Rule annotation from JUnit 4.
 *
 * <p>It supports both {@link TestRule} and {@link MethodRule} based rules when used with the
 * {@code @Rule} annotation on public fields and methods. The rules encapsulate the
 * {@link TestCase#runBare()} method and so are run before the {@link TestCase#setUp()} and after
 * the {@link TestCase#tearDown()} methods.
 *
 * <p>Classes that extend this must have a single no argument constructor.
 */
public abstract class TestCaseWithRules extends TestCase {

    private final TestClass testClass;

    private final List<Throwable> validationErrors;

    public TestCaseWithRules() {
        testClass = new TestClass(getClass());

        validationErrors = new ArrayList<>();
        RULE_VALIDATOR.validate(testClass, validationErrors);
    }

    @Override
    public void runBare() throws Throwable {
        if (!validationErrors.isEmpty()) {
            throw new MultipleFailureException(validationErrors);
        }

        Statement statement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                superRunBare();
            }
        };

        final String name = getName();
        FrameworkMethod frameworkMethod;
        try {
            Method method = getClass().getMethod(name, (Class[]) null);
            frameworkMethod = new FrameworkMethod(method);
        } catch (NoSuchMethodException e) {
            frameworkMethod = new FrameworkMethod(null) {
                @Override
                public String getName() {
                    return name;
                }

                @Override
                public Annotation[] getAnnotations() {
                    return new Annotation[0];
                }

                @Override
                public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                    return null;
                }
            };
        }
        Description description =
                Description.createTestDescription(getClass(), frameworkMethod.getName(),
                        frameworkMethod.getAnnotations());

        List<Object> rules = testClass.getAnnotatedFieldValues(this, Rule.class, Object.class);
        for (Object rule : rules) {
            if (rule instanceof TestRule) {
                statement = ((TestRule) rule).apply(statement, description);
            } else {
                statement = ((MethodRule) rule).apply(statement, frameworkMethod, this);
            }
        }

        statement.evaluate();
    }

    private void superRunBare() throws Throwable {
        super.runBare();
    }
}
