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

package libcore.test.reasons;

/**
 * Reasons for {@link libcore.test.annotation.NonCts}.
 */
public class NonCtsReasons {

    /**
     * Internal APIs are not required to be tested in CTS. Even worse, it stops customization /
     * method signature change even though they are internal APIs.
     *
     * Libcore's tests by default uses these internal APIs to verify the result.
     *
     * Occasionally, unit test for internal APIs are put into the CTS. This is the reason why
     * you want to annotate the test with {@link libcore.test.annotation.NonCts}.
     */
    public static final String INTERNAL_APIS = "Test for internal APIs.";

    /**
     * Some libcore's tests for i18n APIs, java.text, depends on locales and CLDR locale-specific
     * data. However, the data is not stable, and usage changes over time, but the test often
     * asserts the localized output.
     *
     * We don't want to stop customization or improvement of these data. This is the reason why
     * you want to annotate the test with {@link libcore.test.annotation.NonCts}.
     */
    public static final String CLDR_DATA_DEPENDENCY = "The test depends on locale, but "
            + "manufacturers / CLDR improves the locale data over time.";

    /**
     * If a bug has been fixed, and the fix doesn't change the API contract,
     * you can annotate the test with {@link libcore.test.annotation.NonCts} and this reason.
     *
     * This is mainly needed to fix a bug in an ART / Conscrypt / Time Zone module version
     * and skip this test.
     *
     * Note that you still need a test for the basic API behavior, because every public API
     * needs to be tested in CTS.
     */
    public static final String NON_BREAKING_BEHAVIOR_FIX = "The test asserts buggy or non-breaking "
            + "behaviors, but the behavior has been fixed in a new mainline module version.";


    private NonCtsReasons() {}
}
