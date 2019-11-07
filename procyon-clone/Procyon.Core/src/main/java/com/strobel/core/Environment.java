/*
 * Environment.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.core;

import com.strobel.util.ContractUtils;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author strobelm
 */
public final class Environment {

    private static final Logger logger = Logger.getLogger(Environment.class.getName());

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$([a-zA-Z0-9_]+)", Pattern.COMMENTS);

    private static final String OS_NAME = System.getProperty("os.name");
    private static final String OS_NAME_LOWER = OS_NAME.toLowerCase();
    private static final String OS_ARCH = System.getProperty("os.arch");
    private static final String ARCH_DATA_MODEL = System.getProperty("sun.arch.data.model");

    /**
     * Make sure nobody can instantiate the class
     */
    private Environment() {
        throw ContractUtils.unreachable();
    }

    public static boolean isWindows() {
        return OS_NAME_LOWER.startsWith("windows");
    }

    public static boolean isOS2() {
        return OS_NAME_LOWER.startsWith("os/2") ||
               OS_NAME_LOWER.startsWith("os2");
    }

    public static boolean isMac() {
        return OS_NAME_LOWER.startsWith("mac");
    }

    public static boolean isLinux() {
        return OS_NAME_LOWER.startsWith("linux");
    }

    public static boolean isUnix() {
        return !isWindows() && !isOS2();
    }

    public static boolean isFileSystemCaseSensitive() {
        return isUnix() && !isMac();
    }

    public static boolean is32Bit() {
        return ARCH_DATA_MODEL == null ||
               ARCH_DATA_MODEL.equals("32");
    }

    public static boolean is64Bit() {
        return !is32Bit();
    }

    public static boolean isAmd64() {
        return "amd64".equals(OS_ARCH);
    }

    public static boolean isMacX64() {
        return isMac() && "x86_64".equals(OS_ARCH);
    }

    /**
     * Get any variable by name if defined on the system
     *
     * @param variable The string with variables to expand. It should be something like '$VARIABLE'
     * @return The expanded variable, empty if arg is null or variable is not defined
     */
    public static String getVariable(final String variable) {
        if (variable == null) {
            return StringUtilities.EMPTY;
        }

        final String expanded = System.getenv(variable);
        return (expanded != null) ? expanded : StringUtilities.EMPTY;
    }

    /**
     * Recursively expands any environment variable(s) defined within a String.
     * If expansion is not possible, the original string will be returned.
     *
     *
     * @param s a string possibly containing one or more environment variables
     * @return The input string with all environment variables expanded
     */
    public static String expandVariables(final String s) {
        return expandVariables(s, true);
    }

    /**
     * Expands any environment variable(s) defined within a String.
     * If expansion is not possible, the original string will be returned.
     *
     * @param s a string possibly containing one or more environment variables
     * @param recursive whether or not variable values should be expanded recursively
     * @return The input string with all environment variables expanded
     */
    public static String expandVariables(final String s, final boolean recursive) {
        final Matcher variableMatcher = VARIABLE_PATTERN.matcher(s);

        StringBuffer expanded = null;
        String variable = null;

        try {
            while (variableMatcher.find()) {
                final int matches = variableMatcher.groupCount();

                // Perform all the variable expansions (if any)
                for (int i = 1; i <= matches; i++) {
                    variable = variableMatcher.group(i);

                    if (expanded == null) {
                        expanded = new StringBuffer();
                    }

                    final String variableValue = getVariable(variable);

                    variableMatcher.appendReplacement(
                        expanded,
                        (recursive ? expandVariables(variableValue, true) : variableValue).replace("\\", "\\\\")
                    );
                }
            }

            if (expanded != null) {
                variableMatcher.appendTail(expanded);
            }
        }
        catch (Throwable t) {
            logger.log(
                Level.WARNING,
                String.format(
                    "Unable to expand the variable '%s', returning original value: %s",
                    variable,
                    s
                ),
                t
            );
            return s;
        }

        if (expanded != null) {
            return expanded.toString();
        }

        return s;
    }

    public static int getProcessorCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static boolean isSingleProcessor() {
        return getProcessorCount() == 1;
    }
}
