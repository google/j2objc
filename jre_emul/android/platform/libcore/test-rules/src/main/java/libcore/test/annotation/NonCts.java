/*
 * Copyright (C) 2023 The Android Open Source Project
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

package libcore.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test with this annotation doesn't run in CTS.
 *
 * Note that every annotation element below should be associated to a field in
 * {@link vogar.expect.Expectation}, because it will be de- and serialized by
 * {@link vogar.expect.ExpectationStore} for back-porting to an older branch.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface NonCts {
    /**
     * Optional bug id showing why this test fails / shouldn't run in MTS.
     *
     * The associated field is {@link vogar.expect.Expectation#bug}.
     */
    long bug() default -1;

    /**
     * Reason why the test shouldn't run in CTS.
     *
     * The associated field is {@link vogar.expect.Expectation#description}.
     */
    String reason();
}
