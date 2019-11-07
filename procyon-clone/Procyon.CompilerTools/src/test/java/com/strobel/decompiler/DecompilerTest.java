/*
 * DecompilerTest.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.decompiler;

import com.strobel.assembler.InputTypeLoader;
import com.strobel.core.ExceptionUtilities;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.languages.Language;
import com.strobel.decompiler.languages.Languages;
import com.strobel.io.PathHelper;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Pattern;

import static com.strobel.assembler.metadata.Flags.testAny;
import static org.junit.Assert.*;

public abstract class DecompilerTest {
    protected final static int OPTION_EXCLUDE_NESTED = 0x0001;
    protected final static int OPTION_FLATTEN_SWITCH_BLOCKS = 0x0002;
    protected final static int OPTION_INCLUDE_SYNTHETIC = 0x0004;
    protected final static int OPTION_RETAIN_CASTS = 0x0008;
    protected final static int OPTION_EXPLICIT_TYPE_ARGUMENTS = 0x0010;
    protected final static int OPTION_RETAIN_POINTLESS_SWITCHES = 0x0020;
    protected final static int OPTION_ENABLE_UNICODE_OUTPUT = 0x0040;

    protected final static Pattern WHITESPACE;

    static {
        WHITESPACE = Pattern.compile("\\s+");
    }

    protected static DecompilerSettings defaultSettings() {
        return createSettings(Languages.java(), 0);
    }

    protected static DecompilerSettings createSettings(final int options) {
        return createSettings(Languages.java(), options);
    }

    protected static DecompilerSettings createSettings(final Language language, final int options) {
        final DecompilerSettings settings = new DecompilerSettings();

        settings.setLanguage(VerifyArgument.notNull(language, "language"));
        settings.setTypeLoader(new InputTypeLoader());

        if (testAny(options, OPTION_EXCLUDE_NESTED)) {
            settings.setExcludeNestedTypes(true);
        }

        if (testAny(options, OPTION_FLATTEN_SWITCH_BLOCKS)) {
            settings.setFlattenSwitchBlocks(true);
        }

        if (testAny(options, OPTION_INCLUDE_SYNTHETIC)) {
            settings.setShowSyntheticMembers(true);
        }

        if (testAny(options, OPTION_RETAIN_CASTS)) {
            settings.setRetainRedundantCasts(true);
        }

        if (testAny(options, OPTION_EXPLICIT_TYPE_ARGUMENTS)) {
            settings.setForceExplicitTypeArguments(true);
        }

        if (testAny(options, OPTION_RETAIN_POINTLESS_SWITCHES)) {
            settings.setRetainPointlessSwitches(true);
        }

        if (testAny(options, OPTION_ENABLE_UNICODE_OUTPUT)) {
            settings.setUnicodeOutputEnabled(true);
        }

        return settings;
    }

    protected void verifyOutput(final Class<?> type, final DecompilerSettings settings, final String expectedOutput) {
        VerifyArgument.notNull(type, "type");

        try {
            final String packageRoot = type.getProtectionDomain().getCodeSource().getLocation().getFile();
            final String decodedRoot = URLDecoder.decode(packageRoot, Charset.defaultCharset().displayName());
            final String path = PathHelper.combine(decodedRoot, type.getName().replace('.', '/') + ".class");

            verifyOutput(
                new File(path).getCanonicalPath(),
                settings,
                expectedOutput
            );
        }
        catch (final Exception e) {
            throw ExceptionUtilities.asRuntimeException(e);
        }
    }
    protected void verifyOutput(final String internalName, final DecompilerSettings settings, final String expectedOutput) {
        final PlainTextOutput writer = new PlainTextOutput();

        writer.setUnicodeOutputEnabled(settings.isUnicodeOutputEnabled());

        Decompiler.decompile(
            internalName,
            writer,
            settings
        );

        final String actualOutput = writer.toString();
        final List<String> lines = StringUtilities.split(actualOutput, true, '\n');

        int firstCodeLine;

        for (firstCodeLine = 0; firstCodeLine < lines.size(); firstCodeLine++) {
            if (!lines.get(firstCodeLine).startsWith("import ") &&
                !lines.get(firstCodeLine).startsWith("package ")) {

                break;
            }
        }

        assertTrue(firstCodeLine >= 0 && firstCodeLine < lines.size());

        final String outputWithoutImports = StringUtilities.join(" ", lines.subList(firstCodeLine, lines.size()));

        assertEquals(
            WHITESPACE.matcher(expectedOutput.trim()).replaceAll(" "),
            WHITESPACE.matcher(outputWithoutImports).replaceAll(" ")
        );
    }
}
