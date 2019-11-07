/*
 * VerificationException.java
 *
 * Copyright (c) 2015 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.reflection.emit;

import com.strobel.core.ArrayUtilities;
import com.strobel.core.StringUtilities;

public class VerificationException extends IllegalStateException {
    public VerificationException() {
    }

    public VerificationException(final Throwable cause) {
        super(cause);
    }

    VerificationException(final Throwable cause, final Verifier.VerifierFrame[] frames) {
        super(fixMessage("", frames), cause);
    }

    public VerificationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    VerificationException(final String message, final Throwable cause, final Verifier.VerifierFrame[] frames) {
        super(fixMessage(message, frames), cause);
    }

    public VerificationException(final String s) {
        super(s);
    }

    VerificationException(final String s, final Verifier.VerifierFrame[] frames) {
        super(fixMessage(s, frames));
    }

    private static String fixMessage(final String s, final Verifier.VerifierFrame[] frames) {
        final String message = StringUtilities.isNullOrEmpty(s) ? "Code failed verification." : s;

        if (ArrayUtilities.isNullOrEmpty(frames)) {
            return message;
        }

        final StringBuilder sb = new StringBuilder(message);

        for (int i = 0; i < frames.length; i++) {
            final Verifier.VerifierFrame frame = frames[i];

            sb.append("\n\t[").append(frames.length - i).append("] ").append(frame);
        }

        return sb.toString();
    }
}
