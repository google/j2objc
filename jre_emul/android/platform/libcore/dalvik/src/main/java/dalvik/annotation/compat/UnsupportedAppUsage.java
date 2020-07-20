/*
 * Copyright (C) 2018 The Android Open Source Project
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
package dalvik.annotation.compat;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import dalvik.system.VersionCodes;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import libcore.api.CorePlatformApi;
import libcore.api.IntraCoreApi;

/**
 * Indicates that a class member, that is not part of the SDK, is used by apps.
 * Since the member is not part of the SDK, such use is not supported.
 *
 * <p>This annotation acts as a heads up that changing a given method or field
 * may affect apps, potentially breaking them when the next Android version is
 * released. In some cases, for members that are heavily used, this annotation
 * may imply restrictions on changes to the member.
 *
 * <p>This annotation also results in access to the member being permitted by the
 * runtime, with a warning being generated in debug builds.
 *
 * <p>For more details, see go/UnsupportedAppUsage.
 *
 * {@hide}
 */
@Retention(CLASS)
@Target({CONSTRUCTOR, METHOD, FIELD, TYPE})
@Repeatable(UnsupportedAppUsage.Container.class)
@CorePlatformApi
@IntraCoreApi
public @interface UnsupportedAppUsage {

    /**
     * Associates a bug tracking the work to add a public alternative to this API. Optional.
     *
     * @return ID of the associated tracking bug
     */
    @CorePlatformApi
    @IntraCoreApi
    long trackingBug() default 0;

    /**
     * Indicates that usage of this API is limited to apps based on their target SDK version.
     *
     * <p>Access to the API is allowed if the targetSdkVersion in the apps manifest is no greater
     * than this value. Access checks are performed at runtime.
     *
     * <p>This is used to give app developers a grace period to migrate off a non-SDK interface.
     * When making Android version N, existing APIs can have a maxTargetSdk of N-1 added to them.
     * Developers must then migrate off the API when their app is updated in future, but it will
     * continue working in the meantime.
     *
     * <p>Possible values are:
     * <ul>
     *     <li>
     *         {@link VersionCodes#O} - in which case the API is available up to and including the
     *         O release and all intermediate releases between O and P. Or in other words the API
     *         is blacklisted (unavailable) from P onwards.
     *     </li>
     *     <li>
     *         {@link VersionCodes#P} - in which case the API is available up to and including the
     *         P release and all intermediate releases between P and Q. Or in other words the API
     *         is blacklisted (unavailable) from Q onwards.
     *     </li>
     *     <li>
     *         absent (default value) - All apps can access this API, but doing so may result in
     *         warnings in the log, UI warnings (on developer builds) and/or strictmode violations.
     *         The API is likely to be further restricted in future.
     *     </li>
     *
     * </ul>
     *
     * @return The maximum value for an apps targetSdkVersion in order to access this API.
     */
    @CorePlatformApi
    @IntraCoreApi
    int maxTargetSdk() default Integer.MAX_VALUE;

    /**
     * For debug use only. The expected dex signature to be generated for this API, used to verify
     * parts of the build process.
     *
     * @return A dex API signature.
     */
    @CorePlatformApi
    @IntraCoreApi
    String expectedSignature() default "";

    /**
     * The signature of an implicit (not present in the source) member that forms part of the
     * hiddenapi.
     *
     * <p>Allows access to non-SDK API elements that are not represented in the input source to be
     * managed.
     *
     * <p>This must only be used when applying the annotation to a type, using it in any other
     * situation is an error.
     *
     * @return A dex API signature.
     */
    @CorePlatformApi
    @IntraCoreApi
    String implicitMember() default "";

    /**
     * Container for {@link UnsupportedAppUsage} that allows it to be applied repeatedly to types.
     */
    @Retention(CLASS)
    @Target(TYPE)
    @CorePlatformApi
    @IntraCoreApi
    @interface Container {
        @CorePlatformApi
        @IntraCoreApi
        UnsupportedAppUsage[] value();
    }
}
